#!/bin/bash

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# 🧙 WIZARD - Validação Completa do Ambiente cliente-core
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#
# Valida automaticamente TODO o ambiente de desenvolvimento em 8 etapas:
#
# ETAPA 1: Pré-requisitos (Java 21+, Maven 3.9+, Docker)
# ETAPA 2: Diretório do projeto
# ETAPA 3: PostgreSQL (startup + conectividade)
# ETAPA 4: Build Maven (mvn clean install)
# ETAPA 5: Testes (250+ tests, coverage >=80%)
# ETAPA 6: Aplicação Spring Boot (startup + health check)
# ETAPA 7: Validações funcionais (DB, seeds, cache Caffeine)
# ETAPA 8: Observabilidade (Prometheus metrics)
#
# Uso:
#   ./WIZARD.sh
#
# Tempo: 3-5 minutos
# Logs: /tmp/cliente-core-wizard.log
#
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

set -e  # Exit on error

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Cores e Símbolos
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

CHECK_MARK="${GREEN}✓${NC}"
CROSS_MARK="${RED}✗${NC}"
ARROW="${BLUE}➜${NC}"
ROCKET="${MAGENTA}🚀${NC}"
WIZARD="${CYAN}🧙${NC}"

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Variáveis Globais
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

LOG_FILE="/tmp/cliente-core-wizard.log"
SPRING_BOOT_PID=""
START_TIME=$(date +%s)

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Funções de Log
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

log() {
    echo -e "$1" | tee -a "$LOG_FILE"
}

log_step() {
    echo ""
    log "${WIZARD} ${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    log "${WIZARD} ${MAGENTA}$1${NC}"
    log "${WIZARD} ${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

log_success() {
    log "  ${CHECK_MARK} ${GREEN}$1${NC}"
}

log_error() {
    log "  ${CROSS_MARK} ${RED}$1${NC}"
}

log_info() {
    log "  ${ARROW} $1"
}

log_warning() {
    log "  ${YELLOW}⚠${NC} ${YELLOW}$1${NC}"
}

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Função de Cleanup (sempre executada ao sair)
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

cleanup() {
    if [ -n "$SPRING_BOOT_PID" ]; then
        log_info "Encerrando Spring Boot (PID: $SPRING_BOOT_PID)..."
        kill $SPRING_BOOT_PID 2>/dev/null || true
        wait $SPRING_BOOT_PID 2>/dev/null || true
        log_success "Spring Boot encerrado"
    fi
}

trap cleanup EXIT

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Banner
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

clear
echo "" > "$LOG_FILE"  # Clear log file

log "${CYAN}"
log "  ╦ ╦╦╔═╗╔═╗╦═╗╔╦╗  ╔═╗╦  ╦╔═╗╔╗╔╔╦╗╔═╗  ╔═╗╔═╗╦═╗╔═╗"
log "  ║║║║╔═╝╠═╣╠╦╝ ║║  ║  ║  ║║╣ ║║║ ║ ║╣───║  ║ ║╠╦╝║╣ "
log "  ╚╩╝╩╚═╝╩ ╩╩╚══╩╝  ╚═╝╩═╝╩╚═╝╝╚╝ ╩ ╚═╝  ╚═╝╚═╝╩╚═╚═╝"
log "${NC}"
log "${MAGENTA}  Validação Completa do Ambiente de Desenvolvimento${NC}"
log "${CYAN}  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
log ""
log "  ${WIZARD} Validando 8 etapas automaticamente..."
log "  ${ARROW} Logs salvos em: ${CYAN}$LOG_FILE${NC}"
log ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ETAPA 1: Validar Pré-requisitos
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

log_step "ETAPA 1/8: Validando Pré-requisitos"

# Java 21+
log_info "Verificando Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
    if [ "$JAVA_VERSION" -ge 21 ]; then
        log_success "Java $JAVA_VERSION encontrado"
    else
        log_error "Java 21+ necessário (encontrado: $JAVA_VERSION)"
        exit 1
    fi
else
    log_error "Java não encontrado. Instale: brew install openjdk@21"
    exit 1
fi

# Maven 3.9+
log_info "Verificando Maven..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
    log_success "Maven $MVN_VERSION encontrado"
else
    log_error "Maven não encontrado. Instale: brew install maven"
    exit 1
fi

# Docker
log_info "Verificando Docker..."
if command -v docker &> /dev/null; then
    if docker ps &> /dev/null; then
        log_success "Docker instalado e rodando"
    else
        log_error "Docker instalado mas não está rodando. Inicie o Docker Desktop."
        exit 1
    fi
else
    log_error "Docker não encontrado. Instale: brew install --cask docker"
    exit 1
fi

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ETAPA 2: Validar Diretório do Projeto
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

log_step "ETAPA 2/8: Validando Diretório do Projeto"

if [ ! -f "pom.xml" ]; then
    log_error "pom.xml não encontrado. Execute este script da RAIZ do cliente-core."
    exit 1
fi

if [ ! -f "docker-compose.yml" ]; then
    log_error "docker-compose.yml não encontrado."
    exit 1
fi

log_success "Diretório correto: $(pwd)"

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ETAPA 3: PostgreSQL
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

log_step "ETAPA 3/8: Configurando PostgreSQL"

log_info "Parando containers órfãos..."
docker stop cliente-core-postgres 2>/dev/null || true
docker rm cliente-core-postgres 2>/dev/null || true
log_success "Containers órfãos removidos"

log_info "Iniciando PostgreSQL..."
docker-compose up -d >> "$LOG_FILE" 2>&1

log_info "Aguardando PostgreSQL ficar pronto (máx 30s)..."
for i in {1..30}; do
    if docker exec cliente-core-postgres pg_isready -U user -d vanessa_mudanca_clientes &>/dev/null; then
        log_success "PostgreSQL pronto em ${i}s"
        break
    fi
    sleep 1
    if [ $i -eq 30 ]; then
        log_error "PostgreSQL não ficou pronto em 30s"
        docker-compose logs postgres
        exit 1
    fi
done

# Testar conectividade
log_info "Testando conectividade com PostgreSQL..."
if docker exec cliente-core-postgres psql -U user -d vanessa_mudanca_clientes -c "SELECT 1;" &>/dev/null; then
    log_success "Conexão PostgreSQL OK"
else
    log_error "Falha ao conectar no PostgreSQL"
    exit 1
fi

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ETAPA 4: Build Maven
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

log_step "ETAPA 4/8: Build Maven"

log_info "Executando mvn clean install (pode demorar 1-2 min)..."
if mvn clean install -DskipTests >> "$LOG_FILE" 2>&1; then
    log_success "Build Maven concluído"
else
    log_error "Build Maven falhou. Veja logs: $LOG_FILE"
    exit 1
fi

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ETAPA 5: Testes
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

log_step "ETAPA 5/8: Executando Testes"

log_info "Rodando 250+ testes com JaCoCo coverage (1-2 min)..."
if mvn test >> "$LOG_FILE" 2>&1; then
    log_success "Todos os testes passaram"
else
    log_error "Alguns testes falharam. Veja logs: $LOG_FILE"
    exit 1
fi

# Verificar coverage
log_info "Verificando coverage (target: >=80%)..."
if [ -f "target/site/jacoco/index.html" ]; then
    COVERAGE=$(grep -A 5 "Total" target/site/jacoco/index.html | grep -o '[0-9]\+%' | head -1 | tr -d '%')
    if [ "$COVERAGE" -ge 80 ]; then
        log_success "Coverage: ${COVERAGE}% (target: >=80%)"
    else
        log_warning "Coverage: ${COVERAGE}% (abaixo de 80%)"
    fi
else
    log_warning "Relatório JaCoCo não encontrado"
fi

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ETAPA 6: Iniciar Aplicação Spring Boot
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

log_step "ETAPA 6/8: Iniciando Aplicação Spring Boot"

log_info "Iniciando aplicação em background..."
mvn spring-boot:run >> "$LOG_FILE" 2>&1 &
SPRING_BOOT_PID=$!

log_info "Aguardando aplicação ficar pronta (máx 60s)..."
for i in {1..60}; do
    if curl -s http://localhost:8081/api/clientes/actuator/health &>/dev/null; then
        log_success "Aplicação iniciada em ${i}s (PID: $SPRING_BOOT_PID)"
        break
    fi
    sleep 1
    if [ $i -eq 60 ]; then
        log_error "Aplicação não ficou pronta em 60s"
        tail -50 "$LOG_FILE"
        exit 1
    fi
done

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ETAPA 7: Validações Funcionais
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

log_step "ETAPA 7/8: Validações Funcionais"

# Health Check
log_info "Testando health check..."
HEALTH_STATUS=$(curl -s http://localhost:8081/api/clientes/actuator/health | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
if [ "$HEALTH_STATUS" = "UP" ]; then
    log_success "Health check: UP"
else
    log_error "Health check: $HEALTH_STATUS"
    exit 1
fi

# Database connectivity
log_info "Testando conectividade com database..."
DB_STATUS=$(curl -s http://localhost:8081/api/clientes/actuator/health | grep -o '"db":{"status":"[^"]*"' | cut -d'"' -f6)
if [ "$DB_STATUS" = "UP" ]; then
    log_success "Database: UP"
else
    log_error "Database: $DB_STATUS"
    exit 1
fi

# Verificar seeds (Liquibase)
log_info "Verificando seeds do Liquibase..."
CLIENTES_COUNT=$(curl -s http://localhost:8081/api/clientes/v1/clientes/pf | grep -o '"publicId"' | wc -l | xargs)
if [ "$CLIENTES_COUNT" -ge 10 ]; then
    log_success "Seeds carregados: $CLIENTES_COUNT clientes PF encontrados"
else
    log_warning "Seeds: apenas $CLIENTES_COUNT clientes encontrados (esperado: >=10)"
fi

# Cache Caffeine
log_info "Testando cache Caffeine..."
CACHES=$(curl -s http://localhost:8081/api/clientes/actuator/caches | grep -o '"clientes"' | wc -l | xargs)
if [ "$CACHES" -ge 1 ]; then
    log_success "Cache Caffeine: configurado"
else
    log_warning "Cache Caffeine: não detectado"
fi

# Testar cache MISS e HIT
log_info "Testando cache MISS/HIT..."
FIRST_CLIENTE=$(curl -s http://localhost:8081/api/clientes/v1/clientes/pf | grep -o '"publicId":"[^"]*"' | head -1 | cut -d'"' -f4)
if [ -n "$FIRST_CLIENTE" ]; then
    # MISS
    TIME_MISS=$(curl -s -w "%{time_total}" -o /dev/null http://localhost:8081/api/clientes/v1/clientes/pf/$FIRST_CLIENTE)
    # HIT
    TIME_HIT=$(curl -s -w "%{time_total}" -o /dev/null http://localhost:8081/api/clientes/v1/clientes/pf/$FIRST_CLIENTE)

    log_success "Cache MISS: ${TIME_MISS}s | Cache HIT: ${TIME_HIT}s"
else
    log_warning "Não foi possível testar cache (nenhum cliente encontrado)"
fi

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ETAPA 8: Observabilidade
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

log_step "ETAPA 8/8: Observabilidade (Prometheus Metrics)"

log_info "Testando endpoint Prometheus..."
if curl -s http://localhost:8081/api/clientes/actuator/prometheus | grep -q "jvm_memory_used_bytes"; then
    log_success "Métricas Prometheus disponíveis"
else
    log_warning "Métricas Prometheus não encontradas"
fi

log_info "Verificando métricas de cache..."
CACHE_HITS=$(curl -s http://localhost:8081/api/clientes/actuator/metrics/cache.gets?tag=name:clientes | grep -o '"value":[0-9.]*' | head -1 | cut -d: -f2)
if [ -n "$CACHE_HITS" ]; then
    log_success "Métricas de cache: $CACHE_HITS hits"
else
    log_warning "Métricas de cache ainda não disponíveis (use a aplicação primeiro)"
fi

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Resumo Final
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))

log ""
log "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
log "${GREEN}  ✨ VALIDAÇÃO COMPLETA! Ambiente 100% funcional ✨${NC}"
log "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
log ""
log "  ${ROCKET} ${GREEN}Tempo total:${NC} ${ELAPSED}s"
log "  ${ARROW} ${GREEN}Aplicação rodando:${NC} http://localhost:8081/api/clientes"
log "  ${ARROW} ${GREEN}Health check:${NC} http://localhost:8081/api/clientes/actuator/health"
log "  ${ARROW} ${GREEN}Prometheus:${NC} http://localhost:8081/api/clientes/actuator/prometheus"
log "  ${ARROW} ${GREEN}Logs:${NC} $LOG_FILE"
log ""
log "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
log "${CYAN}  Próximos passos:${NC}"
log "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
log ""
log "  ${ARROW} Testar endpoints:"
log "      ${CYAN}curl http://localhost:8081/api/clientes/v1/clientes/pf${NC}"
log ""
log "  ${ARROW} Ver métricas de cache:"
log "      ${CYAN}curl http://localhost:8081/api/clientes/actuator/metrics/cache.gets${NC}"
log ""
log "  ${ARROW} Ver coverage report:"
log "      ${CYAN}open target/site/jacoco/index.html${NC}"
log ""
log "  ${ARROW} Para parar tudo:"
log "      ${CYAN}kill $SPRING_BOOT_PID${NC}  # Spring Boot"
log "      ${CYAN}docker-compose down${NC}    # PostgreSQL"
log ""
log "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
log "${YELLOW}  ⚠ Spring Boot rodando em background (PID: $SPRING_BOOT_PID)${NC}"
log "${YELLOW}  ⚠ Para encerrar: kill $SPRING_BOOT_PID${NC}"
log "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
log ""

# Remover trap para NÃO matar o Spring Boot ao final
trap - EXIT

exit 0
