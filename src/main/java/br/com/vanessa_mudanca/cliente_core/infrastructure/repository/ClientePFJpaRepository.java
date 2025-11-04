package br.com.vanessa_mudanca.cliente_core.infrastructure.repository;

import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository JPA para Cliente Pessoa Física.
 * Interface Spring Data JPA com queries derivadas.
 *
 * IMPORTANTE:
 * - Métodos com sufixo "AndAtivoTrueAndDataDelecaoIsNull" retornam APENAS registros ativos (não deletados)
 * - Métodos sem sufixo retornam TODOS os registros (incluindo deletados) - use com cuidado!
 */
@Repository
public interface ClientePFJpaRepository extends JpaRepository<ClientePF, Long> {

    /**
     * Busca cliente PF por CPF.
     * ATENÇÃO: Retorna cliente mesmo se deletado (soft delete).
     *
     * @param cpf CPF do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePF> findByCpf(String cpf);

    /**
     * Busca cliente PF ATIVO (não deletado) por CPF.
     * Aplica filtro: ativo = true AND data_delecao IS NULL.
     *
     * @param cpf CPF do cliente
     * @return Optional contendo o cliente se encontrado e ativo
     */
    Optional<ClientePF> findByCpfAndAtivoTrueAndDataDelecaoIsNull(String cpf);

    /**
     * Verifica se existe cliente PF com o CPF informado.
     * ATENÇÃO: Considera clientes deletados também.
     *
     * @param cpf CPF a verificar
     * @return true se existe, false caso contrário
     */
    boolean existsByCpf(String cpf);

    /**
     * Verifica se existe cliente PF ATIVO com o CPF informado.
     * Aplica filtro: ativo = true AND data_delecao IS NULL.
     *
     * @param cpf CPF a verificar
     * @return true se existe cliente ativo, false caso contrário
     */
    boolean existsByCpfAndAtivoTrueAndDataDelecaoIsNull(String cpf);

    /**
     * Busca cliente PF por Public ID (UUID).
     * ATENÇÃO: Retorna cliente mesmo se deletado (soft delete).
     * Método usado nas APIs públicas para evitar exposição de IDs sequenciais.
     *
     * @param publicId UUID público do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePF> findByPublicId(UUID publicId);

    /**
     * Busca cliente PF ATIVO (não deletado) por Public ID (UUID).
     * Aplica filtro: ativo = true AND data_delecao IS NULL.
     *
     * @param publicId UUID público do cliente
     * @return Optional contendo o cliente se encontrado e ativo
     */
    Optional<ClientePF> findByPublicIdAndAtivoTrueAndDataDelecaoIsNull(UUID publicId);
}
