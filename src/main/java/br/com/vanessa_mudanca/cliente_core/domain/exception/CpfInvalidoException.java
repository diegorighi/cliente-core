package br.com.vanessa_mudanca.cliente_core.domain.exception;

/**
 * Exceção lançada quando um CPF não é válido.
 */
public class CpfInvalidoException extends BusinessException {

    public CpfInvalidoException(String cpf) {
        super(String.format("CPF '%s' é inválido", cpf));
    }
}
