package br.com.vanessa_mudanca.cliente_core.domain.exception;

import java.util.UUID;

/**
 * Exceção lançada quando tentativa de bloquear um cliente que já está bloqueado.
 */
public class ClienteJaBloqueadoException extends RuntimeException {

    private final UUID publicId;

    public ClienteJaBloqueadoException(UUID publicId) {
        super(String.format("Cliente com PublicId %s já está bloqueado", publicId));
        this.publicId = publicId;
    }

    public UUID getPublicId() {
        return publicId;
    }
}
