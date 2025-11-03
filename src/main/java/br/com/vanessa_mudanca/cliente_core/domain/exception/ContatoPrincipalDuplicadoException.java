package br.com.vanessa_mudanca.cliente_core.domain.exception;

/**
 * Exceção lançada quando há tentativa de marcar mais de um contato como principal
 * para o mesmo cliente.
 *
 * Regra de negócio: Apenas 1 contato pode ser principal por cliente.
 */
public class ContatoPrincipalDuplicadoException extends BusinessException {

    public ContatoPrincipalDuplicadoException() {
        super("Já existe um contato principal. " +
                "Remova a flag principal do outro contato antes de marcar este.");
    }
}
