#!/bin/bash
# Script helper para desenvolvimento local do cliente-core
# Uso: ./local-dev.sh [start|stop|restart|logs|status|test-cache]

set -e

COLOR_GREEN='\033[0;32m'
COLOR_YELLOW='\033[1;33m'
COLOR_RED='\033[0;31m'
COLOR_BLUE='\033[0;34m'
COLOR_NC='\033[0m' # No Color

print_header() {
    echo ""
    echo -e "${COLOR_BLUE}╔══════════════════════════════════════════════════════════════╗${COLOR_NC}"
    echo -e "${COLOR_BLUE}║${COLOR_NC}  🚚 Cliente-Core Local Development Helper                  ${COLOR_BLUE}║${COLOR_NC}"
    echo -e "${COLOR_BLUE}╚══════════════════════════════════════════════════════════════╝${COLOR_NC}"
    echo ""
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${COLOR_RED}❌ Docker não está instalado${COLOR_NC}"
        exit 1
    fi

    if ! docker info &> /dev/null; then
        echo -e "${COLOR_RED}❌ Docker daemon não está rodando${COLOR_NC}"
        exit 1
    fi
}

start_services() {
    echo -e "${COLOR_GREEN}🚀 Iniciando serviços...${COLOR_NC}"

    # Parar postgres se já estiver rodando na porta 5432
    if lsof -Pi :5432 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "${COLOR_YELLOW}⚠️  PostgreSQL já está rodando na porta 5432${COLOR_NC}"
        echo -e "${COLOR_YELLOW}   Se for Docker Compose, rode: docker-compose down${COLOR_NC}"
    fi

    # Subir DynamoDB Local
    docker-compose up -d dynamodb-local

    # Aguardar DynamoDB estar healthy
    echo -e "${COLOR_YELLOW}⏳ Aguardando DynamoDB Local estar pronto...${COLOR_NC}"
    sleep 3

    # Verificar se DynamoDB está rodando
    if docker-compose ps dynamodb-local | grep -q "Up"; then
        echo -e "${COLOR_GREEN}✅ DynamoDB Local rodando em http://localhost:8000${COLOR_NC}"
    else
        echo -e "${COLOR_RED}❌ Erro ao iniciar DynamoDB Local${COLOR_NC}"
        exit 1
    fi

    # Criar tabela de cache no DynamoDB Local
    echo -e "${COLOR_YELLOW}📦 Criando tabela de cache no DynamoDB Local...${COLOR_NC}"

    # Verificar se AWS CLI está instalado
    if ! command -v aws &> /dev/null; then
        echo -e "${COLOR_YELLOW}⚠️  AWS CLI não está instalado (cache não funcionará)${COLOR_NC}"
        echo -e "${COLOR_YELLOW}   Instale com: brew install awscli${COLOR_NC}"
    else
        # Verificar se tabela já existe
        if AWS_ACCESS_KEY_ID=fake AWS_SECRET_ACCESS_KEY=fake AWS_DEFAULT_REGION=us-east-1 \
            aws dynamodb describe-table --table-name cliente-core-cache --endpoint-url http://localhost:8000 &>/dev/null; then
            echo -e "${COLOR_GREEN}✅ Tabela cliente-core-cache já existe${COLOR_NC}"
        else
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
                echo -e "${COLOR_GREEN}✅ Tabela cliente-core-cache criada com sucesso${COLOR_NC}"
            else
                echo -e "${COLOR_RED}❌ Erro ao criar tabela (cache não funcionará)${COLOR_NC}"
            fi
        fi
    fi

    echo ""
    echo -e "${COLOR_GREEN}✅ Serviços prontos!${COLOR_NC}"
    echo ""
    echo -e "${COLOR_BLUE}📋 Próximos passos:${COLOR_NC}"
    echo -e "   1. ${COLOR_YELLOW}mvn spring-boot:run${COLOR_NC} (rodar aplicação)"
    echo -e "   2. ${COLOR_YELLOW}./local-dev.sh test-cache${COLOR_NC} (testar cache)"
    echo ""
}

stop_services() {
    echo -e "${COLOR_YELLOW}🛑 Parando e removendo serviços...${COLOR_NC}"

    # Parar aplicação Spring Boot se estiver rodando
    if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "  ${COLOR_YELLOW}→ Parando aplicação (porta 8081)${COLOR_NC}"
        PID=$(lsof -Pi :8081 -sTCP:LISTEN -t)
        kill $PID 2>/dev/null || true
        sleep 1
    fi

    # Parar containers
    docker ps -a --filter "name=cliente-core" --format "{{.ID}}" | while read container_id; do
        echo -e "  ${COLOR_YELLOW}→ Parando container: $container_id${COLOR_NC}"
        docker stop "$container_id" 2>/dev/null || true
    done

    # Remover containers, networks, volumes
    docker-compose down --remove-orphans --volumes 2>/dev/null || true

    # Remover containers órfãos manualmente
    docker ps -a --filter "name=cliente-core" --format "{{.ID}}" | while read container_id; do
        docker rm -f "$container_id" 2>/dev/null || true
    done

    # Remover networks órfãs
    docker network ls --filter "name=cliente-core" --format "{{.ID}}" | while read network_id; do
        docker network rm "$network_id" 2>/dev/null || true
    done

    echo -e "${COLOR_GREEN}✅ Serviços parados e removidos (limpeza completa)${COLOR_NC}"
}

restart_services() {
    stop_services
    start_services
}

show_logs() {
    echo -e "${COLOR_BLUE}📄 Logs dos serviços (Ctrl+C para sair)${COLOR_NC}"
    docker-compose logs -f
}

show_status() {
    echo -e "${COLOR_BLUE}📊 Status dos serviços:${COLOR_NC}"
    echo ""

    # DynamoDB Local
    if docker-compose ps dynamodb-local | grep -q "Up"; then
        echo -e "  ${COLOR_GREEN}✅ DynamoDB Local${COLOR_NC}"
        echo -e "     URL: ${COLOR_YELLOW}http://localhost:8000${COLOR_NC}"

        # Listar tabelas
        if command -v aws &> /dev/null; then
            echo -e "     Tabelas:"
            aws dynamodb list-tables --endpoint-url http://localhost:8000 --region us-east-1 --no-cli-pager 2>/dev/null | jq -r '.TableNames[]' | sed 's/^/       - /' || echo "       (nenhuma ainda)"
        fi
    else
        echo -e "  ${COLOR_RED}❌ DynamoDB Local (não rodando)${COLOR_NC}"
    fi

    echo ""

    # PostgreSQL
    if lsof -Pi :5432 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "  ${COLOR_GREEN}✅ PostgreSQL${COLOR_NC}"
        echo -e "     Port: ${COLOR_YELLOW}5432${COLOR_NC}"
    else
        echo -e "  ${COLOR_RED}❌ PostgreSQL (não rodando)${COLOR_NC}"
    fi

    echo ""

    # Aplicação Spring Boot
    if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "  ${COLOR_GREEN}✅ Spring Boot Application${COLOR_NC}"
        echo -e "     URL: ${COLOR_YELLOW}http://localhost:8081/api/clientes${COLOR_NC}"
    else
        echo -e "  ${COLOR_YELLOW}⚠️  Spring Boot Application (não rodando)${COLOR_NC}"
        echo -e "     Rode: ${COLOR_YELLOW}mvn spring-boot:run${COLOR_NC}"
    fi

    echo ""
}

test_cache() {
    echo -e "${COLOR_BLUE}🧪 Testando cache DynamoDB...${COLOR_NC}"
    echo ""

    # 🔒 PROTEÇÃO: Verificar se está em ambiente de desenvolvimento
    if ! docker ps --filter "name=cliente-core-dynamodb" --format "{{.Names}}" | grep -q "cliente-core-dynamodb"; then
        echo -e "${COLOR_RED}╔════════════════════════════════════════════════════════════╗${COLOR_NC}"
        echo -e "${COLOR_RED}║  ⚠️  ATENÇÃO: Este script é apenas para DESENVOLVIMENTO  ║${COLOR_NC}"
        echo -e "${COLOR_RED}║  DynamoDB Local não detectado - Ambiente pode ser PROD!   ║${COLOR_NC}"
        echo -e "${COLOR_RED}║  ABORTANDO por segurança para evitar poluir dados reais   ║${COLOR_NC}"
        echo -e "${COLOR_RED}╚════════════════════════════════════════════════════════════╝${COLOR_NC}"
        exit 1
    fi

    # Verificar se app está rodando
    if ! lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "${COLOR_RED}❌ Aplicação não está rodando em localhost:8081${COLOR_NC}"
        echo -e "${COLOR_YELLOW}   Rode: mvn spring-boot:run${COLOR_NC}"
        exit 1
    fi

    # Verificar se DynamoDB está rodando
    if ! docker ps --filter "name=cliente-core-dynamodb" --format "{{.Status}}" | grep -q "Up"; then
        echo -e "${COLOR_RED}❌ DynamoDB Local não está rodando${COLOR_NC}"
        echo -e "${COLOR_YELLOW}   Rode: ./local-dev.sh start${COLOR_NC}"
        exit 1
    fi

    echo -e "${COLOR_GREEN}1️⃣  Buscando cliente para teste (usando seeds do Liquibase)...${COLOR_NC}"

    # 🎯 Usar cliente dos seeds (Ana Silva - CPF 123.456.789-10)
    # Não cria dados novos - usa apenas os seeds existentes
    UUID=$(curl -s "http://localhost:8081/api/clientes/v1/clientes/pf?page=0&size=1" | jq -r '.content[0].publicId' 2>/dev/null)

    if [ -z "$UUID" ] || [ "$UUID" = "null" ]; then
        echo -e "${COLOR_RED}❌ Nenhum cliente encontrado${COLOR_NC}"
        echo -e "${COLOR_YELLOW}   Execute os seeds do Liquibase primeiro:${COLOR_NC}"
        echo -e "${COLOR_YELLOW}   mvn spring-boot:run${COLOR_NC}"
        exit 1
    fi

    echo -e "${COLOR_GREEN}   Cliente ID: ${COLOR_YELLOW}$UUID${COLOR_NC}"
    echo ""

    echo -e "${COLOR_GREEN}2️⃣  Primeira busca (cache MISS - vai no banco)...${COLOR_NC}"
    TIME1=$(date +%s%N)
    curl -s http://localhost:8081/api/clientes/v1/clientes/pf/$UUID > /dev/null
    TIME2=$(date +%s%N)
    ELAPSED1=$(echo "scale=2; ($TIME2 - $TIME1) / 1000000" | bc)
    echo -e "   ${COLOR_YELLOW}⏱️  Tempo: ${ELAPSED1}ms${COLOR_NC}"
    sleep 1

    echo ""
    echo -e "${COLOR_GREEN}3️⃣  Segunda busca (cache HIT - do DynamoDB)...${COLOR_NC}"
    TIME1=$(date +%s%N)
    curl -s http://localhost:8081/api/clientes/v1/clientes/pf/$UUID > /dev/null
    TIME2=$(date +%s%N)
    ELAPSED2=$(echo "scale=2; ($TIME2 - $TIME1) / 1000000" | bc)
    echo -e "   ${COLOR_YELLOW}⏱️  Tempo: ${ELAPSED2}ms${COLOR_NC}"

    echo ""
    echo -e "${COLOR_BLUE}📊 Resultados:${COLOR_NC}"
    echo -e "   1ª busca (DB):    ${COLOR_YELLOW}${ELAPSED1}ms${COLOR_NC}"
    echo -e "   2ª busca (Cache): ${COLOR_YELLOW}${ELAPSED2}ms${COLOR_NC}"

    if (( $(echo "$ELAPSED2 < $ELAPSED1" | bc -l) )); then
        IMPROVEMENT=$(echo "scale=1; ($ELAPSED1 - $ELAPSED2) * 100 / $ELAPSED1" | bc 2>/dev/null || echo "N/A")
        echo -e "   ${COLOR_GREEN}✅ Cache mais rápido em ${IMPROVEMENT}%${COLOR_NC}"
    else
        echo -e "   ${COLOR_YELLOW}⚠️  Cache não mostrou melhoria (pode não estar ativo)${COLOR_NC}"
    fi

    echo ""
    echo -e "${COLOR_BLUE}🔍 Verificar tabela DynamoDB:${COLOR_NC}"
    if command -v aws &> /dev/null; then
        aws dynamodb scan \
            --table-name cliente-core-cache \
            --endpoint-url http://localhost:8000 \
            --region us-east-1 \
            --max-items 5 \
            --no-cli-pager 2>/dev/null | jq -r '.Items[] | "  - " + .cacheKey.S' || echo "  (erro ao acessar DynamoDB)"
    else
        echo -e "  ${COLOR_YELLOW}AWS CLI não instalado (instale para ver itens cached)${COLOR_NC}"
    fi

    echo ""
}

# Main
print_header
check_docker

case "${1:-}" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    logs)
        show_logs
        ;;
    status)
        show_status
        ;;
    test-cache)
        test_cache
        ;;
    *)
        echo -e "${COLOR_YELLOW}Uso: $0 {start|stop|restart|logs|status|test-cache}${COLOR_NC}"
        echo ""
        echo -e "${COLOR_BLUE}Comandos:${COLOR_NC}"
        echo -e "  ${COLOR_GREEN}start${COLOR_NC}       - Inicia DynamoDB Local (Docker)"
        echo -e "  ${COLOR_GREEN}stop${COLOR_NC}        - Para todos os serviços"
        echo -e "  ${COLOR_GREEN}restart${COLOR_NC}     - Reinicia serviços"
        echo -e "  ${COLOR_GREEN}logs${COLOR_NC}        - Mostra logs dos serviços"
        echo -e "  ${COLOR_GREEN}status${COLOR_NC}      - Mostra status dos serviços"
        echo -e "  ${COLOR_GREEN}test-cache${COLOR_NC}  - Testa performance do cache"
        echo ""
        exit 1
        ;;
esac
