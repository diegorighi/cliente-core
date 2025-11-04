package br.com.vanessa_mudanca.cliente_core.infrastructure.repository;

import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository JPA para Cliente (base).
 * Interface Spring Data JPA para operações comuns a todos os tipos de cliente.
 *
 * IMPORTANTE:
 * - Métodos com sufixo "AndAtivoTrueAndDataDelecaoIsNull" retornam APENAS registros ativos (não deletados)
 * - Métodos sem sufixo retornam TODOS os registros (incluindo deletados) - use com cuidado!
 */
@Repository
public interface ClienteJpaRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca cliente por Public ID (UUID).
     * ATENÇÃO: Retorna cliente mesmo se deletado (soft delete).
     *
     * @param publicId UUID público do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<Cliente> findByPublicId(UUID publicId);

    /**
     * Busca cliente ATIVO (não deletado) por Public ID (UUID).
     * Aplica filtro: ativo = true AND data_delecao IS NULL.
     *
     * @param publicId UUID público do cliente
     * @return Optional contendo o cliente se encontrado e ativo
     */
    Optional<Cliente> findByPublicIdAndAtivoTrueAndDataDelecaoIsNull(UUID publicId);
}
