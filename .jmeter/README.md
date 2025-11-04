# 🔥 JMeter Performance Tests

Testes de performance automatizados para o cliente-core.

## 📁 Estrutura

```
.jmeter/
├── README.md
└── tests/
    ├── smoke-test.jmx      # Smoke test (10 users, 30s)
    └── load-test.jmx       # Load test (100 users, 2min)
```

## 🚀 Uso Rápido

### Smoke Test (local)
```bash
# 1. Inicie a aplicação
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 2. Rode o smoke test
jmeter -n -t .jmeter/tests/smoke-test.jmx \
  -l results/smoke.jtl \
  -e -o results/smoke-report/

# 3. Veja o relatório
open results/smoke-report/index.html
```

### Load Test (local)
```bash
jmeter -n -t .jmeter/tests/load-test.jmx \
  -Jthreads=50 \
  -Jduration=60 \
  -l results/load.jtl \
  -e -o results/load-report/

open results/load-report/index.html
```

## 📊 Parâmetros

| Parâmetro | Smoke Test | Load Test | Descrição |
|-----------|------------|-----------|-----------|
| `threads` | 10 | 100 | Usuários simultâneos |
| `rampup` | 5 | 30 | Tempo de ramp-up (segundos) |
| `duration` | 30 | 120 | Duração do teste (segundos) |
| `host` | localhost | localhost | Hostname da aplicação |
| `port` | 8081 | 8081 | Porta da aplicação |

## 🎯 Performance Targets

### Smoke Test
- ✅ Error rate: 0%
- ✅ P95 latency: < 500ms

### Load Test
- ✅ Error rate: < 1%
- ✅ Avg latency: < 200ms
- ✅ P95 latency: < 500ms
- ✅ Throughput: > 100 req/s

## 📖 Documentação Completa

Veja `docs/CI-CD-STRATEGY.md` para detalhes completos da estratégia de CI/CD e testes.
