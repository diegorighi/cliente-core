package br.com.vanessa_mudanca.cliente_core.domain.entity;

import br.com.vanessa_mudanca.cliente_core.domain.enums.EstadoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoEnderecoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "enderecos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cep", length = 9, nullable = false)
    private String cep;

    @Column(name = "logradouro", length = 200, nullable = false)
    private String logradouro;

    @Column(name = "numero", length = 10)
    private String numero;

    @Column(name = "complemento", length = 100)
    private String complemento;

    @Column(name = "bairro", length = 100, nullable = false)
    private String bairro;

    @Column(name = "cidade", length = 100, nullable = false)
    private String cidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 2, nullable = false)
    private EstadoEnum estado;

    @Column(name = "pais", length = 50)
    @Builder.Default
    private String pais = "Brasil";

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_endereco", length = 20)
    private TipoEnderecoEnum tipoEndereco;

    @Column(name = "endereco_principal")
    @Builder.Default
    private Boolean enderecoPrincipal = false;

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
     * Atualiza todos os dados do endereço.
     */
    public void atualizarDadosEndereco(
            String novoCep,
            String novoLogradouro,
            String novoNumero,
            String novoComplemento,
            String novoBairro,
            String novaCidade,
            EstadoEnum novoEstado,
            String novoPais
    ) {
        this.cep = novoCep;
        this.logradouro = novoLogradouro;
        this.numero = novoNumero;
        this.complemento = novoComplemento;
        this.bairro = novoBairro;
        this.cidade = novaCidade;
        this.estado = novoEstado;
        if (novoPais != null) {
            this.pais = novoPais;
        }
    }

    /**
     * Marca este endereço como principal.
     * NOTA: Service deve garantir que apenas 1 endereço é principal por tipo.
     */
    public void marcarComoPrincipal() {
        this.enderecoPrincipal = true;
    }

    /**
     * Remove flag de endereço principal.
     */
    public void removerFlagPrincipal() {
        this.enderecoPrincipal = false;
    }
}
