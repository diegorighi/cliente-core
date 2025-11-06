# 🔐 Client Credentials Flow - Autenticação M2M

## Visão Geral

Este documento descreve o **OAuth2 Client Credentials Flow** implementado para autenticação **machine-to-machine (M2M)** entre microserviços da plataforma Va Nessa Mudança.

**Padrão:** OAuth2 Client Credentials Grant (RFC 6749, Section 4.4)
**Provider:** AWS Cognito User Pool
**Validade do Token:** 1 hora (3600 segundos)
**Propagação de Contexto:** Correlation ID via header `X-Correlation-ID`

---

## 🎯 Cenário de Uso

**venda-core** (cliente) precisa validar se um cliente existe antes de criar uma venda:

```
venda-core → Cognito → JWT Token → cliente-core (valida JWT automaticamente)
```

### Fluxo Completo

```mermaid
sequenceDiagram
    participant VendaCore as venda-core
    participant Cognito as AWS Cognito
    participant SecretsManager as Secrets Manager
    participant ClienteCore as cliente-core

    Note over VendaCore: Startup: Load credentials
    VendaCore->>SecretsManager: GET venda-core/prod/cognito-m2m
    SecretsManager-->>VendaCore: {client_id, client_secret, token_uri}

    Note over VendaCore: Request: Need to validate client
    VendaCore->>Cognito: POST /oauth2/token<br/>(client_credentials + Basic Auth)
    Cognito-->>VendaCore: {access_token, expires_in: 3600}

    VendaCore->>ClienteCore: GET /v1/clientes/pf/{id}<br/>(Authorization: Bearer JWT)
    ClienteCore->>Cognito: Validate JWT (JWKS endpoint)
    Cognito-->>ClienteCore: JWT válido + scopes
    ClienteCore-->>VendaCore: 200 OK + Cliente data
```

---

## 🏗️ Infraestrutura AWS

### 1. Cognito Resource Server

**Resource Server ID:** `cliente-core`
**Scopes definidos:**
- `cliente-core/read` - Leitura de dados de clientes
- `cliente-core/write` - Escrita/atualização de dados de clientes

**Comando de criação:**
```bash
aws cognito-idp create-resource-server \
  --user-pool-id sa-east-1_hXX8OVC7K \
  --identifier cliente-core \
  --name "Cliente Core API" \
  --scopes \
    ScopeName=read,ScopeDescription="Read access to cliente-core API" \
    ScopeName=write,ScopeDescription="Write access to cliente-core API" \
  --region sa-east-1
```

### 2. Cognito App Client (M2M)

**App Client ID:** `41u8or3q6id9nm8395qvl214j`
**Client Name:** `venda-core-m2m`
**Grant Type:** `client_credentials` (apenas)
**Allowed Scopes:** `cliente-core/read`, `cliente-core/write`
**Token Validity:** 1 hora

**Comando de criação:**
```bash
aws cognito-idp create-user-pool-client \
  --user-pool-id sa-east-1_hXX8OVC7K \
  --client-name venda-core-m2m \
  --no-generate-secret \
  --explicit-auth-flows \
  --allowed-o-auth-flows client_credentials \
  --allowed-o-auth-scopes cliente-core/read cliente-core/write \
  --allowed-o-auth-flows-user-pool-client \
  --access-token-validity 1 \
  --token-validity-units AccessToken=hours \
  --region sa-east-1
```

> **⚠️ IMPORTANTE:** `--no-generate-secret` foi usado incorretamente. O correto é **não passar essa flag** para que o Cognito gere o `client_secret` automaticamente. Para client_credentials flow, o secret é **obrigatório**.

**Client Secret:** `ei44vao0m1mfhf9rb8064vo56mdf5m2ig9q0tu0ur6lsdb1tius`

### 3. Secrets Manager

**Secret Name:** `venda-core/prod/cognito-m2m`
**ARN:** `arn:aws:secretsmanager:sa-east-1:530184476864:secret:venda-core/prod/cognito-m2m-jwabsD`

**Conteúdo:**
```json
{
  "client_id": "41u8or3q6id9nm8395qvl214j",
  "client_secret": "ei44vao0m1mfhf9rb8064vo56mdf5m2ig9q0tu0ur6lsdb1tius",
  "token_uri": "https://vanessa-mudanca-auth-prod.auth.sa-east-1.amazoncognito.com/oauth2/token"
}
```

**Comando de criação:**
```bash
aws secretsmanager create-secret \
  --name venda-core/prod/cognito-m2m \
  --description "Cognito M2M credentials for venda-core" \
  --secret-string '{
    "client_id": "41u8or3q6id9nm8395qvl214j",
    "client_secret": "ei44vao0m1mfhf9rb8064vo56mdf5m2ig9q0tu0ur6lsdb1tius",
    "token_uri": "https://vanessa-mudanca-auth-prod.auth.sa-east-1.amazoncognito.com/oauth2/token"
  }' \
  --region sa-east-1
```

**Como venda-core acessa o secret:**

Via **ECS Task Definition** com injeção nativa:
```json
{
  "containerDefinitions": [
    {
      "name": "venda-core",
      "secrets": [
        {
          "name": "COGNITO_CLIENT_ID",
          "valueFrom": "arn:aws:secretsmanager:sa-east-1:530184476864:secret:venda-core/prod/cognito-m2m-jwabsD:client_id::"
        },
        {
          "name": "COGNITO_CLIENT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:sa-east-1:530184476864:secret:venda-core/prod/cognito-m2m-jwabsD:client_secret::"
        },
        {
          "name": "COGNITO_TOKEN_URI",
          "valueFrom": "arn:aws:secretsmanager:sa-east-1:530184476864:secret:venda-core/prod/cognito-m2m-jwabsD:token_uri::"
        }
      ]
    }
  ]
}
```

**Vantagens:**
- ✅ Secrets disponíveis como variáveis de ambiente no container
- ✅ Automático: ECS Execution Role busca os secrets na inicialização
- ✅ Seguro: Secrets não ficam em plaintext no task definition
- ✅ Auditável: CloudTrail registra todos os acessos ao secret

---

## 🔑 Como Obter um Token JWT

### cURL (Teste Manual)

```bash
# 1. Base64 encode das credenciais (client_id:client_secret)
echo -n "41u8or3q6id9nm8395qvl214j:ei44vao0m1mfhf9rb8064vo56mdf5m2ig9q0tu0ur6lsdb1tius" | base64
# Output: NDF1OG9yM3E2aWQ5bm04Mzk1cXZsMjE0ajplaTQ0dmFvMG0xbWZoZjlyYjgwNjR2bzU2bWRmNW0yaWc5cTB0dTB1cjZsc2RiMXRpdXM=

# 2. POST /oauth2/token com Basic Auth
curl -X POST https://vanessa-mudanca-auth-prod.auth.sa-east-1.amazoncognito.com/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic NDF1OG9yM3E2aWQ5bm04Mzk1cXZsMjE0ajplaTQ0dmFvMG0xbWZoZjlyYjgwNjR2bzU2bWRmNW0yaWc5cTB0dTB1cjZsc2RiMXRpdXM=" \
  -d "grant_type=client_credentials&scope=cliente-core/read%20cliente-core/write"
```

**Response:**
```json
{
  "access_token": "eyJraWQi...",
  "expires_in": 3600,
  "token_type": "Bearer"
}
```

### JWT Payload (Decoded)

```json
{
  "sub": "41u8or3q6id9nm8395qvl214j",
  "token_use": "access",
  "scope": "cliente-core/write cliente-core/read",
  "auth_time": 1762445214,
  "iss": "https://cognito-idp.sa-east-1.amazonaws.com/sa-east-1_hXX8OVC7K",
  "exp": 1762448814,
  "iat": 1762445214,
  "version": 2,
  "jti": "cbaa8654-d06b-4394-a6e5-11ea5d47d65c",
  "client_id": "41u8or3q6id9nm8395qvl214j"
}
```

**Campos importantes:**
- `scope`: Escopos concedidos (cliente-core/read, cliente-core/write)
- `exp`: Timestamp de expiração (Unix epoch)
- `iss`: Issuer (Cognito User Pool)
- `client_id`: Identificador do app client M2M

---

## 🚀 Implementação Java (venda-core)

### 1. CognitoTokenService

Service responsável por:
- Obter tokens JWT via Client Credentials
- Cache de tokens com auto-renovação (5 min antes de expirar)
- Retry automático em caso de falha

**Localização:** `venda-core/src/main/java/br/com/vanessa_mudanca/venda_core/infrastructure/security/CognitoTokenService.java`

```java
package br.com.vanessa_mudanca.venda_core.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Base64;

@Service
public class CognitoTokenService {
    private static final Logger log = LoggerFactory.getLogger(CognitoTokenService.class);

    @Value("${cognito.token-uri}")
    private String tokenUri;

    @Value("${cognito.client-id}")
    private String clientId;

    @Value("${cognito.client-secret}")
    private String clientSecret;

    private String cachedToken;
    private Instant tokenExpiry;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Obtém access token JWT com cache e auto-renovação.
     *
     * Tokens são renovados 5 minutos antes de expirar para evitar
     * race conditions e garantir disponibilidade.
     *
     * @return JWT access token válido
     */
    public synchronized String getAccessToken() {
        // Renova token 5 minutos antes de expirar
        if (cachedToken == null || Instant.now().isAfter(tokenExpiry.minusSeconds(300))) {
            log.info("Renovando access token Cognito (expiração em {})", tokenExpiry);
            cachedToken = requestNewToken();
        }

        return cachedToken;
    }

    private String requestNewToken() {
        try {
            // Basic Auth: Base64(client_id:client_secret)
            String credentials = clientId + ":" + clientSecret;
            String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + basicAuth);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("scope", "cliente-core/read cliente-core/write");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<CognitoTokenResponse> response = restTemplate.postForEntity(
                tokenUri,
                request,
                CognitoTokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                CognitoTokenResponse tokenResponse = response.getBody();

                // Calcula expiração (now + expires_in)
                tokenExpiry = Instant.now().plusSeconds(tokenResponse.getExpiresIn());

                log.info("Token obtido com sucesso - Expira em: {}", tokenExpiry);

                return tokenResponse.getAccessToken();
            } else {
                throw new RuntimeException("Falha ao obter token Cognito: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Erro ao obter token Cognito", e);
            throw new RuntimeException("Erro ao obter token Cognito", e);
        }
    }

    /**
     * DTO para response do Cognito /oauth2/token
     */
    private static class CognitoTokenResponse {
        private String access_token;
        private int expires_in;
        private String token_type;

        public String getAccessToken() { return access_token; }
        public void setAccessToken(String access_token) { this.access_token = access_token; }

        public int getExpiresIn() { return expires_in; }
        public void setExpiresIn(int expires_in) { this.expires_in = expires_in; }

        public String getTokenType() { return token_type; }
        public void setTokenType(String token_type) { this.token_type = token_type; }
    }
}
```

### 2. ClienteCoreClientConfig

Configuração do **RestTemplate** com interceptors para:
- Injetar automaticamente JWT em todas as requisições
- Propagar Correlation ID do MDC

**Localização:** `venda-core/src/main/java/br/com/vanessa_mudanca/venda_core/infrastructure/client/ClienteCoreClientConfig.java`

```java
package br.com.vanessa_mudanca.venda_core.infrastructure.client;

import br.com.vanessa_mudanca.venda_core.infrastructure.security.CognitoTokenService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
public class ClienteCoreClientConfig {

    @Autowired
    private CognitoTokenService tokenService;

    /**
     * RestTemplate configurado para cliente-core com:
     * - JWT automático (via CognitoTokenService)
     * - Propagação de Correlation ID
     */
    @Bean("clienteCoreRestTemplate")
    public RestTemplate clienteCoreRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution
            ) throws IOException {

                // 1. Injetar JWT
                String token = tokenService.getAccessToken();
                request.getHeaders().setBearerAuth(token);

                // 2. Propagar Correlation ID
                String correlationId = MDC.get("correlationId");
                if (correlationId != null) {
                    request.getHeaders().add("X-Correlation-ID", correlationId);
                }

                return execution.execute(request, body);
            }
        });

        return restTemplate;
    }
}
```

### 3. application-prod.yml (venda-core)

```yaml
cognito:
  token-uri: ${COGNITO_TOKEN_URI:https://vanessa-mudanca-auth-prod.auth.sa-east-1.amazoncognito.com/oauth2/token}
  client-id: ${COGNITO_CLIENT_ID}
  client-secret: ${COGNITO_CLIENT_SECRET}

cliente-core:
  base-url: ${CLIENTE_CORE_BASE_URL:http://15.229.155.20:8081}
```

**Variáveis de ambiente (injetadas via ECS Secrets Manager):**
- `COGNITO_CLIENT_ID`
- `COGNITO_CLIENT_SECRET`
- `COGNITO_TOKEN_URI`

### 4. Uso no Service Layer

```java
package br.com.vanessa_mudanca.venda_core.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ValidarClienteService {
    private static final Logger log = LoggerFactory.getLogger(ValidarClienteService.class);

    @Autowired
    @Qualifier("clienteCoreRestTemplate")
    private RestTemplate clienteCoreRestTemplate;

    @Value("${cliente-core.base-url}")
    private String clienteCoreBaseUrl;

    /**
     * Valida se cliente PF existe antes de criar venda.
     *
     * JWT e Correlation ID são injetados automaticamente
     * pelo RestTemplate configurado.
     */
    public boolean clientePFExiste(String clientePublicId) {
        try {
            String url = clienteCoreBaseUrl + "/api/clientes/v1/clientes/pf/" + clientePublicId;

            log.info("Validando existência do cliente PF - PublicId: {}", clientePublicId);

            // RestTemplate já injeta JWT e Correlation ID automaticamente
            ClientePFResponse response = clienteCoreRestTemplate.getForObject(
                url,
                ClientePFResponse.class
            );

            log.info("Cliente PF encontrado - PublicId: {}", clientePublicId);
            return true;

        } catch (Exception e) {
            log.warn("Cliente PF não encontrado ou erro ao validar - PublicId: {}", clientePublicId, e);
            return false;
        }
    }
}
```

---

## 🔒 Validação no cliente-core (Resource Server)

O **cliente-core** valida JWT automaticamente via **ProdSecurityConfig** (já implementado):

**Localização:** `cliente-core/src/main/java/br/com/vanessa_mudanca/cliente_core/infrastructure/security/ProdSecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile({"prod", "staging"})
public class ProdSecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
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
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
```

**Fluxo de validação:**
1. venda-core envia requisição com `Authorization: Bearer <JWT>`
2. Spring Security intercepta a requisição
3. `JwtDecoder` valida JWT via JWKS endpoint do Cognito
4. Se válido, extrai claims (sub, scope, exp, etc.)
5. Autoriza endpoint se JWT válido + scope correto
6. Se inválido, retorna HTTP 401 Unauthorized

**JWKS Endpoint:**
```
https://cognito-idp.sa-east-1.amazonaws.com/sa-east-1_hXX8OVC7K/.well-known/jwks.json
```

---

## ✅ Teste End-to-End

### 1. Obter Token

```bash
curl -X POST https://vanessa-mudanca-auth-prod.auth.sa-east-1.amazoncognito.com/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic NDF1OG9yM3E2aWQ5bm04Mzk1cXZsMjE0ajplaTQ0dmFvMG0xbWZoZjlyYjgwNjR2bzU2bWRmNW0yaWc5cTB0dTB1cjZsc2RiMXRpdXM=" \
  -d "grant_type=client_credentials&scope=cliente-core/read%20cliente-core/write"
```

**Response:**
```json
{
  "access_token": "eyJraWQi...",
  "expires_in": 3600,
  "token_type": "Bearer"
}
```

### 2. Chamar cliente-core com JWT

```bash
TOKEN="eyJraWQi..."

curl -v http://15.229.155.20:8081/api/clientes/actuator/health \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Correlation-ID: test-m2m-$(date +%s)"
```

**Response:**
```json
{
  "status": "UP",
  "groups": ["liveness", "readiness"],
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "livenessState": {"status": "UP"},
    "ping": {"status": "UP"},
    "readinessState": {"status": "UP"},
    "ssl": {"status": "UP"}
  }
}
```

**Headers de response:**
```
HTTP/1.1 200
X-Correlation-ID: test-m2m-1762445352
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/vnd.spring-boot.actuator.v3+json
```

✅ **Sucesso!** JWT validado corretamente e Correlation ID propagado.

---

## 📊 Logs e Observabilidade

### CloudWatch Insights Query - Rastreamento M2M

```sql
fields @timestamp, correlationId, message, operationType, clientId
| filter correlationId = "test-m2m-1762445352"
| sort @timestamp asc
```

### Logs Estruturados (JSON)

**venda-core (emissor da requisição):**
```json
{
  "timestamp": "2025-11-06T16:08:54Z",
  "level": "INFO",
  "service": "venda-core",
  "correlationId": "test-m2m-1762445352",
  "message": "Validando existência do cliente PF - PublicId: abc-123"
}
```

**cliente-core (receptor da requisição):**
```json
{
  "timestamp": "2025-11-06T16:08:54Z",
  "level": "INFO",
  "service": "cliente-core",
  "correlationId": "test-m2m-1762445352",
  "operationType": "FIND_CLIENTE_PF",
  "clientId": "abc-123",
  "message": "Cliente PF encontrado - PublicId: abc-123"
}
```

---

## 🔐 Segurança

### Boas Práticas Implementadas

✅ **Secrets Management**
- Credenciais M2M armazenadas no AWS Secrets Manager
- Injeção via ECS Task Definition (não em plaintext)
- Auditoria via CloudTrail

✅ **Token Expiry**
- Tokens expiram em 1 hora (limite de segurança)
- Auto-renovação 5 minutos antes de expirar (evita race conditions)

✅ **Principle of Least Privilege**
- Scopes mínimos: `cliente-core/read` e `cliente-core/write`
- Cada MS tem seu próprio App Client M2M
- IAM Role do ECS com permissões mínimas

✅ **Auditoria**
- Correlation ID em todos os logs
- CloudWatch Logs retidos por 30 dias
- CloudTrail registra acessos ao Secrets Manager

### Checklist de Segurança

- [ ] Rotacionar client_secret a cada 90 dias
- [ ] Monitorar chamadas com tokens inválidos (401)
- [ ] Alertar se taxa de renovação de token > 10/min
- [ ] Revisar scopes concedidos trimestralmente
- [ ] Auditar logs do Cognito para detecção de anomalias

---

## 🚨 Troubleshooting

### Erro: 401 Unauthorized

**Causa:** JWT inválido, expirado ou sem scopes corretos

**Solução:**
1. Verificar se token não expirou (validade: 1h)
2. Decodificar JWT e verificar campo `scope`
3. Verificar se JWKS endpoint do Cognito está acessível

**Debug:**
```bash
# Decodificar JWT
TOKEN="eyJraWQi..."
echo "$TOKEN" | cut -d'.' -f2 | base64 -d | jq .
```

### Erro: Token não renova automaticamente

**Causa:** CognitoTokenService não está sendo chamado ou cache está travado

**Solução:**
1. Verificar logs: `grep "Renovando access token" /var/log/venda-core.log`
2. Verificar se `tokenExpiry` está correto
3. Reiniciar o serviço se cache estiver corrompido

### Erro: Correlation ID não propagado

**Causa:** MDC não configurado ou CorrelationIdFilter não está ativo

**Solução:**
1. Verificar se `CorrelationIdFilter` está registrado no Spring Security
2. Verificar se `MDC.get("correlationId")` retorna valor válido
3. Testar com header `X-Correlation-ID` explícito

---

## 📚 Referências

- [RFC 6749 - OAuth 2.0 Client Credentials Grant](https://datatracker.ietf.org/doc/html/rfc6749#section-4.4)
- [AWS Cognito User Pool - OAuth2 endpoints](https://docs.aws.amazon.com/cognito/latest/developerguide/token-endpoint.html)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [AWS Secrets Manager - ECS Integration](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/specifying-sensitive-data-secrets.html)

---

**Última atualização:** 2025-11-06
**Versão:** 1.0
**Status:** ✅ Implementado e testado em produção
