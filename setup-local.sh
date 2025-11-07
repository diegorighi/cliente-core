#!/bin/bash

# ============================================================================
# Script de Setup Local - cliente-core
# ============================================================================
# Este script configura o ambiente de desenvolvimento local automaticamente
#
# Uso:
#   ./setup-local.sh
#
# O que ele faz:
#   1. Cria arquivo .env com variáveis de ambiente DEV
#   2. Cria banco de dados PostgreSQL local
#   3. Verifica se PostgreSQL está rodando
#   4. Instrui como rodar a aplicação
# ============================================================================

set -euo pipefail

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

separator() {
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

# ============================================================================
# Passo 1: Criar arquivo .env
# ============================================================================

setup_env_file() {
    separator
    log_info "Passo 1: Configurando arquivo .env"
    separator

    if [ -f .env ]; then
        log_warning "Arquivo .env já existe"
        read -p "Deseja sobrescrever? (s/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Ss]$ ]]; then
            log_info "Mantendo .env existente"
            return 0
        fi
    fi

    log_info "Criando .env a partir de .env.example..."
    cp .env.example .env

    log_success "Arquivo .env criado com sucesso!"
    log_info "Você pode editar o arquivo .env para personalizar as configurações"
    echo
}

# ============================================================================
# Passo 2: Verificar PostgreSQL
# ============================================================================

check_postgres() {
    separator
    log_info "Passo 2: Verificando PostgreSQL"
    separator

    if ! command -v psql &> /dev/null; then
        log_error "PostgreSQL não está instalado"
        log_info "Instale PostgreSQL com Homebrew:"
        echo "  brew install postgresql@16"
        echo "  brew services start postgresql@16"
        exit 1
    fi

    log_success "PostgreSQL está instalado"

    # Testar conexão
    if psql -U postgres -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw postgres; then
        log_success "PostgreSQL está rodando"
    else
        log_warning "PostgreSQL não está respondendo"
        log_info "Inicie o PostgreSQL com:"
        echo "  brew services start postgresql@16"
        echo
        log_info "Ou rode manualmente:"
        echo "  pg_ctl -D /opt/homebrew/var/postgresql@16 start"
        exit 1
    fi
    echo
}

# ============================================================================
# Passo 3: Criar banco de dados
# ============================================================================

create_database() {
    separator
    log_info "Passo 3: Criando banco de dados 'clientes'"
    separator

    # Verificar se banco existe
    if psql -U postgres -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw clientes; then
        log_warning "Banco 'clientes' já existe"
        read -p "Deseja recriar o banco? ATENÇÃO: Todos os dados serão perdidos! (s/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Ss]$ ]]; then
            log_info "Dropando banco existente..."
            psql -U postgres -c "DROP DATABASE clientes;" 2>/dev/null || true
            log_info "Criando banco novamente..."
            psql -U postgres -c "CREATE DATABASE clientes;"
            log_success "Banco recriado com sucesso!"
        else
            log_info "Mantendo banco existente"
        fi
    else
        log_info "Criando banco 'clientes'..."
        psql -U postgres -c "CREATE DATABASE clientes;"
        log_success "Banco criado com sucesso!"
    fi
    echo
}

# ============================================================================
# Passo 4: Verificar dependências
# ============================================================================

check_dependencies() {
    separator
    log_info "Passo 4: Verificando dependências"
    separator

    # Java
    if ! command -v java &> /dev/null; then
        log_error "Java não está instalado"
        log_info "Instale Java 21 com SDKMAN:"
        echo "  curl -s \"https://get.sdkman.io\" | bash"
        echo "  sdk install java 21.0.1-tem"
        exit 1
    fi

    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 21 ]; then
        log_error "Java 21+ é necessário (versão instalada: $JAVA_VERSION)"
        exit 1
    fi
    log_success "Java $JAVA_VERSION instalado"

    # Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven não está instalado"
        log_info "Instale Maven com Homebrew:"
        echo "  brew install maven"
        exit 1
    fi
    log_success "Maven instalado"
    echo
}

# ============================================================================
# Passo 5: Instruções finais
# ============================================================================

show_instructions() {
    separator
    echo -e "${GREEN}🎉 SETUP CONCLUÍDO COM SUCESSO!${NC}"
    separator
    echo
    log_info "PRÓXIMOS PASSOS:"
    echo
    echo "  1️⃣  Rode a aplicação:"
    echo -e "      ${YELLOW}mvn spring-boot:run${NC}"
    echo
    echo "  2️⃣  Acesse o health check:"
    echo -e "      ${YELLOW}curl http://localhost:8081/api/clientes/actuator/health${NC}"
    echo
    echo "  3️⃣  Verifique os logs:"
    echo "      A aplicação carregará automaticamente o perfil DEV (sem OAuth2)"
    echo
    echo "  4️⃣  O Liquibase criará as tabelas e seed data automaticamente"
    echo
    separator
    log_info "VARIÁVEIS DE AMBIENTE CONFIGURADAS (arquivo .env):"
    echo
    echo "  • SPRING_PROFILES_ACTIVE=dev      (sem OAuth2, sem autenticação)"
    echo "  • SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/clientes"
    echo "  • SPRING_DATASOURCE_USERNAME=postgres"
    echo "  • SPRING_DATASOURCE_PASSWORD=postgres"
    echo
    separator
    log_warning "IMPORTANTE:"
    echo
    echo "  • O arquivo .env NÃO deve ser commitado (está no .gitignore)"
    echo "  • Cada desenvolvedor deve criar seu próprio .env local"
    echo "  • Para produção, as variáveis são definidas no ECS Task Definition"
    echo
    separator
}

# ============================================================================
# Main
# ============================================================================

main() {
    clear
    separator
    echo -e "${BLUE}🚀 SETUP AMBIENTE LOCAL - cliente-core${NC}"
    separator
    echo

    setup_env_file
    check_postgres
    create_database
    check_dependencies
    show_instructions
}

# Executar
main "$@"
