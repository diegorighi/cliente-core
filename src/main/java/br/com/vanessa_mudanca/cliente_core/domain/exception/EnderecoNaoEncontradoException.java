package br.com.vanessa_mudanca.cliente_core.domain.exception;

/**
 * Exceção lançada quando um endereço não é encontrado pelo ID.
 */
public class EnderecoNaoEncontradoException extends BusinessException {

    public EnderecoNaoEncontradoException(Long enderecoId) {
        super(String.format("Endereço com ID %d não encontrado", enderecoId));
    }
}
