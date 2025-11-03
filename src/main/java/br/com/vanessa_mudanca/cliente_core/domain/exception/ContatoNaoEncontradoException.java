package br.com.vanessa_mudanca.cliente_core.domain.exception;

/**
 * Exceção lançada quando um contato não é encontrado pelo ID.
 */
public class ContatoNaoEncontradoException extends BusinessException {

    public ContatoNaoEncontradoException(Long contatoId) {
        super(String.format("Contato com ID %d não encontrado", contatoId));
    }
}
