package br.com.vanessa_mudanca.cliente_core.application.ports.input;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;

/**
 * Port de entrada (Use Case) para atualização de Cliente Pessoa Física.
 *
 * DESIGN: Single Aggregate Update
 * - Permite atualizar cliente + documentos + endereços + contatos em uma única requisição
 * - Updates são seletivos (apenas campos/itens presentes são atualizados)
 *
 * REGRAS DE NEGÓCIO:
 * - CPF é IMUTÁVEL (identificador do cliente)
 * - Data de nascimento é IMUTÁVEL (impacta idade e documentos legais)
 * - Nome, email, telefone são MUTÁVEIS
 * - TipoCliente pode mudar (PROSPECTO -> COMPRADOR -> CONSIGNANTE)
 * - Documentos: número imutável, datas mutáveis
 * - Endereços: todos campos mutáveis, apenas 1 principal por tipo
 * - Contatos: todos campos mutáveis, apenas 1 principal por cliente
 */
public interface UpdateClientePFUseCase {

    /**
     * Atualiza cliente PF e entidades relacionadas.
     *
     * COMPORTAMENTO:
     * - Atualiza dados básicos do cliente (se presentes no request)
     * - Atualiza documentos da lista (by ID, seletivo)
     * - Atualiza endereços da lista (by ID, seletivo)
     * - Atualiza contatos da lista (by ID, seletivo)
     *
     * @param request DTO com dados para atualização (null = não atualizar)
     * @return cliente atualizado com todas as entidades relacionadas
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException se cliente não existe
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.CampoImutavelException se tentar alterar CPF ou dataNascimento
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.DocumentoNaoEncontradoException se documento não existe
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.EnderecoNaoEncontradoException se endereço não existe
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ContatoNaoEncontradoException se contato não existe
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.DataValidadeInvalidaException se data de validade inválida
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.EnderecoPrincipalDuplicadoException se tentar criar 2 principais do mesmo tipo
     * @throws br.com.vanessa_mudanca.cliente_core.domain.exception.ContatoPrincipalDuplicadoException se tentar criar 2 principais
     */
    ClientePFResponse atualizar(UpdateClientePFRequest request);
}
