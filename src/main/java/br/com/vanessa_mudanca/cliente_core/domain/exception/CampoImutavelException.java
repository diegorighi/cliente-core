package br.com.vanessa_mudanca.cliente_core.domain.exception;

/**
 * Exceção lançada quando há tentativa de alterar um campo imutável.
 *
 * Exemplos de campos imutáveis:
 * - CPF/CNPJ (identificadores únicos)
 * - Número de documentos (RG, CNH, etc)
 * - Data de nascimento (impacta idade e documentos legais)
 */
public class CampoImutavelException extends BusinessException {

    public CampoImutavelException(String campo) {
        super(String.format(
                "O campo '%s' é imutável e não pode ser alterado após criação",
                campo
        ));
    }

    public CampoImutavelException(String campo, String motivoImutabilidade) {
        super(String.format(
                "O campo '%s' é imutável e não pode ser alterado. Motivo: %s",
                campo,
                motivoImutabilidade
        ));
    }
}
