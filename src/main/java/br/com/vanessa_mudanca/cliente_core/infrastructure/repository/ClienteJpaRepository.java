package br.com.vanessa_mudanca.cliente_core.infrastructure.repository;

import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository JPA para Cliente (base).
 * Interface Spring Data JPA para operações comuns a todos os tipos de cliente.
 */
@Repository
public interface ClienteJpaRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca cliente por Public ID (UUID).
     *
     * @param publicId UUID público do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<Cliente> findByPublicId(UUID publicId);
}
