package br.com.vanessa_mudanca.cliente_core.application.ports.input;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;

/**
 * Port de entrada (Use Case) para buscar Cliente Pessoa Física por CPF.
 */
public interface FindClientePFByCpfUseCase {

    /**
     * Busca um cliente pessoa física por CPF.
     * Esse método permite descobrir o UUID público de um cliente através do CPF.
     *
     * @param cpf CPF do cliente (pode estar formatado ou não)
     * @return cliente PF encontrado com o publicId
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException se cliente não existe
     */
    ClientePFResponse findByCpf(String cpf);
}
