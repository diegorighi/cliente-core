package br.com.vanessa_mudanca.cliente_core.domain.exception;

/**
 * Exceção lançada quando se tenta cadastrar um CPF que já existe no sistema.
 */
public class CpfJaCadastradoException extends BusinessException {

    public CpfJaCadastradoException(String cpf) {
        super(String.format("CPF '%s' já está cadastrado no sistema", cpf));
    }
}
