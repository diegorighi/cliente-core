# 🔧 Desenvolvimento Local - cliente-core

Guia completo para configurar e rodar o **cliente-core** localmente.

---

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

| Ferramenta | Versão Mínima | Como Instalar |
|-----------|---------------|---------------|
| **Java** | 21+ | `sdk install java 21.0.1-tem` (via SDKMAN) |
| **Maven** | 3.8+ | `brew install maven` |
| **PostgreSQL** | 16+ | `brew install postgresql@16` |
| **Git** | 2.30+ | `brew install git` |

---

## 🚀 Setup Rápido (Recomendado)

### Opção 1: Script Automatizado

Execute o script de setup que configura **TUDO** automaticamente:

```bash
cd cliente-core
./setup-local.sh
```

O script irá:
1. ✅ Criar arquivo `.env` com variáveis de ambiente DEV
2. ✅ Verificar se PostgreSQL está rodando
3. ✅ Criar banco de dados `clientes`
4. ✅ Validar dependências (Java, Maven)
5. ✅ Mostrar próximos passos

---

## ⚙️ Setup Manual (Passo a Passo)

Se preferir configurar manualmente, siga estes passos:

### Passo 1: Configurar Variáveis de Ambiente

```bash
# 1. Copiar arquivo de exemplo
cp .env.example .env

# 2. Editar .env se necessário (opcional)
# O padrão já funciona para desenvolvimento local
vi .env
```

**Conteúdo padrão do `.env`:**
```bash
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/clientes
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SERVER_PORT=8081
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_BR_COM_VANESSA_MUDANCA=DEBUG
```

### Passo 2: Iniciar PostgreSQL

```bash
# Iniciar PostgreSQL
brew services start postgresql@16

# OU rodar manualmente
pg_ctl -D /opt/homebrew/var/postgresql@16 start

# Verificar status
brew services list | grep postgresql
```

### Passo 3: Criar Banco de Dados

```bash
# Criar banco 'clientes'
psql -U postgres -c "CREATE DATABASE clientes;"

# Verificar criação
psql -U postgres -l | grep clientes
```

### Passo 4: Rodar a Aplicação

```bash
# Buildar e rodar com Maven
mvn spring-boot:run
```

**Output esperado:**
```
  ██████╗ ██╗     ██╗ ███████╗ ███╗   ██╗ ████████╗ ███████╗
  ...
  🚚 Microserviço de Gestão de Clientes | Va Nessa Mudança
  📦 Spring Boot 3.5.7 | ☕ Java 21
  🔧 Ambiente: dev | 🎯 Hexagonal Architecture

[main] Started ClienteCoreApplication in 8.234 seconds
```

---

## ✅ Verificar Instalação

### 1. Health Check

```bash
curl http://localhost:8081/api/clientes/actuator/health
```

**Resposta esperada:**
```json
{
  "status": "UP"
}
```

### 2. Verificar Banco de Dados

```bash
# Conectar ao banco
psql -U postgres -d clientes

# Listar tabelas (devem existir após Liquibase rodar)
\dt

# Exemplo de output:
#  clientes              | table | postgres
#  clientes_pf           | table | postgres
#  clientes_pj           | table | postgres
#  documentos            | table | postgres
#  contatos              | table | postgres
#  enderecos             | table | postgres
```

### 3. Verificar Logs

```bash
# Ver logs em tempo real
tail -f logs/spring-boot-logger.log

# Ou via console do Maven (já mostra por padrão)
```

---

## 🌐 Variáveis de Ambiente Explicadas

### `SPRING_PROFILES_ACTIVE=dev`

**O que faz:**
- Ativa o perfil DEV do Spring Boot
- **SEM OAuth2** - Endpoints abertos (sem autenticação)
- Logs detalhados (DEBUG level)
- SQL queries visíveis no console
- Liquibase roda DDL + SEEDS (dados de teste)

**Quando usar:**
- ✅ Desenvolvimento local
- ✅ Testes manuais
- ✅ Depuração

**Quando NÃO usar:**
- ❌ Produção
- ❌ Staging
- ❌ Qualquer ambiente acessível publicamente

### `SPRING_PROFILES_ACTIVE=prod`

**O que faz:**
- Ativa o perfil PROD do Spring Boot
- **COM OAuth2** - Endpoints protegidos (requer JWT token)
- Logs mínimos (INFO level)
- SQL queries desabilitadas
- Liquibase roda apenas DDL (sem seeds)

**Quando usar:**
- ✅ Produção (AWS ECS)
- ✅ Staging
- ✅ Ambiente de homologação

---

## 🔑 Diferenças entre Perfis (dev vs prod)

| Aspecto | dev | prod |
|---------|-----|------|
| **Autenticação** | ❌ Desabilitada | ✅ OAuth2 + JWT |
| **Logs** | DEBUG (verbose) | INFO (mínimo) |
| **SQL Logging** | ✅ Habilitado | ❌ Desabilitado |
| **Liquibase Seeds** | ✅ Inclui dados de teste | ❌ Apenas DDL |
| **CORS** | ✅ Permitir tudo | ⚠️ Restrito |
| **Swagger UI** | ✅ Habilitado | ❌ Desabilitado |

---

## 🐛 Troubleshooting

### Problema 1: PostgreSQL não inicia

**Sintoma:**
```
psql: error: connection to server on socket "/tmp/.s.PGSQL.5432" failed
```

**Solução:**
```bash
# Verificar status
brew services list

# Reiniciar
brew services restart postgresql@16

# Ou verificar logs
tail -f /opt/homebrew/var/log/postgresql@16.log
```

---

### Problema 2: Banco de dados não encontrado

**Sintoma:**
```
org.postgresql.util.PSQLException: FATAL: database "clientes" does not exist
```

**Solução:**
```bash
# Criar banco
psql -U postgres -c "CREATE DATABASE clientes;"
```

---

### Problema 3: Arquivo .env não carregado

**Sintoma:**
```
The following 1 profile is active: "default"
```

**Solução:**
```bash
# Verificar se .env existe
ls -la | grep .env

# Se não existir, criar
cp .env.example .env

# Verificar conteúdo
cat .env
```

**IMPORTANTE:** Maven carrega `.env` automaticamente via `spring-boot-maven-plugin`. Se não carregar, adicione ao pom.xml:

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <environmentVariables>
            <!-- Variáveis são carregadas do .env automaticamente -->
        </environmentVariables>
    </configuration>
</plugin>
```

---

### Problema 4: Porta 8081 já em uso

**Sintoma:**
```
java.net.BindException: Address already in use
```

**Solução:**
```bash
# Encontrar processo na porta 8081
lsof -i :8081

# Matar processo
kill -9 <PID>

# Ou alterar porta no .env
echo "SERVER_PORT=8082" >> .env
```

---

### Problema 5: Liquibase validation failed

**Sintoma:**
```
liquibase.exception.ValidationFailedException: Validation Failed
```

**Solução (ATENÇÃO: Remove dados!)**:
```bash
# Conectar ao banco
psql -U postgres -d clientes

# Dropar tabelas do Liquibase
DROP TABLE databasechangelog;
DROP TABLE databasechangeloglock;

# Sair
\q

# Reiniciar aplicação
mvn spring-boot:run
```

---

## 📚 Recursos Úteis

### Endpoints Disponíveis (dev)

| Endpoint | Descrição |
|----------|-----------|
| `http://localhost:8081/api/clientes/actuator/health` | Health check |
| `http://localhost:8081/api/clientes/actuator/metrics` | Métricas da aplicação |
| `http://localhost:8081/api/clientes/swagger-ui/index.html` | Documentação Swagger (quando implementado) |

### Comandos Maven

```bash
# Compilar
mvn clean compile

# Rodar testes
mvn test

# Build completo (sem testes)
mvn clean package -DskipTests

# Rodar aplicação
mvn spring-boot:run

# Limpar tudo
mvn clean
```

### Comandos PostgreSQL

```bash
# Conectar
psql -U postgres -d clientes

# Listar tabelas
\dt

# Descrever tabela
\d clientes

# Ver dados
SELECT * FROM clientes LIMIT 10;

# Sair
\q
```

---

## 🔒 Segurança (IMPORTANTE)

### ⚠️ NUNCA commite o arquivo `.env`

O arquivo `.env` contém **credenciais locais** e está no `.gitignore`.

**Verificar antes de commit:**
```bash
# Verificar que .env está ignorado
git status

# .env NÃO deve aparecer na lista de arquivos modificados
```

### 🔐 Para produção

Em produção (ECS), as variáveis de ambiente são definidas via:
- **AWS Secrets Manager** - Senhas do banco
- **ECS Task Definition** - Variáveis não sensíveis

**Exemplo ECS Task Definition:**
```json
{
  "environment": [
    {
      "name": "SPRING_PROFILES_ACTIVE",
      "value": "prod"
    }
  ],
  "secrets": [
    {
      "name": "SPRING_DATASOURCE_PASSWORD",
      "valueFrom": "arn:aws:secretsmanager:sa-east-1:xxx:secret:vanessa/db-password"
    }
  ]
}
```

---

## 📞 Precisa de Ajuda?

- **Documentação do Projeto:** `README.md`
- **Arquitetura:** `docs/ARCHITECTURE.md`
- **Claude.md:** `CLAUDE.md` (guia para LLMs)
- **Issues:** Crie uma issue no repositório

---

**Última atualização:** 2025-11-06
**Versão:** 1.0
**Responsável:** Equipe Va Nessa Mudança
