package br.com.vanessa_mudanca.cliente_core.domain.entity;

import br.com.vanessa_mudanca.cliente_core.domain.enums.OrigemLeadEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "clientes")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    private UUID publicId;

    @Column(name = "email", length = 150)
    private String email;

    // Classificação
    @Enumerated(EnumType.STRING)
    @Column(insertable = false, updatable = false, name = "tipo_cliente", length = 20)
    private TipoClienteEnum tipoCliente;

    // Marketing
    @Enumerated(EnumType.STRING)
    @Column(name = "origem_lead", length = 30)
    private OrigemLeadEnum origemLead;

    @Column(name = "utm_source", length = 100)
    private String utmSource;

    @Column(name = "utm_campaign", length = 100)
    private String utmCampaign;

    @Column(name = "utm_medium", length = 100)
    private String utmMedium;

    // Relacionamento (indicação)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_indicador_id")
    private Cliente clienteIndicador;

    @Column(name = "data_indicacao")
    private LocalDateTime dataIndicacao;

    @Column(name = "indicacao_recompensada")
    private Boolean indicacaoRecompensada;

    // Métricas básicas
    @Column(name = "total_compras_realizadas")
    private Integer totalComprasRealizadas;

    @Column(name = "total_vendas_realizadas")
    private Integer totalVendasRealizadas;

    @Column(name = "valor_total_comprado")
    private BigDecimal valorTotalComprado;

    @Column(name = "valor_total_vendido")
    private BigDecimal valorTotalVendido;

    @Column(name = "data_primeira_transacao")
    private LocalDateTime dataPrimeiraTransacao;

    @Column(name = "data_ultima_transacao")
    private LocalDateTime dataUltimaTransacao;

    // Bloqueio/Segurança
    @Column(name = "bloqueado")
    @Builder.Default
    private Boolean bloqueado = false;

    @Column(name = "motivo_bloqueio", length = 500)
    private String motivoBloqueio;

    @Column(name = "data_bloqueio")
    private LocalDateTime dataBloqueio;

    @Column(name = "usuario_bloqueou", length = 100)
    private String usuarioBloqueou;

    // Soft Delete
    @Column(name = "data_delecao")
    private LocalDateTime dataDelecao;

    @Column(name = "motivo_delecao", length = 500)
    private String motivoDelecao;

    @Column(name = "usuario_deletou", length = 100)
    private String usuarioDeletou;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @Builder.Default
    private List<Documento> listaDocumentos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @Builder.Default
    private List<Contato> listaContatos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @Builder.Default
    private List<Endereco> listaEnderecos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DadosBancarios> listaDadosBancarios = new ArrayList<>();

    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private PreferenciaCliente preferenciaCliente;

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    @Column(name = "ativo")
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @PrePersist
    protected void onCreate() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Métodos de Soft Delete
    public void deletar(String motivo, String usuario) {
        this.ativo = false;
        this.dataDelecao = LocalDateTime.now();
        this.motivoDelecao = motivo;
        this.usuarioDeletou = usuario;

        // Cliente deletado também deve ser bloqueado
        this.bloqueado = true;
        this.motivoBloqueio = "Cliente deletado: " + motivo;
        this.dataBloqueio = LocalDateTime.now();
        this.usuarioBloqueou = usuario;
    }

    public void restaurar(String usuario) {
        this.ativo = true;
        this.dataDelecao = null;
        this.motivoDelecao = null;
        this.usuarioDeletou = null;

        // Limpa bloqueio associado à deleção (apenas se motivo for "Cliente deletado:")
        if (this.motivoBloqueio != null && this.motivoBloqueio.startsWith("Cliente deletado:")) {
            this.bloqueado = false;
            this.motivoBloqueio = null;
            this.dataBloqueio = null;
            this.usuarioBloqueou = null;
        }
    }

    public boolean isDeletado() {
        return !this.ativo && this.dataDelecao != null;
    }

    // Métodos de Bloqueio
    public void bloquear(String motivo, String usuario) {
        this.bloqueado = true;
        this.motivoBloqueio = motivo;
        this.dataBloqueio = LocalDateTime.now();
        this.usuarioBloqueou = usuario;
    }

    public void desbloquear() {
        this.bloqueado = false;
        this.motivoBloqueio = null;
        this.dataBloqueio = null;
        this.usuarioBloqueou = null;
    }

    public boolean isBloqueado() {
        return this.bloqueado != null && this.bloqueado;
    }

    // Métodos auxiliares para gerenciar listas
    public void adicionarDocumento(Documento documento) {
        this.listaDocumentos.add(documento);
    }

    public void removerDocumento(Documento documento) {
        this.listaDocumentos.remove(documento);
    }

    public void adicionarContato(Contato contato) {
        this.listaContatos.add(contato);
    }

    public void removerContato(Contato contato) {
        this.listaContatos.remove(contato);
    }

    public void adicionarEndereco(Endereco endereco) {
        this.listaEnderecos.add(endereco);
    }

    public void removerEndereco(Endereco endereco) {
        this.listaEnderecos.remove(endereco);
    }

    public void adicionarDadosBancarios(DadosBancarios dados) {
        this.listaDadosBancarios.add(dados);
        dados.setCliente(this);
    }

    public void removerDadosBancarios(DadosBancarios dados) {
        this.listaDadosBancarios.remove(dados);
        dados.setCliente(null);
    }
}
