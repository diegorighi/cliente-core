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

    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Permite TODAS as requisições sem autenticação
            );

        return http.build();
    }
}
