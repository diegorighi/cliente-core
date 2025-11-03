package br.com.vanessa_mudanca.cliente_core.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "preferencias_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenciaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;

    @Column(name = "aceita_comunicacao_email")
    @Builder.Default
    private Boolean aceitaComunicacaoEmail = true;

    @Column(name = "aceita_comunicacao_sms")
    @Builder.Default
    private Boolean aceitaComunicacaoSMS = true;

    @Column(name = "aceita_comunicacao_whatsapp")
    @Builder.Default
    private Boolean aceitaComunicacaoWhatsApp = true;

    @Column(name = "aceita_comunicacao_telefone")
    @Builder.Default
    private Boolean aceitaComunicacaoTelefone = false;

    @Column(name = "aceita_newsletters")
    @Builder.Default
    private Boolean aceitaNewsletters = false;

    @Column(name = "aceita_ofertas")
    @Builder.Default
    private Boolean aceitaOfertas = true;

    @Column(name = "aceita_pesquisas")
    @Builder.Default
    private Boolean aceitaPesquisas = false;

    @Column(name = "data_consentimento")
    private LocalDateTime dataConsentimento;

    @Column(name = "ip_consentimento", length = 45)
    private String ipConsentimento;

    @Column(name = "consentimento_ativo")
    @Builder.Default
    private Boolean consentimentoAtivo = true;

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
    }
}
