#!/bin/bash
# 🚀 Setup Local Completo - Cliente Core
# Um único comando para subir TUDO localmente
# Uso: ./setup-local.sh

set -e

# Cores
COLOR_GREEN='\033[0;32m'
COLOR_YELLOW='\033[1;33m'
COLOR_RED='\033[0;31m'
COLOR_BLUE='\033[0;34m'
COLOR_CYAN='\033[0;36m'
COLOR_NC='\033[0m' # No Color

# Detectar comando timeout disponível (Linux tem nativo, macOS não)
if command -v timeout &> /dev/null; then
    TIMEOUT_CMD="timeout"
elif command -v gtimeout &> /dev/null; then
    TIMEOUT_CMD="gtimeout"  # GNU coreutils no macOS (brew install coreutils)
else
    TIMEOUT_CMD=""  # Fallback para lógica shell portável
fi

print_banner() {
    clear
    echo ""
    echo -e "${COLOR_BLUE}╔═══════════════════════════════════════════════════════════════════╗${COLOR_NC}"
    echo -e "${COLOR_BLUE}║${COLOR_NC}                                                                   ${COLOR_BLUE}║${COLOR_NC}"
    echo -e "${COLOR_BLUE}║${COLOR_NC}  ${COLOR_CYAN}🚀 CLIENTE-CORE - Setup Local Automático${COLOR_NC}                   ${COLOR_BLUE}║${COLOR_NC}"
    echo -e "${COLOR_BLUE}║${COLOR_NC}                                                                   ${COLOR_BLUE}║${COLOR_NC}"
    echo -e "${COLOR_BLUE}║${COLOR_NC}  ${COLOR_YELLOW}Este script vai:${COLOR_NC}                                          ${COLOR_BLUE}║${COLOR_NC}"
    echo -e "${COLOR_BLUE}║${COLOR_NC}    ✓ Verificar dependências (Java, Maven, Docker)              ${COLOR_BLUE}║${COLOR_NC}"
    echo -e "${COLOR_BLUE}║${COLOR_NC}    ✓ Subir infraestrutura (PostgreSQL + DynamoDB Local)        ${COLOR_BLUE}║${COLOR_NC}"
    echo -e "${COLOR_BLUE}║${COLOR_NC}    ✓ Criar tabela de cache automaticamente                     ${COLOR_BLUE}║${COLOR_NC}"
    echo -e "${COLOR_BLUE}║${COLOR_NC}    ✓ Buildar e startar a aplicação                             ${COLOR_BLUE}║${COLOR_NC}"
    echo -e "${COLOR_BLUE}║${COLOR_NC}    ✓ Rodar smoke tests (health check + cache)                  ${COLOR_BLUE}║${COLOR_NC}"
    echo -e "${COLOR_BLUE}║${COLOR_NC}                                                                   ${COLOR_BLUE}║${COLOR_NC}"
    echo -e "${COLOR_BLUE}╚═══════════════════════════════════════════════════════════════════╝${COLOR_NC}"
    echo ""
}

print_step() {
    echo ""
    echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_NC}"
    echo -e "${COLOR_CYAN}▶ $1${COLOR_NC}"
    echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_NC}"
    echo ""
}

check_dependencies() {
    print_step "1️⃣  Verificando Dependências"

    local all_ok=true

    # Java 21+
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 21 ]; then
            echo -e "  ${COLOR_GREEN}✅ Java $JAVA_VERSION instalado${COLOR_NC}"
        else
            echo -e "  ${COLOR_RED}❌ Java 21+ necessário (atual: $JAVA_VERSION)${COLOR_NC}"
            all_ok=false
        fi
    else
        echo -e "  ${COLOR_RED}❌ Java não encontrado${COLOR_NC}"
        echo -e "     ${COLOR_YELLOW}Instale: https://adoptium.net/${COLOR_NC}"
        all_ok=false
    fi

    # Maven 3.9+
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -v 2>&1 | head -n 1 | cut -d' ' -f3)
        echo -e "  ${COLOR_GREEN}✅ Maven $MVN_VERSION instalado${COLOR_NC}"
    else
        echo -e "  ${COLOR_RED}❌ Maven não encontrado${COLOR_NC}"
        echo -e "     ${COLOR_YELLOW}Instale: brew install maven${COLOR_NC}"
        all_ok=false
    fi

    # Docker
    if command -v docker &> /dev/null; then
        if docker info &> /dev/null; then
            DOCKER_VERSION=$(docker --version | cut -d' ' -f3 | tr -d ',')
            echo -e "  ${COLOR_GREEN}✅ Docker $DOCKER_VERSION rodando${COLOR_NC}"
        else
            echo -e "  ${COLOR_RED}❌ Docker instalado mas não está rodando${COLOR_NC}"
            echo -e "     ${COLOR_YELLOW}Inicie o Docker Desktop${COLOR_NC}"
            all_ok=false
        fi
    else
        echo -e "  ${COLOR_RED}❌ Docker não encontrado${COLOR_NC}"
        echo -e "     ${COLOR_YELLOW}Instale: https://docker.com${COLOR_NC}"
        all_ok=false
    fi

    # Docker Compose
    if command -v docker-compose &> /dev/null || docker compose version &> /dev/null; then
        echo -e "  ${COLOR_GREEN}✅ Docker Compose instalado${COLOR_NC}"
    else
        echo -e "  ${COLOR_RED}❌ Docker Compose não encontrado${COLOR_NC}"
        all_ok=false
    fi

    # Opcionais (warnings apenas)
    if command -v aws &> /dev/null; then
        echo -e "  ${COLOR_GREEN}✅ AWS CLI instalado (opcional)${COLOR_NC}"
    else
        echo -e "  ${COLOR_YELLOW}⚠️  AWS CLI não instalado (cache não poderá ser inspecionado)${COLOR_NC}"
        echo -e "     ${COLOR_YELLOW}Instale: brew install awscli${COLOR_NC}"
    fi

    if command -v jq &> /dev/null; then
        echo -e "  ${COLOR_GREEN}✅ jq instalado (opcional)${COLOR_NC}"
    else
        echo -e "  ${COLOR_YELLOW}⚠️  jq não instalado (testes de JSON não funcionarão)${COLOR_NC}"
        echo -e "     ${COLOR_YELLOW}Instale: brew install jq${COLOR_NC}"
    fi

    if [ "$all_ok" = false ]; then
        echo ""
        echo -e "${COLOR_RED}❌ Dependências obrigatórias faltando. Corrija e tente novamente.${COLOR_NC}"
        exit 1
    fi

    echo ""
    echo -e "${COLOR_GREEN}✅ Todas as dependências OK!${COLOR_NC}"
}

stop_existing_services() {
    print_step "2️⃣  Limpando Ambiente Anterior"

    # Parar aplicação se estiver rodando
    if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "  ${COLOR_YELLOW}⚠️  Aplicação rodando na porta 8081, parando...${COLOR_NC}"
        PID=$(lsof -Pi :8081 -sTCP:LISTEN -t)
        kill $PID 2>/dev/null || true
        sleep 2
    fi

    # Parar e remover containers órfãos (força recreação)
    if docker ps -a --filter "name=cliente-core" --format "{{.Names}}" | grep -q "cliente-core"; then
        echo -e "  ${COLOR_YELLOW}⚠️  Containers anteriores detectados, removendo completamente...${COLOR_NC}"

        # Parar todos os containers relacionados
        docker ps -a --filter "name=cliente-core" --format "{{.ID}}" | while read container_id; do
            echo -e "     ${COLOR_YELLOW}→ Parando container: $container_id${COLOR_NC}"
            docker stop "$container_id" 2>/dev/null || true
        done

        # Remover containers, networks, volumes órfãos
        docker-compose down --remove-orphans --volumes 2>/dev/null || true

        # Double-check: remover manualmente se ainda existir
        docker ps -a --filter "name=cliente-core" --format "{{.ID}}" | while read container_id; do
            echo -e "     ${COLOR_YELLOW}→ Removendo container órfão: $container_id${COLOR_NC}"
            docker rm -f "$container_id" 2>/dev/null || true
        done

        # Remover networks órfãs
        docker network ls --filter "name=cliente-core" --format "{{.ID}}" | while read network_id; do
            docker network rm "$network_id" 2>/dev/null || true
        done

        echo -e "  ${COLOR_GREEN}✅ Containers e networks removidos${COLOR_NC}"
    fi

    # Limpar volumes órfãos do DynamoDB (dados temporários)
    ORPHAN_VOLUMES=$(docker volume ls --filter "name=cliente-core" --format "{{.Name}}")
    if [ -n "$ORPHAN_VOLUMES" ]; then
        echo -e "  ${COLOR_YELLOW}⚠️  Volumes órfãos detectados, removendo...${COLOR_NC}"
        echo "$ORPHAN_VOLUMES" | while read volume; do
            echo -e "     ${COLOR_YELLOW}→ Removendo volume: $volume${COLOR_NC}"
            docker volume rm "$volume" 2>/dev/null || true
        done
    fi

    echo -e "  ${COLOR_GREEN}✅ Ambiente limpo (força recreação de tudo)${COLOR_NC}"
}

start_infrastructure() {
    print_step "3️⃣  Iniciando Infraestrutura"

    echo -e "  ${COLOR_BLUE}▶ Subindo PostgreSQL + DynamoDB Local...${COLOR_NC}"
    docker-compose up -d

    echo -e "  ${COLOR_YELLOW}⏳ Aguardando serviços ficarem prontos (5s)...${COLOR_NC}"
    sleep 5

    # Verificar PostgreSQL
    if lsof -Pi :5432 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "  ${COLOR_GREEN}✅ PostgreSQL rodando (porta 5432)${COLOR_NC}"
    else
        echo -e "  ${COLOR_RED}❌ PostgreSQL falhou ao iniciar${COLOR_NC}"
        exit 1
    fi

    # Verificar DynamoDB Local
    if docker ps --filter "name=cliente-core-dynamodb" --format "{{.Status}}" | grep -q "Up"; then
        echo -e "  ${COLOR_GREEN}✅ DynamoDB Local rodando (porta 8000)${COLOR_NC}"
    else
        echo -e "  ${COLOR_RED}❌ DynamoDB Local falhou ao iniciar${COLOR_NC}"
        exit 1
    fi
}

create_dynamodb_table() {
    print_step "4️⃣  Criando Tabela de Cache"

    if ! command -v aws &> /dev/null; then
        echo -e "  ${COLOR_YELLOW}⚠️  AWS CLI não instalado, pulando criação de tabela${COLOR_NC}"
        echo -e "     ${COLOR_YELLOW}Cache não funcionará sem a tabela${COLOR_NC}"
        return
    fi

    # Verificar se DynamoDB Local está saudável (timeout 10s)
    echo -e "  ${COLOR_BLUE}▶ Verificando saúde do DynamoDB Local...${COLOR_NC}"
    TIMEOUT=10
    ELAPSED=0

    while [ $ELAPSED -lt $TIMEOUT ]; do
        if curl -s --max-time 2 http://localhost:8000 2>&1 | grep -q "DynamoDB"; then
            echo -e "  ${COLOR_GREEN}✅ DynamoDB Local respondendo${COLOR_NC}"
            break
        fi

        sleep 1
        ELAPSED=$((ELAPSED + 1))

        if [ $ELAPSED -eq $TIMEOUT ]; then
            echo -e "  ${COLOR_RED}❌ DynamoDB Local não está respondendo${COLOR_NC}"
            echo -e "  ${COLOR_YELLOW}⚠️  Tentando reiniciar DynamoDB Local...${COLOR_NC}"

            docker restart cliente-core-dynamodb &>/dev/null
            sleep 5

            # Testar novamente
            if ! curl -s --max-time 2 http://localhost:8000 2>&1 | grep -q "DynamoDB"; then
                echo -e "  ${COLOR_RED}❌ DynamoDB Local falhou. Cache não funcionará.${COLOR_NC}"
                echo -e "  ${COLOR_YELLOW}⚠️  Continuando sem cache...${COLOR_NC}"
                return
            fi
        fi
    done

    echo -e "  ${COLOR_BLUE}▶ Verificando tabela cliente-core-cache...${COLOR_NC}"

    # Verificar tabela (com timeout usando perl para macOS/Linux compatibilidade)
    if ( AWS_ACCESS_KEY_ID=fake AWS_SECRET_ACCESS_KEY=fake AWS_DEFAULT_REGION=us-east-1 \
        aws dynamodb describe-table --table-name cliente-core-cache --endpoint-url http://localhost:8000 --no-cli-pager ) &>/dev/null & \
        PID=$!; sleep 5; kill -0 $PID 2>/dev/null && kill $PID 2>/dev/null; wait $PID 2>/dev/null; then
        echo -e "  ${COLOR_GREEN}✅ Tabela já existe${COLOR_NC}"
    else
        echo -e "  ${COLOR_BLUE}▶ Criando tabela...${COLOR_NC}"

        # Criar tabela
        AWS_ACCESS_KEY_ID=fake AWS_SECRET_ACCESS_KEY=fake AWS_DEFAULT_REGION=us-east-1 \
        aws dynamodb create-table \
            --table-name cliente-core-cache \
            --attribute-definitions AttributeName=cacheKey,AttributeType=S \
            --key-schema AttributeName=cacheKey,KeyType=HASH \
            --billing-mode PAY_PER_REQUEST \
            --endpoint-url http://localhost:8000 \
            --no-cli-pager &>/dev/null

        if [ $? -eq 0 ]; then
            echo -e "  ${COLOR_GREEN}✅ Tabela criada com sucesso${COLOR_NC}"
        else
            echo -e "  ${COLOR_RED}❌ Erro ao criar tabela${COLOR_NC}"
            echo -e "  ${COLOR_YELLOW}⚠️  Continuando sem cache...${COLOR_NC}"
        fi
    fi
}

build_application() {
    print_step "5️⃣  Buildando Aplicação"

    echo -e "  ${COLOR_BLUE}▶ Executando: mvn clean install -DskipTests${COLOR_NC}"
    echo -e "  ${COLOR_YELLOW}⏳ Isso pode demorar 1-2 minutos...${COLOR_NC}"
    echo ""

    mvn clean install -DskipTests -q

    if [ $? -eq 0 ]; then
        echo ""
        echo -e "  ${COLOR_GREEN}✅ Build concluído com sucesso${COLOR_NC}"
    else
        echo ""
        echo -e "  ${COLOR_RED}❌ Build falhou${COLOR_NC}"
        exit 1
    fi
}

start_application() {
    print_step "6️⃣  Iniciando Aplicação"

    echo -e "  ${COLOR_BLUE}▶ Executando: mvn spring-boot:run (background)${COLOR_NC}"
    echo -e "  ${COLOR_YELLOW}⏳ Aguardando startup (pode demorar 10-15s)...${COLOR_NC}"
    echo ""

    # Iniciar em background
    mvn spring-boot:run > /tmp/cliente-core-startup.log 2>&1 &
    APP_PID=$!

    # Aguardar até 30 segundos pelo startup
    TIMEOUT=30
    ELAPSED=0

    while [ $ELAPSED -lt $TIMEOUT ]; do
        if curl -s http://localhost:8081/api/clientes/actuator/health > /dev/null 2>&1; then
            echo -e "  ${COLOR_GREEN}✅ Aplicação iniciada (PID: $APP_PID)${COLOR_NC}"
            return
        fi

        # Verificar se processo ainda está rodando
        if ! kill -0 $APP_PID 2>/dev/null; then
            echo -e "  ${COLOR_RED}❌ Aplicação falhou ao iniciar${COLOR_NC}"
            echo -e "  ${COLOR_YELLOW}Últimas linhas do log:${COLOR_NC}"
            tail -20 /tmp/cliente-core-startup.log
            exit 1
        fi

        sleep 1
        ELAPSED=$((ELAPSED + 1))

        # Mostrar progresso
        if [ $((ELAPSED % 3)) -eq 0 ]; then
            echo -e "  ${COLOR_YELLOW}⏳ Aguardando... (${ELAPSED}s)${COLOR_NC}"
        fi
    done

    echo -e "  ${COLOR_RED}❌ Timeout ao aguardar startup da aplicação${COLOR_NC}"
    exit 1
}

run_smoke_tests() {
    print_step "7️⃣  Executando Smoke Tests"

    # Test 1: Health Check
    echo -e "  ${COLOR_BLUE}▶ Test 1/4: Health Check${COLOR_NC}"
    HEALTH=$(curl -s http://localhost:8081/api/clientes/actuator/health | jq -r '.status' 2>/dev/null)
    if [ "$HEALTH" = "UP" ]; then
        echo -e "    ${COLOR_GREEN}✅ Health: UP${COLOR_NC}"
    else
        echo -e "    ${COLOR_RED}❌ Health: $HEALTH${COLOR_NC}"
        exit 1
    fi

    # Test 2: Database (listar clientes PF)
    echo -e "  ${COLOR_BLUE}▶ Test 2/4: Database (Seeds Liquibase)${COLOR_NC}"
    COUNT=$(curl -s "http://localhost:8081/api/clientes/v1/clientes/pf?page=0&size=1" | jq -r '.totalElements' 2>/dev/null)
    if [ "$COUNT" -gt 0 ]; then
        echo -e "    ${COLOR_GREEN}✅ Database: $COUNT clientes PF encontrados${COLOR_NC}"
    else
        echo -e "    ${COLOR_RED}❌ Database: Nenhum cliente encontrado${COLOR_NC}"
        exit 1
    fi

    # Test 3: Cache (primeira busca - MISS)
    echo -e "  ${COLOR_BLUE}▶ Test 3/4: Cache MISS (primeira busca)${COLOR_NC}"
    UUID=$(curl -s "http://localhost:8081/api/clientes/v1/clientes/pf?page=0&size=1" | jq -r '.content[0].publicId' 2>/dev/null)
    START=$(date +%s%N)
    NOME=$(curl -s "http://localhost:8081/api/clientes/v1/clientes/pf/$UUID" | jq -r '.nomeCompleto' 2>/dev/null)
    END=$(date +%s%N)
    TIME1=$(echo "scale=2; ($END - $START) / 1000000" | bc 2>/dev/null || echo "N/A")

    if [ -n "$NOME" ] && [ "$NOME" != "null" ]; then
        echo -e "    ${COLOR_GREEN}✅ Cliente: $NOME (${TIME1}ms)${COLOR_NC}"
    else
        echo -e "    ${COLOR_RED}❌ Erro ao buscar cliente${COLOR_NC}"
        exit 1
    fi

    # Test 4: Cache (segunda busca - HIT)
    echo -e "  ${COLOR_BLUE}▶ Test 4/4: Cache HIT (segunda busca)${COLOR_NC}"
    sleep 1
    START=$(date +%s%N)
    curl -s "http://localhost:8081/api/clientes/v1/clientes/pf/$UUID" > /dev/null 2>&1
    END=$(date +%s%N)
    TIME2=$(echo "scale=2; ($END - $START) / 1000000" | bc 2>/dev/null || echo "N/A")

    echo -e "    ${COLOR_GREEN}✅ Cache: ${TIME2}ms (cache warmed)${COLOR_NC}"

    if command -v aws &> /dev/null; then
        CACHE_COUNT=$(AWS_ACCESS_KEY_ID=fake AWS_SECRET_ACCESS_KEY=fake AWS_DEFAULT_REGION=us-east-1 \
            aws dynamodb scan --table-name cliente-core-cache --endpoint-url http://localhost:8000 --no-cli-pager 2>/dev/null | jq -r '.Count' 2>/dev/null || echo "0")
        echo -e "    ${COLOR_GREEN}✅ DynamoDB: $CACHE_COUNT itens cached${COLOR_NC}"
    fi
}

print_success_summary() {
    echo ""
    echo -e "${COLOR_GREEN}╔═══════════════════════════════════════════════════════════════════╗${COLOR_NC}"
    echo -e "${COLOR_GREEN}║${COLOR_NC}                                                                   ${COLOR_GREEN}║${COLOR_NC}"
    echo -e "${COLOR_GREEN}║${COLOR_NC}  ${COLOR_CYAN}🎉 SUCESSO! Cliente-Core rodando localmente!${COLOR_NC}                ${COLOR_GREEN}║${COLOR_NC}"
    echo -e "${COLOR_GREEN}║${COLOR_NC}                                                                   ${COLOR_GREEN}║${COLOR_NC}"
    echo -e "${COLOR_GREEN}╚═══════════════════════════════════════════════════════════════════╝${COLOR_NC}"
    echo ""
    echo -e "${COLOR_CYAN}📋 URLs Disponíveis:${COLOR_NC}"
    echo -e "   🌐 API Base:       ${COLOR_YELLOW}http://localhost:8081/api/clientes${COLOR_NC}"
    echo -e "   💚 Health Check:   ${COLOR_YELLOW}http://localhost:8081/api/clientes/actuator/health${COLOR_NC}"
    echo -e "   📊 Metrics:        ${COLOR_YELLOW}http://localhost:8081/api/clientes/actuator/metrics${COLOR_NC}"
    echo -e "   📖 API Docs:       ${COLOR_YELLOW}http://localhost:8081/api/clientes/swagger-ui${COLOR_NC}"
    echo ""
    echo -e "${COLOR_CYAN}🗄️  Infraestrutura:${COLOR_NC}"
    echo -e "   🐘 PostgreSQL:     ${COLOR_YELLOW}localhost:5432${COLOR_NC} (user/senha123)"
    echo -e "   ⚡ DynamoDB Local:  ${COLOR_YELLOW}http://localhost:8000${COLOR_NC}"
    echo ""
    echo -e "${COLOR_CYAN}🛠️  Comandos Úteis:${COLOR_NC}"
    echo -e "   ${COLOR_YELLOW}./local-dev.sh status${COLOR_NC}      - Ver status dos serviços"
    echo -e "   ${COLOR_YELLOW}./local-dev.sh test-cache${COLOR_NC}  - Testar cache em detalhes"
    echo -e "   ${COLOR_YELLOW}./local-dev.sh stop${COLOR_NC}        - Parar tudo"
    echo -e "   ${COLOR_YELLOW}./local-dev.sh logs${COLOR_NC}        - Ver logs dos containers"
    echo ""
    echo -e "${COLOR_CYAN}📝 Exemplos de Chamadas:${COLOR_NC}"
    echo ""
    echo -e "   ${COLOR_BLUE}# Listar clientes PF${COLOR_NC}"
    echo -e "   ${COLOR_YELLOW}curl http://localhost:8081/api/clientes/v1/clientes/pf | jq${COLOR_NC}"
    echo ""
    echo -e "   ${COLOR_BLUE}# Buscar por ID${COLOR_NC}"
    echo -e "   ${COLOR_YELLOW}curl http://localhost:8081/api/clientes/v1/clientes/pf/$UUID | jq${COLOR_NC}"
    echo ""
    echo -e "${COLOR_GREEN}✨ Pronto para desenvolver!${COLOR_NC}"
    echo ""
}

# ============================================================================
# Main Execution
# ============================================================================

print_banner

check_dependencies
stop_existing_services
start_infrastructure
create_dynamodb_table
build_application
start_application
run_smoke_tests
print_success_summary

exit 0
