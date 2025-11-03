package br.com.vanessa_mudanca.cliente_core.domain.entity;

import br.com.vanessa_mudanca.cliente_core.domain.enums.StatusDocumentoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoDocumentoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 30)
    private TipoDocumentoEnum tipoDocumento;

    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @Column(name = "orgao_emissor", length = 50)
    private String orgaoEmissor;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_documento", length = 30)
    @Builder.Default
    private StatusDocumentoEnum statusDocumento = StatusDocumentoEnum.AGUARDANDO_VERIFICACAO;

    @Column(name = "documento_principal")
    @Builder.Default
    private Boolean documentoPrincipal = false;

    @Column(name = "ativo")
    @Builder.Default
    private Boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
        if (isExpirado()) {
            this.statusDocumento = StatusDocumentoEnum.EXPIRADO;
        }
    }

    // Método auxiliar para verificar se o documento está expirado
    public boolean isExpirado() {
        if (dataValidade == null) {
            return false;
        }
        return LocalDate.now().isAfter(dataValidade);
    }

    // ========== MÉTODOS COMPORTAMENTAIS (Tell, Don't Ask) ==========

    /**
     * Atualiza datas de emissão e validade do documento.
     * COMPORTAMENTO:
     * - Recalcula status se documento expirou ou foi renovado
     * - Registra timestamp de atualização
     */
    public void atualizarDatasEEmissor(
            LocalDate novaDataEmissao,
            LocalDate novaDataValidade,
            String novoOrgaoEmissor
    ) {
        if (novaDataEmissao != null) {
            this.dataEmissao = novaDataEmissao;
        }

        if (novaDataValidade != null) {
            this.dataValidade = novaDataValidade;

            // Recalcular status baseado na nova data
            if (isExpirado()) {
                this.statusDocumento = StatusDocumentoEnum.EXPIRADO;
            } else if (this.statusDocumento == StatusDocumentoEnum.EXPIRADO) {
                // Documento foi renovado
                this.statusDocumento = StatusDocumentoEnum.AGUARDANDO_VERIFICACAO;
            }
        }

        if (novoOrgaoEmissor != null && !novoOrgaoEmissor.isBlank()) {
            this.orgaoEmissor = novoOrgaoEmissor;
        }
    }

    /**
     * Atualiza observações do documento.
     */
    public void atualizarObservacoes(String novasObservacoes) {
        this.observacoes = novasObservacoes;
    }

    /**
     * Marca este documento como principal.
     * NOTA: Service deve garantir que apenas 1 documento é principal por cliente.
     */
    public void marcarComoPrincipal() {
        this.documentoPrincipal = true;
    }

    /**
     * Remove flag de documento principal.
     */
    public void removerFlagPrincipal() {
        this.documentoPrincipal = false;
    }
}
