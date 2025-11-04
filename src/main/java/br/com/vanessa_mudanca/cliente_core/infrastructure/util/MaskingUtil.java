package br.com.vanessa_mudanca.cliente_core.infrastructure.util;

/**
 * Utilitario para mascaramento de dados sensiveis em logs (LGPD compliance).
 * <p>
 * Evita exposicao de PII (Personally Identifiable Information) em logs,
 * CloudWatch, Datadog, ou qualquer sistema de observabilidade.
 */
public final class MaskingUtil {

    private MaskingUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Mascara CPF preservando apenas os 3 ultimos digitos antes do DV.
     *
     * @param cpf CPF a ser mascarado (com ou sem formatacao)
     * @return CPF mascarado no formato ***.***.789-10 ou null se input for null
     */
    public static String maskCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return null;
        }

        String cleanCpf = cpf.replaceAll("[^0-9]", "");

        if (cleanCpf.length() != 11) {
            return "***INVALID_CPF***";
        }

        String lastThree = cleanCpf.substring(6, 9);
        String dv = cleanCpf.substring(9, 11);

        return String.format("***.***.%s-%s", lastThree, dv);
    }

    /**
     * Mascara CNPJ preservando apenas os 4 ultimos digitos antes do DV.
     *
     * @param cnpj CNPJ a ser mascarado (com ou sem formatacao)
     * @return CNPJ mascarado ou null se input for null
     */
    public static String maskCnpj(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) {
            return null;
        }

        String cleanCnpj = cnpj.replaceAll("[^0-9]", "");

        if (cleanCnpj.length() != 14) {
            return "***INVALID_CNPJ***";
        }

        String lastFour = cleanCnpj.substring(8, 12);
        String dv = cleanCnpj.substring(12, 14);

        return String.format("**.***.***/****-%s", dv);
    }

    /**
     * Mascara email preservando apenas as 2 primeiras letras do local-part.
     *
     * @param email Email a ser mascarado
     * @return Email mascarado ou null se input for null
     */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        if (!email.contains("@")) {
            return "***INVALID_EMAIL***";
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return "***@" + domain;
        }

        return localPart.substring(0, 2) + "***@" + domain;
    }

    /**
     * Mascara nome completo preservando apenas a primeira letra de cada palavra.
     *
     * @param fullName Nome completo a ser mascarado
     * @return Nome mascarado ou null se input for null
     */
    public static String maskName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }

        String[] words = fullName.trim().split("\\s+");
        StringBuilder masked = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                masked.append(word.charAt(0))
                        .append("***")
                        .append(" ");
            }
        }

        return masked.toString().trim();
    }

    /**
     * Mascara telefone preservando apenas os 4 ultimos digitos.
     *
     * @param phone Telefone a ser mascarado (com ou sem formatacao)
     * @return Telefone mascarado ou null se input for null
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        String cleanPhone = phone.replaceAll("[^0-9]", "");

        if (cleanPhone.length() < 8 || cleanPhone.length() > 11) {
            return "***INVALID_PHONE***";
        }

        int length = cleanPhone.length();
        String lastFour = cleanPhone.substring(length - 4);

        if (cleanPhone.length() == 11) {
            String ddd = cleanPhone.substring(0, 2);
            return String.format("(%s) ****-%s", ddd, lastFour);
        } else if (cleanPhone.length() == 10) {
            String ddd = cleanPhone.substring(0, 2);
            return String.format("(%s) ****-%s", ddd, lastFour);
        } else {
            return "****-" + lastFour;
        }
    }

    /**
     * Mascara generico para dados sensiveis.
     *
     * @param data Dado a ser mascarado
     * @return Dado mascarado ou null se input for null
     */
    public static String maskGeneric(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }

        int length = data.length();

        if (length <= 4) {
            return "***";
        }

        return data.substring(0, 2) + "***" + data.substring(length - 2);
    }
}
