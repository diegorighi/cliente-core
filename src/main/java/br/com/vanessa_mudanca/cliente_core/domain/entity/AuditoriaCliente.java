package br.com.vanessa_mudanca.cliente_core.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "campo_alterado", length = 100, nullable = false)
    private String campoAlterado;

    @Column(name = "valor_anterior", length = 500)
    private String valorAnterior;

    @Column(name = "valor_novo", length = 500)
    private String valorNovo;

    @Column(name = "usuario_responsavel", length = 100)
    private String usuarioResponsavel;

    @Column(name = "data_alteracao", nullable = false)
    private LocalDateTime dataAlteracao;

    @Column(name = "motivo_alteracao", length = 500)
    private String motivoAlteracao;

    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
        if (this.dataAlteracao == null) {
            this.dataAlteracao = LocalDateTime.now();
        }
    }
}
