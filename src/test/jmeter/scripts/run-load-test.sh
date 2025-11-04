#!/bin/bash

##############################################################################
# Script para executar testes de carga JMeter de forma simplificada
#
# Uso:
#   ./run-load-test.sh [carga] [modo]
#
# Exemplos:
#   ./run-load-test.sh low gui       # 10 usuários, modo GUI
#   ./run-load-test.sh medium cli    # 50 usuários, modo CLI com relatório
#   ./run-load-test.sh high cli      # 100 usuários, modo CLI com relatório
#   ./run-load-test.sh                # Default: low + gui
##############################################################################

set -e

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuração
CARGA="${1:-low}"
MODO="${2:-gui}"
BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TEST_PLAN="$BASE_DIR/UpdateClientePF_LoadTest.jmx"
RESULTS_DIR="$BASE_DIR/results"

# Parâmetros por nível de carga
case "$CARGA" in
    low)
        USERS=10
        RAMPUP=5
        LOOPS=10
        ;;
    medium)
        USERS=50
        RAMPUP=10
        LOOPS=10
        ;;
    high)
        USERS=100
        RAMPUP=20
        LOOPS=10
        ;;
    *)
        echo -e "${RED}❌ Carga inválida: $CARGA${NC}"
        echo "Cargas disponíveis: low, medium, high"
        exit 1
        ;;
esac

# Timestamp para arquivos
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

echo -e "${GREEN}=================================================="
echo "   JMeter Load Test - Cliente Core"
echo "==================================================${NC}"
echo "Carga: $CARGA ($USERS usuários, $LOOPS loops cada)"
echo "Modo: $MODO"
echo "Test Plan: UpdateClientePF_LoadTest.jmx"
echo "=================================================="
echo ""

# Verificar se JMeter está instalado
if ! command -v jmeter &> /dev/null; then
    echo -e "${RED}❌ ERRO: JMeter não está instalado${NC}"
    echo ""
    echo "Instalar JMeter:"
    echo "  macOS:   brew install jmeter"
    echo "  Linux:   sudo apt-get install jmeter"
    echo "  Windows: https://jmeter.apache.org/download_jmeter.cgi"
    exit 1
fi

# Verificar se aplicação está rodando
echo -e "${YELLOW}Verificando se aplicação está disponível...${NC}"
if ! curl -s -f "http://localhost:8081/api/clientes/v1/clientes/pf?page=0&size=1" > /dev/null 2>&1; then
    echo -e "${RED}❌ ERRO: Aplicação não está rodando em http://localhost:8081${NC}"
    echo ""
    echo "Iniciar aplicação:"
    echo "  mvn spring-boot:run"
    echo ""
    echo "Aguarde a mensagem 'Started ClienteCoreApplication' e execute novamente."
    exit 1
fi
echo -e "${GREEN}✅ Aplicação disponível${NC}"
echo ""

# Verificar se dados de teste existem
DATA_FILE="$BASE_DIR/data/clientes_pf_testdata.csv"
if [ ! -f "$DATA_FILE" ]; then
    echo -e "${RED}❌ ERRO: Dados de teste não encontrados${NC}"
    echo "Arquivo esperado: $DATA_FILE"
    echo ""
    echo "Criar dados de teste:"
    echo "  cd $BASE_DIR/scripts"
    echo "  ./setup-test-data.sh 20"
    exit 1
fi

TOTAL_CLIENTES=$(( $(wc -l < "$DATA_FILE") - 1 ))
echo -e "${GREEN}✅ Dados de teste encontrados: $TOTAL_CLIENTES clientes${NC}"
echo ""

# Criar diretório de resultados
mkdir -p "$RESULTS_DIR"

# Executar teste
if [ "$MODO" == "gui" ]; then
    echo -e "${YELLOW}Abrindo JMeter em modo GUI...${NC}"
    echo ""
    echo "📊 INSTRUÇÕES:"
    echo "1. Clique no botão verde 'Start' (▶️) para iniciar"
    echo "2. Aguarde conclusão do teste"
    echo "3. Visualize resultados em:"
    echo "   - View Results Tree (cada requisição individual)"
    echo "   - Summary Report (resumo geral)"
    echo "   - Aggregate Report (estatísticas detalhadas)"
    echo "   - Graph Results (gráfico visual)"
    echo ""
    echo "Pressione Enter para abrir JMeter..."
    read

    cd "$BASE_DIR"
    jmeter -t "$TEST_PLAN" \
        -Jusers=$USERS \
        -Jrampup=$RAMPUP \
        -Jloops=$LOOPS

else
    echo -e "${YELLOW}Executando teste em modo CLI...${NC}"
    echo ""

    JTL_FILE="$RESULTS_DIR/test-$CARGA-$TIMESTAMP.jtl"
    REPORT_DIR="$RESULTS_DIR/report-$CARGA-$TIMESTAMP"

    cd "$BASE_DIR"
    jmeter -n -t "$TEST_PLAN" \
        -Jusers=$USERS \
        -Jrampup=$RAMPUP \
        -Jloops=$LOOPS \
        -l "$JTL_FILE" \
        -e -o "$REPORT_DIR"

    echo ""
    echo -e "${GREEN}=================================================="
    echo "   ✅ Teste Concluído!"
    echo "==================================================${NC}"
    echo "JTL File: $JTL_FILE"
    echo "Report: $REPORT_DIR"
    echo ""
    echo "📊 Ver relatório HTML:"
    echo "  open $REPORT_DIR/index.html"
    echo ""
    echo "📄 Resumo rápido:"

    # Extrair resumo do JTL
    if command -v awk &> /dev/null; then
        TOTAL_SAMPLES=$(wc -l < "$JTL_FILE" | xargs)
        TOTAL_SAMPLES=$((TOTAL_SAMPLES - 1)) # Remover header

        echo "  Total de requisições: $TOTAL_SAMPLES"

        # Calcular taxa de erro (coluna 8 = success)
        ERRORS=$(awk -F',' 'NR>1 && $8=="false" {count++} END {print count+0}' "$JTL_FILE")
        ERROR_RATE=$(awk "BEGIN {printf \"%.2f\", ($ERRORS / $TOTAL_SAMPLES) * 100}")

        echo "  Taxa de erro: $ERROR_RATE%"

        if (( $(echo "$ERROR_RATE > 5.0" | bc -l) )); then
            echo -e "  ${RED}❌ FALHA: Taxa de erro > 5%${NC}"
        elif (( $(echo "$ERROR_RATE > 1.0" | bc -l) )); then
            echo -e "  ${YELLOW}⚠️  WARNING: Taxa de erro > 1%${NC}"
        else
            echo -e "  ${GREEN}✅ PASS: Taxa de erro < 1%${NC}"
        fi
    fi

    echo ""
    echo "=================================================="
fi
