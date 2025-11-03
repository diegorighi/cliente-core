package br.com.vanessa_mudanca.cliente_core.domain.entity;

import br.com.vanessa_mudanca.cliente_core.domain.enums.SexoEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "clientes_pf")
@DiscriminatorValue("PF")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClientePF extends Cliente {

    @Column(name = "primeiro_nome", nullable = false, length = 100)
    private String primeiroNome;

    @Column(name = "nome_do_meio", length = 100)
    private String nomeDoMeio;

    @Column(name = "sobrenome", nullable = false, length = 100)
    private String sobrenome;

    @Column(name = "cpf", unique = true, length = 14)
    private String cpf;

    @Column(name = "rg", length = 20)
    private String rg;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", length = 20)
    private SexoEnum sexo;

    @Column(name = "nome_mae", length = 200)
    private String nomeMae;

    @Column(name = "nome_pai", length = 200)
    private String nomePai;

    @Column(name = "estado_civil", length = 30)
    private String estadoCivil;

    @Column(name = "profissao", length = 100)
    private String profissao;

    @Column(name = "nacionalidade", length = 50)
    @Builder.Default
    private String nacionalidade = "Brasileira";

    @Column(name = "naturalidade", length = 100)
    private String naturalidade;

    // Método auxiliar para obter nome completo
    public String getNomeCompleto() {
        StringBuilder nomeCompleto = new StringBuilder(primeiroNome);
        if (nomeDoMeio != null && !nomeDoMeio.isEmpty()) {
            nomeCompleto.append(" ").append(nomeDoMeio);
        }
        nomeCompleto.append(" ").append(sobrenome);
        return nomeCompleto.toString();
    }

    // Método auxiliar para calcular idade
    public Integer getIdade() {
        if (dataNascimento == null) {
            return null;
        }
        return LocalDate.now().getYear() - dataNascimento.getYear();
    }

    // ========== MÉTODOS COMPORTAMENTAIS (Tell, Don't Ask) ==========

    /**
     * Atualiza dados básicos do cliente PF.
     * CAMPOS IMUTÁVEIS (não incluídos): cpf, dataNascimento
     */
    public void atualizarDadosBasicos(
            String novoPrimeiroNome,
            String novoNomeDoMeio,
            String novoSobrenome,
            String novoRg,
            SexoEnum novoSexo,
            String novoEmail
    ) {
        if (novoPrimeiroNome != null) {
            this.primeiroNome = novoPrimeiroNome;
        }
        if (novoNomeDoMeio != null) {
            this.nomeDoMeio = novoNomeDoMeio;
        }
        if (novoSobrenome != null) {
            this.sobrenome = novoSobrenome;
        }
        if (novoRg != null) {
            this.rg = novoRg;
        }
        if (novoSexo != null) {
            this.sexo = novoSexo;
        }
        if (novoEmail != null) {
            this.setEmail(novoEmail); // Email está no Cliente (superclass)
        }
    }

    /**
     * Atualiza dados complementares do cliente PF.
     */
    public void atualizarDadosComplementares(
            String novoNomeMae,
            String novoNomePai,
            String novoEstadoCivil,
            String novaProfissao,
            String novaNacionalidade,
            String novaNaturalidade
    ) {
        if (novoNomeMae != null) {
            this.nomeMae = novoNomeMae;
        }
        if (novoNomePai != null) {
            this.nomePai = novoNomePai;
        }
        if (novoEstadoCivil != null) {
            this.estadoCivil = novoEstadoCivil;
        }
        if (novaProfissao != null) {
            this.profissao = novaProfissao;
        }
        if (novaNacionalidade != null) {
            this.nacionalidade = novaNacionalidade;
        }
        if (novaNaturalidade != null) {
            this.naturalidade = novaNaturalidade;
        }
    }
}
