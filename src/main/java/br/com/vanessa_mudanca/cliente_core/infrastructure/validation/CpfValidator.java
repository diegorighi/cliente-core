package br.com.vanessa_mudanca.cliente_core.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador customizado para CPF (Cadastro de Pessoas Físicas).
 *
 * Valida:
 * - Formato (11 dígitos numéricos)
 * - Sequências inválidas (111.111.111-11, etc)
 * - Dígitos verificadores (algoritmo oficial da Receita Federal)
 *
 * @see <a href="http://www.receita.fazenda.gov.br">Receita Federal</a>
 * @since 1.0.0
 */
public class CpfValidator implements ConstraintValidator<ValidCpf, String> {

    private static final int CPF_LENGTH = 11;
    private static final int[] PESO_PRIMEIRO_DIGITO = {10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESO_SEGUNDO_DIGITO = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null || cpf.isBlank()) {
            return false;
        }

        // Remove formatação (pontos, traços)
        String cpfNumerico = cpf.replaceAll("\\D", "");

        // Valida tamanho
        if (cpfNumerico.length() != CPF_LENGTH) {
            return false;
        }

        // Valida sequências inválidas (111.111.111-11, 222.222.222-22, etc)
        if (cpfNumerico.matches("(\\d)\\1{10}")) {
            return false;
        }

        // Calcula e valida dígitos verificadores
        return validarDigitosVerificadores(cpfNumerico);
    }

    /**
     * Valida dígitos verificadores do CPF usando algoritmo da Receita Federal.
     *
     * @param cpf CPF sem formatação (11 dígitos)
     * @return true se dígitos verificadores são válidos
     */
    private boolean validarDigitosVerificadores(String cpf) {
        // Extrai os 9 primeiros dígitos
        String base = cpf.substring(0, 9);

        // Calcula primeiro dígito verificador
        int primeiroDigito = calcularDigito(base, PESO_PRIMEIRO_DIGITO);

        // Calcula segundo dígito verificador
        int segundoDigito = calcularDigito(base + primeiroDigito, PESO_SEGUNDO_DIGITO);

        // Compara com os dígitos informados
        String digitosCalculados = String.valueOf(primeiroDigito) + segundoDigito;
        String digitosInformados = cpf.substring(9);

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
