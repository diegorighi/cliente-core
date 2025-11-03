package br.com.vanessa_mudanca.cliente_core.application.ports.input;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;

/**
 * Port de entrada (Use Case) para criação de Cliente Pessoa Física.
 * Define o contrato para a funcionalidade de criar um cliente PF.
 */
public interface CreateClientePFUseCase {

    /**
     * Cria um novo cliente pessoa física.
     *
     * @param request dados para criação do cliente PF
     * @return cliente PF criado
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.CpfJaCadastradoException se CPF já existe
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.CpfInvalidoException se CPF é inválido
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteIndicadorNaoEncontradoException se cliente indicador não existe
     */
    ClientePFResponse criar(CreateClientePFRequest request);
}
