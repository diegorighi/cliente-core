package br.com.vanessa_mudanca.cliente_core.domain.enums;

import lombok.Getter;

@Getter
public enum TipoEnderecoEnum {
    RESIDENCIAL("RESIDENCIAL", "Residencial"),
    COMERCIAL("COMERCIAL", "Comercial"),
    ENTREGA("ENTREGA", "Entrega"),
    COBRANCA("COBRANCA", "Cobrança"),
    COLETA("COLETA", "Coleta");

    private final String codigo;
    private final String descricao;

    TipoEnderecoEnum(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static TipoEnderecoEnum fromCodigo(String codigo) {
        for (TipoEnderecoEnum tipo : values()) {
            if (tipo.getCodigo().equalsIgnoreCase(codigo)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Código de tipo de endereço inválido: " + codigo);
    }
}
