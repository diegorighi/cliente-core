package br.com.vanessa_mudanca.cliente_core.domain.validator;

import br.com.vanessa_mudanca.cliente_core.application.ports.output.EnderecoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoEnderecoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.exception.EnderecoPrincipalDuplicadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * Testes para ValidarEnderecoPrincipalUnicoStrategy.
 *
 * Cenários críticos:
 * 1. Já existe endereço principal do mesmo tipo → rejeitar
 * 2. Não existe outro endereço principal do mesmo tipo → aceitar
 * 3. Desmarcando como principal → aceitar (sem validação)
 * 4. Marcando como principal mas o próprio endereço já é principal → aceitar
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ValidarEnderecoPrincipalUnicoStrategy - Validação de Endereço Principal Único")
class ValidarEnderecoPrincipalUnicoStrategyTest {

    @Mock
    private EnderecoRepositoryPort enderecoRepository;

    private ValidarEnderecoPrincipalUnicoStrategy validator;

    @BeforeEach
    void setUp() {
        validator = new ValidarEnderecoPrincipalUnicoStrategy(enderecoRepository);
    }

    @Nested
    @DisplayName("Cenários Válidos - Não deve lançar exceção")
    class CenariosValidos {

        @Test
        @DisplayName("Deve aceitar quando não está marcando como principal (false)")
        void deveAceitarQuandoNaoMarcandoComoPrincipal() {
            // Given
            Long clienteId = 1L;
            Long enderecoId = 10L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.RESIDENCIAL;
            Boolean marcandoComoPrincipal = false;

            // When/Then - Não deve chamar repository nem lançar exceção
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, enderecoId, tipo, marcandoComoPrincipal)
            );

            verifyNoInteractions(enderecoRepository);
        }

        @Test
        @DisplayName("Deve aceitar quando marcandoComoPrincipal é null")
        void deveAceitarQuandoMarcandoComoPrincipalEhNull() {
            // Given
            Long clienteId = 1L;
            Long enderecoId = 10L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.COMERCIAL;
            Boolean marcandoComoPrincipal = null;

            // When/Then - Não deve chamar repository nem lançar exceção
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, enderecoId, tipo, marcandoComoPrincipal)
            );

            verifyNoInteractions(enderecoRepository);
        }

        @Test
        @DisplayName("Deve aceitar quando não existe outro endereço principal do mesmo tipo")
        void deveAceitarQuandoNaoExisteOutroPrincipal() {
            // Given
            Long clienteId = 1L;
            Long enderecoId = 10L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.RESIDENCIAL;
            Boolean marcandoComoPrincipal = true;

            when(enderecoRepository.existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                    clienteId, tipo, true, enderecoId
            )).thenReturn(false); // Não existe outro principal

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, enderecoId, tipo, marcandoComoPrincipal)
            );

            verify(enderecoRepository, times(1))
                    .existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                            clienteId, tipo, true, enderecoId
                    );
        }

        @Test
        @DisplayName("Deve aceitar quando o próprio endereço já é principal (atualizando outros campos)")
        void deveAceitarQuandoProprioEnderecoJaEhPrincipal() {
            // Given
            Long clienteId = 1L;
            Long enderecoId = 10L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.ENTREGA;
            Boolean marcandoComoPrincipal = true;

            // Repository retorna false porque o ID está excluído (IdNot)
            when(enderecoRepository.existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                    clienteId, tipo, true, enderecoId
            )).thenReturn(false);

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, enderecoId, tipo, marcandoComoPrincipal)
            );
        }

        @Test
        @DisplayName("Deve aceitar marcar como principal COMERCIAL quando já existe RESIDENCIAL principal")
        void deveAceitarPrincipalDeTiposDiferentes() {
            // Given
            Long clienteId = 1L;
            Long enderecoId = 20L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.COMERCIAL;
            Boolean marcandoComoPrincipal = true;

            // Existe endereço RESIDENCIAL principal, mas é tipo diferente (ok)
            when(enderecoRepository.existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                    clienteId, tipo, true, enderecoId
            )).thenReturn(false);

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, enderecoId, tipo, marcandoComoPrincipal)
            );
        }

        @Test
        @DisplayName("Deve aceitar primeiro endereço principal de um tipo")
        void deveAceitarPrimeiroEnderecoPrincipalDoTipo() {
            // Given
            Long clienteId = 2L;
            Long enderecoId = 30L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.COBRANCA;
            Boolean marcandoComoPrincipal = true;

            when(enderecoRepository.existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                    clienteId, tipo, true, enderecoId
            )).thenReturn(false); // Primeiro principal desse tipo

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, enderecoId, tipo, marcandoComoPrincipal)
            );
        }
    }

    @Nested
    @DisplayName("Cenários Inválidos - Deve lançar exceção")
    class CenariosInvalidos {

        @Test
        @DisplayName("Deve rejeitar quando já existe outro endereço principal do mesmo tipo")
        void deveRejeitarQuandoJaExisteOutroPrincipal() {
            // Given
            Long clienteId = 1L;
            Long enderecoId = 10L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.RESIDENCIAL;
            Boolean marcandoComoPrincipal = true;

            when(enderecoRepository.existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                    clienteId, tipo, true, enderecoId
            )).thenReturn(true); // JÁ EXISTE outro principal

            // When/Then
            assertThatThrownBy(() ->
                    validator.validar(clienteId, enderecoId, tipo, marcandoComoPrincipal)
            )
                    .isInstanceOf(EnderecoPrincipalDuplicadoException.class)
                    .hasMessageContaining(tipo.name());

            verify(enderecoRepository, times(1))
                    .existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                            clienteId, tipo, true, enderecoId
                    );
        }

        @Test
        @DisplayName("Deve rejeitar quando tentando marcar segundo endereço COMERCIAL como principal")
        void deveRejeitarSegundoComercialPrincipal() {
            // Given
            Long clienteId = 5L;
            Long enderecoId = 50L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.COMERCIAL;
            Boolean marcandoComoPrincipal = true;

            when(enderecoRepository.existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                    clienteId, tipo, true, enderecoId
            )).thenReturn(true); // Já existe outro COMERCIAL principal

            // When/Then
            assertThatThrownBy(() ->
                    validator.validar(clienteId, enderecoId, tipo, marcandoComoPrincipal)
            )
                    .isInstanceOf(EnderecoPrincipalDuplicadoException.class);
        }

        @Test
        @DisplayName("Deve rejeitar quando tentando marcar segundo endereço ENTREGA como principal")
        void deveRejeitarSegundoEntregaPrincipal() {
            // Given
            Long clienteId = 3L;
            Long enderecoId = 40L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.ENTREGA;
            Boolean marcandoComoPrincipal = true;

            when(enderecoRepository.existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                    clienteId, tipo, true, enderecoId
            )).thenReturn(true);

            // When/Then
            assertThatThrownBy(() ->
                    validator.validar(clienteId, enderecoId, tipo, marcandoComoPrincipal)
            )
                    .isInstanceOf(EnderecoPrincipalDuplicadoException.class)
                    .hasMessageContaining(TipoEnderecoEnum.ENTREGA.name());
        }

        @Test
        @DisplayName("Deve rejeitar quando tentando marcar segundo endereço COBRANCA como principal")
        void deveRejeitarSegundoCobrancaPrincipal() {
            // Given
            Long clienteId = 7L;
            Long enderecoId = 70L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.COBRANCA;
            Boolean marcandoComoPrincipal = true;

            when(enderecoRepository.existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                    clienteId, tipo, true, enderecoId
            )).thenReturn(true);

            // When/Then
            assertThatThrownBy(() ->
                    validator.validar(clienteId, enderecoId, tipo, marcandoComoPrincipal)
            )
                    .isInstanceOf(EnderecoPrincipalDuplicadoException.class);
        }

        @Test
        @DisplayName("Deve rejeitar quando tentando marcar segundo endereço COLETA como principal")
        void deveRejeitarSegundoColetaPrincipal() {
            // Given
            Long clienteId = 9L;
            Long enderecoId = 90L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.COLETA;
            Boolean marcandoComoPrincipal = true;

            when(enderecoRepository.existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                    clienteId, tipo, true, enderecoId
            )).thenReturn(true);

            // When/Then
            assertThatThrownBy(() ->
                    validator.validar(clienteId, enderecoId, tipo, marcandoComoPrincipal)
            )
                    .isInstanceOf(EnderecoPrincipalDuplicadoException.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Deve validar corretamente para diferentes clientes")
        void deveValidarCorretamenteParaDiferentesClientes() {
            // Given
            Long clienteId1 = 1L;
            Long clienteId2 = 2L;
            Long enderecoId = 10L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.RESIDENCIAL;
            Boolean marcandoComoPrincipal = true;

            // Cliente 1: Não tem outro principal
            when(enderecoRepository.existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                    clienteId1, tipo, true, enderecoId
            )).thenReturn(false);

            // Cliente 2: Já tem outro principal
            when(enderecoRepository.existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                    clienteId2, tipo, true, enderecoId
            )).thenReturn(true);

            // When/Then - Cliente 1 deve aceitar
            assertDoesNotThrow(() ->
                    validator.validar(clienteId1, enderecoId, tipo, marcandoComoPrincipal)
            );

            // Cliente 2 deve rejeitar
            assertThatThrownBy(() ->
                    validator.validar(clienteId2, enderecoId, tipo, marcandoComoPrincipal)
            ).isInstanceOf(EnderecoPrincipalDuplicadoException.class);
        }

        @Test
        @DisplayName("Deve aceitar Boolean.TRUE explícito")
        void deveAceitarBooleanTrueExplicito() {
            // Given
            Long clienteId = 1L;
            Long enderecoId = 10L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.RESIDENCIAL;
            Boolean marcandoComoPrincipal = Boolean.TRUE;

            when(enderecoRepository.existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                    clienteId, tipo, true, enderecoId
            )).thenReturn(false);

            // When/Then - Deve validar corretamente
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, enderecoId, tipo, marcandoComoPrincipal)
            );

            verify(enderecoRepository, times(1))
                    .existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                            clienteId, tipo, true, enderecoId
                    );
        }

        @Test
        @DisplayName("Deve aceitar Boolean.FALSE explícito sem validar")
        void deveAceitarBooleanFalseExplicitoSemValidar() {
            // Given
            Long clienteId = 1L;
            Long enderecoId = 10L;
            TipoEnderecoEnum tipo = TipoEnderecoEnum.COMERCIAL;
            Boolean marcandoComoPrincipal = Boolean.FALSE;

            // When/Then - Não deve chamar repository
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, enderecoId, tipo, marcandoComoPrincipal)
            );

            verifyNoInteractions(enderecoRepository);
        }
    }
}
