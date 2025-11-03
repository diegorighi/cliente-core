package br.com.vanessa_mudanca.cliente_core.domain.enums;

import lombok.Getter;

@Getter
public enum OrigemLeadEnum {
    GOOGLE_ADS("GOOGLE_ADS", "Google Ads"),
    FACEBOOK_ADS("FACEBOOK_ADS", "Facebook Ads"),
    INSTAGRAM_ADS("INSTAGRAM_ADS", "Instagram Ads"),
    INDICACAO("INDICACAO", "Indicação"),
    GOOGLE_ORGANICO("GOOGLE_ORGANICO", "Google Orgânico"),
    REDES_SOCIAIS("REDES_SOCIAIS", "Redes Sociais"),
    WHATSAPP("WHATSAPP", "WhatsApp"),
    BOCA_A_BOCA("BOCA_A_BOCA", "Boca a Boca"),
    INFLUENCER("INFLUENCER", "Influencer"),
    PARCEIRO("PARCEIRO", "Parceiro"),
    OUTRO("OUTRO", "Outro");

    private final String codigo;
    private final String descricao;

    OrigemLeadEnum(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static OrigemLeadEnum fromCodigo(String codigo) {
        for (OrigemLeadEnum origem : values()) {
            if (origem.getCodigo().equalsIgnoreCase(codigo)) {
                return origem;
            }
        }
        throw new IllegalArgumentException("Código de origem de lead inválido: " + codigo);
    }
}
