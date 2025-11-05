package br.com.vanessa_mudanca.cliente_core.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança OAuth2 Resource Server com AWS Cognito.
 * <p>
 * <b>Arquitetura:</b>
 * <ul>
 *   <li><b>Cognito User Pool:</b> sa-east-1_hXX8OVC7K (vanessa-mudanca-users-prod)</li>
 *   <li><b>Resource Server:</b> https://api.vanessamudanca.com.br/cliente-core</li>
 *   <li><b>OAuth2 Flow:</b> Client Credentials (M2M)</li>
 *   <li><b>Token Format:</b> JWT (validado via JWK Set)</li>
 * </ul>
 * </p>
 *
 * <h3>Scopes Disponíveis:</h3>
 * <ul>
 *   <li><b>clientes.read:</b> Leitura de dados de clientes (GET endpoints)</li>
 *   <li><b>clientes.write:</b> Criação e atualização (POST/PUT endpoints)</li>
 *   <li><b>clientes.delete:</b> Deleção lógica (DELETE endpoints)</li>
 *   <li><b>clientes.admin:</b> Acesso completo (todos endpoints + admin)</li>
 * </ul>
 *
 * <h3>Endpoints Públicos (sem autenticação):</h3>
 * <ul>
 *   <li><b>/actuator/health:</b> Health check (ALB Target Group)</li>
 *   <li><b>/actuator/info:</b> Informações da aplicação</li>
 *   <li><b>/swagger-ui/**:</b> Documentação OpenAPI</li>
 *   <li><b>/v3/api-docs/**:</b> Spec OpenAPI JSON</li>
 * </ul>
 *
 * <h3>Endpoints Protegidos:</h3>
 * <ul>
 *   <li><b>/v1/clientes/**:</b> Requer JWT válido com scope apropriado</li>
 *   <li><b>/actuator/metrics:</b> Requer autenticação (métricas sensíveis)</li>
 *   <li><b>/actuator/prometheus:</b> Requer autenticação (métricas sensíveis)</li>
 * </ul>
 *
 * <h3>Validação de JWT:</h3>
 * <p>
 * Validações automáticas realizadas pelo Spring Security:
 * <ul>
 *   <li><b>Signature:</b> Verificada via JWK Set do Cognito</li>
 *   <li><b>Issuer (iss):</b> https://cognito-idp.sa-east-1.amazonaws.com/sa-east-1_hXX8OVC7K</li>
 *   <li><b>Expiration (exp):</b> Token não pode estar expirado (60 minutos)</li>
 *   <li><b>Token Use:</b> Deve ser "access" (não "id_token")</li>
 *   <li><b>Scopes:</b> Validados via @PreAuthorize em controllers</li>
 * </ul>
 * </p>
 *
 * <h3>Exemplo de Uso nos Controllers:</h3>
 * <pre>
 * {@code
 * @GetMapping("/v1/clientes/pf")
 * @PreAuthorize("hasAuthority('SCOPE_https://api.vanessamudanca.com.br/cliente-core/clientes.read')")
 * public ResponseEntity<List<ClientePFResponse>> listar() {
 *     // ...
 * }
 *
 * @PostMapping("/v1/clientes/pf")
 * @PreAuthorize("hasAuthority('SCOPE_https://api.vanessamudanca.com.br/cliente-core/clientes.write')")
 * public ResponseEntity<ClientePFResponse> criar(@RequestBody CreateClientePFRequest request) {
 *     // ...
 * }
 * }
 * </pre>
 *
 * <h3>Como Obter Token (Postman):</h3>
 * <pre>
 * POST https://vanessa-mudanca-auth-prod.auth.sa-east-1.amazoncognito.com/oauth2/token
 * Headers:
 *   Content-Type: application/x-www-form-urlencoded
 *   Authorization: Basic [Base64(client_id:client_secret)]
 * Body:
 *   grant_type=client_credentials
 *   scope=https://api.vanessamudanca.com.br/cliente-core/clientes.read
 * </pre>
 *
 * <h3>Troubleshooting:</h3>
 * <ul>
 *   <li><b>401 Unauthorized:</b> Token inválido ou expirado (renovar token)</li>
 *   <li><b>403 Forbidden:</b> Scope insuficiente (incluir scope correto no token)</li>
 *   <li><b>No JWK found:</b> Verificar conectividade com Cognito JWK Set URI</li>
 * </ul>
 *
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
 * @see <a href="https://docs.aws.amazon.com/cognito/latest/developerguide/amazon-cognito-user-pools-using-tokens-verifying-a-jwt.html">AWS Cognito JWT Verification</a>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Habilita @PreAuthorize nos controllers
public class SecurityConfig {

    /**
     * Configura a cadeia de filtros de segurança.
     * <p>
     * <b>Estratégia STATELESS:</b> Sem sessão HTTP (cada request valida JWT independentemente)
     * </p>
     *
     * <p>
     * <b>Endpoints Públicos:</b>
     * <ul>
     *   <li>Actuator health/info (health checks do ALB)</li>
     *   <li>Swagger UI (documentação da API)</li>
     * </ul>
     * </p>
     *
     * <p>
     * <b>Endpoints Protegidos:</b> Todos os demais requerem JWT válido
     * </p>
     *
     * @param http HttpSecurity builder
     * @return SecurityFilterChain configurado
     * @throws Exception se houver erro na configuração
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // Desabilita CSRF (não necessário para APIs stateless)
            .csrf(csrf -> csrf.disable())

            // Configura política de sessão STATELESS
            // Cada request valida JWT independentemente (sem sessão HTTP)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Define regras de autorização
            .authorizeHttpRequests(authorize -> authorize
                // Endpoints públicos (sem autenticação)
                .requestMatchers(
                    "/actuator/health",           // Health check (ALB Target Group)
                    "/actuator/health/**",        // Health check detalhado
                    "/actuator/info",             // Informações da aplicação
                    "/swagger-ui/**",             // Swagger UI (documentação)
                    "/v3/api-docs/**",            // OpenAPI spec JSON
                    "/swagger-ui.html"            // Swagger UI HTML
                ).permitAll()

                // Endpoints de métricas (protegidos)
                .requestMatchers(
                    "/actuator/metrics",          // Métricas (dados sensíveis)
                    "/actuator/metrics/**",       // Métricas específicas
                    "/actuator/prometheus"        // Métricas Prometheus (scraping)
                ).authenticated()

                // Todos os endpoints /v1/** requerem autenticação
                // Scopes específicos validados via @PreAuthorize nos controllers
                .requestMatchers("/v1/**").authenticated()

                // Qualquer outro endpoint requer autenticação (fail-safe)
                .anyRequest().authenticated()
            )

            // Configura OAuth2 Resource Server com JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {
                    // JWT validation é automática via application.yml:
                    //   spring.security.oauth2.resourceserver.jwt.jwk-set-uri
                    //   spring.security.oauth2.resourceserver.jwt.issuer-uri
                })
            )

            .build();
    }
}
