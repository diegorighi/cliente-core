package br.com.vanessa_mudanca.cliente_core.domain.exception;

/**
 * Exceção lançada quando um cliente indicador não é encontrado.
 */
public class ClienteIndicadorNaoEncontradoException extends BusinessException {

    public ClienteIndicadorNaoEncontradoException(Long id) {
        super(String.format("Cliente indicador com ID '%d' não encontrado", id));
    }
}
