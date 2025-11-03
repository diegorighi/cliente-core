package br.com.vanessa_mudanca.cliente_core.domain.validator;

import br.com.vanessa_mudanca.cliente_core.application.ports.output.ContatoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ContatoPrincipalDuplicadoException;
import org.springframework.stereotype.Component;

/**
 * Strategy para validar unicidade de contato principal.
 *
 * REGRA:
 * - Apenas 1 contato pode ser principal por cliente
 */
@Component
public class ValidarContatoPrincipalUnicoStrategy {

    private final ContatoRepositoryPort contatoRepository;

    public ValidarContatoPrincipalUnicoStrategy(ContatoRepositoryPort contatoRepository) {
        this.contatoRepository = contatoRepository;
    }

    /**
     * Valida se pode marcar este contato como principal.
     *
     * @param clienteId ID do cliente
     * @param contatoId ID do contato sendo atualizado
     * @param marcandoComoPrincipal Flag sendo atualizada
     */
    public void validar(
            Long clienteId,
            Long contatoId,
            Boolean marcandoComoPrincipal
    ) {
        if (!Boolean.TRUE.equals(marcandoComoPrincipal)) {
            return; // Não está marcando como principal, ok
        }

        // Verificar se já existe outro contato principal
        boolean jaExistePrincipal = contatoRepository
                .existsByClienteIdAndContatoPrincipalAndIdNot(
                        clienteId,
                        true,
                        contatoId
                );

        if (jaExistePrincipal) {
            throw new ContatoPrincipalDuplicadoException();
        }
    }
}
