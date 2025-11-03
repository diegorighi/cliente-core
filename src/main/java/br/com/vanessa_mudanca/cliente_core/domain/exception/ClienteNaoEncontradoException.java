package br.com.vanessa_mudanca.cliente_core.domain.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um cliente não é encontrado.
 */
public class ClienteNaoEncontradoException extends BusinessException {

    public ClienteNaoEncontradoException(UUID publicId) {
        super(String.format("Cliente com Public ID '%s' não encontrado", publicId));
    }

    public ClienteNaoEncontradoException(Long id) {
        super(String.format("Cliente com ID interno '%d' não encontrado", id));
    }

    public ClienteNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
