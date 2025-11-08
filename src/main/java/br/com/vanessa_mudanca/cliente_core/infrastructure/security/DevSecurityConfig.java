package br.com.vanessa_mudanca.cliente_core.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for DEV profile
 *
 * ATENÇÃO: Esta configuração desabilita TODA autenticação e autorização.
 * Usar APENAS em ambiente de desenvolvimento local.
 *
 * Para rodar local sem OAuth2:
 * mvn spring-boot:run -Dspring.profiles.active=dev
 */
@Configuration
@EnableWebSecurity
@Profile("dev")
public class DevSecurityConfig {

    /**
     * IMPORTANTE: Não usar @EnableMethodSecurity aqui!
     * No profile dev, queremos desabilitar TODAS as validações de segurança,
     * incluindo @PreAuthorize, @Secured, etc. dos controllers.
     *
     * Se usar @EnableMethodSecurity, os @PreAuthorize dos controllers
     * vão bloquear requisições mesmo com permitAll() abaixo.
     *
     * A configuração de Method Security está em DevMethodSecurityConfig.java
     */

    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configure(http)) // Habilita CORS
            .csrf(csrf -> csrf.disable())
            // Adiciona filtro que injeta authentication com todas as authorities
            .addFilterBefore(new DevAuthenticationFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Permite TODAS as requisições sem autenticação
            );

        return http.build();
    }
}
