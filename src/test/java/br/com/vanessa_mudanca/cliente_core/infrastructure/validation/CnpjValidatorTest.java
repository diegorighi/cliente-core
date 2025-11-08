package br.com.vanessa_mudanca.cliente_core.infrastructure.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CnpjValidator - Validação de CNPJ")
class CnpjValidatorTest {

    private CnpjValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CnpjValidator();
    }

    @Test
    @DisplayName("Deve aceitar CNPJ válido sem formatação")
    void deveAceitarCnpjValidoSemFormatacao() {
        assertThat(validator.isValid("11222333000181", null)).isTrue();
        assertThat(validator.isValid("34028316000103", null)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar CNPJ válido com formatação")
    void deveAceitarCnpjValidoComFormatacao() {
        assertThat(validator.isValid("11.222.333/0001-81", null)).isTrue();
        assertThat(validator.isValid("34.028.316/0001-03", null)).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ null")
    void deveRejeitarCnpjNull() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ em branco")
    void deveRejeitarCnpjEmBranco() {
        assertThat(validator.isValid("", null)).isFalse();
        assertThat(validator.isValid("   ", null)).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ com tamanho incorreto")
    void deveRejeitarCnpjComTamanhoIncorreto() {
        assertThat(validator.isValid("1122233300018", null)).isFalse();   // 13 dígitos
        assertThat(validator.isValid("112223330001811", null)).isFalse(); // 15 dígitos
        assertThat(validator.isValid("112223330", null)).isFalse();       // 9 dígitos
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ com sequência inválida")
    void deveRejeitarCnpjComSequenciaInvalida() {
        assertThat(validator.isValid("00000000000000", null)).isFalse();
        assertThat(validator.isValid("11111111111111", null)).isFalse();
        assertThat(validator.isValid("22222222222222", null)).isFalse();
        assertThat(validator.isValid("33333333333333", null)).isFalse();
        assertThat(validator.isValid("44444444444444", null)).isFalse();
        assertThat(validator.isValid("55555555555555", null)).isFalse();
        assertThat(validator.isValid("66666666666666", null)).isFalse();
        assertThat(validator.isValid("77777777777777", null)).isFalse();
        assertThat(validator.isValid("88888888888888", null)).isFalse();
        assertThat(validator.isValid("99999999999999", null)).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ com dígitos verificadores inválidos")
    void deveRejeitarCnpjComDigitosVerificadoresInvalidos() {
        assertThat(validator.isValid("11222333000100", null)).isFalse();  // DV correto seria 81
        assertThat(validator.isValid("11222333000199", null)).isFalse();  // DV correto seria 81
        assertThat(validator.isValid("34028316000100", null)).isFalse();  // DV correto seria 03
    }

    @Test
    @DisplayName("Deve aceitar CNPJs válidos conhecidos")
    void deveAceitarCnpjsValidosConhecidos() {
        // CNPJs gerados com algoritmo oficial
        assertThat(validator.isValid("11222333000181", null)).isTrue();
        assertThat(validator.isValid("34028316000103", null)).isTrue();
        assertThat(validator.isValid("06990590000123", null)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar CNPJ válido com formatação mista")
    void deveAceitarCnpjValidoComFormatacaoMista() {
        assertThat(validator.isValid("11.222.333000181", null)).isTrue();     // Parcialmente formatado
        assertThat(validator.isValid("11222333/0001-81", null)).isTrue();     // Parcialmente formatado
        assertThat(validator.isValid("11222333000181", null)).isTrue();       // Sem formatação
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ com caracteres não numéricos")
    void deveRejeitarCnpjComCaracteresNaoNumericos() {
        assertThat(validator.isValid("11.222.333/0001-8a", null)).isFalse();
        assertThat(validator.isValid("ab.cde.fgh/ijkl-mn", null)).isFalse();
    }

    @Test
    @DisplayName("Deve aceitar CNPJ com espaços")
    void deveAceitarCnpjComEspacos() {
        assertThat(validator.isValid("11 222 333 0001 81", null)).isTrue();  // Espaços removidos = 14 dígitos
    }
}
