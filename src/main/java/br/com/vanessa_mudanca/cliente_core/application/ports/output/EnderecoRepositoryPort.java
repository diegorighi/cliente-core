package br.com.vanessa_mudanca.cliente_core.application.ports.output;

import br.com.vanessa_mudanca.cliente_core.domain.entity.Endereco;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoEnderecoEnum;

import java.util.List;
import java.util.Optional;

/**
 * Port de saída para operações de persistência de Endereco.
 * Implementado pela camada de infraestrutura.
 */
public interface EnderecoRepositoryPort {

    Optional<Endereco> findById(Long id);

    Endereco save(Endereco endereco);

    /**
     * Verifica se existe outro endereço principal do mesmo tipo para o cliente.
     *
     * @param clienteId ID do cliente
     * @param tipoEndereco Tipo do endereço
     * @param enderecoPrincipal Flag de principal (true)
     * @param idExcluir ID do endereço a excluir da busca (o que está sendo atualizado)
     * @return true se já existe outro endereço principal do mesmo tipo
     */
    boolean existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
            Long clienteId,
            TipoEnderecoEnum tipoEndereco,
            Boolean enderecoPrincipal,
            Long idExcluir
    );

    List<Endereco> findByClienteId(Long clienteId);
}
