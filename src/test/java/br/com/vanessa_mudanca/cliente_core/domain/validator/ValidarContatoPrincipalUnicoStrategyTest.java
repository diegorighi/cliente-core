package br.com.vanessa_mudanca.cliente_core.domain.validator;

import br.com.vanessa_mudanca.cliente_core.application.ports.output.ContatoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ContatoPrincipalDuplicadoException;
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
 * Testes para ValidarContatoPrincipalUnicoStrategy.
 *
 * Cenários críticos:
 * 1. Já existe contato principal → rejeitar
 * 2. Não existe outro contato principal → aceitar
 * 3. Desmarcando como principal → aceitar (sem validação)
 * 4. Marcando como principal mas o próprio contato já é principal → aceitar
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ValidarContatoPrincipalUnicoStrategy - Validação de Contato Principal Único")
class ValidarContatoPrincipalUnicoStrategyTest {

    @Mock
    private ContatoRepositoryPort contatoRepository;

    private ValidarContatoPrincipalUnicoStrategy validator;

    @BeforeEach
    void setUp() {
        validator = new ValidarContatoPrincipalUnicoStrategy(contatoRepository);
    }

    @Nested
    @DisplayName("Cenários Válidos - Não deve lançar exceção")
    class CenariosValidos {

        @Test
        @DisplayName("Deve aceitar quando não está marcando como principal (false)")
        void deveAceitarQuandoNaoMarcandoComoPrincipal() {
            // Given
            Long clienteId = 1L;
            Long contatoId = 10L;
            Boolean marcandoComoPrincipal = false;

            // When/Then - Não deve chamar repository nem lançar exceção
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            );

            verifyNoInteractions(contatoRepository);
        }

        @Test
        @DisplayName("Deve aceitar quando marcandoComoPrincipal é null")
        void deveAceitarQuandoMarcandoComoPrincipalEhNull() {
            // Given
            Long clienteId = 1L;
            Long contatoId = 10L;
            Boolean marcandoComoPrincipal = null;

            // When/Then - Não deve chamar repository nem lançar exceção
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            );

            verifyNoInteractions(contatoRepository);
        }

        @Test
        @DisplayName("Deve aceitar quando não existe outro contato principal")
        void deveAceitarQuandoNaoExisteOutroPrincipal() {
            // Given
            Long clienteId = 1L;
            Long contatoId = 10L;
            Boolean marcandoComoPrincipal = true;

            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId, true, contatoId
            )).thenReturn(false); // Não existe outro principal

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            );

            verify(contatoRepository, times(1))
                    .existsByClienteIdAndContatoPrincipalAndIdNot(clienteId, true, contatoId);
        }

        @Test
        @DisplayName("Deve aceitar quando o próprio contato já é principal (atualizando outros campos)")
        void deveAceitarQuandoProprioContatoJaEhPrincipal() {
            // Given
            Long clienteId = 1L;
            Long contatoId = 10L;
            Boolean marcandoComoPrincipal = true;

            // Repository retorna false porque o ID está excluído (IdNot)
            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId, true, contatoId
            )).thenReturn(false);

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            );

            verify(contatoRepository, times(1))
                    .existsByClienteIdAndContatoPrincipalAndIdNot(clienteId, true, contatoId);
        }

        @Test
        @DisplayName("Deve aceitar primeiro contato principal do cliente")
        void deveAceitarPrimeiroContatoPrincipal() {
            // Given
            Long clienteId = 2L;
            Long contatoId = 20L;
            Boolean marcandoComoPrincipal = true;

            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId, true, contatoId
            )).thenReturn(false); // Primeiro principal

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            );
        }

        @Test
        @DisplayName("Deve aceitar quando cliente tem múltiplos contatos mas nenhum é principal")
        void deveAceitarQuandoTemMultiplosContatosMasNenhumPrincipal() {
            // Given
            Long clienteId = 5L;
            Long contatoId = 50L;
            Boolean marcandoComoPrincipal = true;

            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId, true, contatoId
            )).thenReturn(false); // Nenhum outro é principal

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            );
        }

        @Test
        @DisplayName("Deve aceitar desmarcando contato como principal")
        void deveAceitarDesmarcandoComoPrincipal() {
            // Given
            Long clienteId = 3L;
            Long contatoId = 30L;
            Boolean marcandoComoPrincipal = false;

            // When/Then - Não deve chamar repository (short-circuit)
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            );

            verifyNoInteractions(contatoRepository);
        }
    }

    @Nested
    @DisplayName("Cenários Inválidos - Deve lançar exceção")
    class CenariosInvalidos {

        @Test
        @DisplayName("Deve rejeitar quando já existe outro contato principal")
        void deveRejeitarQuandoJaExisteOutroPrincipal() {
            // Given
            Long clienteId = 1L;
            Long contatoId = 10L;
            Boolean marcandoComoPrincipal = true;

            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId, true, contatoId
            )).thenReturn(true); // JÁ EXISTE outro principal

            // When/Then
            assertThatThrownBy(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            )
                    .isInstanceOf(ContatoPrincipalDuplicadoException.class)
                    .hasMessageContaining("Já existe um contato principal");

            verify(contatoRepository, times(1))
                    .existsByClienteIdAndContatoPrincipalAndIdNot(clienteId, true, contatoId);
        }

        @Test
        @DisplayName("Deve rejeitar quando tentando marcar segundo contato como principal")
        void deveRejeitarSegundoContatoPrincipal() {
            // Given
            Long clienteId = 5L;
            Long contatoId = 50L;
            Boolean marcandoComoPrincipal = true;

            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId, true, contatoId
            )).thenReturn(true); // Já existe outro principal

            // When/Then
            assertThatThrownBy(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            )
                    .isInstanceOf(ContatoPrincipalDuplicadoException.class);
        }

        @Test
        @DisplayName("Deve rejeitar quando cliente já tem um contato principal e tenta criar outro")
        void deveRejeitarCriarSegundoPrincipal() {
            // Given
            Long clienteId = 7L;
            Long novoContatoId = 99L; // Novo contato sendo criado
            Boolean marcandoComoPrincipal = true;

            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId, true, novoContatoId
            )).thenReturn(true); // Já existe um principal

            // When/Then
            assertThatThrownBy(() ->
                    validator.validar(clienteId, novoContatoId, marcandoComoPrincipal)
            )
                    .isInstanceOf(ContatoPrincipalDuplicadoException.class);
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
            Long contatoId = 10L;
            Boolean marcandoComoPrincipal = true;

            // Cliente 1: Não tem outro principal
            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId1, true, contatoId
            )).thenReturn(false);

            // Cliente 2: Já tem outro principal
            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId2, true, contatoId
            )).thenReturn(true);

            // When/Then - Cliente 1 deve aceitar
            assertDoesNotThrow(() ->
                    validator.validar(clienteId1, contatoId, marcandoComoPrincipal)
            );

            // Cliente 2 deve rejeitar
            assertThatThrownBy(() ->
                    validator.validar(clienteId2, contatoId, marcandoComoPrincipal)
            ).isInstanceOf(ContatoPrincipalDuplicadoException.class);
        }

        @Test
        @DisplayName("Deve aceitar Boolean.TRUE explícito")
        void deveAceitarBooleanTrueExplicito() {
            // Given
            Long clienteId = 1L;
            Long contatoId = 10L;
            Boolean marcandoComoPrincipal = Boolean.TRUE;

            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId, true, contatoId
            )).thenReturn(false);

            // When/Then - Deve validar corretamente
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            );

            verify(contatoRepository, times(1))
                    .existsByClienteIdAndContatoPrincipalAndIdNot(clienteId, true, contatoId);
        }

        @Test
        @DisplayName("Deve aceitar Boolean.FALSE explícito sem validar")
        void deveAceitarBooleanFalseExplicitoSemValidar() {
            // Given
            Long clienteId = 1L;
            Long contatoId = 10L;
            Boolean marcandoComoPrincipal = Boolean.FALSE;

            // When/Then - Não deve chamar repository
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            );

            verifyNoInteractions(contatoRepository);
        }

        @Test
        @DisplayName("Deve validar corretamente ao atualizar contato existente que já é principal")
        void deveValidarAoAtualizarContatoJaPrincipal() {
            // Given
            Long clienteId = 10L;
            Long contatoIdPrincipal = 100L; // Contato que JÁ É principal
            Boolean mantendoComoPrincipal = true;

            // Repository retorna false porque IdNot exclui o próprio contato
            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId, true, contatoIdPrincipal
            )).thenReturn(false); // Não existe OUTRO principal

            // When/Then - Deve aceitar (mantendo status principal)
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, contatoIdPrincipal, mantendoComoPrincipal)
            );
        }

        @Test
        @DisplayName("Deve validar com IDs negativos (edge case)")
        void deveValidarComIdsNegativos() {
            // Given
            Long clienteId = -1L;
            Long contatoId = -10L;
            Boolean marcandoComoPrincipal = true;

            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId, true, contatoId
            )).thenReturn(false);

            // When/Then - Deve funcionar normalmente (IDs são apenas Long)
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            );
        }

        @Test
        @DisplayName("Deve validar com IDs muito grandes (Long.MAX_VALUE)")
        void deveValidarComIdsGrandes() {
            // Given
            Long clienteId = Long.MAX_VALUE;
            Long contatoId = Long.MAX_VALUE - 1;
            Boolean marcandoComoPrincipal = true;

            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId, true, contatoId
            )).thenReturn(false);

            // When/Then - Deve funcionar normalmente
            assertDoesNotThrow(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            );
        }

        @Test
        @DisplayName("Deve rejeitar mesmo com IDs iguais se já existe outro principal")
        void deveRejeitarMesmoComIdsIguaisSeExisteOutroPrincipal() {
            // Given
            Long clienteId = 5L;
            Long contatoId = 5L; // Mesmo ID (edge case raro)
            Boolean marcandoComoPrincipal = true;

            when(contatoRepository.existsByClienteIdAndContatoPrincipalAndIdNot(
                    clienteId, true, contatoId
            )).thenReturn(true); // Existe outro principal

            // When/Then
            assertThatThrownBy(() ->
                    validator.validar(clienteId, contatoId, marcandoComoPrincipal)
            ).isInstanceOf(ContatoPrincipalDuplicadoException.class);
        }
    }
}
