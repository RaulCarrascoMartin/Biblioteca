# Biblioteca

Aplicación web de gestión de una biblioteca: catálogo de libros, préstamos y devoluciones, usuarios con roles (`USER` / `ADMIN`) y una interfaz web ligera servida por la propia aplicación.

- **Backend:** Java 21, Spring Boot 4.1, Spring MVC, Spring Data JPA, Spring Security, JWT (jjwt 0.12), Lombok.
- **Base de datos:** MySQL en ejecución normal; H2 en memoria (modo MySQL) para los tests.
- **Frontend:** HTML + CSS + JavaScript sin framework ni paso de compilación, en `src/main/resources/static`.
- **Arquitectura:** monolito modular (módulos `libro`, `usuario`, `prestamo`, `auth`) con fronteras verificadas por ArchUnit.
- **Tests:** JUnit 5 + MockMvc + ArchUnit, 106 tests (87 de integración/unitarios y 19 reglas de arquitectura).

Este documento explica **qué hace cada capa y cada clase, y por qué se tomó cada decisión**, con especial atención a la arquitectura.

---

## Índice

1. [Puesta en marcha](#1-puesta-en-marcha)
2. [Visión general de la arquitectura](#2-visión-general-de-la-arquitectura)
3. [Módulos y clases](#3-módulos-y-clases)
4. [Seguridad](#4-seguridad)
5. [Modelo de datos y reglas de negocio](#5-modelo-de-datos-y-reglas-de-negocio)
6. [API REST](#6-api-rest)
7. [Frontend](#7-frontend)
8. [Estrategia de tests](#8-estrategia-de-tests)
9. [Decisiones, compromisos y limitaciones conocidas](#9-decisiones-compromisos-y-limitaciones-conocidas)

---

## 1. Puesta en marcha

### Requisitos
- JDK 21
- MySQL (para ejecución normal). Los tests no lo necesitan.

### Variables de configuración

Los secretos **nunca** se versionan; se inyectan por entorno (`application.yml` solo define referencias `${...}`).

| Variable | Obligatoria | Descripción |
|---|---|---|
| `DB_PASSWORD` | Sí | Contraseña del usuario de base de datos |
| `DB_URL` | No | Por defecto `jdbc:mysql://localhost:3306/biblioteca` |
| `DB_USERNAME` | No | Por defecto `biblioteca_app` |
| `JWT_SECRET` | Sí | Clave de firma. Mínimo 32 bytes; la aplicación **no arranca** si es más corta |
| `JWT_EXPIRATION_MS` | No | Vida del token, por defecto `1800000` (30 min) |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | No | Si ambos existen, se crea un administrador en el primer arranque |
| `app.seed-books=true` | No | Propiedad (no variable) que carga un catálogo de ejemplo |

### Perfiles
- `application.yml`: configuración común.
- `application-prod.yml`: `show-sql: false`.
- `application-dev.yml.example`: plantilla para desarrollo. Cópiala a `application-dev.yml` (que está en `.gitignore`) y actívala con el perfil `dev`.
- `application-local.yml` y `.env` están **ignorados por git**: sirven para credenciales locales.

### Ejecutar y probar
```bash
export DB_PASSWORD=... JWT_SECRET=...   # ≥ 32 bytes
./mvnw spring-boot:run
# → http://localhost:8080

./mvnw test                              # usa H2, no necesita MySQL
```

### Esquema de base de datos
`spring.jpa.hibernate.ddl-auto` está en **`validate`**: Hibernate comprueba que el esquema coincide con las entidades pero **no lo crea ni lo modifica**. El repositorio no incluye scripts de migración, por lo que el esquema (tablas `usuarios`, `libros`, `prestamos`) debe existir previamente. La columna de revocación de tokens, si vienes de una versión anterior:

```sql
ALTER TABLE usuarios ADD COLUMN token_version INT NOT NULL DEFAULT 0;
```

---

## 2. Visión general de la arquitectura

La aplicación es un **monolito modular**: un único artefacto desplegable (un solo proceso, una sola base de datos) dividido internamente en **módulos de negocio con fronteras explícitas**. Cada módulo es un paquete de primer nivel que agrupa *todo* lo suyo (entidad, servicio, API REST, persistencia) y solo se comunica con los demás a través de su **capa `domain`** (su API pública).

```
 com.example.demo
 ├── libro/      catálogo de libros            (domain · web · persistence · seed)
 ├── usuario/    usuarios y roles              (domain · web · persistence · seed)
 ├── prestamo/   préstamos y devoluciones      (domain · web · persistence)  ──▶ libro, usuario
 ├── auth/       registro, login, JWT          (domain · web · security)     ──▶ usuario, prestamo, shared
 ├── shared/     núcleo compartido (NotFoundException, PasswordHasher)
 └── config/     composición transversal (GlobalExceptionHandler)
```

### Dependencias entre módulos

```
   config ──▶ auth ──▶ usuario ◀── prestamo ──▶ libro
                                       
   (todos ──▶ shared)
```

| Módulo | Depende de |
|---|---|
| `libro` | nadie |
| `usuario` | `shared` |
| `prestamo` | `libro`, `usuario`, `shared` |
| `auth` | `usuario`, `shared` |
| `config` | `auth`, `shared` |

El grafo es **acíclico** y lo verifica `ArchitectureTest`. Todas las dependencias entre módulos apuntan a la capa `domain` del módulo destino. Nadie depende de `auth` ni de `config`.

### Estructura interna de un módulo

Cada módulo tiene una **capa `domain`** y varios **adaptadores**:

```
 libro/
 ├── domain/                          ◀── DOMINIO (API pública del módulo)
 │    ├── Libro, Genero               modelo
 │    ├── LibroService                casos de uso
 │    ├── LibroRepository             puerto de salida (interfaz)
 │    ├── LibroDTO, LibroResponseDTO  contratos de E/S
 │    └── LibroEnUsoChecker           punto de extensión (SPI) para otros módulos
 │                                    ── adaptadores (interior privado) ──
 ├── web/          LibroController                                   entrada (REST)
 ├── persistence/  JpaLibroRepository, LibroRepositoryAdapter        salida (JPA)
 └── seed/         LibroSeeder                                       carga de datos de ejemplo
```

- **`<modulo>.domain` es la API pública**: lo único que otros módulos pueden usar (entidad, servicio, puerto, DTO, SPI, excepciones).
- **`web`, `persistence`, `seed` y `security` son internos**: ningún otro módulo puede referenciarlos.
- **Regla de dependencias hexagonal dentro de cada módulo:** los adaptadores dependen de `domain`, nunca al revés. `domain` no importa Spring Security, jjwt, Spring Data ni Spring MVC; lo único de Spring que conserva son `@Service` y `@Transactional`.

### Cómo se evitan los ciclos: puntos de extensión (SPI)
`libro` y `usuario` necesitan saber si tienen préstamos asociados (no se pueden retirar/eliminar si los tienen), pero `prestamo` ya depende de ellos. Para no crear un ciclo se **invierte la dependencia**: `libro` publica la interfaz `LibroEnUsoChecker` y `usuario` publica `UsuarioEnUsoChecker`; `prestamo` las implementa (`LibroConPrestamosChecker`, `UsuarioConPrestamosChecker`) como beans, y los servicios reciben `List<...Checker>` por constructor. `libro` y `usuario` ignoran por completo la existencia de `prestamo`.

### Por qué esta arquitectura
- **Cohesión por dominio:** todo lo relativo a libros está en `libro/`; un cambio funcional toca un solo módulo.
- **Fronteras comprobables:** las reglas de dependencia no son una convención oral, las verifica `ArchitectureTest` en cada build (ver [sección 8](#8-estrategia-de-tests)).
- **Camino de evolución:** cada módulo podría extraerse a un servicio independiente si algún día hiciera falta, porque solo se acopla a los demás mediante su API pública. Mientras tanto se conserva la simplicidad operativa de un único despliegue y transacciones locales (p. ej. el bloqueo pesimista entre libro y préstamo).
- **Frontera de seguridad clara:** todo lo que depende de Spring Security o de jjwt vive en `auth/security`; los módulos de negocio no importan ni Spring Security ni jjwt.

### Inyección de dependencias
Toda la aplicación usa **inyección por constructor** (`@RequiredArgsConstructor` de Lombok sobre campos `private final`), sin `@Autowired` en ningún sitio (lo comprueba un test de arquitectura). Las dependencias son explícitas y obligatorias, las clases son inmutables tras construirse, se pueden instanciar en tests sin contenedor y se evitan dependencias circulares ocultas.

---

## 3. Módulos y clases

Paquete raíz: `com.example.demo`. Cada módulo se describe con su capa `domain` (API pública) y sus adaptadores (interior). Las clases sin prefijo están en `<modulo>/domain/`.

### 3.1 `shared/` — núcleo compartido
Contiene solo lo que necesitan varios módulos y no pertenece a ninguno. **No depende de ningún módulo.**

| Clase | Responsabilidad |
|---|---|
| `NotFoundException` | Recurso inexistente → 404. |

Las reglas de negocio violadas se expresan con excepciones propias de cada módulo (`IsbnDuplicadoException`, `LibroConPrestamosException`, `LibroNoDisponibleException`, `PrestamoYaDevueltoException`, `AutoEliminacionException`, `UsuarioConPrestamosException` → 409; `EmailYaRegistradoException` → 400) que `GlobalExceptionHandler` traduce a HTTP.

| `PasswordHasher` | Puerto: «hashear una contraseña». Lo usan `auth` (registro) y `usuario` (cambio de clave, administrador inicial). Implementado en `auth/security`. |

### 3.2 `libro/` — catálogo
| Clase | Responsabilidad |
|---|---|
| `Libro`, `Genero` | Entidad JPA y enumeración de géneros (guardada como `STRING`, no ordinal, para que reordenar no corrompa datos). `equals`/`hashCode` solo por `id`. |
| `LibroRepository` | Puerto con solo las operaciones que el dominio necesita. `findByIdWithLock` documenta que debe usarse dentro de una transacción: el bloqueo pesimista forma parte del contrato. |
| `LibroService` | Consulta, alta y edición del catálogo (valida ISBN único y devuelve `LibroResponseDTO`). Impide retirar un libro «en uso» consultando los `LibroEnUsoChecker` registrados. |
| `LibroEnUsoChecker` | SPI: otros módulos declaran si un libro les impide ser retirado. |
| `LibroDTO`, `LibroResponseDTO` | Entrada (validada) y salida del API. |
| `web/LibroController` | CRUD, delegando en el servicio. Lectura para cualquier autenticado; crear/editar/borrar solo `ADMIN` (`@PreAuthorize`). 409 si el ISBN existe. |
| `persistence/*` | `JpaLibroRepository` (Spring Data, con `@Lock(PESSIMISTIC_WRITE)`) y `LibroRepositoryAdapter` (implementa el puerto). |
| `seed/LibroSeeder` | `CommandLineRunner` condicionado a `app.seed-books=true`; carga idempotente por ISBN. |

### 3.3 `usuario/` — usuarios y roles
| Clase | Responsabilidad |
|---|---|
| `Usuario` | Entidad. `rol` es un `String` (`USER`/`ADMIN`) por simplicidad. `tokenVersion` permite invalidar todos los tokens (ver [Seguridad](#4-seguridad)). |
| `UsuarioRepository` | Puerto de acceso a datos. |
| `UsuarioService` | Consulta (devuelve `UsuarioResponseDTO`), actualización (normaliza el email; si cambia la contraseña **incrementa `tokenVersion`**), revocación de sesiones y borrado (bloqueado si es el propio solicitante o si algún `UsuarioEnUsoChecker` lo indica). |
| `UsuarioEnUsoChecker` | SPI análogo al de `libro`. |
| `UsuarioDTO`, `UsuarioUpdateDTO`, `UsuarioResponseDTO` | Contratos. `UsuarioDTO` no tiene campo `rol`: defensa contra *mass assignment*. La respuesta **nunca incluye la contraseña**. La contraseña admite máximo 72 caracteres (límite de bcrypt). |
| `web/UsuarioController` | Perfil propio (`/me`), consulta/edición (admin o dueño), borrado (solo admin, nunca el propio) y revocación de sesiones. |
| `persistence/*` | Repositorio JPA y adaptador. |
| `seed/AdminSeeder` | Crea el administrador inicial desde configuración (`ADMIN_EMAIL`/`ADMIN_PASSWORD`) si no existe. Sin credenciales no hace nada. |

### 3.4 `prestamo/` — préstamos y devoluciones
Depende de `libro` y `usuario`.

| Clase | Responsabilidad |
|---|---|
| `Prestamo` | Entidad con relaciones a `Libro` y `Usuario`. Activo mientras `fechaDevolucion` es nula. |
| `PrestamoRepository` | Puerto. |
| `PrestamoService` | **Núcleo de negocio.** `crearPrestamo` y `devolverLibro` son `@Transactional` y bloquean la fila del libro (`SELECT … FOR UPDATE`) para que dos peticiones simultáneas no presten el mismo ejemplar ni lo «devuelvan» dos veces. Rechaza (409) devolver un préstamo ya devuelto. |
| `LibroConPrestamosChecker`, `UsuarioConPrestamosChecker` | Implementaciones de los SPI de `libro` y `usuario`: conservan el historial impidiendo borrar libros/usuarios con préstamos. |
| `PrestamoResponseDTO` | Salida del API. |
| `web/PrestamoAuthz` (`@prestamoAuthz`) | Bean usado en `@PreAuthorize` («¿es dueño de este préstamo?»); recibe el email por parámetro (`authentication.name`), así que no depende de Spring Security ni de `auth`. |
| `web/PrestamoController` | Crear, devolver, listar los propios (`/mios`), consultar por id (admin o dueño). Traduce excepciones de dominio a 404/409. |
| `persistence/*` | Repositorio JPA y adaptador. |

### 3.5 `auth/` — autenticación y seguridad
Depende de `usuario` y `shared`. **Es el único módulo que conoce Spring Security y jjwt** (en su subpaquete `security`; su `domain` es independiente de ellos).

| Clase | Responsabilidad |
|---|---|
| `AuthService` | Registro (normaliza el email; si ya existía devuelve vacío sin revelarlo), login (autentica, obtiene la `tokenVersion` y emite el token) y revocación de tokens. |
| `CredentialsAuthenticator`, `TokenService`, `TokenRevocation` | Puertos: comprobar credenciales, emitir token, revocar token. |
| `InvalidCredentialsException`, `LoginDTO` | Error de credenciales (→ 401) y DTO de login. |
| `web/AuthController` | `/api/auth/*`: registro, login y logout. Gestiona la cookie `jwt_token` (HttpOnly, SameSite=Strict, `Secure` en HTTPS). En logout revoca el token tanto de la cabecera como de la cookie. |
| `security/SecurityConfig` | Cadena de filtros, rutas públicas, sesión sin estado, cabeceras (CSP, HSTS, X-Frame-Options…), 401 en JSON, `PasswordEncoder` BCrypt. |
| `security/JwtAuthFilter` | Extrae el token (cabecera o cookie), comprueba revocación, firma/emisor/audiencia/expiración **y que la versión coincida**, y puebla el `SecurityContext`. |
| `security/JwtService` | Genera y valida JWT. Implementa `TokenService`. Falla al arrancar si la clave es corta. |
| `security/CustomUserDetailsService`, `AuthUser` | Carga el usuario desde `UsuarioRepository`; `AuthUser` transporta la `tokenVersion`. |
| `security/TokenDenylistService` | Lista en memoria de tokens revocados con caducidad. Implementa `TokenRevocation`. |
| `security/RateLimitFilter` | Limita login (5/min) y registro (3/min) por IP. |
| `security/AuthzHelper` (`@authz`) | Bean usado en `@PreAuthorize` de `usuario` («¿es dueño de este usuario?»). Se referencia por nombre desde SpEL, por lo que `usuario` no tiene dependencia de compilación con `auth`. |
| `security/SpringPasswordHasher`, `SpringCredentialsAuthenticator` | Adaptadores de `PasswordHasher` y `CredentialsAuthenticator` sobre Spring Security. |

### 3.6 `config/`
| Clase | Responsabilidad |
|---|---|
| `GlobalExceptionHandler` | `@RestControllerAdvice` que convierte excepciones en `{"error": "..."}` con el código adecuado y **oculta los detalles internos** (los 500 devuelven un mensaje genérico). Vive aquí porque conoce excepciones de varios módulos; nadie depende de `config`. |

---

## 4. Seguridad

Las decisiones de esta sección son las que más peso tienen en el diseño.

### Autenticación: JWT en cookie HttpOnly
- El token viaja en una cookie `HttpOnly` (inaccesible desde JavaScript, lo que reduce el impacto de un XSS), `SameSite=Strict` y `Secure` en HTTPS. El frontend **no almacena el token**: solo guarda un indicador de «hay sesión» en `localStorage`.
- La API también acepta `Authorization: Bearer` para clientes que no son el navegador (Postman, scripts). El login devuelve el token en el cuerpo por ese motivo.
- **CSRF está desactivado** de forma deliberada: la API es sin estado y la cookie usa `SameSite=Strict`, que impide que otros sitios la envíen.

### Revocación de tokens (dos mecanismos complementarios)
1. **Lista de denegación (`TokenDenylistService`)** para el *logout* de una sesión concreta. Se guarda la firma del token hasta su caducidad (30 min) y se limpia de forma oportunista al crecer.
2. **Versión de token por usuario (`tokenVersion`)** para invalidar *todos* los tokens de un usuario a la vez. El token lleva la versión en el claim `tv`; si no coincide con la de la base de datos, se rechaza. Se incrementa al **cambiar la contraseña** y mediante `POST /api/usuarios/{id}/revocar-sesiones` (admin o el propio usuario).

**Por qué dos mecanismos:** la lista de denegación resuelve «cerrar *esta* sesión» y es barata, pero no sirve para «cerrar *todas*» (no se conocen los tokens emitidos). La versión por usuario sí, sin estado en memoria y válida tras reiniciar o con varias instancias.

> Corrección relevante: el logout original solo revocaba el token de la cabecera `Authorization`, pero el navegador usa la cookie, así que el token seguía siendo válido tras cerrar sesión. Ahora también se revoca el de la cookie (hay un test).

### Prevención de enumeración de usuarios
- Login con credenciales erróneas o usuario inexistente → siempre el mismo 401 «Credenciales incorrectas».
- Registro con un email ya existente → 200 sin cuerpo (idéntico en apariencia a un registro correcto); el frontend entonces intenta el login.

### Otras medidas
- Contraseñas con BCrypt; nunca se devuelven en ningún DTO.
- Límite de intentos en login y registro (rate limiting).
- Emails normalizados a minúsculas para que `Ana@x.es` y `ana@x.es` no sean cuentas distintas.
- Validación y límites de tamaño en todas las entradas; mensajes de error genéricos hacia el cliente.
- Un administrador no puede eliminar su propia cuenta.
- CSP restrictiva (`default-src 'self'`; solo se permite Google Fonts), HSTS y `X-Frame-Options: DENY`.

---

## 5. Modelo de datos y reglas de negocio

```
Usuario (1) ──< Prestamo >── (1) Libro
```

| Entidad | Campos principales |
|---|---|
| `Usuario` | `id`, `nombre`, `email` (único), `password` (hash), `rol`, `tokenVersion` |
| `Libro` | `id`, `titulo`, `autor`, `isbn` (único), `disponible`, `genero` |
| `Prestamo` | `id`, `libro`, `usuario`, `fechaPrestamo`, `fechaDevolucion` (nula mientras está activo) |

Reglas clave:
- Un libro solo puede estar prestado una vez a la vez. El bloqueo pesimista sobre el libro lo garantiza incluso con peticiones concurrentes.
- Un préstamo está **activo** mientras `fechaDevolucion` es nula; devolverlo dos veces da 409.
- Se conserva el historial: no se puede borrar un libro ni un usuario con préstamos registrados (409 con mensaje claro).
- `ISBN` único (409 al duplicarlo); el email también es único.

---

## 6. API REST

Todas las rutas salvo `/api/auth/**` y los estáticos requieren autenticación.

| Método y ruta | Acceso | Descripción |
|---|---|---|
| `POST /api/auth/register` | Público | Alta de lector (rol `USER`) |
| `POST /api/auth/login` | Público | Devuelve el token y establece la cookie |
| `POST /api/auth/logout` | Público | Revoca el token y limpia la cookie |
| `GET /api/libros` · `GET /api/libros/{id}` | Autenticado | Consulta del catálogo |
| `POST /api/libros` | ADMIN | Alta de libro |
| `PUT /api/libros/{id}` | ADMIN | Edición |
| `DELETE /api/libros/{id}` | ADMIN | Retirada (409 si tiene préstamos) |
| `GET /api/prestamos` | ADMIN | Todos los préstamos |
| `GET /api/prestamos/mios` | Autenticado | Préstamos propios |
| `GET /api/prestamos/{id}` | ADMIN o dueño | Un préstamo |
| `POST /api/prestamos?libroId=` | Autenticado | Pedir un libro (409 si no está disponible) |
| `PUT /api/prestamos/{id}/devolver` | ADMIN o dueño | Devolver (409 si ya estaba devuelto) |
| `GET /api/usuarios` | ADMIN | Listado |
| `GET /api/usuarios/me` | Autenticado | Perfil propio |
| `GET/PUT /api/usuarios/{id}` | ADMIN o dueño | Consulta / edición |
| `POST /api/usuarios/{id}/revocar-sesiones` | ADMIN o dueño | Invalida todos los tokens del usuario |
| `DELETE /api/usuarios/{id}` | ADMIN | Borrado (409 si tiene préstamos o es uno mismo) |

Los errores devuelven `{"error": "mensaje"}` con el código apropiado (400 validación, 401 no autenticado, 403 sin permiso, 404, 409 conflicto, 429 demasiadas peticiones, 500 genérico). El directorio `postman/` solo contiene variables globales del espacio de trabajo, no una colección de peticiones.

---

## 7. Frontend

Ubicación: `src/main/resources/static`. Spring Boot lo sirve directamente, sin servidor aparte ni paso de compilación.

| Archivo | Responsabilidad |
|---|---|
| `index.html` | HTML semántico y accesible (skip-link, landmarks, tabs ARIA, `aria-live`/`aria-busy`). Carga los CSS y `js/main.js` como módulo. |
| `js/api.js` | **Único** sitio con `fetch`: servicio HTTP con `ApiError`, normalización de errores, contador de peticiones (evento `api:carga`) y evento `sesion:caducada` ante un 401 (salvo en `/api/auth/`). |
| `js/state.js` | Estado único (usuario, préstamos, libros, vista, carga) con suscripción y derivados memoizados (`activos`, `devueltos`, `disponibles`, `esAdmin`). |
| `js/views/*.js` | Render: estado a HTML por bloque (cabecera, acceso, estantería, catálogo, historial, admin). |
| `js/acciones.js`, `js/events.js` | Orquestación contra la API y delegación de eventos (`data-solicitar`, `data-devolver`, `data-retirar`, `data-tab`). |
| `js/dom.js`, `js/covers.js` | Helpers (`esc` anti-XSS, toast) y generación de cubiertas/lomos y fechas. |
| `js/main.js` | Punto de entrada: suscribe el render y arranca. |
| `css/tokens.css`, `base.css`, `layout.css`, `components/*.css` | Tokens como custom properties (`--bib-sys-*` de sistema y por componente), globales, layout y un fichero por bloque BEM (`bloque__elemento--modificador`). |

**Decisiones de diseño visual**
- **«Mis préstamos» se muestra como una estantería con los libros de lado (lomos)**. Al pasar el cursor, el libro «sale» de la balda (sube y crece con más sombra) y aparece a su lado una tarjeta con autor, género, fecha y el botón *Devolver*; nunca se superpone al libro. Funciona también con teclado (`:focus-within`).
- El catálogo de disponibles usa tarjetas con la cubierta de frente; el historial de devoluciones se presenta como un ticket.
- Se respeta `prefers-reduced-motion` y el diseño es responsive.
- Sin dependencias externas salvo las fuentes de Google Fonts.

---

## 8. Estrategia de tests

`./mvnw test` ejecuta 106 tests sobre **H2 en memoria en modo MySQL**, con el perfil `test` (`src/test/resources/application-test.yml`, esquema `create-drop`). Los tests viven en el paquete del módulo que ejercitan (`prestamo/domain/PrestamoServiceTest`, `libro/web/LibroControllerTest`…).

| Test | Qué cubre |
|---|---|
| `ArchitectureTest` | **19 reglas ArchUnit** que protegen la arquitectura modular (ver abajo) |
| `auth/web/AuthControllerTest` | Registro (rol forzado a USER, email normalizado, duplicados), login, validación, rate limiting, logout con cookie |
| `auth/security/JwtServiceTest` | Generación y validación de tokens (firma manipulada, emisor/audiencia distintos, caducado, clave corta o nula) |
| `libro/web/LibroControllerTest` | CRUD del catálogo, permisos, conflicto de ISBN |
| `prestamo/web/PrestamoControllerTest` | Alta y devolución, aislamiento entre usuarios, acceso ajeno 403, devolución doble 409 |
| `prestamo/domain/PrestamoServiceTest` | Reglas del servicio de préstamos directamente |
| `usuario/web/UsuarioControllerTest` | Permisos admin/dueño, edición, revocación de sesiones e invalidación por cambio de contraseña |
| `SecurityIntegrationTest` | Rutas públicas y 401 en JSON en las protegidas sin token |
| `persistence/RepositoryTest` | Consultas de los repositorios JPA |
| `DemoApplicationTests` | Arranque del contexto |

`BaseIntegrationTest` arranca el contexto completo con MockMvc y ofrece utilidades para crear usuarios, libros y tokens.

### Tests de arquitectura (`ArchitectureTest`)
Fallan el build si alguien rompe las fronteras del monolito modular:

| Grupo | Regla |
|---|---|
| **Estructura modular** | Sin ciclos entre módulos; `shared` no depende de ningún módulo; `libro` y `usuario` no dependen de ningún otro módulo; `prestamo` solo depende de `libro` y `usuario` (no de `auth` ni `config`); `auth` solo depende de `usuario` y `shared`; nadie depende de `config`. |
| **Encapsulación** | Las clases internas (`web`, `persistence`, `seed`, `security`) de un módulo solo pueden ser usadas desde ese mismo módulo: la comunicación entre módulos pasa siempre por su capa `domain`. Además, un módulo solo puede contener los subpaquetes `domain`, `web`, `persistence`, `seed` y `security` (nada suelto en la raíz). |
| **Hexagonal interna** | El `domain` de cada módulo no depende de sus adaptadores (`web`, `persistence`, `seed`, `security`); `web` y `persistence` no se conocen entre sí. |
| **Independencia de frameworks** | El `domain` no usa Spring Security, jjwt, Spring Data ni Spring MVC/HTTP; la API servlet solo se usa en `web`, `security` y `config`. |
| **Convenciones** | Los `@RestController` viven en `*.web`; las entidades JPA en `*.domain`; los repositorios Spring Data en `*.persistence`; no hay `@Autowired`. |

---

## 9. Decisiones, compromisos y limitaciones conocidas

**Compromisos de arquitectura (deliberados)**
- **Las entidades llevan anotaciones JPA y forman parte de la capa `domain` de su módulo.** La hexagonal «pura» separaría un modelo de dominio de las entidades de persistencia y las mapearía entre sí. Se descartó por el coste de mapeo y duplicación en una aplicación de este tamaño; la consecuencia es que el `domain` de los módulos depende de `jakarta.persistence` (una especificación, no una implementación).
- **`@Service` y `@Transactional` en `domain`.** Son anotaciones de Spring, pero ligeras; evitan crear una capa de configuración de beans solo para registrar los servicios. El límite se mantuvo en lo que importa: ningún servicio importa tipos de persistencia ni de seguridad (lo verifican los tests de arquitectura).
- **`prestamo` comparte entidades con `libro` y `usuario`.** `Prestamo` mantiene `@ManyToOne` hacia `Libro` y `Usuario` (y `AuthzHelper` navega `prestamo.getUsuario()`). Un monolito modular estricto referenciaría por id para poder separar esquemas; se mantuvo la relación JPA por simplicidad y porque el bloqueo pesimista y las consultas se benefician de ella. Es la costura a deshacer si un módulo se extrae.
- **Todos los módulos comparten una sola base de datos y un solo esquema**, sin esquemas separados por módulo.
- **Los servicios devuelven DTO de respuesta**, no entidades: los controladores no construyen entidades ni mapean; las lecturas son `@Transactional(readOnly = true)`.

**Limitaciones conocidas**
- **Estado en memoria en un solo nodo:** el rate limiter y la lista de tokens revocados viven en memoria de la JVM. Con varias instancias o tras un reinicio no se comparten. La revocación por `tokenVersion` sí es robusta porque está en base de datos. Para un despliegue en varios nodos habría que mover ambos a Redis o a la base de datos.
- **El rate limiting usa `request.getRemoteAddr()`.** Detrás de un proxy inverso depende de `forward-headers-strategy: native` (ya configurado) para ver la IP real del cliente.
- **Sin migraciones de esquema** (`ddl-auto: validate`, sin Flyway/Liquibase): los cambios de esquema se aplican a mano. Añadir Flyway sería la mejora más inmediata.
- **Cambiar la contraseña invalida también la sesión actual** del usuario, que debe volver a entrar.
- **`JpaPrestamoRepository.findByFechaDevolucionIsNull` (en `prestamo/persistence`)** solo se usa en `RepositoryTest`; ningún caso de uso lo consume.
- **Reglas sin test automático:** el 409 al retirar un libro o borrar un usuario con préstamos, el bloqueo de autoborrado del administrador y la concurrencia real sobre el bloqueo pesimista (los tests ejercitan la lógica en un solo hilo).
- **No hay límite de préstamos por usuario ni fecha de vencimiento**: el modelo solo registra préstamo y devolución.
