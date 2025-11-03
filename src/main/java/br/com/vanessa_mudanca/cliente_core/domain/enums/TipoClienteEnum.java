package br.com.vanessa_mudanca.cliente_core.domain.enums;

import lombok.Getter;

@Getter
public enum TipoClienteEnum {
    CONSIGNANTE("CONSIGNANTE", "Pessoa que VAI VENDER"),
    COMPRADOR("COMPRADOR", "Pessoa que VAI COMPRAR"),
    AMBOS("AMBOS", "Vende E compra"),
    PROSPECTO("PROSPECTO", "Ainda não fez transação"),
    PARCEIRO("PARCEIRO", "Prestador de serviço"),
    INATIVO("INATIVO", "Desativado");

    private final String codigo;
    private final String descricao;

    TipoClienteEnum(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static TipoClienteEnum fromCodigo(String codigo) {
        for (TipoClienteEnum tipo : values()) {
            if (tipo.getCodigo().equalsIgnoreCase(codigo)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Código de tipo de cliente inválido: " + codigo);
    }
}
