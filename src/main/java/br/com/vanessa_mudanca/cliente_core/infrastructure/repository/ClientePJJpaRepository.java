package br.com.vanessa_mudanca.cliente_core.infrastructure.repository;

import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePJ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository JPA para Cliente Pessoa Jurídica.
 * Interface Spring Data JPA com queries derivadas.
 */
@Repository
public interface ClientePJJpaRepository extends JpaRepository<ClientePJ, Long> {

    /**
     * Busca cliente PJ por CNPJ.
     *
     * @param cnpj CNPJ do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePJ> findByCnpj(String cnpj);

    /**
     * Verifica se existe cliente PJ com o CNPJ informado.
     *
     * @param cnpj CNPJ a verificar
     * @return true se existe, false caso contrário
     */
    boolean existsByCnpj(String cnpj);

    /**
     * Busca cliente PJ por Public ID (UUID).
     * Método usado nas APIs públicas para evitar exposição de IDs sequenciais.
     *
     * @param publicId UUID público do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePJ> findByPublicId(UUID publicId);
}
