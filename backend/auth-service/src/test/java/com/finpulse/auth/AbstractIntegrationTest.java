package com.finpulse.auth;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * Container PostgreSQL compartilhado entre todos os testes que herdam essa classe.
     * Sobe UMA VEZ, é reutilizado, e Spring conecta nele.
     */
    static final PostgreSQLContainer<?> postgres = 
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("finpulse_auth_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    static {
        // Sobe o container ANTES da JVM rodar os testes
        // (mais eficiente que @BeforeAll porque é uma única vez por execução do Maven)
        postgres.start();
    }

    /**
     * Injeta as propriedades do container no Spring Environment.
     * Substitui ${spring.datasource.url}, etc., com valores reais do container.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}