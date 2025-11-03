package br.com.vanessa_mudanca.cliente_core.domain.validator;

import br.com.vanessa_mudanca.cliente_core.application.ports.output.EnderecoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoEnderecoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.exception.EnderecoPrincipalDuplicadoException;
import org.springframework.stereotype.Component;

/**
 * Strategy para validar unicidade de endereço principal por tipo.
 *
 * REGRA:
 * - Apenas 1 endereço pode ser principal por tipo (RESIDENCIAL, COMERCIAL, etc.)
 * - Permitir múltiplos endereços do mesmo tipo, mas só 1 principal
 */
@Component
public class ValidarEnderecoPrincipalUnicoStrategy {

    private final EnderecoRepositoryPort enderecoRepository;

    public ValidarEnderecoPrincipalUnicoStrategy(EnderecoRepositoryPort enderecoRepository) {
        this.enderecoRepository = enderecoRepository;
    }

    /**
     * Valida se pode marcar este endereço como principal.
     *
     * @param clienteId ID do cliente
     * @param enderecoId ID do endereço sendo atualizado
     * @param tipo Tipo do endereço
     * @param marcandoComoPrincipal Flag sendo atualizada
     */
    public void validar(
            Long clienteId,
            Long enderecoId,
            TipoEnderecoEnum tipo,
            Boolean marcandoComoPrincipal
    ) {
        if (!Boolean.TRUE.equals(marcandoComoPrincipal)) {
            return; // Não está marcando como principal, ok
        }

        // Verificar se já existe outro endereço principal do mesmo tipo
        boolean jaExistePrincipal = enderecoRepository
                .existsByClienteIdAndTipoEnderecoAndEnderecoPrincipalAndIdNot(
                        clienteId,
                        tipo,
                        true,
                        enderecoId
                );

        if (jaExistePrincipal) {
            throw new EnderecoPrincipalDuplicadoException(tipo);
        }
    }
}
