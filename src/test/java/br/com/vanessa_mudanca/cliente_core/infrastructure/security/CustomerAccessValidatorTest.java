package br.com.vanessa_mudanca.cliente_core.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de segurança para CustomerAccessValidator.
 *
 * Cenários críticos:
 * 1. ADMIN/EMPLOYEE podem acessar qualquer cliente
 * 2. CUSTOMER só pode acessar próprio cadastro
 * 3. CUSTOMER tentando acessar outro cliente → AccessDeniedException
 * 4. Usuário não autenticado → AccessDeniedException
 * 5. JWT com UUID inválido → AccessDeniedException
 */
@DisplayName("CustomerAccessValidator - Testes de Segurança")
class CustomerAccessValidatorTest {

    private CustomerAccessValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CustomerAccessValidator();
    }

    @Nested
    @DisplayName("validateAccess() - Validação de Acesso")
    class ValidateAccessTests {

        @Test
        @DisplayName("ADMIN deve acessar qualquer cliente")
        void adminDeveAcessarQualquerCliente() {
            // Given
            UUID clienteId = UUID.randomUUID();
            Authentication auth = createAuthentication(UUID.randomUUID(), "ADMIN");

            // When/Then - Não deve lançar exceção
            validator.validateAccess(clienteId, auth);
        }

        @Test
        @DisplayName("EMPLOYEE deve acessar qualquer cliente")
        void employeeDeveAcessarQualquerCliente() {
            // Given
            UUID clienteId = UUID.randomUUID();
            Authentication auth = createAuthentication(UUID.randomUUID(), "EMPLOYEE");

            // When/Then - Não deve lançar exceção
            validator.validateAccess(clienteId, auth);
        }

        @Test
        @DisplayName("SERVICE deve acessar qualquer cliente")
        void serviceDeveAcessarQualquerCliente() {
            // Given
            UUID clienteId = UUID.randomUUID();
            Authentication auth = createAuthentication(UUID.randomUUID(), "SERVICE");

            // When/Then - Não deve lançar exceção
            validator.validateAccess(clienteId, auth);
        }

        @Test
        @DisplayName("CUSTOMER deve acessar próprio cadastro")
        void customerDeveAcessarProprioCadastro() {
            // Given
            UUID clienteId = UUID.fromString("12345678-1234-1234-1234-123456789012");
            Authentication auth = createAuthentication(clienteId, "CUSTOMER");

            // When/Then - Não deve lançar exceção
            validator.validateAccess(clienteId, auth);
        }

        @Test
        @DisplayName("CUSTOMER NÃO deve acessar cadastro de outro cliente")
        void customerNaoDeveAcessarOutroCliente() {
            // Given
            UUID clienteIdPropio = UUID.fromString("12345678-1234-1234-1234-123456789012");
            UUID clienteIdAlheio = UUID.fromString("87654321-4321-4321-4321-210987654321");
            Authentication auth = createAuthentication(clienteIdPropio, "CUSTOMER");

            // When/Then
            assertThatThrownBy(() -> validator.validateAccess(clienteIdAlheio, auth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("CUSTOMER só pode acessar seu próprio cadastro")
                .hasMessageContaining(clienteIdAlheio.toString())
                .hasMessageContaining(clienteIdPropio.toString());
        }

        @Test
        @DisplayName("Authentication null deve lançar exceção")
        void authenticationNullDeveLancarExcecao() {
            // Given
            UUID clienteId = UUID.randomUUID();
            Authentication auth = null;

            // When/Then
            assertThatThrownBy(() -> validator.validateAccess(clienteId, auth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Usuário não autenticado");
        }

        @Test
        @DisplayName("Authentication não autenticado deve lançar exceção")
        void authenticationNaoAutenticadoDeveLancarExcecao() {
            // Given
            UUID clienteId = UUID.randomUUID();
            Authentication auth = createUnauthenticatedAuthentication(UUID.randomUUID(), "CUSTOMER");

            // When/Then
            assertThatThrownBy(() -> validator.validateAccess(clienteId, auth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Usuário não autenticado");
        }

        @Test
        @DisplayName("JWT com UUID inválido deve lançar exceção")
        void jwtComUuidInvalidoDeveLancarExcecao() {
            // Given
            UUID clienteId = UUID.randomUUID();
            Authentication auth = createAuthenticationWithInvalidUuid("customer@example.com", "CUSTOMER");

            // When/Then
            assertThatThrownBy(() -> validator.validateAccess(clienteId, auth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Token inválido: 'sub' não é um UUID válido")
                .hasMessageContaining("customer@example.com");
        }

        @Test
        @DisplayName("CUSTOMER com múltiplas roles deve validar acesso próprio")
        void customerComMultiplasRolesDeveValidarAcessoProprio() {
            // Given
            UUID clienteId = UUID.fromString("12345678-1234-1234-1234-123456789012");
            Authentication auth = createAuthentication(clienteId, "CUSTOMER", "READ_ONLY");

            // When/Then - Não deve lançar exceção
            validator.validateAccess(clienteId, auth);
        }

        @Test
        @DisplayName("Usuário sem nenhuma role conhecida deve ser tratado como CUSTOMER")
        void usuarioSemRoleConhecidaDeveSerTratadoComoCustomer() {
            // Given
            UUID clienteIdPropio = UUID.fromString("12345678-1234-1234-1234-123456789012");
            UUID clienteIdAlheio = UUID.fromString("87654321-4321-4321-4321-210987654321");

            // Usuário com role desconhecida (não é CUSTOMER, ADMIN, EMPLOYEE, SERVICE)
            Authentication auth = createAuthentication(clienteIdPropio, "UNKNOWN_ROLE");

            // When/Then - Sem role CUSTOMER, deve permitir acesso (comportamento de ADMIN/EMPLOYEE)
            validator.validateAccess(clienteIdAlheio, auth);
        }
    }

    @Nested
    @DisplayName("isCustomer() - Verificação de Role")
    class IsCustomerTests {

        @Test
        @DisplayName("Deve retornar true quando usuário é CUSTOMER")
        void deveRetornarTrueQuandoUsuarioEhCustomer() {
            // Given
            Authentication auth = createAuthentication(UUID.randomUUID(), "CUSTOMER");

            // When
            boolean result = validator.isCustomer(auth);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando usuário é ADMIN")
        void deveRetornarFalseQuandoUsuarioEhAdmin() {
            // Given
            Authentication auth = createAuthentication(UUID.randomUUID(), "ADMIN");

            // When
            boolean result = validator.isCustomer(auth);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando usuário é EMPLOYEE")
        void deveRetornarFalseQuandoUsuarioEhEmployee() {
            // Given
            Authentication auth = createAuthentication(UUID.randomUUID(), "EMPLOYEE");

            // When
            boolean result = validator.isCustomer(auth);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando Authentication é null")
        void deveRetornarFalseQuandoAuthenticationEhNull() {
            // Given
            Authentication auth = null;

            // When
            boolean result = validator.isCustomer(auth);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Deve retornar true quando CUSTOMER está entre múltiplas roles")
        void deveRetornarTrueQuandoCustomerEstaEntreMultiplasRoles() {
            // Given
            Authentication auth = createAuthentication(UUID.randomUUID(), "READ_ONLY", "CUSTOMER", "WRITE");

            // When
            boolean result = validator.isCustomer(auth);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("getAuthenticatedCustomerId() - Extração de UUID do JWT")
    class GetAuthenticatedCustomerIdTests {

        @Test
        @DisplayName("Deve extrair UUID válido do JWT.sub")
        void deveExtrairUuidValidoDoJwtSub() {
            // Given
            UUID expectedUuid = UUID.fromString("12345678-1234-1234-1234-123456789012");
            Authentication auth = createAuthentication(expectedUuid, "CUSTOMER");

            // When
            UUID result = validator.getAuthenticatedCustomerId(auth);

            // Then
            assertThat(result).isEqualTo(expectedUuid);
        }

        @Test
        @DisplayName("Deve funcionar independente da role")
        void deveFuncionarIndependenteDaRole() {
            // Given
            UUID expectedUuid = UUID.fromString("87654321-4321-4321-4321-210987654321");
            Authentication auth = createAuthentication(expectedUuid, "ADMIN");

            // When
            UUID result = validator.getAuthenticatedCustomerId(auth);

            // Then
            assertThat(result).isEqualTo(expectedUuid);
        }

        @Test
        @DisplayName("Deve lançar exceção quando Authentication é null")
        void deveLancarExcecaoQuandoAuthenticationEhNull() {
            // Given
            Authentication auth = null;

            // When/Then
            assertThatThrownBy(() -> validator.getAuthenticatedCustomerId(auth))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Usuário não autenticado");
        }

        @Test
        @DisplayName("Deve lançar exceção quando JWT.sub não é UUID válido")
        void deveLancarExcecaoQuandoJwtSubNaoEhUuidValido() {
            // Given
            String invalidUuid = "not-a-uuid";
            Authentication auth = createAuthenticationWithInvalidUuid(invalidUuid, "CUSTOMER");

            // When/Then
            assertThatThrownBy(() -> validator.getAuthenticatedCustomerId(auth))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Token inválido: 'sub' não é um UUID")
                .hasMessageContaining(invalidUuid);
        }

        @Test
        @DisplayName("Deve lançar exceção quando JWT.sub é string vazia")
        void deveLancarExcecaoQuandoJwtSubEhStringVazia() {
            // Given
            String emptyString = "";
            Authentication auth = createAuthenticationWithInvalidUuid(emptyString, "CUSTOMER");

            // When/Then
            assertThatThrownBy(() -> validator.getAuthenticatedCustomerId(auth))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Token inválido: 'sub' não é um UUID");
        }

        @Test
        @DisplayName("Deve lançar exceção quando JWT.sub é email ao invés de UUID")
        void deveLancarExcecaoQuandoJwtSubEhEmailAoInvesDeUuid() {
            // Given
            String email = "customer@example.com";
            Authentication auth = createAuthenticationWithInvalidUuid(email, "CUSTOMER");

            // When/Then
            assertThatThrownBy(() -> validator.getAuthenticatedCustomerId(auth))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Token inválido: 'sub' não é um UUID")
                .hasMessageContaining(email);
        }
    }

    // ========== Helper Methods ==========

    /**
     * Cria Authentication mock com UUID válido e roles especificadas.
     */
    private Authentication createAuthentication(UUID userId, String... roles) {
        return new Authentication() {
            @Override
            public String getName() {
                return userId.toString(); // Cognito retorna 'sub' no getName()
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(roles).stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return userId.toString();
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }
        };
    }

    /**
     * Cria Authentication mock NÃO autenticado.
     */
    private Authentication createUnauthenticatedAuthentication(UUID userId, String... roles) {
        return new Authentication() {
            @Override
            public String getName() {
                return userId.toString();
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(roles).stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return userId.toString();
            }

            @Override
            public boolean isAuthenticated() {
                return false; // NÃO autenticado
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }
        };
    }

    /**
     * Cria Authentication mock com getName() retornando string inválida (não-UUID).
     */
    private Authentication createAuthenticationWithInvalidUuid(String invalidSubValue, String... roles) {
        return new Authentication() {
            @Override
            public String getName() {
                return invalidSubValue; // String inválida, não é UUID
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(roles).stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return invalidSubValue;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }
        };
    }
}
