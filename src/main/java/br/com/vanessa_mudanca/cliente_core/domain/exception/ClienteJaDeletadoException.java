package br.com.vanessa_mudanca.cliente_core.domain.exception;

import java.util.UUID;

/**
 * Exceção lançada quando tentativa de deletar um cliente que já está deletado.
 */
public class ClienteJaDeletadoException extends RuntimeException {

    private final UUID publicId;

    public ClienteJaDeletadoException(UUID publicId) {
        super(String.format("Cliente com PublicId %s já foi deletado anteriormente", publicId));
        this.publicId = publicId;
    }

    public UUID getPublicId() {
        return publicId;
    }
}
