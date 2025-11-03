package br.com.vanessa_mudanca.cliente_core.domain.exception;

import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoEnderecoEnum;

/**
 * Exceção lançada quando há tentativa de marcar mais de um endereço como principal
 * para o mesmo tipo de endereço.
 *
 * Regra de negócio: Apenas 1 endereço pode ser principal por tipo
 * (RESIDENCIAL, COMERCIAL, ENTREGA, etc).
 */
public class EnderecoPrincipalDuplicadoException extends BusinessException {

    public EnderecoPrincipalDuplicadoException(TipoEnderecoEnum tipo) {
        super(String.format(
                "Já existe um endereço principal do tipo %s. " +
                        "Remova a flag principal do outro endereço antes de marcar este.",
                tipo
        ));
    }
}
