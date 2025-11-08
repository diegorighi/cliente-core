package br.com.vanessa_mudanca.cliente_core.infrastructure.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CpfValidator - Validação de CPF")
class CpfValidatorTest {

    private CpfValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CpfValidator();
    }

    @Test
    @DisplayName("Deve aceitar CPF válido sem formatação")
    void deveAceitarCpfValidoSemFormatacao() {
        assertThat(validator.isValid("12345678909", null)).isTrue();
        assertThat(validator.isValid("11144477735", null)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar CPF válido com formatação")
    void deveAceitarCpfValidoComFormatacao() {
        assertThat(validator.isValid("123.456.789-09", null)).isTrue();
        assertThat(validator.isValid("111.444.777-35", null)).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar CPF null")
    void deveRejeitarCpfNull() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar CPF em branco")
    void deveRejeitarCpfEmBranco() {
        assertThat(validator.isValid("", null)).isFalse();
        assertThat(validator.isValid("   ", null)).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar CPF com tamanho incorreto")
    void deveRejeitarCpfComTamanhoIncorreto() {
        assertThat(validator.isValid("123456789", null)).isFalse();     // 9 dígitos
        assertThat(validator.isValid("123456789012", null)).isFalse();  // 12 dígitos
        assertThat(validator.isValid("1234567890", null)).isFalse();    // 10 dígitos
    }

    @Test
    @DisplayName("Deve rejeitar CPF com sequência inválida")
    void deveRejeitarCpfComSequenciaInvalida() {
        assertThat(validator.isValid("00000000000", null)).isFalse();
        assertThat(validator.isValid("11111111111", null)).isFalse();
        assertThat(validator.isValid("22222222222", null)).isFalse();
        assertThat(validator.isValid("33333333333", null)).isFalse();
        assertThat(validator.isValid("44444444444", null)).isFalse();
        assertThat(validator.isValid("55555555555", null)).isFalse();
        assertThat(validator.isValid("66666666666", null)).isFalse();
        assertThat(validator.isValid("77777777777", null)).isFalse();
        assertThat(validator.isValid("88888888888", null)).isFalse();
        assertThat(validator.isValid("99999999999", null)).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar CPF com dígitos verificadores inválidos")
    void deveRejeitarCpfComDigitosVerificadoresInvalidos() {
        assertThat(validator.isValid("12345678900", null)).isFalse();  // DV correto seria 09
        assertThat(validator.isValid("12345678999", null)).isFalse();  // DV correto seria 09
        assertThat(validator.isValid("11144477700", null)).isFalse();  // DV correto seria 35
    }

    @Test
    @DisplayName("Deve aceitar CPFs válidos conhecidos")
    void deveAceitarCpfsValidosConhecidos() {
        // CPFs gerados com algoritmo oficial
        assertThat(validator.isValid("52998224725", null)).isTrue();
        assertThat(validator.isValid("86734718000", null)).isTrue();
        assertThat(validator.isValid("93095135270", null)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar CPF válido com formatação mista")
    void deveAceitarCpfValidoComFormatacaoMista() {
        assertThat(validator.isValid("123.456.78909", null)).isTrue();  // Parcialmente formatado
        assertThat(validator.isValid("12345678909", null)).isTrue();     // Sem formatação
    }

    @Test
    @DisplayName("Deve rejeitar CPF com caracteres não numéricos")
    void deveRejeitarCpfComCaracteresNaoNumericos() {
        assertThat(validator.isValid("123.456.789-0a", null)).isFalse();
        assertThat(validator.isValid("abc.def.ghi-jk", null)).isFalse();
        assertThat(validator.isValid("123 456 789 09", null)).isTrue(); // Espaços removidos = 11 dígitos
    }
}
