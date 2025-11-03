package br.com.vanessa_mudanca.cliente_core.domain.fixture;

import br.com.vanessa_mudanca.cliente_core.domain.entity.Documento;
import br.com.vanessa_mudanca.cliente_core.domain.enums.StatusDocumentoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoDocumentoEnum;

import java.time.LocalDate;

/**
 * Fixture para criação de objetos Documento em testes.
 * Fornece dados realistas e consistentes para cenários de teste.
 */
public record DocumentoFixture(
        Long id,
        TipoDocumentoEnum tipoDocumento,
        String numero,
        String orgaoEmissor,
        LocalDate dataEmissao,
        LocalDate dataValidade,
        String observacoes,
        StatusDocumentoEnum statusDocumento,
        Boolean documentoPrincipal,
        Boolean ativo
) {

    /**
     * CPF válido e verificado (documento principal).
     */
    public static DocumentoFixture cpfValido() {
        return new DocumentoFixture(
                1L,
                TipoDocumentoEnum.CPF,
                "12345678909",
                "Receita Federal do Brasil",
                LocalDate.of(2020, 1, 15),
                null, // CPF não expira
                "CPF verificado",
                StatusDocumentoEnum.VERIFICADO,
                true,
                true
        );
    }

    /**
     * RG expirado (precisa renovação).
     */
    public static DocumentoFixture rgExpirado() {
        return new DocumentoFixture(
                2L,
                TipoDocumentoEnum.RG,
                "MG-12.345.678",
                "SSP-MG",
                LocalDate.of(2015, 1, 1),
                LocalDate.of(2020, 1, 1), // Expirado
                "RG antigo - precisa renovação",
                StatusDocumentoEnum.EXPIRADO,
                false,
                true
        );
    }

    /**
     * CNH válida aguardando verificação.
     */
    public static DocumentoFixture cnhPendente() {
        return new DocumentoFixture(
                3L,
                TipoDocumentoEnum.CNH,
                "12345678901",
                "DETRAN-MG",
                LocalDate.of(2023, 6, 1),
                LocalDate.of(2028, 6, 1),
                "CNH categoria B",
                StatusDocumentoEnum.AGUARDANDO_VERIFICACAO,
                false,
                true
        );
    }

    /**
     * CNPJ verificado (empresa ativa).
     */
    public static DocumentoFixture cnpjVerificado() {
        return new DocumentoFixture(
                4L,
                TipoDocumentoEnum.CNPJ,
                "11222333000181",
                "Receita Federal do Brasil",
                LocalDate.of(2010, 3, 10),
                null, // CNPJ não expira
                "CNPJ ativo",
                StatusDocumentoEnum.VERIFICADO,
                true,
                true
        );
    }

    /**
     * Passaporte válido.
     */
    public static DocumentoFixture passaporteValido() {
        return new DocumentoFixture(
                5L,
                TipoDocumentoEnum.PASSAPORTE,
                "FA123456",
                "Polícia Federal",
                LocalDate.of(2020, 8, 15),
                LocalDate.of(2030, 8, 15),
                "Passaporte brasileiro",
                StatusDocumentoEnum.VERIFICADO,
                false,
                true
        );
    }

    /**
     * Converte para entidade Documento.
     */
    public Documento toEntity() {
        return Documento.builder()
                .id(id)
                .tipoDocumento(tipoDocumento)
                .numero(numero)
                .orgaoEmissor(orgaoEmissor)
                .dataEmissao(dataEmissao)
                .dataValidade(dataValidade)
                .observacoes(observacoes)
                .statusDocumento(statusDocumento)
                .documentoPrincipal(documentoPrincipal)
                .ativo(ativo)
                .build();
    }

    /**
     * Cria um builder customizado a partir deste fixture.
     */
    public DocumentoBuilder toBuilder() {
        return new DocumentoBuilder(this);
    }

    /**
     * Builder para customização de fixtures.
     */
    public static class DocumentoBuilder {
        private Long id;
        private TipoDocumentoEnum tipoDocumento;
        private String numero;
        private String orgaoEmissor;
        private LocalDate dataEmissao;
        private LocalDate dataValidade;
        private String observacoes;
        private StatusDocumentoEnum statusDocumento;
        private Boolean documentoPrincipal;
        private Boolean ativo;

        public DocumentoBuilder(DocumentoFixture fixture) {
            this.id = fixture.id;
            this.tipoDocumento = fixture.tipoDocumento;
            this.numero = fixture.numero;
            this.orgaoEmissor = fixture.orgaoEmissor;
            this.dataEmissao = fixture.dataEmissao;
            this.dataValidade = fixture.dataValidade;
            this.observacoes = fixture.observacoes;
            this.statusDocumento = fixture.statusDocumento;
            this.documentoPrincipal = fixture.documentoPrincipal;
            this.ativo = fixture.ativo;
        }

        public DocumentoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DocumentoBuilder dataEmissao(LocalDate dataEmissao) {
            this.dataEmissao = dataEmissao;
            return this;
        }

        public DocumentoBuilder dataValidade(LocalDate dataValidade) {
            this.dataValidade = dataValidade;
            return this;
        }

        public DocumentoBuilder observacoes(String observacoes) {
            this.observacoes = observacoes;
            return this;
        }

        public DocumentoBuilder statusDocumento(StatusDocumentoEnum statusDocumento) {
            this.statusDocumento = statusDocumento;
            return this;
        }

        public DocumentoBuilder documentoPrincipal(Boolean documentoPrincipal) {
            this.documentoPrincipal = documentoPrincipal;
            return this;
        }

        public DocumentoFixture build() {
            return new DocumentoFixture(
                    id, tipoDocumento, numero, orgaoEmissor,
                    dataEmissao, dataValidade, observacoes,
                    statusDocumento, documentoPrincipal, ativo
            );
        }
    }
}
