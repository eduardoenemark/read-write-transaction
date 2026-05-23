package br.com.eduardoenemark.rwt.app.server.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@Testcontainers
public class BaseTestConfiguration {

    @Container
    public static final PostgreSQLContainer<?> POSTGRES_DATABASE = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("123456")
            .withInitScript("ddl-init.sql");

    @DynamicPropertySource
    public static void datasourceProperties(DynamicPropertyRegistry registry) {
        log.info("Setting datasource properties");
        registry.add("datasource.url", POSTGRES_DATABASE::getJdbcUrl);
        registry.add("datasource.username", POSTGRES_DATABASE::getUsername);
        registry.add("datasource.password", POSTGRES_DATABASE::getPassword);
    }

    @PostConstruct
    public void init() {
        log.info("Starting Postgres test container...");
        POSTGRES_DATABASE.start();
    }

    @PreDestroy
    public void destroy() {
        log.info("Stopping Postgres test container...");
        POSTGRES_DATABASE.stop();
    }
}
