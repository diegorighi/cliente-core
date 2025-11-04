# ⚡ JMeter - Início Rápido (5 minutos)

## 🎯 Objetivo

Executar teste de performance no endpoint `PUT /api/clientes/pf/{id}` e ver resultados.

---

## 📋 Passo 1: Instalar JMeter (1x apenas)

```bash
brew install jmeter
```

---

## 🚀 Passo 2: Iniciar Aplicação

```bash
# Abrir novo terminal e manter rodando
mvn spring-boot:run
```

**Aguardar mensagem:**
```
Started ClienteCoreApplication in X seconds
```

---

## 📊 Passo 3: Criar Dados de Teste

```bash
cd src/test/jmeter/scripts
./setup-test-data.sh 20
```

**Output esperado:**
```
✅ Criados: 20 clientes
Arquivo: ../data/clientes_pf_testdata.csv
```

---

## 🎮 Passo 4: Executar Teste (ESCOLHA UM)

### Opção A: Modo GUI (Visual, bom para aprender)

```bash
cd src/test/jmeter/scripts
./run-load-test.sh low gui
```

**O que fazer:**
1. JMeter abre automaticamente
2. Clicar no botão verde "▶️ Start"
3. Aguardar conclusão
4. Clicar em "Summary Report" → Ver resultados

---

### Opção B: Modo CLI (Rápido, gera relatório HTML)

```bash
cd src/test/jmeter/scripts
./run-load-test.sh low cli
```

**O que fazer:**
1. Aguardar conclusão (10-20 segundos)
2. Abrir relatório HTML:
   ```bash
   open ../results/report-*/index.html
   ```

---

## 📊 Passo 5: Interpretar Resultados

### No Summary Report (GUI) ou Dashboard (HTML):

```
┌────────────────────────────────────────────┐
│ Average: 125 ms        ← Tempo médio       │
│ 90% Line: 220 ms       ← 90% das requests  │
│ 95% Line: 280 ms       ← 95% das requests  │
│ Error %: 0.00%         ← Taxa de erro      │
│ Throughput: 15.2/sec   ← Req por segundo   │
└────────────────────────────────────────────┘
```

### ✅ Resultado ESPERADO (cliente-core):
- ✅ Average < 200ms
- ✅ 95% Line < 500ms
- ✅ Error % < 1%
- ✅ Throughput > 10 req/s

### 🎉 Se todos ✅ → Performance está ÓTIMA!

---

## 🔧 Troubleshooting

### "Aplicação não está rodando"
```bash
# Verificar se está rodando
curl http://localhost:8081/api/clientes/pf

# Se não, iniciar:
mvn spring-boot:run
```

### "Dados de teste não encontrados"
```bash
cd src/test/jmeter/scripts
./setup-test-data.sh 20
```

### "JMeter não instalado"
```bash
brew install jmeter
```

---

## 🎓 Próximos Passos

**Aumentar carga:**
```bash
# Carga média (50 usuários)
./run-load-test.sh medium cli

# Carga alta (100 usuários)
./run-load-test.sh high cli
```

**Documentação completa:**
- `COMO_VER_RESULTADOS.md` - Guia visual detalhado
- `README.md` - Documentação completa

---

**🎉 Pronto! Em 5 minutos você executou teste de performance!**
