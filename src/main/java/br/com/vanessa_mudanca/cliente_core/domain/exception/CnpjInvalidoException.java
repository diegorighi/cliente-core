package br.com.vanessa_mudanca.cliente_core.domain.exception;

/**
 * Exceção lançada quando um CNPJ não é válido.
 */
public class CnpjInvalidoException extends BusinessException {

    public CnpjInvalidoException(String cnpj) {
        super(String.format("CNPJ '%s' é inválido", cnpj));
    }
}
