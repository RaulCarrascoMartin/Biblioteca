package com.example.demo.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

/**
 * Base de los tests de repositorio contra MariaDB real. El esquema lo crea Flyway (no Hibernate) y se
 * valida contra las entidades. Se omite si no hay Docker. Uso:
 * {@code @DataJpaTest @AutoConfigureTestDatabase(replace = NONE) @Import(...) class XTest extends MariaDbContainerTest}.
 */
@Testcontainers(disabledWithoutDocker = true)
public abstract class MariaDbContainerTest {

    @Container
    @ServiceConnection
    protected static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.4");
}
