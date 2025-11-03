package br.com.vanessa_mudanca.cliente_core.domain.enums;

import lombok.Getter;

@Getter
public enum StatusDocumentoEnum {
    VALIDO("VALIDO", "Válido"),
    EXPIRADO("EXPIRADO", "Expirado"),
    AGUARDANDO_VERIFICACAO("AGUARDANDO_VERIFICACAO", "Aguardando Verificação"),
    VERIFICADO("VERIFICADO", "Verificado"),
    REJEITADO("REJEITADO", "Rejeitado");

    private final String codigo;
    private final String descricao;

    StatusDocumentoEnum(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static StatusDocumentoEnum fromCodigo(String codigo) {
        for (StatusDocumentoEnum status : values()) {
            if (status.getCodigo().equalsIgnoreCase(codigo)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Código de status de documento inválido: " + codigo);
    }
}
