package br.com.vanessa_mudanca.cliente_core.infrastructure.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

/**
 * Filtro para injetar authentication no profile DEV.
 *
 * Cria um usuário autenticado com TODAS as authorities (ADMIN, EMPLOYEE, CUSTOMER, SERVICE)
 * para que @PreAuthorize sempre passe.
 *
 * ATENÇÃO: Usar APENAS em desenvolvimento local.
 */
public class DevAuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Cria authentication com authorities de ADMIN e EMPLOYEE
        // NÃO inclui CUSTOMER para evitar validações de propriedade (CUSTOMER só vê próprio cadastro)
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "00000000-0000-0000-0000-000000000000", // principal (UUID fake para dev)
                "dev-password", // credentials
                List.of(
                        new SimpleGrantedAuthority("ADMIN"),
                        new SimpleGrantedAuthority("EMPLOYEE"),
                        new SimpleGrantedAuthority("SERVICE")
                )
        );

        // Injeta no SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            chain.doFilter(request, response);
        } finally {
            // Limpa o contexto após request
            SecurityContextHolder.clearContext();
        }
    }
}
