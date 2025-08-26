package com.maxiflexy.library.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;

/**
 * Abstract base class for integration tests.
 * <p>
 * This class sets up a MySQL Testcontainer and configures dynamic properties
 * for Spring Boot integration tests. It ensures that each test runs in a
 * transactional context and uses an isolated database instance.
 */
@SpringBootTest
@Transactional
public abstract class AbstractIntegrationTest {

    /**
     * Static MySQL Testcontainer instance.
     * <p>
     * This container is initialized with a specific database name, username, and password.
     * It is started once when the class is loaded.
     */
    static final MySQLContainer<?> MYSQL_CONTAINER;

    // Static block to initialize and start the MySQL Testcontainer
    static {
        MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        MYSQL_CONTAINER.start();
    }

    /**
     * Configures dynamic properties for the Spring application context.
     * <p>
     * This method registers database connection properties and additional
     * HikariCP settings to ensure proper connection pooling. It also configures
     * Hibernate schema management for the test environment.
     *
     * @param registry the dynamic property registry to configure
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Register database connection properties
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);

        // Configure HikariCP connection pool settings
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "2");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "1");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "3000");
        registry.add("spring.datasource.hikari.validation-timeout", () -> "1000");
        registry.add("spring.datasource.hikari.max-lifetime", () -> "60000");

        // Configure Hibernate schema management and SQL logging
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
    }
}