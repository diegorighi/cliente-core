package br.com.vanessa_mudanca.cliente_core.infrastructure.security;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Method Security Configuration for DEV profile
 *
 * DESABILITA completamente validação de @PreAuthorize.
 * AuthorizationManager SEMPRE retorna TRUE (acesso permitido).
 *
 * ATENÇÃO: Usar APENAS em desenvolvimento local.
 */
@Configuration
@EnableMethodSecurity
@Profile("dev")
public class DevMethodSecurityConfig {

    /**
     * AuthorizationManager customizado que SEMPRE permite acesso.
     * Substitui o comportamento padrão do @PreAuthorize.
     *
     * Todos os métodos anotados com @PreAuthorize vão passar,
     * independente das authorities requeridas.
     */
    @Bean
    public AuthorizationManager<MethodInvocation> preAuthorizeAuthorizationManager() {
        return (authentication, object) -> new AuthorizationDecision(true);
    }
}
