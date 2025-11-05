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
    echo -e "${COLOR_GREEN}🚀 Iniciando PostgreSQL...${COLOR_NC}"

    # Parar postgres se já estiver rodando na porta 5432
    if lsof -Pi :5432 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "${COLOR_YELLOW}⚠️  PostgreSQL já está rodando na porta 5432${COLOR_NC}"
        echo -e "${COLOR_YELLOW}   Se for Docker Compose, rode: docker-compose down${COLOR_NC}"
    fi

    # Subir PostgreSQL
    docker-compose up -d

    # Aguardar PostgreSQL estar healthy
    echo -e "${COLOR_YELLOW}⏳ Aguardando PostgreSQL estar pronto...${COLOR_NC}"
    sleep 3

    # Verificar se PostgreSQL está rodando
    if docker-compose ps postgres | grep -q "Up"; then
        echo -e "${COLOR_GREEN}✅ PostgreSQL rodando em localhost:5432${COLOR_NC}"
    else
        echo -e "${COLOR_RED}❌ Erro ao iniciar PostgreSQL${COLOR_NC}"
        exit 1
    fi

    echo ""
    echo -e "${COLOR_GREEN}✅ PostgreSQL pronto!${COLOR_NC}"
    echo -e "${COLOR_BLUE}ℹ️  Cache: Caffeine in-memory (configurado automaticamente no Spring Boot)${COLOR_NC}"
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
    echo -e "${COLOR_BLUE}📄 Logs do PostgreSQL (Ctrl+C para sair)${COLOR_NC}"
    docker-compose logs -f postgres
}

show_status() {
    echo -e "${COLOR_BLUE}📊 Status dos serviços:${COLOR_NC}"
    echo ""

    # PostgreSQL
    if lsof -Pi :5432 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "  ${COLOR_GREEN}✅ PostgreSQL${COLOR_NC}"
        echo -e "     Port: ${COLOR_YELLOW}5432${COLOR_NC}"
        echo -e "     Database: ${COLOR_YELLOW}vanessa_mudanca_clientes${COLOR_NC}"
    else
        echo -e "  ${COLOR_RED}❌ PostgreSQL (não rodando)${COLOR_NC}"
        echo -e "     Rode: ${COLOR_YELLOW}./local-dev.sh start${COLOR_NC}"
    fi

    echo ""

    # Aplicação Spring Boot
    if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "  ${COLOR_GREEN}✅ Spring Boot Application${COLOR_NC}"
        echo -e "     URL: ${COLOR_YELLOW}http://localhost:8081/api/clientes${COLOR_NC}"

        # Verificar cache Caffeine
        if command -v curl &> /dev/null && command -v jq &> /dev/null; then
            CACHE_NAMES=$(curl -s http://localhost:8081/api/clientes/actuator/caches 2>/dev/null | jq -r '.cacheManagers.cacheManager.caches | keys[]' 2>/dev/null || echo "")
            if [ -n "$CACHE_NAMES" ]; then
                echo -e "     Cache: ${COLOR_YELLOW}Caffeine (in-memory)${COLOR_NC}"
                echo "$CACHE_NAMES" | while read cache; do
                    echo -e "       - $cache"
                done
            fi
        fi
    else
        echo -e "  ${COLOR_YELLOW}⚠️  Spring Boot Application (não rodando)${COLOR_NC}"
        echo -e "     Rode: ${COLOR_YELLOW}mvn spring-boot:run${COLOR_NC}"
    fi

    echo ""
}

test_cache() {
    echo -e "${COLOR_BLUE}🧪 Testando cache Caffeine in-memory...${COLOR_NC}"
    echo ""

    # Verificar se app está rodando
    if ! lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "${COLOR_RED}❌ Aplicação não está rodando em localhost:8081${COLOR_NC}"
        echo -e "${COLOR_YELLOW}   Rode: mvn spring-boot:run${COLOR_NC}"
        exit 1
    fi

    echo -e "${COLOR_GREEN}1️⃣  Buscando cliente para teste (usando seeds do Liquibase)...${COLOR_NC}"

    # Usar cliente dos seeds (Ana Silva - CPF 123.456.789-10)
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
    echo -e "${COLOR_GREEN}3️⃣  Segunda busca (cache HIT - do Caffeine in-memory)...${COLOR_NC}"
    TIME1=$(date +%s%N)
    curl -s http://localhost:8081/api/clientes/v1/clientes/pf/$UUID > /dev/null
    TIME2=$(date +%s%N)
    ELAPSED2=$(echo "scale=2; ($TIME2 - $TIME1) / 1000000" | bc)
    echo -e "   ${COLOR_YELLOW}⏱️  Tempo: ${ELAPSED2}ms${COLOR_NC}"

    echo ""
    echo -e "${COLOR_BLUE}📊 Resultados:${COLOR_NC}"
    echo -e "   1ª busca (DB):    ${COLOR_YELLOW}${ELAPSED1}ms${COLOR_NC}"
    echo -e "   2ª busca (Cache): ${COLOR_YELLOW}${ELAPSED2}ms${COLOR_NC} (esperado <1ms)"

    if (( $(echo "$ELAPSED2 < $ELAPSED1" | bc -l) )); then
        IMPROVEMENT=$(echo "scale=1; ($ELAPSED1 - $ELAPSED2) * 100 / $ELAPSED1" | bc 2>/dev/null || echo "N/A")
        echo -e "   ${COLOR_GREEN}✅ Cache mais rápido em ${IMPROVEMENT}%${COLOR_NC}"
    else
        echo -e "   ${COLOR_YELLOW}⚠️  Cache não mostrou melhoria (pode não estar ativo)${COLOR_NC}"
    fi

    echo ""
    echo -e "${COLOR_BLUE}🔍 Métricas do Caffeine (Spring Actuator):${COLOR_NC}"
    if command -v jq &> /dev/null; then
        # Cache gets (total de leituras)
        GETS=$(curl -s http://localhost:8081/api/clientes/actuator/metrics/cache.gets | jq -r '.measurements[0].value' 2>/dev/null || echo "0")
        echo -e "   Cache Gets:       ${COLOR_YELLOW}$GETS${COLOR_NC}"

        # Cache puts (total de escritas)
        PUTS=$(curl -s http://localhost:8081/api/clientes/actuator/metrics/cache.puts | jq -r '.measurements[0].value' 2>/dev/null || echo "0")
        echo -e "   Cache Puts:       ${COLOR_YELLOW}$PUTS${COLOR_NC}"

        # Cache evictions (itens removidos)
        EVICTIONS=$(curl -s http://localhost:8081/api/clientes/actuator/metrics/cache.evictions | jq -r '.measurements[0].value' 2>/dev/null || echo "0")
        echo -e "   Cache Evictions:  ${COLOR_YELLOW}$EVICTIONS${COLOR_NC}"

        # Lista de caches ativos
        echo ""
        echo -e "${COLOR_BLUE}📦 Caches ativos:${COLOR_NC}"
        curl -s http://localhost:8081/api/clientes/actuator/caches | jq -r '.cacheManagers.cacheManager.caches | keys[]' 2>/dev/null | while read cache; do
            echo -e "   - ${COLOR_YELLOW}$cache${COLOR_NC}"
        done
    else
        echo -e "  ${COLOR_YELLOW}jq não instalado (instale para ver métricas detalhadas)${COLOR_NC}"
    fi

    echo ""
    echo -e "${COLOR_GREEN}✅ Teste concluído!${COLOR_NC}"
    echo ""
    echo -e "${COLOR_BLUE}💡 Dica:${COLOR_NC} Acesse ${COLOR_YELLOW}http://localhost:8081/api/clientes/actuator/caches${COLOR_NC} para ver todas as métricas"
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
        echo -e "  ${COLOR_GREEN}start${COLOR_NC}       - Inicia PostgreSQL (Docker)"
        echo -e "  ${COLOR_GREEN}stop${COLOR_NC}        - Para todos os serviços"
        echo -e "  ${COLOR_GREEN}restart${COLOR_NC}     - Reinicia serviços"
        echo -e "  ${COLOR_GREEN}logs${COLOR_NC}        - Mostra logs do PostgreSQL"
        echo -e "  ${COLOR_GREEN}status${COLOR_NC}      - Mostra status dos serviços"
        echo -e "  ${COLOR_GREEN}test-cache${COLOR_NC}  - Testa performance do cache Caffeine"
        echo ""
        exit 1
        ;;
esac
