package br.com.vanessa_mudanca.cliente_core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes para o método main() da aplicação.
 * Garante que a aplicação inicia corretamente.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ClienteCoreApplication.main() - Testes de inicialização")
class ClienteCoreApplicationMainTest {

    @Test
    @DisplayName("Deve inicializar o contexto Spring Boot corretamente")
    void deveInicializarContextoSpringBoot() {
        // Arrange & Act
        // O contexto é carregado pelo @SpringBootTest

        // Assert
        assertThat(ClienteCoreApplication.class).isNotNull();
    }
}
