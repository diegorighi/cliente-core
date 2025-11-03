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
}
