package br.com.vanessa_mudanca.cliente_core.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador customizado para CNPJ (Cadastro Nacional da Pessoa Jurídica).
 *
 * Valida:
 * - Formato (14 dígitos numéricos)
 * - Sequências inválidas (00.000.000/0000-00, etc)
 * - Dígitos verificadores (algoritmo oficial da Receita Federal)
 *
 * @see <a href="http://www.receita.fazenda.gov.br">Receita Federal</a>
 * @since 1.0.0
 */
public class CnpjValidator implements ConstraintValidator<ValidCnpj, String> {

    private static final int CNPJ_LENGTH = 14;
    private static final int[] PESO_PRIMEIRO_DIGITO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESO_SEGUNDO_DIGITO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    @Override
    public boolean isValid(String cnpj, ConstraintValidatorContext context) {
        if (cnpj == null || cnpj.isBlank()) {
            return false;
        }

        // Remove formatação (pontos, traços, barra)
        String cnpjNumerico = cnpj.replaceAll("\\D", "");

        // Valida tamanho
        if (cnpjNumerico.length() != CNPJ_LENGTH) {
            return false;
        }

        // Valida sequências inválidas (00.000.000/0000-00, 11.111.111/1111-11, etc)
        if (cnpjNumerico.matches("(\\d)\\1{13}")) {
            return false;
        }

        // Calcula e valida dígitos verificadores
        return validarDigitosVerificadores(cnpjNumerico);
    }

    /**
     * Valida dígitos verificadores do CNPJ usando algoritmo da Receita Federal.
     *
     * @param cnpj CNPJ sem formatação (14 dígitos)
     * @return true se dígitos verificadores são válidos
     */
    private boolean validarDigitosVerificadores(String cnpj) {
        // Extrai os 12 primeiros dígitos
        String base = cnpj.substring(0, 12);

        // Calcula primeiro dígito verificador
        int primeiroDigito = calcularDigito(base, PESO_PRIMEIRO_DIGITO);

        // Calcula segundo dígito verificador
        int segundoDigito = calcularDigito(base + primeiroDigito, PESO_SEGUNDO_DIGITO);

        // Compara com os dígitos informados
        String digitosCalculados = String.valueOf(primeiroDigito) + segundoDigito;
        String digitosInformados = cnpj.substring(12);

        return digitosCalculados.equals(digitosInformados);
    }

    /**
     * Calcula dígito verificador usando array de pesos.
     *
     * @param base String numérica base
     * @param pesos Array de pesos para multiplicação
     * @return Dígito verificador calculado (0-9)
     */
    private int calcularDigito(String base, int[] pesos) {
        int soma = 0;

        for (int i = 0; i < base.length(); i++) {
            int digito = Character.getNumericValue(base.charAt(i));
            soma += digito * pesos[i];
        }

        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }
}
