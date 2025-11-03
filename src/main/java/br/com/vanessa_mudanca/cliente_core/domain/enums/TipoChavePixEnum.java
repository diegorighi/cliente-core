package br.com.vanessa_mudanca.cliente_core.domain.enums;

import lombok.Getter;

@Getter
public enum TipoChavePixEnum {
    CPF("CPF", "CPF"),
    CNPJ("CNPJ", "CNPJ"),
    EMAIL("EMAIL", "E-mail"),
    TELEFONE("TELEFONE", "Telefone"),
    ALEATORIA("ALEATORIA", "Chave Aleatória");

    private final String codigo;
    private final String descricao;

    TipoChavePixEnum(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static TipoChavePixEnum fromCodigo(String codigo) {
        for (TipoChavePixEnum tipo : values()) {
            if (tipo.getCodigo().equalsIgnoreCase(codigo)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Código de tipo de chave PIX inválido: " + codigo);
    }
}
