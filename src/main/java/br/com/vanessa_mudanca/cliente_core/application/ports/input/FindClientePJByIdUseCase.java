package br.com.vanessa_mudanca.cliente_core.application.ports.input;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;

import java.util.UUID;

/**
 * Port de entrada (Use Case) para buscar Cliente Pessoa Jurídica por ID público.
 */
public interface FindClientePJByIdUseCase {

    /**
     * Busca um cliente pessoa jurídica por Public ID (UUID).
     * Esse método usa o identificador público para evitar exposição de IDs sequenciais.
     *
     * @param publicId UUID público do cliente
     * @return cliente PJ encontrado
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException se cliente não existe
     */
    ClientePJResponse findByPublicId(UUID publicId);
}
