package br.com.vanessa_mudanca.cliente_core.domain.entity;

import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoContatoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contatos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contato", nullable = false, length = 20)
    private TipoContatoEnum tipoContato;

    @Column(name = "valor", nullable = false, length = 100)
    private String valor;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Column(name = "contato_principal")
    @Builder.Default
    private Boolean contatoPrincipal = false;

    @Column(name = "verificado")
    @Builder.Default
    private Boolean verificado = false;

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
