package com.portfolio.erp.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.github.dockerjava.api.model.ContainerNetwork;

/**
 * Base class for integration tests. Migrations run against a real, isolated
 * PostgreSQL 16 instance managed by Testcontainers (no H2).
 *
 * <p>The container is a JVM-wide singleton so every Spring context (which may
 * be cached across test classes) points to the same database. Testcontainers'
 * shutdown hook stops it at the end of the test run.</p>
 *
 * <p>When the test JVM itself runs inside a container (all-Docker workflow),
 * {@code TESTCONTAINERS_NETWORK} makes test containers join that Docker
 * network and the datasource uses the container IP directly, which avoids
 * relying on published ports (rootless Podman friendly).</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    private static final String CONTAINER_NETWORK = System.getenv("TESTCONTAINERS_NETWORK");

    private static final PostgreSQLContainer POSTGRES = createPostgres();

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", AbstractIntegrationTest::jdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    private static PostgreSQLContainer createPostgres() {
        PostgreSQLContainer container = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));
        if (isSharedNetwork()) {
            container.withNetworkMode(CONTAINER_NETWORK);
        }
        return container;
    }

    private static String jdbcUrl() {
        if (isSharedNetwork()) {
            ContainerNetwork network = POSTGRES.getContainerInfo().getNetworkSettings()
                    .getNetworks().get(CONTAINER_NETWORK);
            if (network == null || network.getIpAddress() == null || network.getIpAddress().isBlank()) {
                throw new IllegalStateException("Test container is not attached to network " + CONTAINER_NETWORK);
            }
            return "jdbc:postgresql://" + network.getIpAddress() + ":5432/" + POSTGRES.getDatabaseName();
        }
        return POSTGRES.getJdbcUrl();
    }

    private static boolean isSharedNetwork() {
        return CONTAINER_NETWORK != null && !CONTAINER_NETWORK.isBlank();
    }
}
