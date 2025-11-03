package br.com.vanessa_mudanca.cliente_core.application.ports.output;

import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de saída para persistência de Cliente (base).
 * Define o contrato para operações comuns a todos os tipos de cliente.
 */
public interface ClienteRepositoryPort {

    /**
     * Busca um cliente por ID interno (qualquer tipo).
     *
     * @param id ID interno do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<Cliente> findById(Long id);

    /**
     * Busca um cliente por Public ID (UUID).
     *
     * @param publicId UUID público do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<Cliente> findByPublicId(UUID publicId);

    /**
     * Verifica se existe um cliente com o ID informado.
     *
     * @param id ID a verificar
     * @return true se existe, false caso contrário
     */
    boolean existsById(Long id);
}
