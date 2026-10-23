package com.pmt;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/**
 * Classe de base des tests d'integration.
 * Demarre un MySQL reel dans Docker et y joue le script Database/schema.sql,
 * afin que les tests valident le mapping JPA contre le schema livre.
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("pmt")
            .withCopyFileToContainer(
                    MountableFile.forHostPath("../Database/schema.sql"),
                    "/docker-entrypoint-initdb.d/schema.sql");
}
