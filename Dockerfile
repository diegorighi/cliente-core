# ============================================================================
# Dockerfile - cliente-core (Multi-stage build for production)
# ============================================================================
# Stage 1: Build da aplicação com Maven
# Stage 2: Runtime com Amazon Corretto (otimizado para AWS)
# ============================================================================

# ============================================================================
# STAGE 1: BUILD
# ============================================================================
FROM maven:3.9.9-amazoncorretto-21 AS build

WORKDIR /app

# Copy apenas pom.xml primeiro (cache de dependências)
COPY pom.xml .

# Baixa dependências (layer separado para cache)
RUN mvn dependency:go-offline -B

# Copy código fonte
COPY src ./src

# Build da aplicação (skip tests para CI/CD)
RUN mvn clean package -DskipTests -B

# ============================================================================
# STAGE 2: RUNTIME
# ============================================================================
FROM amazoncorretto:21-alpine

# Metadata
LABEL maintainer="Va Nessa Mudança <dev@vanessamudanca.com.br>"
LABEL description="Microserviço de gestão de clientes - cliente-core"
LABEL version="0.1.0"

# Variáveis de ambiente (podem ser sobrescritas no ECS)
ENV JAVA_OPTS="-Xms512m -Xmx768m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV SPRING_PROFILES_ACTIVE=prod
ENV AWS_REGION=sa-east-1
ENV SERVER_PORT=8081

# Criar usuário não-root para segurança
RUN addgroup -g 1000 appgroup && \
    adduser -D -u 1000 -G appgroup appuser

# Criar diretório da aplicação
WORKDIR /app

# Copiar JAR do stage de build
COPY --from=build /app/target/cliente-core-*.jar app.jar

# Alterar ownership para usuário não-root
RUN chown -R appuser:appgroup /app

# Mudar para usuário não-root
USER appuser

# Health check (Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/clientes/actuator/health || exit 1

# Expor porta da aplicação
EXPOSE 8081

# Comando de inicialização
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
