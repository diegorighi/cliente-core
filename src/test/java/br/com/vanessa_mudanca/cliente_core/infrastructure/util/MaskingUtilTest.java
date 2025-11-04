package br.com.vanessa_mudanca.cliente_core.infrastructure.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes para MaskingUtil - Utilitário de mascaramento de dados sensíveis (LGPD).
 *
 * Valida que:
 * 1. CPF é mascarado corretamente preservando últimos 3 dígitos + DV
 * 2. CNPJ é mascarado corretamente preservando últimos 4 dígitos + DV
 * 3. Email é mascarado preservando apenas 2 primeiras letras do local-part
 * 4. Nome é mascarado preservando apenas primeira letra de cada palavra
 * 5. Telefone é mascarado preservando apenas 4 últimos dígitos
 * 6. Valores null/empty são tratados corretamente
 * 7. Formatos inválidos são sinalizados
 */
class MaskingUtilTest {

    // ==================== Testes CPF ====================

    @Test
    void deveMascararCpfComFormatacao() {
        // Arrange
        String cpf = "123.456.789-10";

        // Act
        String resultado = MaskingUtil.maskCpf(cpf);

        // Assert
        assertThat(resultado).isEqualTo("***.***.789-10");
    }

    @Test
    void deveMascararCpfSemFormatacao() {
        // Arrange
        String cpf = "12345678910";

        // Act
        String resultado = MaskingUtil.maskCpf(cpf);

        // Assert
        assertThat(resultado).isEqualTo("***.***.789-10");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void deveRetornarNullParaCpfNuloOuVazio(String cpf) {
        // Act
        String resultado = MaskingUtil.maskCpf(cpf);

        // Assert
        assertThat(resultado).isNull();
    }

    @Test
    void deveRetornarInvalidoParaCpfComTamanhoIncorreto() {
        // Arrange
        String cpfInvalido = "123456789"; // 9 dígitos (inválido)

        // Act
        String resultado = MaskingUtil.maskCpf(cpfInvalido);

        // Assert
        assertThat(resultado).isEqualTo("***INVALID_CPF***");
    }

    // ==================== Testes CNPJ ====================

    @Test
    void deveMascararCnpjComFormatacao() {
        // Arrange
        String cnpj = "12.345.678/0001-45";

        // Act
        String resultado = MaskingUtil.maskCnpj(cnpj);

        // Assert
        assertThat(resultado).isEqualTo("**.***.***/****-45");
    }

    @Test
    void deveMascararCnpjSemFormatacao() {
        // Arrange
        String cnpj = "12345678000145";

        // Act
        String resultado = MaskingUtil.maskCnpj(cnpj);

        // Assert
        assertThat(resultado).isEqualTo("**.***.***/****-45");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void deveRetornarNullParaCnpjNuloOuVazio(String cnpj) {
        // Act
        String resultado = MaskingUtil.maskCnpj(cnpj);

        // Assert
        assertThat(resultado).isNull();
    }

    @Test
    void deveRetornarInvalidoParaCnpjComTamanhoIncorreto() {
        // Arrange
        String cnpjInvalido = "12345678000"; // 11 dígitos (inválido)

        // Act
        String resultado = MaskingUtil.maskCnpj(cnpjInvalido);

        // Assert
        assertThat(resultado).isEqualTo("***INVALID_CNPJ***");
    }

    // ==================== Testes Email ====================

    @Test
    void deveMascararEmailComLocalPartMaiorQue2() {
        // Arrange
        String email = "joao.silva@example.com";

        // Act
        String resultado = MaskingUtil.maskEmail(email);

        // Assert
        assertThat(resultado).isEqualTo("jo***@example.com");
    }

    @Test
    void deveMascararEmailComLocalPartMenorOuIgualA2() {
        // Arrange
        String email = "ab@example.com";

        // Act
        String resultado = MaskingUtil.maskEmail(email);

        // Assert
        assertThat(resultado).isEqualTo("***@example.com");
    }

    @Test
    void deveMascararEmailCom1CaracterNoLocalPart() {
        // Arrange
        String email = "a@example.com";

        // Act
        String resultado = MaskingUtil.maskEmail(email);

        // Assert
        assertThat(resultado).isEqualTo("***@example.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void deveRetornarNullParaEmailNuloOuVazio(String email) {
        // Act
        String resultado = MaskingUtil.maskEmail(email);

        // Assert
        assertThat(resultado).isNull();
    }

    @Test
    void deveRetornarInvalidoParaEmailSemArroba() {
        // Arrange
        String emailInvalido = "email.sem.arroba.com";

        // Act
        String resultado = MaskingUtil.maskEmail(emailInvalido);

        // Assert
        assertThat(resultado).isEqualTo("***INVALID_EMAIL***");
    }

    // ==================== Testes Nome ====================

    @Test
    void deveMascararNomeCompletoPreservandoPrimeiraLetra() {
        // Arrange
        String nomeCompleto = "João da Silva Santos";

        // Act
        String resultado = MaskingUtil.maskName(nomeCompleto);

        // Assert
        assertThat(resultado).isEqualTo("J*** d*** S*** S***");
    }

    @Test
    void deveMascararNomeSimples() {
        // Arrange
        String nome = "João";

        // Act
        String resultado = MaskingUtil.maskName(nome);

        // Assert
        assertThat(resultado).isEqualTo("J***");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void deveRetornarNullParaNomeNuloOuVazio(String nome) {
        // Act
        String resultado = MaskingUtil.maskName(nome);

        // Assert
        assertThat(resultado).isNull();
    }

    // ==================== Testes Telefone ====================

    @Test
    void deveMascararTelefoneComDDD11Digitos() {
        // Arrange
        String telefone = "11987654321"; // Celular SP

        // Act
        String resultado = MaskingUtil.maskPhone(telefone);

        // Assert
        assertThat(resultado).isEqualTo("(11) ****-4321");
    }

    @Test
    void deveMascararTelefoneComDDD10Digitos() {
        // Arrange
        String telefone = "1133334444"; // Fixo SP

        // Act
        String resultado = MaskingUtil.maskPhone(telefone);

        // Assert
        assertThat(resultado).isEqualTo("(11) ****-4444");
    }

    @Test
    void deveMascararTelefoneComFormatacao() {
        // Arrange
        String telefone = "(11) 98765-4321";

        // Act
        String resultado = MaskingUtil.maskPhone(telefone);

        // Assert
        assertThat(resultado).isEqualTo("(11) ****-4321");
    }

    @Test
    void deveMascararTelefoneSemDDD() {
        // Arrange
        String telefone = "12345678"; // 8 dígitos (sem DDD)

        // Act
        String resultado = MaskingUtil.maskPhone(telefone);

        // Assert
        assertThat(resultado).isEqualTo("****-5678");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void deveRetornarNullParaTelefoneNuloOuVazio(String telefone) {
        // Act
        String resultado = MaskingUtil.maskPhone(telefone);

        // Assert
        assertThat(resultado).isNull();
    }

    @Test
    void deveRetornarInvalidoParaTelefoneComTamanhoIncorreto() {
        // Arrange
        String telefoneInvalido = "123456"; // Muito curto

        // Act
        String resultado = MaskingUtil.maskPhone(telefoneInvalido);

        // Assert
        assertThat(resultado).isEqualTo("***INVALID_PHONE***");
    }

    // ==================== Testes Genérico ====================

    @Test
    void deveMascararDadoGenericoPreservandoPrimeirosDoisEUltimosDoisCaracteres() {
        // Arrange
        String dado = "SenhaSegura123";

        // Act
        String resultado = MaskingUtil.maskGeneric(dado);

        // Assert
        assertThat(resultado).isEqualTo("Se***23");
    }

    @Test
    void deveMascararDadoGenericoCom4CaracteresOuMenos() {
        // Arrange
        String dado = "1234";

        // Act
        String resultado = MaskingUtil.maskGeneric(dado);

        // Assert
        assertThat(resultado).isEqualTo("***");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void deveRetornarNullParaDadoGenericoNuloOuVazio(String dado) {
        // Act
        String resultado = MaskingUtil.maskGeneric(dado);

        // Assert
        assertThat(resultado).isNull();
    }
}
