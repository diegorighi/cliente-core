package br.com.vanessa_mudanca.cliente_core.domain.exception;

import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoEnderecoEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes para exceções de domínio não cobertas.
 * Garante que todas as exceções podem ser instanciadas corretamente.
 */
@DisplayName("DomainExceptions - Testes de instanciação e mensagens")
class DomainExceptionsTest {

    @Test
    @DisplayName("EnderecoPrincipalDuplicadoException deve conter tipo de endereço na mensagem")
    void enderecoPrincipalDuplicadoDeveConterTipo() {
        // Act
        EnderecoPrincipalDuplicadoException exception =
                new EnderecoPrincipalDuplicadoException(TipoEnderecoEnum.RESIDENCIAL);

        // Assert
        assertThat(exception.getMessage())
                .contains("Já existe um endereço")
                .contains("RESIDENCIAL")
                .contains("principal");
    }

    @Test
    @DisplayName("ContatoPrincipalDuplicadoException deve ter mensagem padrão")
    void contatoPrincipalDuplicadoDeveTerMensagemPadrao() {
        // Act
        ContatoPrincipalDuplicadoException exception = new ContatoPrincipalDuplicadoException();

        // Assert
        assertThat(exception.getMessage())
                .contains("Já existe um contato")
                .contains("principal");
    }

    @Test
    @DisplayName("DataValidadeInvalidaException deve aceitar mensagem customizada")
    void dataValidadeInvalidaDeveAceitarMensagemCustomizada() {
        // Arrange
        String mensagemCustomizada = "Data de validade não pode ser superior a 50 anos";

        // Act
        DataValidadeInvalidaException exception = new DataValidadeInvalidaException(mensagemCustomizada);

        // Assert
        assertThat(exception.getMessage()).isEqualTo(mensagemCustomizada);
    }

    @Test
    @DisplayName("CampoImutavelException deve aceitar campo e gerar mensagem padrão")
    void campoImutavelDeveGerarMensagemPadrao() {
        // Arrange
        String campo = "CPF";

        // Act
        CampoImutavelException exception = new CampoImutavelException(campo);

        // Assert
        assertThat(exception.getMessage())
                .contains(campo)
                .contains("imutável")
                .contains("não pode ser alterado");
    }

    @Test
    @DisplayName("CampoImutavelException deve aceitar campo como parâmetro")
    void campoImutavelDeveAceitarCampo() {
        // Arrange
        String campo = "CPF";

        // Act
        CampoImutavelException exception = new CampoImutavelException(campo);

        // Assert
        assertThat(exception.getMessage())
                .contains(campo)
                .contains("não pode ser alterado");
    }

    @Test
    @DisplayName("CampoImutavelException deve estender BusinessException")
    void campoImutavelDeveEstenderBusinessException() {
        // Act
        CampoImutavelException exception = new CampoImutavelException("teste");

        // Assert
        assertThat(exception).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("DataValidadeInvalidaException deve estender BusinessException")
    void dataValidadeInvalidaDeveEstenderBusinessException() {
        // Act
        DataValidadeInvalidaException exception = new DataValidadeInvalidaException("teste");

        // Assert
        assertThat(exception).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("ContatoPrincipalDuplicadoException deve estender BusinessException")
    void contatoPrincipalDuplicadoDeveEstenderBusinessException() {
        // Act
        ContatoPrincipalDuplicadoException exception = new ContatoPrincipalDuplicadoException();

        // Assert
        assertThat(exception).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("EnderecoPrincipalDuplicadoException deve estender BusinessException")
    void enderecoPrincipalDuplicadoDeveEstenderBusinessException() {
        // Act
        EnderecoPrincipalDuplicadoException exception =
                new EnderecoPrincipalDuplicadoException(TipoEnderecoEnum.RESIDENCIAL);

        // Assert
        assertThat(exception).isInstanceOf(BusinessException.class);
    }
}
