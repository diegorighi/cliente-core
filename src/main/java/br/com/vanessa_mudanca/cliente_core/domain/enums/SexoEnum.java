package br.com.vanessa_mudanca.cliente_core.domain.enums;

import lombok.Getter;

@Getter
public enum SexoEnum {
    MASCULINO("M", "Masculino"),
    FEMININO("F", "Feminino"),
    OUTRO("O", "Outro"),
    NAO_INFORMADO("N", "Não Informado");

    private final String codigo;
    private final String descricao;

    SexoEnum(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static SexoEnum fromCodigo(String codigo) {
        for (SexoEnum sexo : values()) {
            if (sexo.getCodigo().equalsIgnoreCase(codigo)) {
                return sexo;
            }
        }
        throw new IllegalArgumentException("Código de sexo inválido: " + codigo);
    }
}
