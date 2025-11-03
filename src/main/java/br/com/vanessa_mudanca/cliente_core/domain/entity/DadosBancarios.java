package br.com.vanessa_mudanca.cliente_core.domain.entity;

import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoChavePixEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dados_bancarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DadosBancarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "tipo_conta", length = 20)
    private String tipoConta;

    @Column(name = "banco", length = 100)
    private String banco;

    @Column(name = "agencia", length = 10)
    private String agencia;

    @Column(name = "conta", length = 20)
    private String conta;

    @Column(name = "digito_conta", length = 2)
    private String digitoConta;

    @Column(name = "chave_pix", length = 100)
    private String chavePix;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_chave_pix", length = 20)
    private TipoChavePixEnum tipoChavePix;

    @Column(name = "dados_verificados")
    @Builder.Default
    private Boolean dadosVerificados = false;

    @Column(name = "conta_principal")
    @Builder.Default
    private Boolean contaPrincipal = false;

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
    }
}
