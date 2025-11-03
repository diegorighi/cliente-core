package br.com.vanessa_mudanca.cliente_core.domain.enums;

import lombok.Getter;

@Getter
public enum TipoContatoEnum {
    CELULAR("CELULAR", "Celular"),
    TELEFONE_FIXO("FIXO", "Telefone Fixo"),
    EMAIL("EMAIL", "E-mail"),
    WHATSAPP("WHATSAPP", "WhatsApp"),
    TELEGRAM("TELEGRAM", "Telegram"),
    OUTRO("OUTRO", "Outro");

    private final String codigo;
    private final String descricao;

    TipoContatoEnum(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static TipoContatoEnum fromCodigo(String codigo) {
        for (TipoContatoEnum tipo : values()) {
            if (tipo.getCodigo().equalsIgnoreCase(codigo)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Código de tipo de contato inválido: " + codigo);
    }
}
