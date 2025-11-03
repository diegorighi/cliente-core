package br.com.vanessa_mudanca.cliente_core.domain.exception;

/**
 * Exceção lançada quando a data de validade de um documento é inválida.
 *
 * Casos típicos:
 * - Data de validade no passado
 * - Data de validade muito distante no futuro (> 50 anos)
 * - Data de validade anterior à data de emissão
 */
public class DataValidadeInvalidaException extends BusinessException {

    public DataValidadeInvalidaException(String mensagem) {
        super(mensagem);
    }
}
