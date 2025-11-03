package br.com.vanessa_mudanca.cliente_core.domain.enums;

import lombok.Getter;

@Getter
public enum TipoDocumentoEnum {
    CPF("CPF", "Cadastro de Pessoa Física"),
    RG("RG", "Registro Geral"),
    CNH("CNH", "Carteira Nacional de Habilitação"),
    PASSAPORTE("PASSAPORTE", "Passaporte"),
    CNPJ("CNPJ", "Cadastro Nacional de Pessoa Jurídica"),
    INSCRICAO_ESTADUAL("IE", "Inscrição Estadual"),
    INSCRICAO_MUNICIPAL("IM", "Inscrição Municipal"),
    CERTIDAO_NASCIMENTO("CN", "Certidão de Nascimento"),
    TITULO_ELEITOR("TE", "Título de Eleitor"),
    CARTEIRA_TRABALHO("CT", "Carteira de Trabalho"),
    OUTRO("OUTRO", "Outro");

    private final String codigo;
    private final String descricao;

    TipoDocumentoEnum(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static TipoDocumentoEnum fromCodigo(String codigo) {
        for (TipoDocumentoEnum tipo : values()) {
            if (tipo.getCodigo().equalsIgnoreCase(codigo)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Código de tipo de documento inválido: " + codigo);
    }
}
