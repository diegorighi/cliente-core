package br.com.vanessa_mudanca.cliente_core.domain.exception;

/**
 * Exceção lançada quando se tenta cadastrar um CNPJ que já existe no sistema.
 */
public class CnpjJaCadastradoException extends BusinessException {

    public CnpjJaCadastradoException(String cnpj) {
        super(String.format("CNPJ '%s' já está cadastrado no sistema", cnpj));
    }
}
