# 📚 Índice de Documentação - Setup Local

Guia completo de todas as opções para rodar o **cliente-core** localmente.

---

## 🎯 Qual opção escolher?

| Perfil | Opção Recomendada | Tempo Setup | Pré-requisitos |
|--------|-------------------|-------------|----------------|
| **Novo no projeto** | 🐳 [Docker Compose](#docker-compose) | ~3 min | Apenas Docker |
| **Desenvolvedor diário** | 💻 [Maven Local](#maven-local-com-env) | ~5 min | Java 21, Maven, PostgreSQL |
| **CI/CD Pipeline** | 🐳 [Docker Compose](#docker-compose) | ~2 min | Docker |
| **Onboarding rápido** | 🧙 [Wizard](#wizard) | ~1 min | Docker |

---

## 🐳 Docker Compose (RECOMENDADO)

**Para quem:** Novos desenvolvedores, onboarding rápido, ambientes isolados

**Vantagens:**
- ✅ Setup em **1 comando**
- ✅ NÃO precisa instalar Java, Maven, PostgreSQL
- ✅ Funciona em **qualquer OS** (macOS, Linux, Windows)
- ✅ Ambiente **idêntico ao CI/CD**
- ✅ Isolamento completo (não polui sistema)

**Desvantagens:**
- ⚠️ Overhead de containers (~10% performance)
- ⚠️ Precisa rebuild manual ao alterar código

**Documentação:**
- 📖 **[WIZARD.md](../../WIZARD.md)** - Guia de 1 minuto (START HERE!)
- 📖 **Comandos:**
  ```bash
  # Subir tudo
  docker-compose up --build

  # Parar
  docker-compose down

  # Ver logs
  docker-compose logs -f app
  ```

---

## 💻 Maven Local (com .env)

**Para quem:** Desenvolvedores que já tem ambiente Java configurado

**Vantagens:**
- ✅ Performance nativa (sem overhead)
- ✅ Hot reload automático (Spring DevTools)
- ✅ Controle total do ambiente

**Desvantagens:**
- ⚠️ Precisa instalar Java 21, Maven, PostgreSQL
- ⚠️ Setup mais demorado (~5-10 min)
- ⚠️ Problemas de compatibilidade entre OS

**Documentação:**
- 📖 **[LOCAL_DEVELOPMENT.md](LOCAL_DEVELOPMENT.md)** - Guia completo manual
- 📖 **Script automatizado:**
  ```bash
  ./setup-local.sh
  mvn spring-boot:run
  ```

---

## 🧙 Wizard (Docker Simplificado)

**Para quem:** Quem quer começar **AGORA** sem ler documentação

**É literalmente 3 comandos:**
```bash
cd cliente-core
docker-compose up --build
# Aguardar "Started ClienteCoreApplication" → PRONTO!
```

**Documentação:**
- 📖 **[WIZARD.md](../../WIZARD.md)** - Guia de 1 minuto

---

## 📁 Estrutura de Documentação

```
cliente-core/
├── WIZARD.md                      # 🧙 Setup de 1 minuto (Docker)
├── docker-compose.yml             # 🐳 Configuração Docker
├── Dockerfile.dev                 # 🐳 Imagem de desenvolvimento
├── setup-local.sh                 # 💻 Script de setup manual
├── .env.example                   # 💻 Template de variáveis locais
└── docs/
    └── setup/
        ├── SETUP_INDEX.md         # 📚 Este arquivo (índice)
        ├── LOCAL_DEVELOPMENT.md   # 💻 Guia detalhado Maven
        ├── COMO_SUBIR_LOCAL_STACK.md  # (legacy - substituído)
        └── DOCKER_SETUP.md        # (futuro - hot reload)
```

---

## 🔑 Variáveis de Ambiente

### Docker Compose (docker-compose.yml)

Já configurado! Não precisa criar `.env`.

```yaml
environment:
  SPRING_PROFILES_ACTIVE: dev
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/clientes
  SPRING_DATASOURCE_USERNAME: postgres
  SPRING_DATASOURCE_PASSWORD: postgres
```

### Maven Local (.env)

Crie arquivo `.env` (não commitado):

```bash
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/clientes
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

---

## 🚀 Comandos Rápidos

### Docker Compose

```bash
# Subir
docker-compose up --build

# Background
docker-compose up -d

# Logs
docker-compose logs -f app

# Parar
docker-compose down

# Reset completo (deleta banco)
docker-compose down -v
```

### Maven Local

```bash
# Setup (primeira vez)
./setup-local.sh

# Rodar
mvn spring-boot:run

# Testes
mvn test

# Build
mvn clean package
```

---

## ✅ Verificar Instalação

### 1. Health Check

```bash
curl http://localhost:8081/api/clientes/actuator/health
```

**Resposta esperada:**
```json
{"status":"UP"}
```

### 2. Verificar Banco

```bash
# Docker
docker exec -it cliente-core-postgres psql -U postgres -d clientes -c "\dt"

# Local
psql -U postgres -d clientes -c "\dt"
```

---

## 🐛 Troubleshooting

| Problema | Solução |
|----------|---------|
| Porta 8081 em uso | `lsof -i :8081` → `kill -9 <PID>` |
| PostgreSQL não inicia | `brew services restart postgresql@16` |
| Docker "no space left" | `docker system prune -a` |
| Maven "compilation error" | `mvn clean install` |

**Guias detalhados:**
- 🐳 Docker: [WIZARD.md - Troubleshooting](../../WIZARD.md#-troubleshooting)
- 💻 Maven: [LOCAL_DEVELOPMENT.md - Troubleshooting](LOCAL_DEVELOPMENT.md#-troubleshooting)

---

## 🔄 Migração entre Métodos

### De Maven Local → Docker

```bash
# 1. Parar aplicação local
# Ctrl+C no terminal do Maven

# 2. Subir Docker
docker-compose up --build
```

### De Docker → Maven Local

```bash
# 1. Parar Docker
docker-compose down

# 2. Subir Maven
mvn spring-boot:run
```

**IMPORTANTE:** Banco de dados é compartilhado (porta 5432), então dados persistem!

---

## 📞 Precisa de Ajuda?

1. **Leia primeiro:**
   - 🧙 [WIZARD.md](../../WIZARD.md) - Setup rápido
   - 💻 [LOCAL_DEVELOPMENT.md](LOCAL_DEVELOPMENT.md) - Detalhes técnicos

2. **Issues comuns:**
   - Porta em uso → Veja troubleshooting acima
   - Credenciais erradas → Verifique `.env` ou `docker-compose.yml`

3. **Contato:**
   - Crie issue no repositório
   - Slack: #cliente-core

---

**Última atualização:** 2025-11-06
**Versão:** 1.0
**Responsável:** Equipe Va Nessa Mudança
