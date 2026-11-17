-- Esquema base (compatible con MariaDB/MySQL y con H2 en modo MySQL/MariaDB).
CREATE TABLE usuarios (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(255),
    email         VARCHAR(255),
    password      VARCHAR(255),
    rol           VARCHAR(255),
    token_version INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);

CREATE TABLE libros (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    titulo     VARCHAR(255),
    autor      VARCHAR(255),
    isbn       VARCHAR(255),
    disponible BOOLEAN      NOT NULL DEFAULT TRUE,
    genero     VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_libros_isbn UNIQUE (isbn)
);

-- Relación por id entre módulos (sin claves foráneas JPA); solo índices de búsqueda.
CREATE TABLE prestamos (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    libro_id         BIGINT NOT NULL,
    usuario_id       BIGINT NOT NULL,
    fecha_prestamo   DATE   NOT NULL,
    fecha_devolucion DATE,
    PRIMARY KEY (id)
);

CREATE INDEX idx_prestamos_libro   ON prestamos (libro_id);
CREATE INDEX idx_prestamos_usuario ON prestamos (usuario_id);
