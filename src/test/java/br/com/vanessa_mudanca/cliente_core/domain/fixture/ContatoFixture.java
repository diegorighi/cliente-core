package br.com.vanessa_mudanca.cliente_core.domain.fixture;

import br.com.vanessa_mudanca.cliente_core.domain.entity.Contato;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoContatoEnum;

/**
 * Fixture para criação de objetos Contato em testes.
 * Fornece contatos realistas com diferentes tipos e status.
 */
public record ContatoFixture(
        Long id,
        TipoContatoEnum tipoContato,
        String valor,
        String observacoes,
        Boolean contatoPrincipal,
        Boolean verificado,
        Boolean ativo
) {

    /**
     * Celular principal verificado.
     */
    public static ContatoFixture celularPrincipal() {
        return new ContatoFixture(
                1L,
                TipoContatoEnum.CELULAR,
                "(31) 98765-4321",
                "WhatsApp disponível",
                true,
                true,
                true
        );
    }

    /**
     * Email secundário não verificado.
     */
    public static ContatoFixture emailSecundario() {
        return new ContatoFixture(
                2L,
                TipoContatoEnum.EMAIL,
                "contato@example.com",
                "Email alternativo",
                false,
                false,
                true
        );
    }

    /**
     * Telefone fixo residencial.
     */
    public static ContatoFixture telefoneFixo() {
        return new ContatoFixture(
                3L,
                TipoContatoEnum.TELEFONE_FIXO,
                "(31) 3333-4444",
                "Residência",
                false,
                true,
                true
        );
    }

    /**
     * Email principal verificado.
     */
    public static ContatoFixture emailPrincipal() {
        return new ContatoFixture(
                4L,
                TipoContatoEnum.EMAIL,
                "principal@example.com",
                "Email principal para comunicações",
                true,
                true,
                true
        );
    }

    /**
     * WhatsApp comercial.
     */
    public static ContatoFixture whatsappComercial() {
        return new ContatoFixture(
                5L,
                TipoContatoEnum.WHATSAPP,
                "(31) 99999-8888",
                "WhatsApp Business",
                false,
                true,
                true
        );
    }

    /**
     * Telefone comercial.
     */
    public static ContatoFixture telefoneComercial() {
        return new ContatoFixture(
                6L,
                TipoContatoEnum.TELEFONE_FIXO,
                "(31) 3500-1234",
                "Escritório - ramal 105",
                false,
                true,
                true
        );
    }

    /**
     * Contato inativo (número antigo).
     */
    public static ContatoFixture inativo() {
        return new ContatoFixture(
                7L,
                TipoContatoEnum.CELULAR,
                "(31) 99999-0000",
                "Número antigo - não usar",
                false,
                true,
                false // Inativo
        );
    }

    /**
     * Converte para entidade Contato.
     */
    public Contato toEntity() {
        return Contato.builder()
                .id(id)
                .tipoContato(tipoContato)
                .valor(valor)
                .observacoes(observacoes)
                .contatoPrincipal(contatoPrincipal)
                .verificado(verificado)
                .ativo(ativo)
                .build();
    }

    /**
     * Cria um builder customizado a partir deste fixture.
     */
    public ContatoBuilder toBuilder() {
        return new ContatoBuilder(this);
    }

    /**
     * Builder para customização de fixtures.
     */
    public static class ContatoBuilder {
        private Long id;
        private TipoContatoEnum tipoContato;
        private String valor;
        private String observacoes;
        private Boolean contatoPrincipal;
        private Boolean verificado;
        private Boolean ativo;

        public ContatoBuilder(ContatoFixture fixture) {
            this.id = fixture.id;
            this.tipoContato = fixture.tipoContato;
            this.valor = fixture.valor;
            this.observacoes = fixture.observacoes;
            this.contatoPrincipal = fixture.contatoPrincipal;
            this.verificado = fixture.verificado;
            this.ativo = fixture.ativo;
        }

        public ContatoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ContatoBuilder tipoContato(TipoContatoEnum tipoContato) {
            this.tipoContato = tipoContato;
            return this;
        }

        public ContatoBuilder valor(String valor) {
            this.valor = valor;
            return this;
        }

        public ContatoBuilder observacoes(String observacoes) {
            this.observacoes = observacoes;
            return this;
        }

        public ContatoBuilder contatoPrincipal(Boolean contatoPrincipal) {
            this.contatoPrincipal = contatoPrincipal;
            return this;
        }

        public ContatoBuilder verificado(Boolean verificado) {
            this.verificado = verificado;
            return this;
        }

        public ContatoBuilder ativo(Boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public ContatoFixture build() {
            return new ContatoFixture(
                    id, tipoContato, valor, observacoes,
                    contatoPrincipal, verificado, ativo
            );
        }
    }
}
