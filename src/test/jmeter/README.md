# Testes de Performance com Apache JMeter

Este diretório contém planos de teste JMeter para validar performance dos endpoints do cliente-core.

## 📋 Pré-requisitos

### 1. Instalar Apache JMeter

**macOS (via Homebrew):**
```bash
brew install jmeter
```

**Windows:**
1. Baixar de https://jmeter.apache.org/download_jmeter.cgi
2. Extrair ZIP para `C:\jmeter`
3. Adicionar `C:\jmeter\bin` ao PATH

**Linux:**
```bash
sudo apt-get install jmeter  # Debian/Ubuntu
# ou
sudo yum install jmeter      # RedHat/CentOS
```

**Verificar instalação:**
```bash
jmeter --version
```

### 2. Iniciar Aplicação

```bash
# Terminal 1 - Iniciar aplicação em modo dev
mvn spring-boot:run

# Aguardar até ver:
# "Started ClienteCoreApplication in X seconds"
```

## 📊 Planos de Teste Disponíveis

### 1. `UpdateClientePF_LoadTest.jmx`

**Objetivo:** Validar performance do endpoint `PUT /v1/clientes/pf/{id}`

**Cenários:**
- **Carga Baixa:** 10 usuários simultâneos, 100 requisições
- **Carga Média:** 50 usuários simultâneos, 500 requisições
- **Carga Alta:** 100 usuários simultâneos, 1000 requisições

**Métricas coletadas:**
- Tempo de resposta médio (ms)
- Tempo de resposta 90º percentil (ms)
- Tempo de resposta 95º percentil (ms)
- Throughput (requisições/segundo)
- Taxa de erro (%)

## 🚀 Executar Testes

### Modo GUI (Desenvolvimento/Ajustes)

```bash
cd src/test/jmeter
jmeter -t UpdateClientePF_LoadTest.jmx
```

**Passos:**
1. Abrir JMeter GUI
2. Ajustar parâmetros (se necessário):
   - **Thread Group** → Number of Threads (usuários)
   - **Thread Group** → Loop Count (repetições)
3. Clicar em "Run" → "Start"
4. Visualizar resultados em "View Results Tree" e "Summary Report"

### Modo CLI (Produção/CI/CD)

**Carga Baixa (10 usuários):**
```bash
jmeter -n -t UpdateClientePF_LoadTest.jmx \
  -Jusers=10 \
  -Jrampup=5 \
  -Jloops=10 \
  -l results/load-low.jtl \
  -e -o results/load-low-report
```

**Carga Média (50 usuários):**
```bash
jmeter -n -t UpdateClientePF_LoadTest.jmx \
  -Jusers=50 \
  -Jrampup=10 \
  -Jloops=10 \
  -l results/load-medium.jtl \
  -e -o results/load-medium-report
```

**Carga Alta (100 usuários):**
```bash
jmeter -n -t UpdateClientePF_LoadTest.jmx \
  -Jusers=100 \
  -Jrampup=20 \
  -Jloops=10 \
  -l results/load-high.jtl \
  -e -o results/load-high-report
```

**Parâmetros:**
- `-n`: Modo non-GUI
- `-t`: Test plan file
- `-J`: Propriedades dinâmicas (users, rampup, loops)
- `-l`: Log file (JTL format)
- `-e`: Gerar relatório HTML
- `-o`: Diretório de saída do relatório

### Visualizar Relatórios

```bash
# Abrir relatório HTML no navegador
open results/load-high-report/index.html  # macOS
start results/load-high-report/index.html # Windows
xdg-open results/load-high-report/index.html # Linux
```

## 📈 Análise de Resultados

### SLA Esperado (Cliente-Core)

| Métrica | Target | Crítico |
|---------|--------|---------|
| Tempo de resposta médio | < 200ms | < 500ms |
| 90º percentil | < 300ms | < 800ms |
| 95º percentil | < 500ms | < 1000ms |
| Taxa de erro | < 1% | < 5% |
| Throughput | > 100 req/s | > 50 req/s |

### Interpretar Resultados

**🟢 PASS (Performance Aceitável):**
- Tempo médio < 200ms
- 95º percentil < 500ms
- Taxa de erro < 1%

**🟡 WARNING (Performance Degradada):**
- Tempo médio entre 200-500ms
- 95º percentil entre 500-1000ms
- Taxa de erro entre 1-5%

**🔴 FAIL (Performance Inaceitável):**
- Tempo médio > 500ms
- 95º percentil > 1000ms
- Taxa de erro > 5%

## 🔧 Troubleshooting

### Erro: "Connection refused"

**Causa:** Aplicação não está rodando
**Solução:**
```bash
# Iniciar aplicação
mvn spring-boot:run
```

### Erro: "Too many open files"

**Causa:** Limite de file descriptors do SO
**Solução (macOS/Linux):**
```bash
ulimit -n 10000
```

### Performance baixa (> 1000ms)

**Possíveis causas:**
1. **Banco de dados lento:**
   - Verificar índices (ver `010-create-indexes.sql`)
   - Ativar logs SQL: `logging.level.org.hibernate.SQL=DEBUG`

2. **Pool de conexões insuficiente:**
   - Aumentar `spring.datasource.hikari.maximum-pool-size` (default: 10)

3. **CPU/memória insuficiente:**
   - Aumentar heap JVM: `mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx2g"`

## 📁 Estrutura de Arquivos

```
src/test/jmeter/
├── README.md                           # Este arquivo
├── UpdateClientePF_LoadTest.jmx       # Plano de teste UPDATE PF
├── UpdateClientePJ_LoadTest.jmx       # Plano de teste UPDATE PJ (futuro)
├── data/
│   ├── clientes_pf_testdata.csv      # Dados de teste (CPFs válidos)
│   └── update_payloads.csv            # Payloads de UPDATE
└── results/                            # Resultados dos testes (gitignored)
    ├── load-low.jtl
    ├── load-low-report/
    ├── load-medium.jtl
    └── load-medium-report/
```

## 🔒 Boas Práticas

1. **Sempre criar dados de teste antes:**
   ```bash
   # POST /v1/clientes/pf para criar clientes válidos
   # Guardar publicIds em data/clientes_pf_testdata.csv
   ```

2. **Executar testes em ambiente isolado:**
   - Não executar em produção
   - Usar banco de dados de teste/desenvolvimento

3. **Validar dados após testes:**
   - Verificar que updates foram persistidos corretamente
   - Limpar dados de teste após execução

4. **Monitorar recursos:**
   ```bash
   # Terminal separado - monitorar CPU/memória
   htop  # ou top
   ```

5. **Versionar apenas planos (.jmx) e dados (.csv):**
   - Adicionar `results/` ao `.gitignore`

## 📚 Referências

- [Apache JMeter Documentation](https://jmeter.apache.org/usermanual/index.html)
- [JMeter Best Practices](https://jmeter.apache.org/usermanual/best-practices.html)
- [Performance Testing Guide](https://martinfowler.com/articles/practical-test-pyramid.html#PerformanceTests)

---

**Última atualização:** 2025-11-03
**Versão:** 1.0
**Responsável:** Equipe Va Nessa Mudança
