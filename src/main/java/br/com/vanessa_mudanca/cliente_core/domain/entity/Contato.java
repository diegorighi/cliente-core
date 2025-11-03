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
    }

    // ========== MÉTODOS COMPORTAMENTAIS (Tell, Don't Ask) ==========

    /**
     * Atualiza valor do contato.
     * COMPORTAMENTO: Invalida verificação se valor mudou.
     */
    public void atualizarValor(String novoValor) {
        if (!this.valor.equals(novoValor)) {
            this.valor = novoValor;
            this.verificado = false; // Precisa re-verificar
        }
    }

    /**
     * Atualiza tipo do contato.
     * COMPORTAMENTO: Invalida verificação se tipo mudou.
     */
    public void atualizarTipo(TipoContatoEnum novoTipo) {
        if (this.tipoContato != novoTipo) {
            this.tipoContato = novoTipo;
            this.verificado = false; // Mudou tipo, re-verificar
        }
    }

    /**
     * Atualiza observações do contato.
     */
    public void atualizarObservacoes(String novasObservacoes) {
        this.observacoes = novasObservacoes;
    }

    /**
     * Marca este contato como principal.
     * NOTA: Service deve garantir que apenas 1 contato é principal por cliente.
     */
    public void marcarComoPrincipal() {
        this.contatoPrincipal = true;
    }

    /**
     * Remove flag de contato principal.
     */
    public void removerFlagPrincipal() {
        this.contatoPrincipal = false;
    }

    /**
     * Marca contato como verificado.
     */
    public void marcarComoVerificado() {
        this.verificado = true;
    }
}
