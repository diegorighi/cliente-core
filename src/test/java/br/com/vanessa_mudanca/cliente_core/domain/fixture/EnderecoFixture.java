package br.com.vanessa_mudanca.cliente_core.domain.fixture;

import br.com.vanessa_mudanca.cliente_core.domain.entity.Endereco;
import br.com.vanessa_mudanca.cliente_core.domain.enums.EstadoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoEnderecoEnum;

/**
 * Fixture para criação de objetos Endereco em testes.
 * Fornece endereços realistas de Belo Horizonte e região.
 */
public record EnderecoFixture(
        Long id,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        EstadoEnum estado,
        String pais,
        TipoEnderecoEnum tipoEndereco,
        Boolean enderecoPrincipal,
        Boolean ativo
) {

    /**
     * Endereço residencial principal (Centro de BH).
     */
    public static EnderecoFixture residencialPrincipal() {
        return new EnderecoFixture(
                1L,
                "30130-100",
                "Rua da Bahia",
                "1234",
                "Apto 501",
                "Centro",
                "Belo Horizonte",
                EstadoEnum.MG,
                "Brasil",
                TipoEnderecoEnum.RESIDENCIAL,
                true,
                true
        );
    }

    /**
     * Endereço comercial secundário (Savassi).
     */
    public static EnderecoFixture comercialSecundario() {
        return new EnderecoFixture(
                2L,
                "30140-071",
                "Av. Getúlio Vargas",
                "1500",
                "Sala 1205",
                "Savassi",
                "Belo Horizonte",
                EstadoEnum.MG,
                "Brasil",
                TipoEnderecoEnum.COMERCIAL,
                false,
                true
        );
    }

    /**
     * Endereço de entrega (bairro nobre).
     */
    public static EnderecoFixture entregaNobre() {
        return new EnderecoFixture(
                3L,
                "30360-000",
                "Av. Raja Gabaglia",
                "3000",
                "Cobertura",
                "Luxemburgo",
                "Belo Horizonte",
                EstadoEnum.MG,
                "Brasil",
                TipoEnderecoEnum.ENTREGA,
                true,
                true
        );
    }

    /**
     * Endereço de cobrança (mesmo que residencial).
     */
    public static EnderecoFixture cobranca() {
        return new EnderecoFixture(
                4L,
                "30130-100",
                "Rua da Bahia",
                "1234",
                "Apto 501",
                "Centro",
                "Belo Horizonte",
                EstadoEnum.MG,
                "Brasil",
                TipoEnderecoEnum.COBRANCA,
                true,
                true
        );
    }

    /**
     * Endereço rural (Zona Rural).
     */
    public static EnderecoFixture rural() {
        return new EnderecoFixture(
                5L,
                "35530-000",
                "Zona Rural",
                "S/N",
                "Fazenda Santa Clara",
                "Zona Rural",
                "Nova Lima",
                EstadoEnum.MG,
                "Brasil",
                TipoEnderecoEnum.RESIDENCIAL,
                false,
                true
        );
    }

    /**
     * Endereço inativo (mudança antiga).
     */
    public static EnderecoFixture inativo() {
        return new EnderecoFixture(
                6L,
                "30110-000",
                "Rua dos Carijós",
                "100",
                null,
                "Centro",
                "Belo Horizonte",
                EstadoEnum.MG,
                "Brasil",
                TipoEnderecoEnum.RESIDENCIAL,
                false,
                false // Inativo
        );
    }

    /**
     * Converte para entidade Endereco.
     */
    public Endereco toEntity() {
        return Endereco.builder()
                .id(id)
                .cep(cep)
                .logradouro(logradouro)
                .numero(numero)
                .complemento(complemento)
                .bairro(bairro)
                .cidade(cidade)
                .estado(estado)
                .pais(pais)
                .tipoEndereco(tipoEndereco)
                .enderecoPrincipal(enderecoPrincipal)
                .ativo(ativo)
                .build();
    }

    /**
     * Cria um builder customizado a partir deste fixture.
     */
    public EnderecoBuilder toBuilder() {
        return new EnderecoBuilder(this);
    }

    /**
     * Builder para customização de fixtures.
     */
    public static class EnderecoBuilder {
        private Long id;
        private String cep;
        private String logradouro;
        private String numero;
        private String complemento;
        private String bairro;
        private String cidade;
        private EstadoEnum estado;
        private String pais;
        private TipoEnderecoEnum tipoEndereco;
        private Boolean enderecoPrincipal;
        private Boolean ativo;

        public EnderecoBuilder(EnderecoFixture fixture) {
            this.id = fixture.id;
            this.cep = fixture.cep;
            this.logradouro = fixture.logradouro;
            this.numero = fixture.numero;
            this.complemento = fixture.complemento;
            this.bairro = fixture.bairro;
            this.cidade = fixture.cidade;
            this.estado = fixture.estado;
            this.pais = fixture.pais;
            this.tipoEndereco = fixture.tipoEndereco;
            this.enderecoPrincipal = fixture.enderecoPrincipal;
            this.ativo = fixture.ativo;
        }

        public EnderecoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public EnderecoBuilder cep(String cep) {
            this.cep = cep;
            return this;
        }

        public EnderecoBuilder logradouro(String logradouro) {
            this.logradouro = logradouro;
            return this;
        }

        public EnderecoBuilder numero(String numero) {
            this.numero = numero;
            return this;
        }

        public EnderecoBuilder complemento(String complemento) {
            this.complemento = complemento;
            return this;
        }

        public EnderecoBuilder enderecoPrincipal(Boolean enderecoPrincipal) {
            this.enderecoPrincipal = enderecoPrincipal;
            return this;
        }

        public EnderecoBuilder ativo(Boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public EnderecoFixture build() {
            return new EnderecoFixture(
                    id, cep, logradouro, numero, complemento,
                    bairro, cidade, estado, pais, tipoEndereco,
                    enderecoPrincipal, ativo
            );
        }
    }
}
