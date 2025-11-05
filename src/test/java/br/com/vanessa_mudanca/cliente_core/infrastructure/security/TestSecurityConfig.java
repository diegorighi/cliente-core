package br.com.vanessa_mudanca.cliente_core.infrastructure.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for TESTS
 *
 * Desabilita TODA autenticação e autorização durante os testes.
 * Garante que os testes funcionem sem precisar fornecer tokens JWT.
 *
 * @Primary garante que esta configuração seja usada ao invés da configuração de prod/dev.
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Permite TODAS as requisições sem autenticação
            );

        return http.build();
    }
}
