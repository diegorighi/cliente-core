package br.com.vanessa_mudanca.cliente_core.domain.validator;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateDocumentoDTO;
import br.com.vanessa_mudanca.cliente_core.domain.exception.DataValidadeInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Testes para ValidarDataValidadeStrategy.
 *
 * Cenários críticos:
 * 1. Data de validade > 50 anos no futuro → rejeitar
 * 2. Data de validade < data de emissão → rejeitar
 * 3. Datas válidas → aceitar
 * 4. Data de validade null → aceitar (documentos sem validade)
 */
@DisplayName("ValidarDataValidadeStrategy - Validação de Data de Validade")
class ValidarDataValidadeStrategyTest {

    private ValidarDataValidadeStrategy validator;

    @BeforeEach
    void setUp() {
        validator = new ValidarDataValidadeStrategy();
    }

    @Nested
    @DisplayName("Cenários Válidos - Não deve lançar exceção")
    class CenariosValidos {

        @Test
        @DisplayName("Deve aceitar data de validade null (documentos sem validade como CPF)")
        void deveAceitarDataValidadeNull() {
            // Given
            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,   // id
                    null, // dataEmissao
                    null, // dataValidade - CPF não tem validade
                    null, // orgaoEmissor
                    null  // observacoes
            );

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() -> validator.validar(dto));
        }

        @Test
        @DisplayName("Deve aceitar data de validade válida (dentro de 50 anos)")
        void deveAceitarDataValidadeValida() {
            // Given
            LocalDate hoje = LocalDate.now();
            LocalDate validadeEm10Anos = hoje.plusYears(10);

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    hoje.minusYears(5), // emitida há 5 anos
                    validadeEm10Anos,   // válida por mais 10 anos
                    "DETRAN-SP",
                    null
            );

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() -> validator.validar(dto));
        }

        @Test
        @DisplayName("Deve aceitar data de validade exatamente 50 anos no futuro")
        void deveAceitarDataValidadeExatamente50Anos() {
            // Given
            LocalDate hoje = LocalDate.now();
            LocalDate validadeEm50Anos = hoje.plusYears(50);

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    hoje,
                    validadeEm50Anos, // Exatamente 50 anos
                    "PF",
                    null
            );

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() -> validator.validar(dto));
        }

        @Test
        @DisplayName("Deve aceitar data de validade no passado (documento expirado)")
        void deveAceitarDataValidadeNoPassado() {
            // Given
            LocalDate hoje = LocalDate.now();
            LocalDate validadePassada = hoje.minusYears(2);

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    hoje.minusYears(10),
                    validadePassada, // Expirada há 2 anos (ok, status será calculado pela entidade)
                    "DETRAN-RJ",
                    null
            );

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() -> validator.validar(dto));
        }

        @Test
        @DisplayName("Deve aceitar quando dataEmissao é null")
        void deveAceitarQuandoDataEmissaoEhNull() {
            // Given
            LocalDate validadeFutura = LocalDate.now().plusYears(5);

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    null, // dataEmissao null
                    validadeFutura,
                    "SSP-MG",
                    null
            );

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() -> validator.validar(dto));
        }

        @Test
        @DisplayName("Deve aceitar quando dataValidade == dataEmissao (emitido e vence no mesmo dia)")
        void deveAceitarQuandoDataValidadeIgualDataEmissao() {
            // Given
            LocalDate mesmaData = LocalDate.now();

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    mesmaData,
                    mesmaData, // Válida até o mesmo dia (caso raro mas possível)
                    "Cartório",
                    null
            );

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() -> validator.validar(dto));
        }

        @Test
        @DisplayName("Deve aceitar quando dataValidade é 1 dia após dataEmissao")
        void deveAceitarQuandoDataValidadeUmDiaAposEmissao() {
            // Given
            LocalDate emissao = LocalDate.now();
            LocalDate validade = emissao.plusDays(1);

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    emissao,
                    validade,
                    "Órgão",
                    null
            );

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() -> validator.validar(dto));
        }
    }

    @Nested
    @DisplayName("Cenários Inválidos - Deve lançar exceção")
    class CenariosInvalidos {

        @Test
        @DisplayName("Deve rejeitar data de validade > 50 anos no futuro")
        void deveRejeitarDataValidadeMuitoDistante() {
            // Given
            LocalDate hoje = LocalDate.now();
            LocalDate validadeMuitoDistante = hoje.plusYears(51); // 51 anos > limite de 50

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    hoje,
                    validadeMuitoDistante,
                    "DETRAN-SP",
                    null
            );

            // When/Then
            assertThatThrownBy(() -> validator.validar(dto))
                    .isInstanceOf(DataValidadeInvalidaException.class)
                    .hasMessageContaining("Data de validade não pode ser superior a 50 anos")
                    .hasMessageContaining(validadeMuitoDistante.toString());
        }

        @Test
        @DisplayName("Deve rejeitar data de validade 100 anos no futuro")
        void deveRejeitarDataValidade100Anos() {
            // Given
            LocalDate hoje = LocalDate.now();
            LocalDate validadeAbsurda = hoje.plusYears(100);

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    hoje,
                    validadeAbsurda,
                    "PF",
                    null
            );

            // When/Then
            assertThatThrownBy(() -> validator.validar(dto))
                    .isInstanceOf(DataValidadeInvalidaException.class)
                    .hasMessageContaining("Data de validade não pode ser superior a 50 anos");
        }

        @Test
        @DisplayName("Deve rejeitar data de validade anterior à data de emissão")
        void deveRejeitarDataValidadeAnteriorEmissao() {
            // Given
            LocalDate emissao = LocalDate.of(2020, 1, 15);
            LocalDate validadeAnterior = LocalDate.of(2019, 12, 31); // Antes da emissão

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    emissao,
                    validadeAnterior, // Validade < emissão (inválido)
                    "SSP-SP",
                    null
            );

            // When/Then
            assertThatThrownBy(() -> validator.validar(dto))
                    .isInstanceOf(DataValidadeInvalidaException.class)
                    .hasMessageContaining("Data de validade")
                    .hasMessageContaining("não pode ser anterior à data de emissão")
                    .hasMessageContaining(validadeAnterior.toString())
                    .hasMessageContaining(emissao.toString());
        }

        @Test
        @DisplayName("Deve rejeitar quando validade é 1 dia antes da emissão")
        void deveRejeitarQuandoValidadeUmDiaAntesEmissao() {
            // Given
            LocalDate emissao = LocalDate.now();
            LocalDate validade = emissao.minusDays(1);

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    emissao,
                    validade,
                    "DETRAN-RJ",
                    null
            );

            // When/Then
            assertThatThrownBy(() -> validator.validar(dto))
                    .isInstanceOf(DataValidadeInvalidaException.class)
                    .hasMessageContaining("não pode ser anterior à data de emissão");
        }

        @Test
        @DisplayName("Deve rejeitar quando validade é 1 ano antes da emissão")
        void deveRejeitarQuandoValidadeUmAnoAntesEmissao() {
            // Given
            LocalDate emissao = LocalDate.of(2023, 6, 15);
            LocalDate validade = LocalDate.of(2022, 6, 15); // 1 ano antes

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    emissao,
                    validade,
                    "Cartório",
                    null
            );

            // When/Then
            assertThatThrownBy(() -> validator.validar(dto))
                    .isInstanceOf(DataValidadeInvalidaException.class)
                    .hasMessageContaining("não pode ser anterior à data de emissão");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Deve rejeitar dataValidade exatamente 50 anos + 1 dia")
        void deveRejeitarDataValidade50AnosMaisUmDia() {
            // Given
            LocalDate hoje = LocalDate.now();
            LocalDate validadeAlemLimite = hoje.plusYears(50).plusDays(1);

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    hoje,
                    validadeAlemLimite,
                    "Órgão",
                    null
            );

            // When/Then
            assertThatThrownBy(() -> validator.validar(dto))
                    .isInstanceOf(DataValidadeInvalidaException.class)
                    .hasMessageContaining("Data de validade não pode ser superior a 50 anos");
        }

        @Test
        @DisplayName("Deve aceitar ambas as datas null")
        void deveAceitarAmbasAsDatasNull() {
            // Given
            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    null, // dataEmissao null
                    null, // dataValidade null
                    null,
                    null
            );

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() -> validator.validar(dto));
        }

        @Test
        @DisplayName("Deve validar corretamente com datas no limite de 50 anos menos 1 dia")
        void deveValidarCorretamenteComDataNoLimite() {
            // Given
            LocalDate hoje = LocalDate.now();
            LocalDate validadeLimite = hoje.plusYears(50).minusDays(1); // Dentro do limite

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    hoje,
                    validadeLimite,
                    "PF",
                    null
            );

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() -> validator.validar(dto));
        }

        @Test
        @DisplayName("Deve validar documento emitido há 10 anos e válido por mais 5 anos")
        void deveValidarDocumentoAntigo() {
            // Given
            LocalDate emissaoAntiga = LocalDate.now().minusYears(10);
            LocalDate validadeFutura = LocalDate.now().plusYears(5);

            UpdateDocumentoDTO dto = new UpdateDocumentoDTO(
                    1L,
                    emissaoAntiga,
                    validadeFutura,
                    "SSP-PR",
                    null
            );

            // When/Then - Não deve lançar exceção
            assertDoesNotThrow(() -> validator.validar(dto));
        }
    }
}
