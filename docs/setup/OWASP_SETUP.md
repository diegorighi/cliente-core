# OWASP Dependency Check - Configuração

## Status Atual

Por padrão, o OWASP Dependency Check está **DESABILITADO** no ambiente local para evitar erros de rate limit do NVD (National Vulnerability Database).

```xml
<owasp.skip>true</owasp.skip>
```

## Por que foi desabilitado?

Desde 2023, o NVD (fonte de dados do OWASP) passou a exigir **API Key** para consultas frequentes. Sem a key, você recebe erros `403/404` após algumas requisições.

## Como habilitar o OWASP (Produção/CI/CD)

### Opção 1: Obter API Key do NVD (RECOMENDADO)

1. Acesse: https://nvd.nist.gov/developers/request-an-api-key
2. Preencha o formulário e solicite uma API Key gratuita
3. Configure a key no Maven:

**Via variável de ambiente:**
```bash
export NVD_API_KEY="sua-api-key-aqui"
mvn verify -Dowasp.skip=false
```

**Via pom.xml (NÃO commitar a key!):**
```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <configuration>
        <nvdApiKey>${env.NVD_API_KEY}</nvdApiKey>
        <skip>false</skip>
    </configuration>
</plugin>
```

**Via settings.xml (recomendado):**
```xml
<!-- ~/.m2/settings.xml -->
<settings>
    <profiles>
        <profile>
            <id>owasp</id>
            <properties>
                <nvd.api.key>sua-api-key-aqui</nvd.api.key>
            </properties>
        </profile>
    </profiles>
</settings>
```

Depois use:
```bash
mvn verify -Powasp -Dowasp.skip=false
```

### Opção 2: Rodar sem API Key (limitado)

Funciona para poucos builds por dia:
```bash
mvn verify -Dowasp.skip=false
```

**IMPORTANTE**: Pode falhar se você rodar múltiplos builds no mesmo dia.

## Configuração no CI/CD

### GitHub Actions

```yaml
name: Security Scan

on:
  push:
    branches: [ main, developer ]
  pull_request:
    branches: [ main ]

jobs:
  owasp:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: OWASP Dependency Check
        env:
          NVD_API_KEY: ${{ secrets.NVD_API_KEY }}
        run: mvn verify -Dowasp.skip=false

      - name: Upload OWASP Report
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: owasp-report
          path: target/dependency-check-report.html
```

**Configurar secret no GitHub:**
1. Settings → Secrets and variables → Actions
2. New repository secret: `NVD_API_KEY`

### GitLab CI

```yaml
owasp-scan:
  stage: security
  script:
    - mvn verify -Dowasp.skip=false
  variables:
    NVD_API_KEY: $NVD_API_KEY
  artifacts:
    reports:
      dependency_scanning: target/dependency-check-report.html
    when: always
```

**Configurar variável no GitLab:**
1. Settings → CI/CD → Variables
2. Add variable: `NVD_API_KEY` (masked)

## Threshold de Severidade

Atualmente configurado para **CVSS 7+** (HIGH/CRITICAL):

```xml
<failBuildOnCVSS>7</failBuildOnCVSS>
```

**Níveis CVSS:**
- 0.0 - 3.9: LOW
- 4.0 - 6.9: MEDIUM
- **7.0 - 8.9: HIGH** ← Build falha aqui
- **9.0 - 10.0: CRITICAL** ← Build falha aqui

## Suppressions

Vulnerabilidades conhecidas e aceitas estão em:
```
.dependency-check-suppressions.xml
```

**IMPORTANTE**: Apenas adicione suppressions após análise e aprovação do time de segurança!

## Comandos Úteis

```bash
# Build normal (OWASP desabilitado)
mvn clean verify

# Build com OWASP (requer API Key ou poucos builds/dia)
mvn verify -Dowasp.skip=false

# Gerar apenas relatório OWASP (não falha build)
mvn dependency-check:check

# Ver relatório HTML
open target/dependency-check-report.html
```

## Links Úteis

- [OWASP Dependency Check](https://jeremylong.github.io/DependencyCheck/)
- [NVD API Key Registration](https://nvd.nist.gov/developers/request-an-api-key)
- [CVSS Calculator](https://nvd.nist.gov/vuln-metrics/cvss/v3-calculator)
- [Suppression File Format](https://jeremylong.github.io/DependencyCheck/general/suppression.html)

---

**Última atualização:** 2025-11-04
