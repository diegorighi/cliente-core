package br.com.vanessa_mudanca.cliente_core.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for PROD/STAGING profiles
 *
 * Configuração OAuth2 Resource Server com validação JWT via AWS Cognito.
 *
 * Fluxo:
 * 1. Requisição chega com header: Authorization: Bearer <JWT>
 * 2. Spring Security valida JWT via JWKS endpoint do Cognito
 * 3. Se válido, extrai claims (sub, email, cognito:groups, etc.)
 * 4. Autoriza endpoint baseado em @PreAuthorize nos controllers
 *
 * Endpoints públicos (sem autenticação):
 * - /actuator/health
 * - /actuator/info
 * - /actuator/prometheus
 *
 * Todos os demais endpoints requerem JWT válido.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize
@Profile({"prod", "staging"})
public class ProdSecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // API REST stateless não precisa CSRF
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Sem sessão, apenas JWT
            )
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (observability)
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/actuator/prometheus").permitAll()
                .requestMatchers("/actuator/metrics").permitAll()

                // Swagger (protegido, requer autenticação)
                .requestMatchers("/v3/api-docs/**").authenticated()
                .requestMatchers("/swagger-ui/**").authenticated()
                .requestMatchers("/swagger-ui.html").authenticated()

                // Todos os demais endpoints requerem JWT válido
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder())) // Validação JWT via JWKS
            );

        return http.build();
    }

    /**
     * JWT Decoder configurado com JWKS endpoint do Cognito
     *
     * JWKS (JSON Web Key Set) é o endpoint público do Cognito que contém
     * as chaves públicas para validar a assinatura do JWT.
     *
     * Exemplo de JWKS URI:
     * https://cognito-idp.sa-east-1.amazonaws.com/sa-east-1_hXX8OVC7K/.well-known/jwks.json
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
