# 🚀 Cliente-Core - Quick Start

Setup local em **1 único comando**.

## ⚡ Iniciar Tudo

```bash
cd cliente-core
./setup-local.sh
```

Isso vai:
- ✅ Validar dependências
- ✅ Subir PostgreSQL + DynamoDB Local
- ✅ Buildar aplicação
- ✅ Rodar testes automáticos
- ✅ Mostrar URLs e comandos úteis

**Tempo:** ~2 minutos ⏱️

---

## 🛠️ Outros Comandos

```bash
./local-dev.sh status       # Ver status
./local-dev.sh test-cache   # Testar cache
./local-dev.sh stop         # Parar tudo
```

---

## 📖 Documentação Completa

- **Setup detalhado:** [COMO_SUBIR_LOCAL_STACK.md](COMO_SUBIR_LOCAL_STACK.md)
- **Guia do projeto:** [README.md](README.md)
- **Guia para Claude:** [CLAUDE.md](CLAUDE.md)

---

## 🌐 URLs Importantes

Após o setup, acesse:

- API Base: http://localhost:8081/api/clientes
- Health: http://localhost:8081/api/clientes/actuator/health
- Swagger UI: http://localhost:8081/api/clientes/swagger-ui

---

## 💡 Primeiro Acesso?

1. Clone o repositório
2. Instale: Java 21+, Maven 3.9+, Docker Desktop
3. Rode: `./setup-local.sh`
4. Pronto! ✨
