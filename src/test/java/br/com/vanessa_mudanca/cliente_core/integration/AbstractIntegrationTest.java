package br.com.vanessa_mudanca.cliente_core.integration;

import br.com.vanessa_mudanca.cliente_core.infrastructure.security.TestSecurityConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Classe base abstrata para testes de integração end-to-end com TestContainers.
 *
 * Configuração:
 * - Spring Boot Test com servidor web em porta aleatória
 * - PostgreSQL rodando em Docker via TestContainers (ambiente realista)
 * - Liquibase executa migrations automaticamente
 * - Profile 'integration' ativo (diferente de 'test' que usa H2)
 *
 * Padrão Singleton Container:
 * - Container PostgreSQL compartilhado entre todos os testes (performance)
 * - Dados limpos entre testes via @Transactional ou limpeza manual
 *
 * IMPORTANTE: Testes unitários usam profile 'test' com H2.
 *            Apenas testes E2E estendem esta classe.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Import(TestSecurityConfig.class)
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

        // Liquibase configuration
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db-changelog-master.xml");
        // Context "integration" não existe em nenhum changeset
        // Resultado: roda apenas changesets SEM context (DDL) e ignora os COM context (seeds)
        registry.add("spring.liquibase.contexts", () -> "integration");

        // JPA deve validar schema (não criar)
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    /**
     * Retorna base URL para chamadas REST.
     * Inclui context-path do servidor (/api/clientes) configurado em application.yml.
     */
    protected String getBaseUrl() {
        return "http://localhost:" + port + "/api/clientes/v1";
    }
}
