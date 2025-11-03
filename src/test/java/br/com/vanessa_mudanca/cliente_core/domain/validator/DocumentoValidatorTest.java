package br.com.vanessa_mudanca.cliente_core.domain.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para DocumentoValidator.
 * Valida todos os cenários possíveis de validação de CPF e CNPJ.
 */
@DisplayName("DocumentoValidator - Testes de validação de CPF e CNPJ")
class DocumentoValidatorTest {

    // ========== TESTES DE VALIDAÇÃO DE CPF ==========

    @Test
    @DisplayName("Deve validar CPF correto sem formatação")
    void deveValidarCpfCorretoSemFormatacao() {
        assertTrue(DocumentoValidator.isValidCpf("12345678909"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123.456.789-09", "111.444.777-35", "000.000.001-91"})
    @DisplayName("Deve validar CPFs corretos com formatação")
    void deveValidarCpfsCorretosComFormatacao(String cpf) {
        assertTrue(DocumentoValidator.isValidCpf(cpf));
    }

    @Test
    @DisplayName("Deve rejeitar CPF com todos dígitos iguais")
    void deveRejeitarCpfComTodosDigitosIguais() {
        assertFalse(DocumentoValidator.isValidCpf("11111111111"));
        assertFalse(DocumentoValidator.isValidCpf("222.222.222-22"));
        assertFalse(DocumentoValidator.isValidCpf("333.333.333-33"));
    }

    @Test
    @DisplayName("Deve rejeitar CPF com dígito verificador inválido")
    void deveRejeitarCpfComDigitoVerificadorInvalido() {
        assertFalse(DocumentoValidator.isValidCpf("12345678900")); // último dígito errado
        assertFalse(DocumentoValidator.isValidCpf("12345678919")); // último dígito errado
    }

    @Test
    @DisplayName("Deve rejeitar CPF com menos de 11 dígitos")
    void deveRejeitarCpfComMenosDeOnzeDigitos() {
        assertFalse(DocumentoValidator.isValidCpf("123456789"));
        assertFalse(DocumentoValidator.isValidCpf("1234567890"));
    }

    @Test
    @DisplayName("Deve rejeitar CPF com mais de 11 dígitos")
    void deveRejeitarCpfComMaisDeOnzeDigitos() {
        assertFalse(DocumentoValidator.isValidCpf("123456789012"));
    }

    @Test
    @DisplayName("Deve rejeitar CPF nulo")
    void deveRejeitarCpfNulo() {
        assertFalse(DocumentoValidator.isValidCpf(null));
    }

    @Test
    @DisplayName("Deve rejeitar CPF vazio")
    void deveRejeitarCpfVazio() {
        assertFalse(DocumentoValidator.isValidCpf(""));
        assertFalse(DocumentoValidator.isValidCpf("   "));
    }

    // ========== TESTES DE VALIDAÇÃO DE CNPJ ==========

    @Test
    @DisplayName("Deve validar CNPJ correto sem formatação")
    void deveValidarCnpjCorretoSemFormatacao() {
        assertTrue(DocumentoValidator.isValidCnpj("11222333000181"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"11.222.333/0001-81", "00.000.000/0001-91"})
    @DisplayName("Deve validar CNPJs corretos com formatação")
    void deveValidarCnpjsCorretosComFormatacao(String cnpj) {
        assertTrue(DocumentoValidator.isValidCnpj(cnpj));
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ com todos dígitos iguais")
    void deveRejeitarCnpjComTodosDigitosIguais() {
        assertFalse(DocumentoValidator.isValidCnpj("11111111111111"));
        assertFalse(DocumentoValidator.isValidCnpj("22.222.222/2222-22"));
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ com dígito verificador inválido")
    void deveRejeitarCnpjComDigitoVerificadorInvalido() {
        assertFalse(DocumentoValidator.isValidCnpj("11222333000180")); // último dígito errado
        assertFalse(DocumentoValidator.isValidCnpj("11222333000191")); // último dígito errado
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ com menos de 14 dígitos")
    void deveRejeitarCnpjComMenosDeQuatorzeDigitos() {
        assertFalse(DocumentoValidator.isValidCnpj("1122233300018"));
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ com mais de 14 dígitos")
    void deveRejeitarCnpjComMaisDeQuatorzeDigitos() {
        assertFalse(DocumentoValidator.isValidCnpj("112223330001811"));
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ nulo")
    void deveRejeitarCnpjNulo() {
        assertFalse(DocumentoValidator.isValidCnpj(null));
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ vazio")
    void deveRejeitarCnpjVazio() {
        assertFalse(DocumentoValidator.isValidCnpj(""));
        assertFalse(DocumentoValidator.isValidCnpj("   "));
    }

    // ========== TESTES DE LIMPEZA E FORMATAÇÃO ==========

    @Test
    @DisplayName("Deve limpar formatação de CPF")
    void deveLimparFormatacaoDeCpf() {
        assertEquals("12345678909", DocumentoValidator.limparDocumento("123.456.789-09"));
    }

    @Test
    @DisplayName("Deve limpar formatação de CNPJ")
    void deveLimparFormatacaoDeCnpj() {
        assertEquals("11222333000181", DocumentoValidator.limparDocumento("11.222.333/0001-81"));
    }

    @Test
    @DisplayName("Deve retornar null ao limpar documento nulo")
    void deveRetornarNullAoLimparDocumentoNulo() {
        assertNull(DocumentoValidator.limparDocumento(null));
    }

    @Test
    @DisplayName("Deve formatar CPF corretamente")
    void deveFormatarCpfCorretamente() {
        assertEquals("123.456.789-09", DocumentoValidator.formatarCpf("12345678909"));
    }

    @Test
    @DisplayName("Deve formatar CNPJ corretamente")
    void deveFormatarCnpjCorretamente() {
        assertEquals("11.222.333/0001-81", DocumentoValidator.formatarCnpj("11222333000181"));
    }

    @Test
    @DisplayName("Deve retornar CPF original se não tiver 11 dígitos")
    void deveRetornarCpfOriginalSeNaoTiverOnzeDigitos() {
        String cpfInvalido = "123456789";
        assertEquals(cpfInvalido, DocumentoValidator.formatarCpf(cpfInvalido));
    }

    @Test
    @DisplayName("Deve retornar CNPJ original se não tiver 14 dígitos")
    void deveRetornarCnpjOriginalSeNaoTiverQuatorzeDigitos() {
        String cnpjInvalido = "1122233300018";
        assertEquals(cnpjInvalido, DocumentoValidator.formatarCnpj(cnpjInvalido));
    }
}
