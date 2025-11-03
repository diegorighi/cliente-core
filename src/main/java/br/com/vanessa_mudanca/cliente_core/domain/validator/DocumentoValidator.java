package br.com.vanessa_mudanca.cliente_core.domain.validator;

/**
 * Validador de documentos (CPF e CNPJ).
 * Implementa algoritmos de validação conforme Receita Federal.
 */
public class DocumentoValidator {

    private DocumentoValidator() {
        // Utility class
    }

    /**
     * Valida um CPF.
     *
     * @param cpf CPF a validar (com ou sem formatação)
     * @return true se válido, false caso contrário
     */
    public static boolean isValidCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return false;
        }

        // Remove formatação
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");

        // Verifica se tem 11 dígitos
        if (cpfLimpo.length() != 11) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (CPF inválido)
        if (cpfLimpo.matches("(\\d)\\1{10}")) {
            return false;
        }

        // Calcula primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpfLimpo.charAt(i)) * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito >= 10) {
            primeiroDigito = 0;
        }

        // Verifica primeiro dígito
        if (Character.getNumericValue(cpfLimpo.charAt(9)) != primeiroDigito) {
            return false;
        }

        // Calcula segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpfLimpo.charAt(i)) * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito >= 10) {
            segundoDigito = 0;
        }

        // Verifica segundo dígito
        return Character.getNumericValue(cpfLimpo.charAt(10)) == segundoDigito;
    }

    /**
     * Valida um CNPJ.
     *
     * @param cnpj CNPJ a validar (com ou sem formatação)
     * @return true se válido, false caso contrário
     */
    public static boolean isValidCnpj(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) {
            return false;
        }

        // Remove formatação
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");

        // Verifica se tem 14 dígitos
        if (cnpjLimpo.length() != 14) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (CNPJ inválido)
        if (cnpjLimpo.matches("(\\d)\\1{13}")) {
            return false;
        }

        // Pesos para cálculo dos dígitos verificadores
        int[] pesosPrimeiroDigito = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesosSegundoDigito = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        // Calcula primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += Character.getNumericValue(cnpjLimpo.charAt(i)) * pesosPrimeiroDigito[i];
        }
        int primeiroDigito = soma % 11;
        primeiroDigito = primeiroDigito < 2 ? 0 : 11 - primeiroDigito;

        // Verifica primeiro dígito
        if (Character.getNumericValue(cnpjLimpo.charAt(12)) != primeiroDigito) {
            return false;
        }

        // Calcula segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += Character.getNumericValue(cnpjLimpo.charAt(i)) * pesosSegundoDigito[i];
        }
        int segundoDigito = soma % 11;
        segundoDigito = segundoDigito < 2 ? 0 : 11 - segundoDigito;

        // Verifica segundo dígito
        return Character.getNumericValue(cnpjLimpo.charAt(13)) == segundoDigito;
    }

    /**
     * Remove formatação de CPF ou CNPJ, mantendo apenas números.
     *
     * @param documento documento a limpar
     * @return documento sem formatação
     */
    public static String limparDocumento(String documento) {
        if (documento == null) {
            return null;
        }
        return documento.replaceAll("[^0-9]", "");
    }

    /**
     * Formata CPF no padrão XXX.XXX.XXX-XX.
     *
     * @param cpf CPF a formatar (apenas números)
     * @return CPF formatado
     */
    public static String formatarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return String.format("%s.%s.%s-%s",
                cpf.substring(0, 3),
                cpf.substring(3, 6),
                cpf.substring(6, 9),
                cpf.substring(9, 11));
    }

    /**
     * Formata CNPJ no padrão XX.XXX.XXX/XXXX-XX.
     *
     * @param cnpj CNPJ a formatar (apenas números)
     * @return CNPJ formatado
     */
    public static String formatarCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return cnpj;
        }
        return String.format("%s.%s.%s/%s-%s",
                cnpj.substring(0, 2),
                cnpj.substring(2, 5),
                cnpj.substring(5, 8),
                cnpj.substring(8, 12),
                cnpj.substring(12, 14));
    }
}
