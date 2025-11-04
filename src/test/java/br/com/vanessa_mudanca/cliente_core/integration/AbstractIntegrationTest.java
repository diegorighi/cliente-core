package br.com.vanessa_mudanca.cliente_core.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Classe base abstrata para testes de integração com TestContainers.
 *
 * Configuração:
 * - Spring Boot Test com servidor web em porta aleatória
 * - PostgreSQL rodando em Docker via TestContainers
 * - Liquibase executa migrations automaticamente
 * - Profile 'test' ativo
 *
 * Padrão Singleton Container:
 * - Container PostgreSQL compartilhado entre todos os testes (performance)
 * - Dados limpos entre testes via @Transactional ou limpeza manual
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    /**
     * Container PostgreSQL compartilhado (singleton pattern).
     * Reutilizado entre testes para melhor performance.
     */
    @Container
    protected static final PostgreSQLContainer<?> postgresContainer =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true); // Reutiliza container entre execuções

    /**
     * Configura propriedades dinâmicas do Spring para conectar ao container.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);

        // Desabilita Liquibase contexts de seeds (apenas DDL)
        registry.add("spring.liquibase.contexts", () -> "ddl-only");

        // JPA deve validar schema (não criar)
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    /**
     * Retorna base URL para chamadas REST.
     */
    protected String getBaseUrl() {
        return "http://localhost:" + port + "/v1";
    }
}
