package br.com.vanessa_mudanca.cliente_core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "clientes_pj")
@DiscriminatorValue("PJ")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClientePJ extends Cliente {

    @Column(name = "razao_social", nullable = false, length = 200)
    private String razaoSocial;

    @Column(name = "nome_fantasia", length = 200)
    private String nomeFantasia;

    @Column(name = "cnpj", unique = true, nullable = false, length = 18)
    private String cnpj;

    @Column(name = "inscricao_estadual", length = 20)
    private String inscricaoEstadual;

    @Column(name = "inscricao_municipal", length = 20)
    private String inscricaoMunicipal;

    @Column(name = "data_abertura")
    private LocalDate dataAbertura;

    @Column(name = "porte_empresa", length = 50)
    private String porteEmpresa;

    @Column(name = "natureza_juridica", length = 100)
    private String naturezaJuridica;

    @Column(name = "atividade_principal", length = 200)
    private String atividadePrincipal;

    @Column(name = "capital_social", precision = 15, scale = 2)
    private BigDecimal capitalSocial;

    @Column(name = "nome_responsavel", length = 200)
    private String nomeResponsavel;

    @Column(name = "cpf_responsavel", length = 14)
    private String cpfResponsavel;

    @Column(name = "cargo_responsavel", length = 100)
    private String cargoResponsavel;

    @Column(name = "site", length = 200)
    private String site;

    // Método auxiliar para obter nome de exibição
    public String getNomeExibicao() {
        if (nomeFantasia != null && !nomeFantasia.isEmpty()) {
            return nomeFantasia;
        }
        return razaoSocial;
    }

    // ========== MÉTODOS COMPORTAMENTAIS (Tell, Don't Ask) ==========

    /**
     * Atualiza dados básicos da empresa.
     * COMPORTAMENTO: Update seletivo (null = não atualizar).
     */
    public void atualizarDadosBasicos(
            String novaRazaoSocial,
            String novoNomeFantasia,
            String novaInscricaoEstadual,
            String novaInscricaoMunicipal,
            LocalDate novaDataAbertura,
            String novoEmail
    ) {
        if (novaRazaoSocial != null) {
            this.razaoSocial = novaRazaoSocial;
        }
        if (novoNomeFantasia != null) {
            this.nomeFantasia = novoNomeFantasia;
        }
        if (novaInscricaoEstadual != null) {
            this.inscricaoEstadual = novaInscricaoEstadual;
        }
        if (novaInscricaoMunicipal != null) {
            this.inscricaoMunicipal = novaInscricaoMunicipal;
        }
        if (novaDataAbertura != null) {
            this.dataAbertura = novaDataAbertura;
        }
        if (novoEmail != null) {
            this.setEmail(novoEmail);
        }
    }

    /**
     * Atualiza dados de classificação da empresa.
     * COMPORTAMENTO: Update seletivo (null = não atualizar).
     */
    public void atualizarDadosClassificacao(
            String novoPorteEmpresa,
            String novaNaturezaJuridica,
            String novaAtividadePrincipal,
            BigDecimal novoCapitalSocial
    ) {
        if (novoPorteEmpresa != null) {
            this.porteEmpresa = novoPorteEmpresa;
        }
        if (novaNaturezaJuridica != null) {
            this.naturezaJuridica = novaNaturezaJuridica;
        }
        if (novaAtividadePrincipal != null) {
            this.atividadePrincipal = novaAtividadePrincipal;
        }
        if (novoCapitalSocial != null) {
            this.capitalSocial = novoCapitalSocial;
        }
    }

    /**
     * Atualiza dados do responsável legal da empresa.
     * COMPORTAMENTO: Update seletivo (null = não atualizar).
     */
    public void atualizarDadosResponsavel(
            String novoNomeResponsavel,
            String novoCpfResponsavel,
            String novoCargoResponsavel
    ) {
        if (novoNomeResponsavel != null) {
            this.nomeResponsavel = novoNomeResponsavel;
        }
        if (novoCpfResponsavel != null) {
            this.cpfResponsavel = novoCpfResponsavel;
        }
        if (novoCargoResponsavel != null) {
            this.cargoResponsavel = novoCargoResponsavel;
        }
    }
}
