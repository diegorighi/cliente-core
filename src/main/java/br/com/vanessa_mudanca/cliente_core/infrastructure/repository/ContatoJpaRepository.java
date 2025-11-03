package br.com.vanessa_mudanca.cliente_core.infrastructure.repository;

import br.com.vanessa_mudanca.cliente_core.domain.entity.Contato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA para Contato.
 * Interface Spring Data JPA com queries derivadas.
 */
@Repository
public interface ContatoJpaRepository extends JpaRepository<Contato, Long> {

    /**
     * Busca todos os contatos de um cliente.
     *
     * @param clienteId ID do cliente
     * @return Lista de contatos do cliente
     */
    List<Contato> findByClienteId(Long clienteId);

    /**
     * Verifica se existe outro contato principal para o cliente (excluindo o ID especificado).
     *
     * @param clienteId ID do cliente
     * @param contatoPrincipal Flag de principal (true)
     * @param id ID do contato a excluir da busca
     * @return true se já existe outro contato principal
     */
    boolean existsByClienteIdAndContatoPrincipalAndIdNot(
            Long clienteId,
            Boolean contatoPrincipal,
            Long id
    );
}
