package br.com.vanessa_mudanca.cliente_core.application.ports.input;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;

import java.util.UUID;

/**
 * Port de entrada (Use Case) para buscar Cliente Pessoa Física por ID público.
 */
public interface FindClientePFByIdUseCase {

    /**
     * Busca um cliente pessoa física por Public ID (UUID).
     * Esse método usa o identificador público para evitar exposição de IDs sequenciais.
     *
     * @param publicId UUID público do cliente
     * @return cliente PF encontrado
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException se cliente não existe
     */
    ClientePFResponse findByPublicId(UUID publicId);
}
