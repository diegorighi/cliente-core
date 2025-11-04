package br.com.vanessa_mudanca.cliente_core.application.ports.output;

import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePJ;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de saída para persistência de Cliente Pessoa Jurídica.
 * Define o contrato que os adapters de persistência devem implementar.
 */
public interface ClientePJRepositoryPort {

    /**
     * Salva um cliente pessoa jurídica.
     *
     * @param clientePJ cliente a ser salvo
     * @return cliente salvo com ID gerado
     */
    ClientePJ save(ClientePJ clientePJ);

    /**
     * Busca um cliente pessoa jurídica por CNPJ.
     *
     * @param cnpj CNPJ do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePJ> findByCnpj(String cnpj);

    /**
     * Verifica se existe um cliente com o CNPJ informado.
     *
     * @param cnpj CNPJ a verificar
     * @return true se existe, false caso contrário
     */
    boolean existsByCnpj(String cnpj);

    /**
     * Busca um cliente pessoa jurídica por ID interno (uso interno apenas).
     *
     * @param id ID interno do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePJ> findById(Long id);

    /**
     * Busca um cliente pessoa jurídica por Public ID (UUID).
     * Esse método deve ser usado nas APIs públicas.
     *
     * @param publicId UUID público do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePJ> findByPublicId(UUID publicId);

    /**
     * Lista todos os clientes pessoa jurídica com paginação.
     * ATENÇÃO: Inclui clientes deletados (soft delete).
     *
     * @param pageable configuração de paginação e ordenação
     * @return página com clientes PJ
     */
    Page<ClientePJ> findAll(Pageable pageable);

    /**
     * Busca cliente PJ ATIVO (não deletado) por CNPJ.
     *
     * @param cnpj CNPJ do cliente
     * @return Optional contendo o cliente se encontrado e ativo
     */
    Optional<ClientePJ> findActiveByCnpj(String cnpj);

    /**
     * Busca cliente PJ ATIVO (não deletado) por Public ID.
     *
     * @param publicId UUID público do cliente
     * @return Optional contendo o cliente se encontrado e ativo
     */
    Optional<ClientePJ> findActiveByPublicId(UUID publicId);

    /**
     * Verifica se existe cliente PJ ATIVO com o CNPJ informado.
     *
     * @param cnpj CNPJ a verificar
     * @return true se existe cliente ativo, false caso contrário
     */
    boolean existsActiveByCnpj(String cnpj);
}
