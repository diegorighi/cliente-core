package br.com.vanessa_mudanca.cliente_core.infrastructure.repository;

import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository JPA para Cliente Pessoa Física.
 * Interface Spring Data JPA com queries derivadas.
 */
@Repository
public interface ClientePFJpaRepository extends JpaRepository<ClientePF, Long> {

    /**
     * Busca cliente PF por CPF.
     *
     * @param cpf CPF do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePF> findByCpf(String cpf);

    /**
     * Verifica se existe cliente PF com o CPF informado.
     *
     * @param cpf CPF a verificar
     * @return true se existe, false caso contrário
     */
    boolean existsByCpf(String cpf);

    /**
     * Busca cliente PF por Public ID (UUID).
     * Método usado nas APIs públicas para evitar exposição de IDs sequenciais.
     *
     * @param publicId UUID público do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePF> findByPublicId(UUID publicId);
}
