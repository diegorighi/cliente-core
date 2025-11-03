package br.com.vanessa_mudanca.cliente_core.application.ports.output;

import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de saída para persistência de Cliente Pessoa Física.
 * Define o contrato que os adapters de persistência devem implementar.
 */
public interface ClientePFRepositoryPort {

    /**
     * Salva um cliente pessoa física.
     *
     * @param clientePF cliente a ser salvo
     * @return cliente salvo com ID gerado
     */
    ClientePF save(ClientePF clientePF);

    /**
     * Busca um cliente pessoa física por CPF.
     *
     * @param cpf CPF do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePF> findByCpf(String cpf);

    /**
     * Verifica se existe um cliente com o CPF informado.
     *
     * @param cpf CPF a verificar
     * @return true se existe, false caso contrário
     */
    boolean existsByCpf(String cpf);

    /**
     * Busca um cliente pessoa física por ID interno (uso interno apenas).
     *
     * @param id ID interno do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePF> findById(Long id);

    /**
     * Busca um cliente pessoa física por Public ID (UUID).
     * Esse método deve ser usado nas APIs públicas.
     *
     * @param publicId UUID público do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<ClientePF> findByPublicId(UUID publicId);

    /**
     * Lista todos os clientes pessoa física com paginação.
     *
     * @param pageable configuração de paginação e ordenação
     * @return página com clientes PF
     */
    Page<ClientePF> findAll(Pageable pageable);
}
