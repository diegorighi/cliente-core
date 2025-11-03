package br.com.vanessa_mudanca.cliente_core.application.ports.input;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.CreateClientePJRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;

/**
 * Port de entrada (Use Case) para criação de Cliente Pessoa Jurídica.
 * Define o contrato para a funcionalidade de criar um cliente PJ.
 */
public interface CreateClientePJUseCase {

    /**
     * Cria um novo cliente pessoa jurídica.
     *
     * @param request dados para criação do cliente PJ
     * @return cliente PJ criado
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.CnpjJaCadastradoException se CNPJ já existe
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.CnpjInvalidoException se CNPJ é inválido
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteIndicadorNaoEncontradoException se cliente indicador não existe
     */
    ClientePJResponse criar(CreateClientePJRequest request);
}
