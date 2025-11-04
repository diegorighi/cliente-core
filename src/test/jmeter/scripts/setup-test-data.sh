#!/bin/bash

##############################################################################
# Script para criar dados de teste para JMeter
#
# Uso:
#   ./setup-test-data.sh [quantidade]
#
# Exemplo:
#   ./setup-test-data.sh 100  # Cria 100 clientes PF para testes
#
# Output:
#   - src/test/jmeter/data/clientes_pf_testdata.csv
#   - Lista de publicIds para uso no JMeter
##############################################################################

set -e  # Exit on error

# Configuração
BASE_URL="${BASE_URL:-http://localhost:8081/api/clientes/v1}"
QTY="${1:-10}"  # Default: 10 clientes
OUTPUT_FILE="../data/clientes_pf_testdata.csv"

echo "=================================================="
echo "   Criando dados de teste para JMeter"
echo "=================================================="
echo "URL: $BASE_URL"
echo "Quantidade: $QTY clientes"
echo "Output: $OUTPUT_FILE"
echo "=================================================="
echo ""

# Verificar se aplicação está rodando
echo "Verificando se aplicação está disponível..."
if ! curl -s -f "$BASE_URL/clientes/pf" > /dev/null 2>&1; then
    echo "❌ ERRO: Aplicação não está rodando em $BASE_URL"
    echo ""
    echo "Iniciar aplicação antes de executar este script:"
    echo "  mvn spring-boot:run"
    exit 1
fi
echo "✅ Aplicação disponível"
echo ""

# Criar arquivo CSV header
echo "publicId,cpf,primeiroNome,sobrenome,email" > "$OUTPUT_FILE"

# Lista de nomes para variedade
FIRST_NAMES=("João" "Maria" "Pedro" "Ana" "Carlos" "Julia" "Fernando" "Patricia" "Lucas" "Camila")
LAST_NAMES=("Silva" "Santos" "Oliveira" "Souza" "Costa" "Ferreira" "Rodrigues" "Almeida" "Nascimento" "Lima")

echo "Criando $QTY clientes..."

# CPFs válidos para teste (gerados com algoritmo correto)
CPFS_VALIDOS=(
    "123.456.789-09"
    "111.444.777-35"
    "893.521.683-32"
    "397.278.408-07"
    "665.174.830-30"
    "192.283.374-03"
    "839.465.700-66"
    "447.556.880-90"
    "285.697.930-40"
    "574.808.040-21"
)

for i in $(seq 1 "$QTY"); do
    # Usar CPFs válidos da lista (reutiliza se necessário)
    CPF_IDX=$(( (i - 1) % ${#CPFS_VALIDOS[@]} ))
    CPF="${CPFS_VALIDOS[$CPF_IDX]}"

    # Selecionar nome aleatório
    FIRST_IDX=$(( i % ${#FIRST_NAMES[@]} ))
    LAST_IDX=$(( (i + 3) % ${#LAST_NAMES[@]} ))

    FIRST_NAME="${FIRST_NAMES[$FIRST_IDX]}"
    LAST_NAME="${LAST_NAMES[$LAST_IDX]}"
    EMAIL="$(echo ${FIRST_NAME} | tr '[:upper:]' '[:lower:]').$(echo ${LAST_NAME} | tr '[:upper:]' '[:lower:]')${i}@testdata.com"

    # Payload JSON
    JSON_PAYLOAD=$(cat <<EOF
{
  "primeiroNome": "$FIRST_NAME",
  "sobrenome": "$LAST_NAME",
  "cpf": "$CPF",
  "rg": "MG-12.345.${i}",
  "dataNascimento": "1990-01-15",
  "sexo": "MASCULINO",
  "email": "$EMAIL",
  "tipoCliente": "COMPRADOR",
  "observacoes": "Cliente criado para testes JMeter"
}
EOF
)

    # Criar cliente via POST
    RESPONSE=$(curl -s -X POST "$BASE_URL/clientes/pf" \
        -H "Content-Type: application/json" \
        -d "$JSON_PAYLOAD")

    # Extrair publicId do response (usando grep/sed para portabilidade)
    PUBLIC_ID=$(echo "$RESPONSE" | grep -o '"publicId":"[^"]*"' | sed 's/"publicId":"\([^"]*\)"/\1/')

    if [ -n "$PUBLIC_ID" ]; then
        echo "$PUBLIC_ID,$CPF,$FIRST_NAME,$LAST_NAME,$EMAIL" >> "$OUTPUT_FILE"
        echo "[$i/$QTY] ✅ Criado: $FIRST_NAME $LAST_NAME (publicId: ${PUBLIC_ID:0:8}...)"
    else
        echo "[$i/$QTY] ❌ Falha ao criar cliente: $FIRST_NAME $LAST_NAME"
        echo "Response: $RESPONSE"
    fi

    # Pequeno delay para não sobrecarregar
    sleep 0.1
done

echo ""
echo "=================================================="
echo "   ✅ Concluído!"
echo "=================================================="
echo "Criados: $(( $(wc -l < "$OUTPUT_FILE") - 1 )) clientes"
echo "Arquivo: $OUTPUT_FILE"
echo ""
echo "Próximos passos:"
echo "1. Revisar arquivo CSV gerado"
echo "2. Executar plano JMeter:"
echo "   jmeter -t ../UpdateClientePF_LoadTest.jmx"
echo "=================================================="
