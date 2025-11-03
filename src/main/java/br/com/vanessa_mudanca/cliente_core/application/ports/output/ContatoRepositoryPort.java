package br.com.vanessa_mudanca.cliente_core.application.ports.output;

import br.com.vanessa_mudanca.cliente_core.domain.entity.Contato;

import java.util.List;
import java.util.Optional;

/**
 * Port de saída para operações de persistência de Contato.
 * Implementado pela camada de infraestrutura.
 */
public interface ContatoRepositoryPort {

    Optional<Contato> findById(Long id);

    Contato save(Contato contato);

    /**
     * Verifica se existe outro contato principal para o cliente.
     *
     * @param clienteId ID do cliente
     * @param contatoPrincipal Flag de principal (true)
     * @param idExcluir ID do contato a excluir da busca (o que está sendo atualizado)
     * @return true se já existe outro contato principal
     */
    boolean existsByClienteIdAndContatoPrincipalAndIdNot(
            Long clienteId,
            Boolean contatoPrincipal,
            Long idExcluir
    );

    List<Contato> findByClienteId(Long clienteId);
}
