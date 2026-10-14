# Biblioteca

Aplicación web de gestión de una biblioteca: catálogo de libros, préstamos y devoluciones, usuarios con roles (`USER` / `ADMIN`) y una interfaz web ligera servida por la propia aplicación.

- **Backend:** Java 21, Spring Boot 4.1, Spring MVC, Spring Data JPA, Spring Security, JWT (jjwt 0.12), Lombok.
- **Base de datos:** MySQL en ejecución normal; H2 en memoria (modo MySQL) para los tests.
- **Frontend:** HTML + CSS + JavaScript sin framework ni paso de compilación, en `src/main/resources/static`.
- **Tests:** JUnit 5 + MockMvc, 87 tests de integración y unitarios.

Este documento explica **qué hace cada capa y cada clase, y por qué se tomó cada decisión**, con especial atención a la arquitectura.

---

## Índice

1. [Puesta en marcha](#1-puesta-en-marcha)
2. [Visión general de la arquitectura](#2-visión-general-de-la-arquitectura)
3. [Capas y clases](#3-capas-y-clases)
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

La aplicación sigue una **arquitectura hexagonal (puertos y adaptadores) pragmática**. La idea central es la **inversión de dependencias**: el dominio (reglas de negocio) no depende de ningún framework de infraestructura; son la persistencia y la seguridad las que dependen del dominio, implementando interfaces (puertos) que el dominio define.

```
                     ┌─────────────────────────────────────────────┐
   HTTP / navegador  │  container/   (adaptador de entrada: REST)   │
          │          └──────────────────────┬──────────────────────┘
          ▼                                  │ usa
 ┌──────────────────┐        ┌───────────────▼───────────────┐
 │ security/        │        │ domain/                       │
 │  filtros, JWT,   │        │  service/   casos de uso      │
 │  Spring Security │        │  entity/    modelo            │
 │  (adaptador)     │───────▶│  dto/       contratos de E/S  │
 │ implementa los   │ imple- │  exception/ errores de negocio│
 │ puertos de       │ menta  │  port/      ◀── INTERFACES    │
 │ seguridad        │        └───────────────▲───────────────┘
 └──────────────────┘                        │ implementa
                              ┌──────────────┴───────────────┐
                              │ persistance/ (adaptador de   │
                              │ salida: JPA + MySQL)         │
                              └──────────────────────────────┘
```

**La regla de dependencias:** las flechas apuntan siempre hacia `domain`. Ninguna clase de `domain` importa `persistance`, `security`, `container`, Spring Security, Spring Data ni jjwt (se verificó con una búsqueda de imports tras el refactor). Lo único de Spring que permanece en el dominio son las anotaciones ligeras `@Service` y `@Transactional`, una concesión consciente (ver [sección 9](#9-decisiones-compromisos-y-limitaciones-conocidas)).

### Por qué esta arquitectura
- **Testabilidad y sustitución:** el dominio habla con interfaces. Cambiar MySQL por otra fuente, o JWT por otro mecanismo, implica escribir un adaptador nuevo sin tocar las reglas de negocio.
- **Reglas de negocio en un solo sitio:** los controladores no deciden nada; los servicios de dominio concentran las reglas (p. ej. «no se puede devolver dos veces un préstamo»).
- **Frontera de seguridad clara:** todo lo que depende de Spring Security o de jjwt vive en `security/`; el dominio solo conoce conceptos como «hashear una contraseña» o «emitir un token».

### Inyección de dependencias
Toda la aplicación usa **inyección por constructor** (`@RequiredArgsConstructor` de Lombok sobre campos `private final`), sin `@Autowired` en ningún sitio. Motivos: las dependencias son explícitas y obligatorias, las clases son inmutables tras construirse, se pueden instanciar en tests sin contenedor y se evitan dependencias circulares ocultas (el contenedor las detecta al arrancar).

---

## 3. Capas y clases

Paquete raíz: `com.example.demo`.

### 3.1 `container/` — adaptador de entrada (REST)

**Propósito:** traducir HTTP a llamadas de dominio y de vuelta. No contiene reglas de negocio. Se llama `container` y no `controller` por decisión histórica del proyecto.

| Clase | Responsabilidad |
|---|---|
| `AuthController` | `/api/auth/*`: registro, login y logout. Gestiona la **cookie** `jwt_token` (HttpOnly, SameSite=Strict, `Secure` si la petición es HTTPS). La lógica de registro/login/revocación se delega en `AuthService`; el controlador solo se ocupa del transporte (cabeceras, cookies). En logout revoca el token tanto si llega por cabecera `Authorization` como por cookie. |
| `LibroController` | CRUD del catálogo. Lectura abierta a cualquier usuario autenticado; crear, editar y borrar solo `ADMIN` (`@PreAuthorize`). Devuelve 409 si el ISBN ya existe. |
| `PrestamoController` | Crear préstamo, devolver, listar los propios (`/mios`), consultar por id (admin o dueño). Traduce excepciones de dominio a 404/409. |
| `UsuarioController` | Perfil propio (`/me`), consulta/edición (admin o dueño), borrado (solo admin, nunca el propio) y revocación de sesiones. |

**Decisión:** la autorización fina se expresa con `@PreAuthorize` en el propio endpoint (`hasRole('ADMIN') or @authz.esDueñoDePrestamo(#id)`), de modo que la regla de acceso queda visible junto al método que protege.

### 3.2 `domain/` — núcleo de negocio

**Propósito:** contener las reglas de la biblioteca, independientes de cómo se almacenen los datos o se autentique al usuario.

#### `domain/entity/` — modelo
- `Libro`, `Usuario`, `Prestamo`: entidades JPA. `equals`/`hashCode` se basan **solo en el `id`** (evitan problemas con proxies y colecciones de Hibernate) y `toString` omite relaciones y la contraseña (no filtra datos en logs).
- `Genero`: enumeración de géneros (`CLASICA`, `FANTASIA`, …). Se guarda como `STRING`, no como ordinal, para que reordenar la enumeración no corrompa datos existentes.
- `Usuario.tokenVersion`: contador para invalidar tokens (ver [Seguridad](#4-seguridad)).
- `Usuario.rol` es un `String` (`USER`/`ADMIN`) por simplicidad; no hay jerarquía de roles que justifique un enum o una tabla.

#### `domain/dto/` — contratos de entrada/salida
Los DTO separan el contrato HTTP del modelo persistente y son la **defensa contra la asignación masiva** (mass assignment): por ejemplo, `UsuarioDTO` no tiene campo `rol`, así que un cliente no puede registrarse como administrador enviándolo en el JSON (hay un test que lo comprueba).
- Entrada con validación Bean Validation: `UsuarioDTO` (registro), `UsuarioUpdateDTO` (edición; contraseña opcional), `LoginDTO`, `LibroDTO`. Incluyen límites de tamaño; la contraseña admite **máximo 72 caracteres** porque bcrypt trunca/rechaza por encima de 72 bytes.
- Salida: `UsuarioResponseDTO` (**nunca incluye la contraseña**), `LibroResponseDTO`, `PrestamoResponseDTO`.

#### `domain/port/` — puertos (interfaces definidas por el dominio)
Es la pieza clave de la inversión de dependencias. El dominio declara *qué necesita*; otros paquetes deciden *cómo se hace*.

| Puerto | Qué expresa | Implementado por |
|---|---|---|
| `UsuarioRepository`, `LibroRepository`, `PrestamoRepository` | Acceso a datos con solo las operaciones que el dominio necesita (no todo `JpaRepository`) | `persistance/adapter/*RepositoryAdapter` |
| `PasswordHasher` | «Hashear una contraseña» | `security/SpringPasswordHasher` |
| `TokenService` | «Emitir un token para este usuario y versión» | `security/JwtService` |
| `TokenRevocation` | «Revocar este token» | `security/TokenDenylistService` |
| `CredentialsAuthenticator` | «Comprobar email/contraseña» (lanza `InvalidCredentialsException`) | `security/SpringCredentialsAuthenticator` |

`LibroRepository.findByIdWithLock` documenta explícitamente que debe usarse dentro de una transacción: el bloqueo pesimista forma parte del contrato, no es un detalle oculto.

#### `domain/service/` — casos de uso
| Clase | Responsabilidad |
|---|---|
| `AuthService` | Registro (normaliza el email a minúsculas; si ya existía devuelve vacío sin revelarlo), login (autentica, obtiene la `tokenVersion` actual y emite el token) y revocación de tokens. |
| `LibroService` | Consulta y persistencia del catálogo. Impide retirar un libro con préstamos registrados (conserva el historial). |
| `UsuarioService` | Consulta, actualización (normaliza email; si cambia la contraseña **incrementa `tokenVersion`**), revocación de sesiones y borrado (bloqueado si el usuario tiene préstamos). |
| `PrestamoService` | **Núcleo de negocio.** `crearPrestamo` y `devolverLibro` son `@Transactional` y bloquean la fila del libro (`SELECT … FOR UPDATE`) para que dos peticiones simultáneas no presten el mismo ejemplar ni lo «devuelvan» dos veces. `devolverLibro` rechaza (409) un préstamo ya devuelto: sin esa comprobación, una segunda devolución volvería a marcar el libro como disponible aunque otro usuario ya lo tuviera. |
| `LibroSeeder` | `CommandLineRunner` condicionado a `app.seed-books=true`. Carga el catálogo de ejemplo de forma **idempotente** (por ISBN; solo completa el género de libros ya existentes). Depende únicamente del puerto, no de JPA. |

#### `domain/exception/`
- `NotFoundException`: recurso inexistente → 404.
- `InvalidCredentialsException`: credenciales inválidas → 401. Existe para que el dominio **no conozca** `BadCredentialsException` de Spring Security; el adaptador traduce una en otra.

### 3.3 `persistance/` — adaptador de salida (JPA)

**Propósito:** implementar los puertos de repositorio sobre Spring Data JPA. (El nombre del paquete conserva la grafía original `persistance`.)

- `repository/Jpa*Repository`: interfaces de Spring Data (`JpaRepository`). Contienen las consultas derivadas y la consulta con `@Lock(PESSIMISTIC_WRITE)`. Llevan prefijo `Jpa` para distinguirlas de los puertos del dominio, que tienen el nombre «limpio».
- `adapter/*RepositoryAdapter`: implementan el puerto del dominio delegando en el repositorio JPA.

**Decisión:** la capa extra de adaptadores parece redundante hoy (delegan 1:1), pero cumple una función: el dominio queda limitado a las operaciones que *realmente* usa y deja de ver `JpaRepository` con todos sus métodos (`deleteAll`, paginación, etc.). Además es el punto donde se cambiaría la tecnología de persistencia.

### 3.4 `security/` — adaptador de seguridad y configuración

**Propósito:** todo lo que depende de Spring Security y de jjwt.

| Clase | Responsabilidad |
|---|---|
| `SecurityConfig` | Cadena de filtros: rutas públicas (`/api/auth/**`, estáticos) y el resto autenticadas; **sesión sin estado**; cabeceras (CSP, HSTS, `X-Content-Type-Options`, `X-Frame-Options: DENY`, Referrer-Policy); respuesta 401 en JSON. Define `PasswordEncoder` (BCrypt). |
| `JwtAuthFilter` | Extrae el token (cabecera `Authorization: Bearer` o cookie `jwt_token`), comprueba la lista de revocados, valida firma/emisor/audiencia/expiración **y que la versión del token coincida con la del usuario**, y puebla el `SecurityContext`. Ante un token inválido no autentica y deja que Spring responda 401. |
| `JwtService` | Genera y valida JWT (HS-firmados con la clave configurada). Exige `issuer` y `audience`. Implementa `TokenService`. Falla al arrancar si la clave es corta. |
| `CustomUserDetailsService` + `AuthUser` | Carga el usuario desde el puerto `UsuarioRepository`. `AuthUser` extiende `User` para transportar la `tokenVersion` hasta el filtro. |
| `TokenDenylistService` | Lista en memoria de tokens revocados (por su firma) con caducidad. Implementa `TokenRevocation`. |
| `RateLimitFilter` | Limita `/api/auth/login` (5/min) y `/api/auth/register` (3/min) por IP, con ventana fija. Orden de máxima precedencia: se ejecuta antes que cualquier otro filtro. |
| `AuthzHelper` (`@authz`) | Bean usado en expresiones `@PreAuthorize` para decidir «¿es dueño de este usuario/préstamo?». Usa los puertos. |
| `SpringPasswordHasher`, `SpringCredentialsAuthenticator` | Adaptadores de los puertos `PasswordHasher` y `CredentialsAuthenticator` sobre `PasswordEncoder` y `AuthenticationManager`. |
| `AdminSeeder` | Crea el administrador inicial desde configuración si no existe. Si no hay credenciales configuradas, no hace nada (no hay administrador por defecto). |
| `GlobalExceptionHandler` | `@RestControllerAdvice` que convierte excepciones en respuestas JSON `{"error": "..."}` con el código HTTP adecuado y **oculta los detalles internos** (los 500 devuelven un mensaje genérico y el detalle se registra solo en el log). |

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
| `index.html` | Estructura de la página: vista de acceso, vista de biblioteca (préstamos, catálogo, historial, gestión admin) y el contenedor del *toast*. |
| `js/api.js` | Cliente HTTP (`fetch` con `credentials: same-origin`). Gestiona el «sesión caducada» ante un 401 (excepto en las rutas de autenticación, para no mostrar «sesión caducada» en un login fallido). |
| `js/app.js` | Lógica de interfaz: cambio de vistas, formularios, pintado de estanterías, catálogo, historial y administración. Escapa todo texto del servidor antes de insertarlo en el DOM (`esc`) para evitar XSS. |
| `js/covers.js` | Genera las cubiertas de los libros: color según el género (o por hash del ISBN/título si no tiene), lomo con título vertical y formato de fechas. |
| `css/styles.css` | Estilos. Está organizado en capas: base temática (tipografías, estantería, ticket), y una «capa estética» posterior que refina fondo, cabecera, formularios y botones. |

**Decisiones de diseño visual**
- **«Mis préstamos» se muestra como una estantería con los libros de lado (lomos)**. Al pasar el cursor, el libro «sale» de la balda (sube y crece con más sombra) y aparece a su lado una tarjeta con autor, género, fecha y el botón *Devolver*; nunca se superpone al libro. Funciona también con teclado (`:focus-within`).
- El catálogo de disponibles usa tarjetas con la cubierta de frente; el historial de devoluciones se presenta como un ticket.
- Se respeta `prefers-reduced-motion` y el diseño es responsive.
- Sin dependencias externas salvo las fuentes de Google Fonts.

---

## 8. Estrategia de tests

`./mvnw test` ejecuta 87 tests sobre **H2 en memoria en modo MySQL**, con el perfil `test` (`src/test/resources/application-test.yml`, esquema `create-drop`).

| Test | Qué cubre |
|---|---|
| `AuthControllerTest` | Registro (rol forzado a USER, email normalizado, duplicados), login, validación, rate limiting, logout con cookie |
| `LibroControllerTest` | CRUD del catálogo, permisos, conflicto de ISBN |
| `PrestamoControllerTest` | Alta y devolución, aislamiento entre usuarios, acceso ajeno 403, devolución doble 409 |
| `UsuarioControllerTest` | Permisos admin/dueño, edición, revocación de sesiones e invalidación por cambio de contraseña |
| `SecurityIntegrationTest` | Rutas públicas (página, CSS, JS, registro) y respuesta 401 en JSON en las protegidas sin token |
| `PrestamoServiceTest` | Reglas del servicio de préstamos directamente |
| `JwtServiceTest` | Generación y validación de tokens (firma manipulada, emisor/audiencia distintos, caducado, clave corta o nula) |
| `RepositoryTest` | Consultas de los repositorios JPA |
| `DemoApplicationTests` | Arranque del contexto |

`BaseIntegrationTest` arranca el contexto completo con MockMvc y ofrece utilidades para crear usuarios, libros y tokens. Los tests de integración usan directamente los repositorios `Jpa*` (la capa de persistencia) para preparar y comprobar datos.

---

## 9. Decisiones, compromisos y limitaciones conocidas

**Compromisos de arquitectura (deliberados)**
- **Las entidades llevan anotaciones JPA y viven en el dominio.** La hexagonal «pura» separaría un modelo de dominio de las entidades de persistencia y las mapearía entre sí. Se descartó por el coste de mapeo y duplicación en una aplicación de este tamaño; la consecuencia es que el dominio depende de `jakarta.persistence` (una especificación, no una implementación).
- **`@Service` y `@Transactional` en el dominio.** Son anotaciones de Spring, pero ligeras; evitan crear una capa de configuración de beans solo para registrar los servicios. El límite se mantuvo en lo que importa: ningún servicio importa tipos de persistencia ni de seguridad.
- **Los controladores construyen entidades a partir de DTO** en algunos casos (`LibroController`), en lugar de delegarlo del todo al servicio. Es una simplificación aceptable aquí; moverlo al servicio sería el siguiente paso natural.

**Limitaciones conocidas**
- **Estado en memoria en un solo nodo:** el rate limiter y la lista de tokens revocados viven en memoria de la JVM. Con varias instancias o tras un reinicio no se comparten. La revocación por `tokenVersion` sí es robusta porque está en base de datos. Para un despliegue en varios nodos habría que mover ambos a Redis o a la base de datos.
- **El rate limiting usa `request.getRemoteAddr()`.** Detrás de un proxy inverso depende de `forward-headers-strategy: native` (ya configurado) para ver la IP real del cliente.
- **Sin migraciones de esquema** (`ddl-auto: validate`, sin Flyway/Liquibase): los cambios de esquema se aplican a mano. Añadir Flyway sería la mejora más inmediata.
- **Cambiar la contraseña invalida también la sesión actual** del usuario, que debe volver a entrar.
- **`JpaPrestamoRepository.findByFechaDevolucionIsNull`** solo se usa en `RepositoryTest`; ningún caso de uso lo consume.
- **Reglas sin test automático:** el 409 al retirar un libro o borrar un usuario con préstamos, el bloqueo de autoborrado del administrador y la concurrencia real sobre el bloqueo pesimista (los tests ejercitan la lógica en un solo hilo).
- **No hay límite de préstamos por usuario ni fecha de vencimiento**: el modelo solo registra préstamo y devolución.
