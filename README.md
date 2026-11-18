# Biblioteca

Aplicación web de gestión de una biblioteca: catálogo de libros, préstamos y devoluciones, usuarios con roles (`USER` / `ADMIN`) y una interfaz web ligera servida por la propia aplicación.

- **Backend:** Java 21, Spring Boot 4.1, Spring MVC, Spring Data JPA, Spring Security, JWT (jjwt 0.12), MapStruct, springdoc (OpenAPI), Lombok.
- **Base de datos:** MySQL en ejecución normal; H2 en memoria (modo MariaDB) para los tests. Esquema gestionado con **Flyway**.
- **Frontend:** HTML + CSS + JavaScript sin framework ni paso de compilación, en `src/main/resources/static`.
- **Arquitectura:** monolito modular (módulos `catalog`, `users`, `loans`, `auth`) con capas `controller → usecase → domain ← persistence` dentro de cada módulo. Las fronteras las verifican **Spring Modulith** y **ArchUnit**.
- **Tests:** JUnit 5 + Mockito + MockMvc + ArchUnit + Spring Modulith + Testcontainers (MariaDB). `./mvnw clean test` ejecuta 532 tests: 504 pasan sin Docker y 28 (los de repositorio contra MariaDB) se omiten si no hay Docker.

Este documento explica **qué hace cada módulo y cada capa, de qué depende y por qué se tomó cada decisión**, con especial atención a la arquitectura.

---

## Índice

1. [Puesta en marcha](#1-puesta-en-marcha)
2. [Visión general de la arquitectura](#2-visión-general-de-la-arquitectura)
3. [Capas de un módulo: responsabilidades y dependencias](#3-capas-de-un-módulo-responsabilidades-y-dependencias)
4. [Módulos y clases](#4-módulos-y-clases)
5. [Seguridad](#5-seguridad)
6. [Modelo de datos y reglas de negocio](#6-modelo-de-datos-y-reglas-de-negocio)
7. [API REST](#7-api-rest)
8. [Frontend](#8-frontend)
9. [Estrategia de tests y reglas de arquitectura](#9-estrategia-de-tests-y-reglas-de-arquitectura)
10. [Decisiones, compromisos y limitaciones conocidas](#10-decisiones-compromisos-y-limitaciones-conocidas)

---

## 1. Puesta en marcha

### Requisitos
- JDK 21
- MySQL (para ejecución normal). Los tests no lo necesitan; los de repositorio contra MariaDB usan Testcontainers y requieren Docker (sin Docker se omiten).

### Variables de configuración

Los secretos **nunca** se versionan; se inyectan por entorno (`application.yml` solo define referencias `${...}`).

| Variable | Obligatoria | Descripción |
|---|---|---|
| `DB_PASSWORD` | Sí | Contraseña del usuario de base de datos |
| `DB_URL` | No | Por defecto `jdbc:mysql://localhost:3306/biblioteca` |
| `DB_USERNAME` | No | Por defecto `biblioteca_app` |
| `JWT_SECRET` | Sí | Clave de firma. Mínimo 32 bytes; la aplicación **no arranca** si es más corta |
| `JWT_EXPIRATION_MS` | No | Vida del token de acceso, por defecto `900000` (15 min) |
| `JWT_REFRESH_EXPIRATION_MS` | No | Vida del refresh token, por defecto `1209600000` (14 días) |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | No | Si ambos existen, se crea un administrador en el primer arranque (idempotente) |

### Perfiles
- `application.yml`: configuración común.
- `application-prod.yml`: `show-sql: false` y **Swagger UI / OpenAPI desactivados**.
- `application-dev.yml.example`: plantilla para desarrollo. Cópiala a `application-dev.yml` (que está en `.gitignore`) y actívala con el perfil `dev`.
- `application-local.yml` y `.env` están **ignorados por git**: sirven para credenciales locales.

### Ejecutar y probar
```bash
export DB_PASSWORD=... JWT_SECRET=...   # ≥ 32 bytes
./mvnw spring-boot:run
# → http://localhost:8080

./mvnw clean test                        # usa H2; no necesita MySQL
```

### Esquema de base de datos
Lo gestiona **Flyway** (`src/main/resources/db/migration`); Hibernate solo lo **valida** (`ddl-auto: validate`).

| Migración | Contenido |
|---|---|
| `V1__baseline.sql` | Tablas `usuarios`, `libros`, `prestamos` |
| `V2__seed_books.sql` | Catálogo inicial de ejemplo (idempotente por ISBN) |
| `V3__loan_version.sql` | Columna `version` en `prestamos` (bloqueo optimista) |
| `V4__refresh_tokens.sql` | Tabla `refresh_tokens` |

Con `baseline-on-migrate` y `baseline-version: 1`, una base de datos creada antes de Flyway se toma como V1 y no se vuelve a ejecutar el esquema base.

---

## 2. Visión general de la arquitectura

La aplicación es un **monolito modular**: un único artefacto desplegable (un proceso, una base de datos) dividido en **módulos de negocio con fronteras explícitas**. Cada módulo es un paquete de primer nivel con **API pública** (lo que otros módulos pueden usar) y un paquete **`internal`** (privado) donde viven sus capas.

```
 com.example.demo
 ├── catalog/   catálogo de libros                  API: CatalogModule · dto · spi
 ├── users/     cuentas y roles                     API: UsersModule · dto · spi
 ├── loans/     préstamos y devoluciones            (sin API pública)
 ├── auth/      registro, login, refresh, logout    (sin API pública)
 ├── shared/    núcleo transversal sin Spring       (Requester, puertos técnicos, excepciones)
 └── config/    composición técnica y seguridad     (SecurityConfig, JWT, filtros, hash, seeders)
```

### Dependencias entre módulos

```
                 ┌──────────────┐        ┌──────────────┐
   loans ───────▶│ catalog      │        │ users        │◀────── auth
     │           │ (API+dto+spi)│        │ (API+dto+spi)│
     └───────────┴──────────────┘        └──────────────┘
         loans ──▶ catalog, users           auth ──▶ users

   Todos ──▶ shared          config ──▶ (conoce a todos)     nadie ──▶ config
```

| Módulo | Puede depender de | Qué usa exactamente |
|---|---|---|
| `catalog` | `shared` | `Requester`, `ForbiddenException` |
| `users` | `shared` | `Requester`, `ForbiddenException`, `PasswordHasher` |
| `loans` | `catalog`, `users`, `shared` | `CatalogModule`, `catalog.dto`, `catalog.spi`; `UsersModule`, `users.dto`, `users.spi` |
| `auth` | `users`, `shared` | `UsersModule`, `users.dto`; `PasswordHasher`, `TokenProvider`, `InvalidCredentialsException` |
| `shared` | nadie | — |
| `config` | cualquiera | Es una pieza abierta (`Type.OPEN`); **nadie depende de ella** |

Estas dependencias están declaradas en el `package-info.java` de cada módulo con `@ApplicationModule(allowedDependencies = ...)` y **Spring Modulith** las verifica (`ApplicationModulesTest`). El grafo es **acíclico**.

### API pública de un módulo

Un módulo solo expone tres cosas, y todo lo demás está en `internal`:

| Elemento | Qué es | Ejemplo |
|---|---|---|
| **`XModule`** (raíz del módulo) | Interfaz con las operaciones que ofrece a otros módulos. Recibe y devuelve DTO públicos, nunca el modelo interno ni sus excepciones. | `CatalogModule.reserve(id)`, `UsersModule.getByIds(ids)` |
| **`dto`** (`@NamedInterface("dto")`) | Records de lectura/entrada para otros módulos. | `BookSummary`, `ReservationResult`, `UserSummary`, `NewUser`, `UserCredentials` |
| **`spi`** (`@NamedInterface("spi")`) | Puntos de extensión que otros módulos implementan. | `BookInUseChecker`, `UserInUseChecker` |

`XModuleImpl` (en `internal/application`) implementa la API pública delegando en el servicio de dominio y **traduciendo excepciones a resultados** (p. ej. `ReservationResult.Status.NOT_AVAILABLE`), de modo que las excepciones internas no cruzan la frontera.

`loans` y `auth` **no publican API** porque ningún otro módulo los invoca directamente.

### Relaciones entre módulos: solo por ID

`loans` **no tiene relaciones JPA** con libros ni usuarios: la entidad `LoanEntity` guarda solo `bookId` y `userId`. Cuando necesita los datos, los pide a `CatalogModule.getByIds(...)` y `UsersModule.getByIds(...)` (en lote, sin N+1) y compone el DTO. Así cada módulo podría tener su propio esquema o extraerse a un servicio.

### Cómo se evitan los ciclos: SPI

`catalog` y `users` necesitan saber si un libro/usuario tiene préstamos (para impedir borrarlo), pero `loans` ya depende de ellos. Se **invierte la dependencia**: `catalog` publica `BookInUseChecker` y `users` publica `UserInUseChecker`; `loans` los implementa (`BookWithLoansChecker`, `UserWithLoansChecker`, en `loans/internal/application`) como beans, y los servicios reciben `List<...Checker>` por constructor. `catalog` y `users` ignoran por completo la existencia de `loans`.

Es un **veto síncrono** y no un evento, a propósito: el resultado condiciona el borrado y debe decidirse dentro de la misma transacción. Es la única excepción a la comunicación por eventos, y por eso existe el paquete `spi`.

### Por qué esta arquitectura
- **Cohesión por dominio:** todo lo relativo a libros está en `catalog/`; un cambio funcional toca un solo módulo.
- **Fronteras comprobables:** no son una convención oral; Modulith y ArchUnit rompen el build si alguien las viola (ver [sección 9](#9-estrategia-de-tests-y-reglas-de-arquitectura)).
- **Camino de evolución:** como los módulos solo se acoplan por API pública, DTO e IDs, podrían extraerse a servicios independientes. Mientras tanto se conserva un único despliegue y transacciones locales (p. ej. `loans` reserva el libro y crea el préstamo en una sola transacción).
- **Seguridad en un solo sitio:** Spring Security y jjwt solo se usan en `config`; los módulos de negocio no los importan.

### Inyección de dependencias
Inyección **por constructor**, sin `@Autowired` en ningún sitio (lo comprueba un test de arquitectura). Las dependencias son explícitas, las clases se instancian en tests sin contenedor y se evitan ciclos ocultos.

---

## 3. Capas de un módulo: responsabilidades y dependencias

Estructura de cada módulo (ejemplo `catalog`; `users`, `loans` y `auth` siguen el mismo esquema):

```
 catalog/
 ├── CatalogModule                     ┐
 ├── dto/        BookSummary, …        │ API PÚBLICA (otros módulos)
 ├── spi/        BookInUseChecker      ┘
 └── internal/                         PRIVADO
      ├── application/   CatalogModuleImpl
      ├── controller/    BookHttpApi · BookController
      │    ├── dto/      BookRequest, BookResponse
      │    ├── handler/  BookExceptionHandler
      │    └── mapper/   BookWebMapper
      ├── usecase/       GetAllBooksUseCase, GetBookUseCase, CreateBookUseCase, UpdateBookUseCase, DeleteBookUseCase
      ├── domain/
      │    ├── model/       Book, Isbn, Genre
      │    ├── dto/         BookDto, SaveBookDto
      │    ├── mapper/      BookMapper
      │    ├── service/     BookService, BookServiceImpl
      │    ├── repository/  BookRepository            (interfaz)
      │    └── exception/   BookNotFoundException, …
      └── persistence/
           ├── dao/entity/            BookEntity
           ├── dao/impl/jpa/          BookSpringDataRepository
           └── repository/
                ├── impl/             BookRepositoryImpl
                └── mapper/           BookPersistenceMapper
```

### Dos flujos de entrada

```
 HTTP:        Controller ──▶ UseCase ──▶ Service ──▶ Repository (interfaz) ◀── RepositoryImpl ──▶ DAO/JPA
 Otro módulo: XModule ──▶ XModuleImpl ──▶ Service ──▶ Repository (interfaz) ◀── RepositoryImpl ──▶ DAO/JPA
```

La lógica de negocio vive en el **servicio de dominio**; HTTP y otros módulos son solo dos puertas de entrada distintas.

### Responsabilidades y dependencias por capa

| Capa | Responsabilidad | Puede usar | **No** puede usar |
|---|---|---|---|
| **`controller`** | Contrato HTTP (`XHttpApi` con OpenAPI, `XController` que lo implementa), DTO web (`*Request`/`*Response`), mappers web, validación de formato (`@Valid`), traducción de excepciones a HTTP (`handler`). | `usecase`, sus DTO y mappers, `shared` | `service`, `persistence`, `repository`, Spring Security |
| **`usecase`** | **Autorización** (¿puede este `Requester` hacer esto?) y orquestación. **Un caso de uso por clase**, con un único método público `execute(...)` que recibe `Requester` como primer parámetro (salvo los públicos de auth: `RegisterUseCase`, `LoginUseCase` y `RefreshTokenUseCase`). | `domain.service`, `shared` | `controller`, `persistence`, `repository`, otros `UseCase` |
| **`application`** | Implementa la API pública del módulo (`XModuleImpl`) y los adaptadores SPI (`loans`). | `domain` (servicio), DTO públicos | `controller`, `usecase`, `persistence` |
| **`domain`** | **Reglas de negocio.** Modelo inmutable con invariantes (`Book`, `Isbn`, `Email`, `Loan`…), servicio (`XService` interfaz + `XServiceImpl`), puerto `XRepository` (interfaz), DTO internos (`*Dto`), mapper del dominio y excepciones propias. | `shared` y, en `loans`/`auth`, la API pública de otros módulos | `persistence`, `controller`, `usecase`, `application`; tampoco `jakarta.persistence`, `jakarta.validation`, Spring Web/Data/Security, servlet ni jjwt. Solo conserva `@Service` y `@Transactional`. |
| **`persistence`** | Implementa `XRepository` (`XRepositoryImpl`), entidad JPA (`XEntity`), repositorio Spring Data (`XSpringDataRepository`) y mapper modelo⇄entidad (`XPersistenceMapper`). | `domain` (modelo y puerto) | `controller`, `usecase`, `application` |

### Dónde se decide la autorización
- **En el `usecase`**, con el `Requester` (`isAdmin()`, `isOwner(id)`), lanzando `ForbiddenException` (→ 403). No se usa `@PreAuthorize` en los módulos de negocio: solo `config` puede usar seguridad por método o `SecurityContextHolder` (lo verifica ArchUnit).
- `config` convierte el principal autenticado en un `Requester` (`RequesterArgumentResolver`) y los controladores lo reciben como parámetro, sin importar nada de Spring Security.
- `SecurityConfig` solo decide qué rutas son públicas y exige autenticación en el resto.

### Convenciones de nombres
`XHttpApi`/`XController` (HTTP) · `XRequest`/`XResponse` (DTO web) · `XDto` (DTO de dominio o públicos del módulo) · `VerbNounUseCase` (p. ej. `CreateBookUseCase`, con un solo método `execute`) · `XService`/`XServiceImpl` · `XRepository` (puerto) · `XRepositoryImpl` · `XEntity` · `XSpringDataRepository` · `XPersistenceMapper` · `XMapper`/`XWebMapper` (MapStruct) · `XModule`/`XModuleImpl` · `XInUseChecker` (SPI).

---

## 4. Módulos y clases

Paquete raíz: `com.example.demo`.

### 4.1 `shared/` — núcleo transversal
Módulo **abierto** (cualquiera puede usarlo), **sin dependencias de módulos ni de Spring/Jakarta/Lombok** (ArchUnit).

| Clase | Responsabilidad |
|---|---|
| `Requester` | Quién inicia un flujo: `id` + roles. `isAdmin()`, `isOwner(id)`. Lo usan los casos de uso para autorizar. |
| `AuthenticatedPrincipal` | Identidad que sale de un token de acceso validado (`userId` + roles con prefijo `ROLE_`). |
| `PasswordHasher` | Puerto técnico: `hash` / `matches`. Implementado en `config` (BCrypt). |
| `TokenProvider` | Puerto técnico: `generate(userId, roles)` y `validate(token)`. Implementado en `config` (JWT). |
| `ForbiddenException` | Sin permiso → 403. |
| `InvalidCredentialsException` | Credenciales/refresh no válidos → 401. No distingue email inexistente de contraseña errónea. |

### 4.2 `catalog/` — catálogo de libros
**Depende de:** `shared`.

| Elemento | Responsabilidad |
|---|---|
| `CatalogModule` | API pública: `getById`, `getByIds`, `reserve(id)` y `release(id)` (con bloqueo; devuelven `ReservationResult` con `OK`, `NOT_FOUND`, `NOT_AVAILABLE` o `ALREADY_AVAILABLE`). |
| `dto/` | `BookSummary`, `Genre`, `ReservationResult`. |
| `spi/BookInUseChecker` | Veto síncrono: otros módulos indican si un libro les impide ser retirado. |
| `domain/model` | `Book` (inmutable, con invariantes), `Isbn` (valida y normaliza el ISBN), `Genre`. |
| `domain/service` | `BookServiceImpl`: consulta, alta y edición (ISBN único), `reserve`/`release` y borrado (consulta a todos los `BookInUseChecker`). |
| `usecase/` | `GetAllBooksUseCase` y `GetBookUseCase` (cualquier autenticado); `CreateBookUseCase`, `UpdateBookUseCase` y `DeleteBookUseCase` (solo `ADMIN`). |
| `controller` | `BookHttpApi` + `BookController` (`/api/libros`), `BookRequest`/`BookResponse`, `BookExceptionHandler`. |
| `persistence` | `BookEntity`, `BookSpringDataRepository` (bloqueo pesimista `PESSIMISTIC_WRITE` para reservar), `BookRepositoryImpl`. |

### 4.3 `users/` — cuentas y roles
**Depende de:** `shared`.

| Elemento | Responsabilidad |
|---|---|
| `UsersModule` | API pública: `getById`, `getByEmail`, `getByIds`, `register` (rol `USER`; vacío si el email existe), `getCredentials` (con hash, para `auth`) y `ensureAdmin`. |
| `dto/` | `UserSummary` (nunca incluye contraseña), `NewUser`, `UserCredentials` (su `toString` oculta el hash). |
| `spi/UserInUseChecker` | Veto síncrono para el borrado. |
| `domain/model` | `User` (inmutable; `rename`, `changeEmail`, `changePassword`, `revokeSessions`), `Email` (valida y normaliza a minúsculas), `Role`. `tokenVersion` se incrementa al cambiar la contraseña o revocar sesiones. |
| `domain/service` | `UserServiceImpl`: edición con comprobación de email duplicado, revocación de sesiones y borrado (bloqueado para uno mismo o si algún `UserInUseChecker` lo veta). |
| `usecase/` | `GetAllUsersUseCase` y `DeleteUserUseCase` (solo `ADMIN`); `GetCurrentUserUseCase` (autenticado); `GetUserUseCase`, `UpdateUserUseCase` y `RevokeUserSessionsUseCase` (`ADMIN` o dueño). |
| `controller` | `UserHttpApi` + `UserController` (`/api/usuarios`). `UserUpdateRequest` no tiene campo `rol` (defensa contra *mass assignment*). |
| `persistence` | `UserEntity`, repositorio Spring Data y `UserRepositoryImpl`. |

### 4.4 `loans/` — préstamos y devoluciones
**Depende de:** `catalog` (raíz, `dto`, `spi`), `users` (raíz, `dto`, `spi`) y `shared`. **Sin API pública.**

| Elemento | Responsabilidad |
|---|---|
| `domain/model/Loan` | Modelo inmutable con solo `bookId` y `userId`. `Loan.open(...)` y `markReturned(...)`; activo mientras no hay fecha de devolución. |
| `domain/service/LoanServiceImpl` | **Núcleo de negocio.** `create` y `returnLoan` son `@Transactional`: llaman a `CatalogModule.reserve/release` (que bloquea la fila del libro) y guardan el préstamo; si algo falla, todo se revierte. Traduce `ReservationResult` a excepciones de `loans`. Compone los DTO consultando `catalog` y `users` **en lote**. |
| `domain/service/LoanQueryService` | Consultas `existsByBookId` / `existsByUserId`. Separado de `LoanService` para que los checkers no cierren ciclos de beans. |
| `application/BookWithLoansChecker`, `UserWithLoansChecker` | Implementan los SPI de `catalog` y `users`: conservan el historial impidiendo borrar libros y usuarios con préstamos. |
| `usecase/` | `GetAllLoansUseCase` (`ADMIN`); `GetOwnLoansUseCase` y `CreateLoanUseCase` (autenticado); `GetLoanUseCase` y `ReturnLoanUseCase` (`ADMIN` o dueño). |
| `controller` | `LoanHttpApi` + `LoanController` (`/api/prestamos`), `LoanResponse`, `LoanBookResponse`, `LoanExceptionHandler`. |
| `persistence` | `LoanEntity` (con `@Version` para bloqueo optimista; solo ids), repositorio Spring Data y `LoanRepositoryImpl`. |

### 4.5 `auth/` — registro, login, refresh y logout
**Depende de:** `users` (raíz y `dto`) y `shared`. **Sin API pública.** No conoce Spring Security ni jjwt: la tecnología vive en `config`.

| Elemento | Responsabilidad |
|---|---|
| `domain/service/AuthServiceImpl` | `register` (delegando en `UsersModule`), `login` (comprueba el hash con `PasswordHasher`, igual de costoso exista o no el usuario), `refresh` (rota el refresh token) y `logout` (revoca). Emite el par acceso + refresh. |
| `domain/model/RefreshToken` | Hash del token, `userId`, `tokenVersion`, caducidad y revocación. |
| `domain/repository/RefreshTokenRepository` | Puerto; implementado en `persistence` (`RefreshTokenEntity`, tabla `refresh_tokens`). |
| `usecase/` | `RegisterUseCase`, `LoginUseCase` y `RefreshTokenUseCase` (públicos, sin `Requester`); `LogoutUseCase` (recibe `Requester`). |
| `controller` | `AuthHttpApi` + `AuthController` (`/api/auth`), DTO web (`LoginRequest`, `LoginResponse`, `RefreshRequest`, `LogoutRequest`…) y `AuthExceptionHandler`. |

### 4.6 `config/` — composición técnica y seguridad
Pieza **abierta**. Nadie depende de ella; ella puede conocer a los demás. Es el único sitio con Spring Security, jjwt y la API servlet. Se organiza en subpaquetes por responsabilidad:

```
 config/
 ├── ClockConfig
 ├── security/   SecurityConfig, JwtAuthenticationFilter, JwtTokenProvider, RateLimitFilter,
 │               JsonAuthenticationEntryPoint, JsonAccessDeniedHandler,
 │               BcryptPasswordHasher, PasswordEncoderConfig
 ├── web/        WebConfig, RequesterArgumentResolver, GlobalExceptionHandler
 └── startup/    AdminSeeder
```

**`config/security`**

| Clase | Responsabilidad |
|---|---|
| `SecurityConfig` | Cadena de filtros: sin estado, CSRF/form/basic desactivados, rutas públicas, cabeceras (CSP, HSTS, X-Frame-Options, Referrer-Policy), 401/403 en JSON. Activa `@EnableMethodSecurity`. |
| `JwtAuthenticationFilter` | Lee `Authorization: Bearer`, valida el token con `TokenProvider` y puebla el `SecurityContext` con un `AuthenticatedPrincipal`. |
| `JwtTokenProvider` | Implementa `TokenProvider` (jjwt): firma, emisor, audiencia y caducidad. Falla al arrancar si la clave es corta. |
| `BcryptPasswordHasher`, `PasswordEncoderConfig` | Implementan `PasswordHasher` con BCrypt. |
| `RateLimitFilter` | Limita login (5/min) y registro (3/min) por IP. |
| `JsonAuthenticationEntryPoint`, `JsonAccessDeniedHandler` | Respuestas 401/403 en JSON. |

**`config/web`**

| Clase | Responsabilidad |
|---|---|
| `RequesterArgumentResolver`, `WebConfig` | Convierten el principal autenticado en el `Requester` que reciben los controladores. |
| `GlobalExceptionHandler` | `@RestControllerAdvice` de último recurso (`ProblemDetail`): validación, JSON mal formado, `ForbiddenException`, integridad de datos, y un 500 genérico que **oculta los detalles internos**. Los errores de negocio los traduce cada módulo con su `handler`. |

**`config/startup` y raíz**

| Clase | Responsabilidad |
|---|---|
| `AdminSeeder` | `CommandLineRunner` que llama a `UsersModule.ensureAdmin` con `ADMIN_EMAIL`/`ADMIN_PASSWORD` si existen. |
| `ClockConfig` | Bean `Clock` inyectable (tests deterministas). |

---

## 5. Seguridad

### Autenticación: JWT de acceso + refresh token opaco
- **Login** devuelve un **par de tokens**:
  - **Acceso:** JWT firmado (claims `sub` = id de usuario y `roles`), de vida corta (15 min por defecto). Se envía en `Authorization: Bearer`.
  - **Refresh:** cadena aleatoria opaca de 32 bytes, vida de 14 días. En base de datos solo se guarda su **hash SHA-256**, nunca el valor.
- **No hay cookies ni sesión de servidor.** La API es sin estado, por eso **CSRF está desactivado** de forma deliberada: no hay credencial ambiental que un sitio ajeno pueda reutilizar.
- El filtro JWT no consulta la base de datos: los roles son los del momento de la emisión del token.

### Rotación y detección de reutilización del refresh
- `POST /api/auth/refresh` **rota** el refresh: revoca el recibido y emite un par nuevo.
- Si se presenta un refresh **ya revocado** (rotado o cerrado), se asume robo y se **revocan todas las sesiones del usuario**.
- Un refresh caducado, desconocido o cuya `tokenVersion` no coincide con la del usuario → 401.
- `POST /api/auth/logout` revoca el refresh indicado.

### Invalidar todas las sesiones de un usuario (`tokenVersion`)
Cada usuario tiene una `tokenVersion`. Cada refresh guarda la versión con la que se emitió. Se incrementa al **cambiar la contraseña** y con `POST /api/usuarios/{id}/revocar-sesiones` (admin o el propio usuario): desde entonces ningún refresh anterior sirve. **Los tokens de acceso ya emitidos siguen siendo válidos hasta que caducan** (máximo 15 min); es el coste de no consultar la base de datos en cada petición.

### Prevención de enumeración de usuarios
- Login con credenciales erróneas o usuario inexistente → siempre el mismo 401, y el coste del hash es el mismo (hay un hash ficticio para el caso «no existe»).
- Registro con un email ya existente → 200 sin cuerpo, indistinguible de un registro correcto.

### Otras medidas
- Contraseñas con BCrypt; nunca se devuelven en ningún DTO y los `toString` de los DTO con credenciales las ocultan.
- Rate limiting en login y registro.
- Emails normalizados a minúsculas.
- Registro público siempre con rol `USER`; el rol no se puede enviar en ninguna petición.
- Un administrador no puede eliminar su propia cuenta.
- CSP restrictiva (`default-src 'self'`; solo se permite Google Fonts), HSTS y `X-Frame-Options: DENY`.
- Swagger UI y OpenAPI desactivados en el perfil `prod`.

---

## 6. Modelo de datos y reglas de negocio

```
usuarios (1) ──< prestamos >── (1) libros          prestamos guarda solo ids (sin FK de JPA entre módulos)
usuarios (1) ──< refresh_tokens                    (por user_id)
```

| Tabla | Campos principales |
|---|---|
| `usuarios` | `id`, `nombre`, `email` (único), `password` (hash), `rol`, `token_version` |
| `libros` | `id`, `titulo`, `autor`, `isbn` (único), `disponible`, `genero` |
| `prestamos` | `id`, `libro_id`, `usuario_id`, fechas de préstamo y devolución (nula mientras está activo), `version` |
| `refresh_tokens` | `token_hash`, `user_id`, `token_version`, caducidad, revocación |

Reglas clave:
- Un libro solo puede estar prestado una vez a la vez. `reserve` bloquea la fila del libro (`SELECT … FOR UPDATE`) dentro de la transacción del préstamo, incluso con peticiones concurrentes.
- Devolver un préstamo ya devuelto da 409. Una devolución concurrente la detecta el `@Version` del préstamo y revierte también la liberación del libro.
- Se conserva el historial: no se puede borrar un libro ni un usuario con préstamos (409).
- `ISBN` único (409 al duplicarlo); el email también es único.

---

## 7. API REST

Todas las rutas salvo `POST /api/auth/{register,login,refresh}` y los estáticos requieren un token de acceso. Con el perfil `dev`/`local` la documentación OpenAPI está en `/swagger-ui.html`.

| Método y ruta | Acceso | Descripción |
|---|---|---|
| `POST /api/auth/register` | Público | Alta de lector (rol `USER`) |
| `POST /api/auth/login` | Público | Devuelve el par de tokens |
| `POST /api/auth/refresh` | Público | Rota el refresh y devuelve un par nuevo |
| `POST /api/auth/logout` | Autenticado | Revoca el refresh indicado |
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
| `POST /api/usuarios/{id}/revocar-sesiones` | ADMIN o dueño | Invalida los refresh tokens del usuario |
| `DELETE /api/usuarios/{id}` | ADMIN | Borrado (409 si tiene préstamos; no permite borrarse a uno mismo) |

Los errores son `ProblemDetail` (RFC 9457) con una propiedad `error` que repite el mensaje (y `errors` con la lista en validaciones). Códigos: 400 validación, 401 no autenticado o credenciales/refresh inválidos, 403 sin permiso, 404 no existe, 409 conflicto, 422 invariante de dominio incumplida, 429 demasiadas peticiones, 500 genérico. El directorio `postman/` solo contiene variables globales del espacio de trabajo, no una colección de peticiones.

---

## 8. Frontend

Ubicación: `src/main/resources/static`. Spring Boot lo sirve directamente, sin servidor aparte ni paso de compilación.

| Archivo | Responsabilidad |
|---|---|
| `index.html` | HTML semántico y accesible (skip-link, landmarks, tabs ARIA, `aria-live`/`aria-busy`). Carga los CSS y `js/main.js` como módulo. |
| `js/api.js` | **Único** sitio con `fetch`. Guarda el par de tokens en `sessionStorage`, envía el acceso en `Authorization: Bearer` y, ante un 401, intenta **renovar con el refresh una sola vez** antes de dar la sesión por caducada. Expone `ApiError`, un contador de peticiones (evento `api:carga`) y el evento `sesion:caducada`. |
| `js/state.js` | Estado único (usuario, préstamos, libros, vista, carga) con suscripción y derivados memoizados. |
| `js/views/*.js` | Render de estado a HTML por bloque: `header`, `auth`, `shelf`, `catalog`, `history`, `admin`. |
| `js/acciones.js`, `js/events.js` | Orquestación contra la API y delegación de eventos (`data-solicitar`, `data-devolver`, `data-retirar`, `data-tab`). |
| `js/dom.js`, `js/covers.js` | Helpers (`esc` anti-XSS, toast) y generación de cubiertas/lomos y fechas. |
| `js/main.js` | Punto de entrada: suscribe el render y arranca. |
| `css/tokens.css`, `base.css`, `layout.css`, `components/*.css` | Tokens como custom properties, globales, layout y un fichero por bloque BEM. |

**Decisiones de diseño visual**
- **«Mis préstamos» se muestra como una estantería con los libros de lado (lomos)**. Al pasar el cursor, el libro «sale» de la balda y aparece a su lado una tarjeta con autor, género, fecha y el botón *Devolver*. Funciona también con teclado (`:focus-within`).
- El catálogo de disponibles usa tarjetas con la cubierta de frente; el historial de devoluciones se presenta como un ticket.
- Se respeta `prefers-reduced-motion` y el diseño es responsive.
- Sin dependencias externas salvo las fuentes de Google Fonts.

---

## 9. Estrategia de tests y reglas de arquitectura

`./mvnw clean test` ejecuta 532 tests. Los de repositorio contra **MariaDB real** (Testcontainers) se omiten si no hay Docker (28 tests); el resto usa **H2 en modo MariaDB** con Flyway y el perfil `test` (`src/test/resources/application-test.yml`).

> Usa `clean`: si quedan en `target/` clases de compilaciones anteriores (por ejemplo de una estructura de paquetes antigua), la suite puede fallar con errores que no existen en el código actual.

Los tests viven en el mismo paquete que la clase que prueban, capa por capa:

| Tipo | Qué cubre |
|---|---|
| Modelo y *value objects* (`BookTest`, `IsbnTest`, `EmailTest`, `UserTest`, `LoanTest`, `RefreshTokenTest`) | Invariantes del dominio |
| Servicios (`*ServiceImplTest`, `LoanServiceTest`, `LoanQueryServiceTest`) | Reglas de negocio con repositorios simulados |
| Casos de uso (un `*UseCaseTest` por caso de uso) | Autorización por rol / dueño |
| Controladores y *handlers* (`*ControllerTest`, `*ExceptionHandlerTest`, `*WebMapperTest`) | Contrato HTTP, códigos y errores |
| Persistencia (`*RepositoryImplTest`, `*RepositoryImplMariaDbTest`, `*PersistenceMapperTest`) | Mapeo y consultas (H2 y MariaDB) |
| `*ModuleImplTest`, `*WithLoansCheckerTest` | API pública de cada módulo y los vetos SPI |
| `config/**/*Test` | Filtro JWT, proveedor JWT, rate limit, hash, resolver de `Requester`, seeder (en el mismo subpaquete que la clase probada) |
| `SecurityIntegrationTest`, `EndpointAuthorizationIntegrationTest` | Rutas públicas/protegidas y autorización de extremo a extremo |
| `ArchitectureTest`, `ApplicationModulesTest` | Arquitectura (abajo) |
| `DemoApplicationTests` | Arranque del contexto |

`BaseIntegrationTest` arranca el contexto completo con MockMvc; `support/TestAuthentication` y `support/MariaDbContainerTest` ofrecen utilidades.

### Verificación de la arquitectura (dos herramientas complementarias)

**Spring Modulith — `ApplicationModulesTest`.** Comprueba los límites *entre módulos*: ciclos, acceso a `internal` y dependencias fuera de `allowedDependencies`.

**ArchUnit — `ArchitectureTest` (41 reglas).** Comprueba lo que Modulith no cubre: la estructura *dentro* de cada módulo.

| Grupo | Reglas |
|---|---|
| **Módulos y encapsulación** | Sin ciclos entre módulos; `internal` solo lo usa su propio módulo; `dto`, `exception` y `shared` no dependen de `internal`; nadie depende de `config`; cada módulo solo contiene los subpaquetes conocidos. |
| **Capas** | `controller` no usa `service`, `persistence` ni `repository`; `usecase` no usa `controller`, `persistence` ni `repository`, ni otros `UseCase`; cada `UseCase` tiene un único método público, `execute`; `XModule`/`XModuleImpl` no usan `usecase` ni `controller`; `application` no usa web ni persistencia; `domain` no depende de otras capas; `persistence` no depende de web ni de casos de uso. |
| **Independencia de frameworks** | `domain` sin web, HTTP, validación, JPA, Spring Data, Spring Security, servlet ni jjwt; `shared` sin Spring, Jakarta ni Lombok; la API servlet solo en `controller` y `config`. |
| **Ubicación por sufijo** | Servicios en `domain.service`; repositorios (interfaz) en `domain.repository`; `*SpringDataRepository` en `persistence.dao.impl.jpa`; `*Dto` en `dto`; `*Request`/`*Response` en `controller.dto`; `*Entity` en `dao.entity`; `*PersistenceMapper` en `repository.mapper`; `*RepositoryImpl` en `persistence`; `*UseCase` en `usecase`; `@RestController` en `controller`; `*ModuleImpl` en `application`; `@RestControllerAdvice` en `controller.handler`; mappers con MapStruct. |
| **Autorización** | Todo método público de un `UseCase` recibe `Requester` primero (salvo `RegisterUseCase`, `LoginUseCase` y `RefreshTokenUseCase`); `@PreAuthorize`, `@Secured` y `SecurityContextHolder` solo en `config`; los controladores no importan Spring Security; los DTO públicos no exponen modelos de dominio. |
| **Otras** | Sin eventos de aplicación (la comunicación entre módulos es por API pública y SPI); sin `@Autowired`. |

---

## 10. Decisiones, compromisos y limitaciones conocidas

**Decisiones de arquitectura**
- **Modelo de dominio separado de la entidad JPA.** `domain/model` es Java puro e inmutable; `persistence` tiene la `XEntity` y el `XPersistenceMapper` que las convierte. Cuesta más mapeo, pero el dominio no depende de JPA y se prueba sin contenedor.
- **Los módulos se comunican por DTO públicos, no por el modelo.** `XModuleImpl` traduce y no deja pasar excepciones internas (devuelve resultados como `ReservationResult`).
- **`loans` solo guarda ids.** No hay `@ManyToOne` entre módulos; los datos se componen en lote en el servicio. Es la razón de que un módulo pueda extraerse.
- **Autorización en el caso de uso, no en el controlador ni con `@PreAuthorize`.** Es explícita, se prueba como código normal y no ata los módulos a Spring Security.
- **Veto síncrono por SPI** en vez de eventos para el borrado de libros y usuarios con préstamos (consistencia dentro de la misma transacción). Es la única excepción a la comunicación sin eventos.
- **`@Service` y `@Transactional` en `domain`.** Son anotaciones de Spring, pero ligeras; evitan una capa de configuración de beans solo para registrarlos. El límite se mantuvo en lo importante: el dominio no importa tipos de persistencia, web ni seguridad (lo verifica ArchUnit).
- **Todos los módulos comparten una sola base de datos y un solo esquema**, sin esquemas separados por módulo.

**Limitaciones conocidas**
- **Los tokens de acceso no se pueden revocar antes de caducar** (máx. 15 min por defecto): no se consulta la base de datos en cada petición. Revocar sesiones o cambiar la contraseña invalida los refresh, no los accesos ya emitidos, y los roles son los de la emisión del token.
- **Estado en memoria en un solo nodo:** el rate limiter vive en la memoria de la JVM y no se comparte entre instancias ni tras un reinicio. Los refresh tokens sí están en base de datos.
- **El rate limiting usa `request.getRemoteAddr()`.** Detrás de un proxy inverso depende de `forward-headers-strategy: native` (ya configurado) para ver la IP real del cliente.
- **Tokens en `sessionStorage` en el frontend.** Son accesibles desde JavaScript, así que un XSS podría leerlos; se mitiga con la CSP estricta y el escapado (`esc`), y se pierden al cerrar la pestaña.
- **Cambiar la contraseña invalida los refresh del usuario**, que debe volver a iniciar sesión cuando caduque el acceso.
- **Tests de MariaDB solo con Docker:** sin Docker, los 28 tests de repositorio contra MariaDB se omiten y la cobertura de consultas queda en H2.
- **No hay límite de préstamos por usuario ni fecha de vencimiento**: el modelo solo registra préstamo y devolución.
