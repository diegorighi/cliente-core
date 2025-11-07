# 🧙 Setup Wizard - cliente-core

**Guia de 1 minuto para rodar o cliente-core localmente usando Docker!**

---

## 🎯 Para Desenvolvedores (Primeira Vez)

### Passo 1: Pré-requisitos

Apenas **Docker** é necessário. Nada mais!

```bash
# Verificar se Docker está instalado
docker --version
docker-compose --version

# Se não estiver instalado:
# macOS: brew install --cask docker
# Linux: https://docs.docker.com/engine/install/
# Windows: https://docs.docker.com/desktop/install/windows-install/
```

---

### Passo 2: Clonar e Rodar (3 comandos)

```bash
# 1. Clonar repositório (se ainda não clonou)
git clone <repo-url>
cd cliente-core

# 2. Subir TUDO (PostgreSQL + Spring Boot)
docker-compose up --build

# 3. Aguardar ~2 minutos (download de dependências na primeira vez)
# Quando ver "Started ClienteCoreApplication" → PRONTO! ✅
```

**Output esperado:**
```
cliente-core-postgres  | database system is ready to accept connections
cliente-core-app       | Started ClienteCoreApplication in 12.345 seconds
```

---

### Passo 3: Testar

```bash
# Health check
curl http://localhost:8081/api/clientes/actuator/health

# Resultado esperado:
# {"status":"UP"}
```

---

## 🔥 Comandos Úteis

### Rodar em background (sem travar o terminal)

```bash
docker-compose up -d
```

### Ver logs em tempo real

```bash
# Logs de tudo
docker-compose logs -f

# Logs apenas da aplicação
docker-compose logs -f app

# Logs apenas do banco
docker-compose logs -f postgres
```

### Parar tudo

```bash
docker-compose down
```

### Parar e DELETAR dados do banco (reset completo)

```bash
docker-compose down -v
```

### Rebuild (quando alterar código)

```bash
# Parar
docker-compose down

# Rebuild e subir
docker-compose up --build
```

---

## 🌐 O que está rodando?

| Serviço | Porta | URL |
|---------|-------|-----|
| **Spring Boot** | 8081 | `http://localhost:8081/api/clientes` |
| **PostgreSQL** | 5432 | `localhost:5432` (user: postgres, pass: postgres) |
| **Health Check** | 8081 | `http://localhost:8081/api/clientes/actuator/health` |
| **Metrics** | 8081 | `http://localhost:8081/api/clientes/actuator/metrics` |

---

## 🔑 Variáveis de Ambiente (já configuradas no docker-compose.yml)

Tudo já está configurado! Você **NÃO** precisa criar arquivo `.env`.

As variáveis estão definidas em `docker-compose.yml`:

```yaml
environment:
  SPRING_PROFILES_ACTIVE: dev             # Modo DEV (sem OAuth2)
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/clientes
  SPRING_DATASOURCE_USERNAME: postgres
  SPRING_DATASOURCE_PASSWORD: postgres
  SERVER_PORT: 8081
  LOGGING_LEVEL_ROOT: INFO
  LOGGING_LEVEL_BR_COM_VANESSA_MUDANCA: DEBUG
```

**Para alterar uma variável:**
1. Edite `docker-compose.yml`
2. Rode `docker-compose up --build` novamente

---

## 🐛 Troubleshooting

### Problema: Porta 8081 ou 5432 já em uso

```bash
# Ver o que está usando a porta
lsof -i :8081
lsof -i :5432

# Matar processo
kill -9 <PID>

# Ou alterar porta no docker-compose.yml:
# ports:
#   - "8082:8081"  # Mapear 8082 (host) -> 8081 (container)
```

---

### Problema: Erro de permissão no Docker

```bash
# macOS/Linux - Adicionar usuário ao grupo docker
sudo usermod -aG docker $USER

# Logout e login novamente
```

---

### Problema: "Address already in use" (container anterior não parou)

```bash
# Parar TODOS os containers
docker-compose down

# Verificar que nenhum container está rodando
docker ps

# Se ainda houver containers:
docker stop $(docker ps -q)
docker rm $(docker ps -a -q)

# Subir novamente
docker-compose up --build
```

---

### Problema: "No space left on device"

```bash
# Limpar imagens não utilizadas
docker system prune -a

# Liberar espaço (CUIDADO: remove TUDO que não está rodando)
docker system prune -a --volumes
```

---

## 🔄 Desenvolvimento com Hot Reload (Futuro)

**Atualmente:** Precisa fazer rebuild manual quando altera código.

**Futuro (Spring DevTools):**
- Adicionar Spring DevTools ao pom.xml
- Mapear diretório src/ como volume
- Alterações no código refletem automaticamente

---

## 🎓 Para Desenvolvedores Avançados

### Conectar ao PostgreSQL via terminal

```bash
# Conectar ao container do PostgreSQL
docker exec -it cliente-core-postgres psql -U postgres -d clientes

# Listar tabelas
\dt

# Ver dados
SELECT * FROM clientes LIMIT 10;

# Sair
\q
```

### Executar comandos Maven dentro do container

```bash
# Entrar no container
docker exec -it cliente-core-app /bin/bash

# Rodar testes
mvn test

# Buildar
mvn clean package

# Sair
exit
```

---

## 📊 Comparação: Docker vs Local

| Aspecto | Docker Compose | Maven Local |
|---------|----------------|-------------|
| **Setup** | 1 comando | 5+ passos |
| **Dependências** | Apenas Docker | Java 21, Maven, PostgreSQL |
| **Portabilidade** | ✅ Funciona em qualquer OS | ⚠️ Problemas de ambiente |
| **Isolamento** | ✅ Containers isolados | ❌ Polui sistema |
| **Performance** | ⚠️ Overhead de container | ✅ Nativo |
| **Hot Reload** | ❌ Precisa rebuild | ✅ DevTools automático |
| **Tempo primeira rodada** | ~3 min (download images) | ~2 min |

**Recomendação:**
- **Docker Compose**: Para novos desenvolvedores, onboarding rápido, CI/CD
- **Maven Local**: Para desenvolvimento intenso com hot reload

---

## 🚀 Próximos Passos

Agora que o ambiente está rodando:

1. **Explore a API** - Leia `README.md` para entender as entidades
2. **Teste endpoints** - Use Swagger UI (quando implementado)
3. **Implemente features** - Siga o guia em `CLAUDE.md`
4. **Rode testes** - `docker exec -it cliente-core-app mvn test`

---

**Última atualização:** 2025-11-06
**Versão:** 1.0
**Responsável:** Equipe Va Nessa Mudança
