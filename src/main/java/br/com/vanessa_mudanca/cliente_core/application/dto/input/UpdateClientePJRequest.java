package br.com.vanessa_mudanca.cliente_core.application.dto.input;

import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO para atualização de Cliente Pessoa Jurídica.
 *
 * PADRÃO: Aggregate Update (DDD)
 * - Single endpoint para atualizar cliente + documentos + endereços + contatos
 * - Atualização SELETIVA: apenas campos/entidades presentes serão atualizados
 * - Null/ausente = não atualizar (idempotente)
 *
 * IMUTABILIDADE:
 * - CNPJ: NÃO incluído (imutável após criação)
 *
 * USO:
 * PUT /v1/clientes/pj/{publicId}
 * - publicId do path tem precedência sobre body (segurança)
 */
@Builder
public record UpdateClientePJRequest(
        // ========== ID (usado apenas para segurança, path tem precedência) ==========
        UUID publicId,

        // ========== Dados Básicos da Empresa ==========
        String razaoSocial,
        String nomeFantasia,
        String inscricaoEstadual,
        String inscricaoMunicipal,
        LocalDate dataAbertura,
        @Email String email,

        // ========== Classificação da Empresa ==========
        String porteEmpresa,              // Ex: MEI, ME, EPP, Grande Porte
        String naturezaJuridica,          // Ex: LTDA, SA, EIRELI
        String atividadePrincipal,        // CNAE principal
        BigDecimal capitalSocial,

        // ========== Dados do Responsável Legal ==========
        String nomeResponsavel,
        String cpfResponsavel,            // CPF do responsável (não muda)
        String cargoResponsavel,          // Ex: Sócio, Diretor, Presidente

        // ========== Informações Adicionais ==========
        String site,
        TipoClienteEnum tipoCliente,      // CONSIGNANTE, COMPRADOR, LOCATARIO, etc.
        String observacoes,

        // ========== Entidades Relacionadas (Update Seletivo) ==========
        @Valid List<UpdateDocumentoDTO> documentos,
        @Valid List<UpdateEnderecoDTO> enderecos,
        @Valid List<UpdateContatoDTO> contatos
) {
    /**
     * Verifica se há dados básicos da empresa para atualizar.
     */
    public boolean temDadosBasicosParaAtualizar() {
        return razaoSocial != null
                || nomeFantasia != null
                || inscricaoEstadual != null
                || inscricaoMunicipal != null
                || dataAbertura != null
                || email != null;
    }

    /**
     * Verifica se há dados de classificação para atualizar.
     */
    public boolean temDadosClassificacaoParaAtualizar() {
        return porteEmpresa != null
                || naturezaJuridica != null
                || atividadePrincipal != null
                || capitalSocial != null;
    }

    /**
     * Verifica se há dados do responsável para atualizar.
     */
    public boolean temDadosResponsavelParaAtualizar() {
        return nomeResponsavel != null
                || cpfResponsavel != null
                || cargoResponsavel != null;
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
