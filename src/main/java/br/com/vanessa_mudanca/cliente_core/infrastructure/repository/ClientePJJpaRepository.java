package br.com.vanessa_mudanca.cliente_core.infrastructure.repository;

import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePJ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository JPA para Cliente Pessoa Jurídica.
 * Interface Spring Data JPA com queries derivadas.
 *
 * IMPORTANTE:
 * - Métodos com sufixo "AndAtivoTrueAndDataDelecaoIsNull" retornam APENAS registros ativos (não deletados)
 * - Métodos sem sufixo retornam TODOS os registros (incluindo deletados) - use com cuidado!
 */
@Repository
public interface ClientePJJpaRepository extends JpaRepository<ClientePJ, Long> {

    /**
     * Busca cliente PJ por CNPJ.
     * ATENÇÃO: Retorna cliente mesmo se deletado (soft delete).
     *
     * @param cnpj CNPJ do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePJ> findByCnpj(String cnpj);

    /**
     * Busca cliente PJ ATIVO (não deletado) por CNPJ.
     * Aplica filtro: ativo = true AND data_delecao IS NULL.
     *
     * @param cnpj CNPJ do cliente
     * @return Optional contendo o cliente se encontrado e ativo
     */
    Optional<ClientePJ> findByCnpjAndAtivoTrueAndDataDelecaoIsNull(String cnpj);

    /**
     * Verifica se existe cliente PJ com o CNPJ informado.
     * ATENÇÃO: Considera clientes deletados também.
     *
     * @param cnpj CNPJ a verificar
     * @return true se existe, false caso contrário
     */
    boolean existsByCnpj(String cnpj);

    /**
     * Verifica se existe cliente PJ ATIVO com o CNPJ informado.
     * Aplica filtro: ativo = true AND data_delecao IS NULL.
     *
     * @param cnpj CNPJ a verificar
     * @return true se existe cliente ativo, false caso contrário
     */
    boolean existsByCnpjAndAtivoTrueAndDataDelecaoIsNull(String cnpj);

    /**
     * Busca cliente PJ por Public ID (UUID).
     * ATENÇÃO: Retorna cliente mesmo se deletado (soft delete).
     * Método usado nas APIs públicas para evitar exposição de IDs sequenciais.
     *
     * @param publicId UUID público do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePJ> findByPublicId(UUID publicId);

    /**
     * Busca cliente PJ ATIVO (não deletado) por Public ID (UUID).
     * Aplica filtro: ativo = true AND data_delecao IS NULL.
     *
     * @param publicId UUID público do cliente
     * @return Optional contendo o cliente se encontrado e ativo
     */
    Optional<ClientePJ> findByPublicIdAndAtivoTrueAndDataDelecaoIsNull(UUID publicId);
}
