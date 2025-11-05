package br.com.vanessa_mudanca.cliente_core.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Validador de acesso para role CUSTOMER.
 *
 * CUSTOMER só pode acessar seus próprios dados.
 * O JWT do Cognito contém o 'sub' (subject) que é o UUID do cliente.
 *
 * Uso nos controllers:
 * ```java
 * @GetMapping("/{publicId}")
 * public ResponseEntity<ClientePFResponse> buscarPorId(
 *     @PathVariable UUID publicId,
 *     Authentication authentication) {
 *
 *     customerAccessValidator.validateAccess(publicId, authentication);
 *     // ... resto do código
 * }
 * ```
 */
@Component
public class CustomerAccessValidator {

    /**
     * Valida se CUSTOMER pode acessar o recurso.
     *
     * Regras:
     * - ADMIN, EMPLOYEE, SERVICE: sempre pode acessar qualquer cliente
     * - CUSTOMER: só pode acessar se publicId == JWT.sub (próprio cadastro)
     *
     * @param resourceId UUID do cliente sendo acessado
     * @param authentication Objeto Authentication do Spring Security (contém JWT)
     * @throws AccessDeniedException se CUSTOMER tentar acessar dados de outro cliente
     */
    public void validateAccess(UUID resourceId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        // Verifica se é CUSTOMER
        boolean isCustomer = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equals("CUSTOMER"));

        // Se não é CUSTOMER, libera acesso (ADMIN, EMPLOYEE, SERVICE)
        if (!isCustomer) {
            return;
        }

        // Se é CUSTOMER, valida se está acessando próprio cadastro
        // JWT.sub contém o UUID do cliente (configurado no Cognito)
        String authenticatedUserId = authentication.getName(); // Cognito retorna 'sub' no getName()

        try {
            UUID authenticatedUserUuid = UUID.fromString(authenticatedUserId);

            if (!resourceId.equals(authenticatedUserUuid)) {
                throw new AccessDeniedException(
                    "CUSTOMER só pode acessar seu próprio cadastro. " +
                    "Você tentou acessar: " + resourceId +
                    " mas seu cadastro é: " + authenticatedUserUuid
                );
            }
        } catch (IllegalArgumentException e) {
            // JWT.sub não é um UUID válido (não deveria acontecer em produção)
            throw new AccessDeniedException(
                "Token inválido: 'sub' não é um UUID válido. sub=" + authenticatedUserId
            );
        }
    }

    /**
     * Verifica se usuário autenticado é CUSTOMER.
     * Útil para lógica condicional nos controllers.
     */
    public boolean isCustomer(Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equals("CUSTOMER"));
    }

    /**
     * Obtém UUID do cliente autenticado (do JWT.sub).
     * Útil quando CUSTOMER precisa buscar próprio cadastro sem passar publicId.
     */
    public UUID getAuthenticatedCustomerId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "Token inválido: 'sub' não é um UUID. sub=" + authentication.getName()
            );
        }
    }
}
