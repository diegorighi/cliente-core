# 📊 Como Ver os Resultados do JMeter - Guia Passo a Passo

## 🎯 Opção 1: Modo GUI (Recomendado para Iniciantes)

### Passo 1: Instalar JMeter

```bash
# macOS
brew install jmeter

# Verificar instalação
jmeter --version
```

### Passo 2: Preparar Dados de Teste

```bash
# Terminal 1 - Iniciar aplicação
cd /Users/diegorighi/Desenvolvimento/va-nessa-mudanca/cliente-core
mvn spring-boot:run

# Aguardar mensagem: "Started ClienteCoreApplication in X seconds"
```

```bash
# Terminal 2 - Criar dados de teste
cd src/test/jmeter/scripts
./setup-test-data.sh 20

# Aguardar mensagem: "✅ Concluído! Criados: 20 clientes"
```

### Passo 3: Abrir JMeter GUI

```bash
cd src/test/jmeter
jmeter -t UpdateClientePF_LoadTest.jmx
```

**O que você verá:**
```
┌─────────────────────────────────────────────────┐
│ Apache JMeter                              [x]  │
├─────────────────────────────────────────────────┤
│ File  Edit  Search  Run  Options  Help         │
├─────────────────────────────────────────────────┤
│ ├─ Test Plan: Update ClientePF - Load Test     │
│ │  ├─ Thread Group - Update ClientePF          │
│ │  │  ├─ CSV Data Set - Clientes Test Data     │
│ │  │  ├─ PUT - Update ClientePF                │
│ │  │  └─ Think Time (500ms)                    │
│ │  ├─ 📊 View Results Tree                     │ ← AQUI!
│ │  ├─ 📊 Summary Report                        │ ← AQUI!
│ │  ├─ 📊 Aggregate Report                      │ ← AQUI!
│ │  └─ 📊 Graph Results                         │ ← AQUI!
└─────────────────────────────────────────────────┘
```

### Passo 4: Executar Teste

1. **Clicar no botão verde "Start" (▶️)** ou Menu → Run → Start
2. **Aguardar conclusão** (barra de progresso no canto inferior direito)

### Passo 5: Ver Resultados - 4 Visualizações

#### **A) View Results Tree** (Detalhamento Individual)

**Como acessar:**
- Clicar em "View Results Tree" na árvore da esquerda

**O que você verá:**
```
┌────────────────────────────────────────────────────┐
│ View Results Tree                                  │
├────────────────────────────────────────────────────┤
│ ✅ PUT - Update ClientePF (Thread 1-1)            │
│    Response Code: 200                              │
│    Response Time: 125 ms                           │
│    Size: 1024 bytes                                │
│                                                    │
│ [Sampler result] [Request] [Response data]        │
│                                                    │
│ {                                                  │
│   "publicId": "abc-123-def...",                   │
│   "primeiroNome": "João_Updated",                 │
│   "profissao": "Engenheiro de Software",          │
│   ...                                              │
│ }                                                  │
└────────────────────────────────────────────────────┘
```

**Para que serve:**
- ✅ Ver **cada requisição individual**
- ✅ Ver **request e response completos**
- ✅ Debugar **falhas específicas**

---

#### **B) Summary Report** (Resumo Geral)

**Como acessar:**
- Clicar em "Summary Report" na árvore da esquerda

**O que você verá:**
```
┌──────────────────────────────────────────────────────────────────────┐
│ Summary Report                                                       │
├──────────────┬─────────┬─────────┬────────┬────────┬────────┬───────┤
│ Label        │ Samples │ Average │ Min    │ Max    │ Std Dev│ Error%│
├──────────────┼─────────┼─────────┼────────┼────────┼────────┼───────┤
│ PUT - Update │   200   │  125 ms │  45 ms │ 380 ms │  65 ms │  0.0% │
│ ClientePF    │         │         │        │        │        │       │
├──────────────┼─────────┼─────────┼────────┼────────┼────────┼───────┤
│ TOTAL        │   200   │  125 ms │  45 ms │ 380 ms │  65 ms │  0.0% │
└──────────────┴─────────┴─────────┴────────┴────────┴────────┴───────┘

📊 Throughput: 15.2 req/sec
```

**Para que serve:**
- ✅ Ver **performance geral** rapidamente
- ✅ Identificar **taxa de erro**
- ✅ Ver **throughput** (requisições/segundo)

**Como interpretar:**
- **Average < 200ms** → 🟢 EXCELENTE
- **Average 200-500ms** → 🟡 ACEITÁVEL
- **Average > 500ms** → 🔴 RUIM
- **Error% > 5%** → 🔴 PROBLEMA CRÍTICO

---

#### **C) Aggregate Report** (Estatísticas Detalhadas)

**Como acessar:**
- Clicar em "Aggregate Report" na árvore da esquerda

**O que você verá:**
```
┌────────────────────────────────────────────────────────────────────────────┐
│ Aggregate Report                                                           │
├──────────────┬─────────┬─────────┬────────┬────────┬──────┬──────┬────────┤
│ Label        │ Samples │ Average │ Median │ 90% Line│ 95%  │ 99%  │ Error% │
├──────────────┼─────────┼─────────┼────────┼─────────┼──────┼──────┼────────┤
│ PUT - Update │   200   │  125 ms │ 110 ms │  220 ms │ 280ms│ 360ms│  0.0%  │
│ ClientePF    │         │         │        │         │      │      │        │
└──────────────┴─────────┴─────────┴────────┴─────────┴──────┴──────┴────────┘

📊 Min: 45 ms | Max: 380 ms | Std Dev: 65 ms
📊 Throughput: 15.2/sec | KB/sec: 45.8
```

**Para que serve:**
- ✅ Ver **percentis** (90%, 95%, 99%)
- ✅ Validar **SLA** (Service Level Agreement)
- ✅ Identificar **outliers** (Max vs 99%)

**SLA do cliente-core:**
| Métrica | Target | Crítico |
|---------|--------|---------|
| Average | < 200ms | < 500ms |
| 90% Line | < 300ms | < 800ms |
| 95% Line | < 500ms | < 1000ms |
| Error% | < 1% | < 5% |

---

#### **D) Graph Results** (Gráfico Visual)

**Como acessar:**
- Clicar em "Graph Results" na árvore da esquerda

**O que você verá:**
```
┌────────────────────────────────────────────────────────────┐
│ Graph Results                                              │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  Response Time (ms)                                        │
│  500 ┤                                    ●                │
│  400 ┤              ●         ●                            │
│  300 ┤         ●         ●         ●                       │
│  200 ┤    ●         ●         ●         ●                  │
│  100 ┤●         ●                                          │
│    0 └────────────────────────────────────────────────────┤
│       0   20   40   60   80  100  120  140  160  180  200 │
│                     Samples                                │
│                                                            │
│  ━━━ Average   ━━━ Median   ━━━ Throughput               │
└────────────────────────────────────────────────────────────┘
```

**Para que serve:**
- ✅ Ver **evolução temporal** do teste
- ✅ Identificar **picos de latência**
- ✅ Ver **tendências** (melhora/piora ao longo do tempo)

---

## 🎯 Opção 2: Modo CLI + Relatório HTML (Recomendado para CI/CD)

### Passo 1: Executar Teste em CLI

```bash
cd src/test/jmeter

# Criar diretório de resultados
mkdir -p results

# Executar teste (10 usuários, 10 loops)
jmeter -n -t UpdateClientePF_LoadTest.jmx \
  -Jusers=10 \
  -Jrampup=5 \
  -Jloops=10 \
  -l results/test-$(date +%Y%m%d-%H%M%S).jtl \
  -e -o results/report-$(date +%Y%m%d-%H%M%S)
```

**Output esperado:**
```
Creating summariser <summary>
Created the tree successfully using UpdateClientePF_LoadTest.jmx
Starting standalone test @ 2025 Nov 03 22:00:00 BRT (1730678400000)
Waiting for possible Shutdown/StopTestNow/HeapDump/ThreadDump message on port 4445

summary +    100 in 00:00:08 =   12.5/s Avg:   125 Min:    45 Max:   380 Err:     0 (0.00%)
summary +    100 in 00:00:08 =   12.5/s Avg:   128 Min:    48 Max:   375 Err:     0 (0.00%)
summary =    200 in 00:00:16 =   12.5/s Avg:   126 Min:    45 Max:   380 Err:     0 (0.00%)

Tidying up ...    @ 2025 Nov 03 22:00:16 BRT (1730678416000)
... end of run
```

### Passo 2: Abrir Relatório HTML

```bash
# macOS
open results/report-20251103-220000/index.html

# Windows
start results/report-20251103-220000/index.html

# Linux
xdg-open results/report-20251103-220000/index.html
```

**O que você verá no navegador:**

```
┌─────────────────────────────────────────────────────────────┐
│ 📊 Apache JMeter Dashboard                                 │
│ Test and Report information                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ 📈 APDEX (Application Performance Index)                   │
│     Score: 0.95 (Excellent) ✅                             │
│                                                             │
│ 📊 Statistics                                              │
│     Total Samples: 200                                     │
│     KO: 0 (0.00%) ✅                                       │
│     Error Rate: 0.00% ✅                                   │
│     Average Response Time: 125 ms ✅                       │
│     Min/Max: 45 ms / 380 ms                                │
│     Throughput: 12.5 req/s                                 │
│                                                             │
│ 📊 Response Times Over Time                                │
│     [GRÁFICO DE LINHA INTERATIVO]                          │
│                                                             │
│ 📊 Response Time Percentiles                               │
│     90th: 220 ms ✅                                        │
│     95th: 280 ms ✅                                        │
│     99th: 360 ms ✅                                        │
│                                                             │
│ 📊 Active Threads Over Time                                │
│     [GRÁFICO MOSTRANDO RAMP-UP]                            │
│                                                             │
│ 📊 Bytes Throughput Over Time                              │
│     [GRÁFICO DE BANDA]                                     │
└─────────────────────────────────────────────────────────────┘

[Top 5 Errors by sampler]  [Statistics]  [Charts]  [Requests]
```

**Abas disponíveis no relatório:**
1. **Dashboard** → Resumo executivo
2. **Charts** → Gráficos detalhados
3. **Statistics** → Tabelas numéricas
4. **Errors** → Análise de falhas (se houver)

---

## 🎯 Opção 3: Análise Rápida via Linha de Comando

### Ver Resumo do JTL (arquivo de log)

```bash
# Instalar jtl-reporter (primeira vez)
npm install -g jtl-reporter

# Gerar relatório a partir do JTL
jtl-reporter results/test-20251103-220000.jtl

# Ou usar comando nativo do JMeter
jmeter -g results/test-20251103-220000.jtl -o results/quick-report
```

---

## 📋 Checklist de Validação

Após ver os resultados, validar:

### ✅ Performance Aceitável
- [ ] Tempo médio < 200ms
- [ ] 95º percentil < 500ms
- [ ] Taxa de erro < 1%
- [ ] Throughput > 10 req/s

### ✅ Estabilidade
- [ ] Gráfico não mostra degradação ao longo do tempo
- [ ] Desvio padrão razoável (não muito alto)
- [ ] Sem outliers extremos (Max não é 10x Average)

### ✅ Escalabilidade
- [ ] Performance similar com 10, 50, 100 usuários
- [ ] Throughput aumenta linearmente com usuários
- [ ] Sem erros de timeout ou connection refused

---

## 🐛 Problemas Comuns

### 1. Gráficos vazios no GUI
**Causa:** Executou teste em CLI e depois abriu GUI
**Solução:** Executar teste direto no GUI ou usar relatório HTML

### 2. "FileNotFoundException: data/clientes_pf_testdata.csv"
**Causa:** Não executou script de setup
**Solução:**
```bash
cd src/test/jmeter/scripts
./setup-test-data.sh 20
```

### 3. Todos os requests falhando (100% erro)
**Causa:** Aplicação não está rodando
**Solução:**
```bash
# Terminal separado
mvn spring-boot:run
```

### 4. Performance muito ruim (> 2000ms)
**Causa:** Banco de dados sem índices ou pool insuficiente
**Solução:** Verificar logs SQL e configuração Hikari

---

## 🎓 Dicas Avançadas

### Comparar Resultados de Testes Diferentes

```bash
# Teste 1 - Carga baixa
jmeter -n -t UpdateClientePF_LoadTest.jmx -Jusers=10 -l results/low.jtl

# Teste 2 - Carga média
jmeter -n -t UpdateClientePF_LoadTest.jmx -Jusers=50 -l results/medium.jtl

# Teste 3 - Carga alta
jmeter -n -t UpdateClientePF_LoadTest.jmx -Jusers=100 -l results/high.jtl

# Comparar no Excel ou ferramenta de BI
```

### Exportar Resultados para CSV

No GUI:
1. Summary Report → Botão "Save Table Data"
2. Salvar como `.csv`
3. Abrir no Excel/Google Sheets

---

**🎉 Pronto! Agora você sabe como ver os resultados do JMeter de 3 formas diferentes!**

**Dúvidas? Verifique o README.md principal em src/test/jmeter/README.md**
