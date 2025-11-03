package br.com.vanessa_mudanca.cliente_core.application.dto.input;

import br.com.vanessa_mudanca.cliente_core.domain.enums.SexoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

/**
 * DTO unificado para atualização de Cliente Pessoa Física.
 *
 * Permite atualizar em uma única requisição:
 * - Dados básicos do cliente (nome, email, etc)
 * - Lista de documentos (updates parciais)
 * - Lista de endereços (updates parciais)
 * - Lista de contatos (updates parciais)
 *
 * CAMPOS IMUTÁVEIS DO CLIENTE (não presentes):
 * - cpf (identificador único)
 * - dataNascimento (impacta documentos legais)
 *
 * CAMPOS MUTÁVEIS:
 * - Nome completo (casamento, correções)
 * - Email, RG, sexo
 * - Dados complementares (profissão, estado civil, etc)
 * - TipoCliente (evolução: PROSPECTO -> COMPRADOR)
 *
 * PATTERN: Selective Update
 * - Campos null = não atualizar
 * - Campos presentes = atualizar com novo valor
 * - Listas vazias = não atualizar nada
 * - Listas com itens = atualizar apenas os itens da lista (by ID)
 */
@Builder
public record UpdateClientePFRequest(

        // ========== IDENTIFICAÇÃO ==========
        UUID publicId, // Usado apenas para validação/roteamento

        // ========== DADOS BÁSICOS (MUTÁVEIS) ==========
        String primeiroNome,

        String nomeDoMeio,

        String sobrenome,

        String rg,

        SexoEnum sexo,

        @Email(message = "Email deve ser válido")
        String email,

        // ========== DADOS COMPLEMENTARES ==========
        String nomeMae,

        String nomePai,

        String estadoCivil,

        String profissao,

        String nacionalidade,

        String naturalidade,

        TipoClienteEnum tipoCliente,

        String observacoes,

        // ========== UPDATES DE ENTIDADES RELACIONADAS ==========

        /**
         * Lista de documentos a atualizar.
         * Apenas os documentos com ID especificado serão atualizados.
         * Documentos não incluídos permanecem inalterados.
         */
        @Valid
        List<UpdateDocumentoDTO> documentos,

        /**
         * Lista de endereços a atualizar.
         * Apenas os endereços com ID especificado serão atualizados.
         * Endereços não incluídos permanecem inalterados.
         */
        @Valid
        List<UpdateEnderecoDTO> enderecos,

        /**
         * Lista de contatos a atualizar.
         * Apenas os contatos com ID especificado serão atualizados.
         * Contatos não incluídos permanecem inalterados.
         */
        @Valid
        List<UpdateContatoDTO> contatos

) {

    /**
     * Verifica se há algum dado básico do cliente para atualizar.
     */
    public boolean temDadosBasicosParaAtualizar() {
        return primeiroNome != null ||
                nomeDoMeio != null ||
                sobrenome != null ||
                rg != null ||
                sexo != null ||
                email != null ||
                nomeMae != null ||
                nomePai != null ||
                estadoCivil != null ||
                profissao != null ||
                nacionalidade != null ||
                naturalidade != null ||
                tipoCliente != null ||
                observacoes != null;
    }

    /**
     * Verifica se há documentos para atualizar.
     */
    public boolean temDocumentosParaAtualizar() {
        return documentos != null && !documentos.isEmpty();
    }

    /**
     * Verifica se há endereços para atualizar.
     */
    public boolean temEnderecosParaAtualizar() {
        return enderecos != null && !enderecos.isEmpty();
    }

    /**
     * Verifica se há contatos para atualizar.
     */
    public boolean temContatosParaAtualizar() {
        return contatos != null && !contatos.isEmpty();
    }
}
