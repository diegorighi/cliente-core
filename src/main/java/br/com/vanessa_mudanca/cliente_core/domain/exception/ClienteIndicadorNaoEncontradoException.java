package br.com.vanessa_mudanca.cliente_core.domain.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um cliente indicador não é encontrado.
 */
public class ClienteIndicadorNaoEncontradoException extends BusinessException {

    public ClienteIndicadorNaoEncontradoException(UUID id) {
        super(String.format("Cliente indicador com ID '%s' não encontrado", id));
    }
}
