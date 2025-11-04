# 🔍 SonarQube + Security Scanning - Guia de Configuração

## 📋 Visão Geral

Este guia detalha como configurar **SonarQube**, **OWASP Dependency Check** e **CodeQL** no GitHub Actions para análise contínua de qualidade e segurança do código.

---

## 🎯 Ferramentas Integradas

| Ferramenta | Propósito | Quando Roda |
|------------|-----------|-------------|
| **SonarQube** | Qualidade de código, code smells, bugs | Todos os pushes/PRs |
| **JaCoCo** | Cobertura de testes (80% mínimo) | Durante build |
| **OWASP Dependency Check** | Vulnerabilidades em dependências | Todos os pushes/PRs |
| **CodeQL** | SAST - vulnerabilidades no código | Todos os pushes/PRs |
| **JMeter** | Testes de performance | Developer/Release branches |

---

## 🚀 Parte 1: Configuração do SonarQube

### **Opção A: SonarCloud (Recomendado para início)**

SonarCloud é gratuito para projetos open-source.

#### **1. Criar conta no SonarCloud:**
```bash
# Acesse
https://sonarcloud.io

# Login com GitHub
# Autorize acesso ao repositório va-nessa-mudanca
```

#### **2. Criar novo projeto:**
```
Organization: va-nessa-mudanca
Project Key: va-nessa-mudanca-cliente-core
Project Name: cliente-core
```

**⚠️ Importante:** SonarCloud não aceita underscores no Project Key. Use hífens.

#### **3. Obter Token:**
```
My Account → Security → Generate Token
Nome: github-actions-cliente-core
Type: User Token
Scope: All
```

**Copie o token:** `sqp_xxxxxxxxxxxxxxxxxxxx`

#### **4. Configurar GitHub Secrets:**
```bash
# Via CLI
gh secret set SONAR_TOKEN --body "sqp_xxxxxxxxxxxxxxxxxxxx"
gh secret set SONAR_HOST_URL --body "https://sonarcloud.io"

# Ou via interface web:
# Settings → Secrets and variables → Actions → New repository secret
```

---

### **Opção B: SonarQube Self-Hosted (Produção)**

Para ambiente corporativo com dados sensíveis.

#### **1. Deploy SonarQube via Docker:**
```yaml
# docker-compose.yml
version: '3'
services:
  sonarqube:
    image: sonarqube:10-community
    ports:
      - 9000:9000
    environment:
      - SONAR_JDBC_URL=jdbc:postgresql://db:5432/sonar
      - SONAR_JDBC_USERNAME=sonar
      - SONAR_JDBC_PASSWORD=sonar
    volumes:
      - sonarqube_data:/opt/sonarqube/data
      - sonarqube_extensions:/opt/sonarqube/extensions
      - sonarqube_logs:/opt/sonarqube/logs

  db:
    image: postgres:16-alpine
    environment:
      - POSTGRES_USER=sonar
      - POSTGRES_PASSWORD=sonar
      - POSTGRES_DB=sonar
    volumes:
      - postgresql_data:/var/lib/postgresql/data

volumes:
  sonarqube_data:
  sonarqube_extensions:
  sonarqube_logs:
  postgresql_data:
```

```bash
# Iniciar
docker-compose up -d

# Acessar
open http://localhost:9000
# Login padrão: admin / admin
```

#### **2. Configurar Projeto:**
```
Administration → Projects → Create Project
Project Key: cliente-core
Display Name: Cliente Core
```

#### **3. Gerar Token:**
```
My Account → Security → Generate Token
```

#### **4. Configurar Secrets:**
```bash
gh secret set SONAR_TOKEN --body "squ_xxxxxxxxxxxxxxxxxxxx"
gh secret set SONAR_HOST_URL --body "https://sonar.vanessa-mudanca.com.br"
```

---

## 📦 Parte 2: Quality Gates no SonarQube

### **Configurar Quality Gates:**

```
Quality Gates → Create
Nome: Cliente-Core-Gate

Condições:
✅ Coverage: > 80%
✅ Duplicated Lines: < 3%
✅ Maintainability Rating: A
✅ Reliability Rating: A
✅ Security Rating: A
✅ Security Hotspots Reviewed: 100%
✅ New Bugs: 0
✅ New Vulnerabilities: 0
```

### **Associar ao Projeto:**
```
Project Settings → Quality Gate
Select: Cliente-Core-Gate
```

---

## 🛡️ Parte 3: Configuração do CodeQL (GitHub)

### **1. Habilitar CodeQL:**

CodeQL é integrado ao GitHub Advanced Security (gratuito para repos públicos).

```
Settings → Security → Code security and analysis
Enable: Code scanning → CodeQL analysis
```

### **2. Permissões do Workflow:**

Já configurado em `.github/workflows/code-quality.yml`:
```yaml
permissions:
  security-events: write  # Necessário para CodeQL
```

### **3. Visualizar Resultados:**
```
Security → Code scanning alerts
```

---

## 📊 Parte 4: Configuração do OWASP Dependency Check

### **1. Configuração no pom.xml:**

Já adicionado automaticamente:
```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.9</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>  <!-- Falha se CVSS >= 7 (HIGH/CRITICAL) -->
    </configuration>
</plugin>
```

### **2. Rodar Localmente:**
```bash
# Análise de dependências
mvn org.owasp:dependency-check-maven:check

# Relatório gerado em:
open target/dependency-check-report.html
```

### **3. Suprimir Falsos Positivos:**

Editar `.dependency-check-suppressions.xml`:
```xml
<suppress>
   <notes>
      Falso positivo: CVE afeta apenas módulo não usado.
      Validado por: Tech Lead
      Data: 2025-11-03
   </notes>
   <gav regex="true">org\.springframework:.*:5\.3\..*</gav>
   <cve>CVE-2023-12345</cve>
</suppress>
```

---

## 🎯 Parte 5: JaCoCo Coverage

### **1. Configuração no pom.xml:**

Já configurado com **80% minimum coverage**:
```xml
<configuration>
    <rules>
        <rule>
            <limits>
                <limit>
                    <counter>LINE</counter>
                    <minimum>0.80</minimum>  <!-- 80% mínimo -->
                </limit>
            </limits>
        </rule>
    </rules>
</configuration>
```

### **2. Gerar Relatório:**
```bash
# Rodar testes + coverage
mvn clean test

# Relatório gerado em:
open target/site/jacoco/index.html
```

### **3. Verificar Coverage:**
```bash
# Falha se coverage < 80%
mvn verify
```

---

## 🔄 Parte 6: Workflow Completo

### **Arquivo:** `.github/workflows/code-quality.yml`

**Triggers:**
- Push em `developer`, `release`, `main`
- Pull requests para essas branches

**Jobs executados:**

#### **1. SonarQube Analysis**
- Build + testes
- Análise de qualidade (bugs, code smells, duplicação)
- Coverage report (JaCoCo)
- **Quality Gate validation** (bloqueia se falhar)

#### **2. OWASP Dependency Check**
- Scan de vulnerabilidades em dependências
- Falha se CVE >= 7 (HIGH/CRITICAL)
- Upload de relatório HTML

#### **3. CodeQL Security Scan**
- SAST (Static Application Security Testing)
- Detecta vulnerabilidades no código
- Integrado ao GitHub Security tab

---

## 📈 Parte 7: Visualizar Resultados

### **SonarQube:**
```
https://sonarcloud.io/dashboard?id=va-nessa-mudanca-cliente-core

Métricas:
- Coverage: 95.2%
- Bugs: 0
- Vulnerabilities: 0
- Code Smells: 12
- Technical Debt: 2h
- Duplications: 1.2%
```

### **GitHub Security:**
```
Security → Code scanning
Security → Dependabot alerts
Actions → code-quality workflow
```

### **Relatórios Locais:**
```bash
# JaCoCo
open target/site/jacoco/index.html

# OWASP
open target/dependency-check-report.html
```

---

## ⚠️ Troubleshooting

### **Erro: SonarQube Quality Gate Failed**

```
ERROR: Quality gate failed
Coverage: 75% (required: 80%)
```

**Solução:**
```bash
# Adicionar testes
# Rodar localmente
mvn test jacoco:report

# Verificar coverage
open target/site/jacoco/index.html

# Identificar classes sem coverage
# Adicionar testes unitários
```

---

### **Erro: OWASP encontrou CVE crítica**

```
ERROR: Dependency CVE-2023-12345 (CVSS: 9.8) found in spring-core:5.3.20
```

**Solução:**
```bash
# 1. Atualizar dependência
# Em pom.xml:
<spring.version>5.3.30</spring.version>

# 2. Se não houver versão corrigida, avaliar risco e suprimir
# Em .dependency-check-suppressions.xml:
<suppress>
   <notes>
      Aceito risco: Funcionalidade vulnerável não é usada.
      Aprovado por: CISO
   </notes>
   <cve>CVE-2023-12345</cve>
</suppress>
```

---

### **Erro: CodeQL encontrou vulnerabilidade**

```
Code scanning alert: SQL Injection risk in CustomerRepository.java
```

**Solução:**
```java
// ❌ VULNERÁVEL
String query = "SELECT * FROM clientes WHERE cpf = '" + cpf + "'";

// ✅ CORRETO - Usar JPA/PreparedStatement
@Query("SELECT c FROM Cliente c WHERE c.cpf = :cpf")
Cliente findByCpf(@Param("cpf") String cpf);
```

---

## 🎯 Quality Gates - Critérios de Aprovação

### **Para Merge em `developer`:**
- ✅ SonarQube: Quality Gate PASS
- ✅ Coverage: >= 80%
- ✅ OWASP: Sem CVE >= 7
- ✅ CodeQL: Sem vulnerabilidades HIGH/CRITICAL
- ✅ Testes: 100% passando

### **Para Merge em `release`:**
- ✅ Todos critérios de `developer` +
- ✅ JMeter load test: PASS
- ✅ Aprovação manual de QA

### **Para Merge em `main` (PROD):**
- ✅ Todos critérios de `release` +
- ✅ 2 aprovações de Tech Leads
- ✅ Aprovação manual de deploy

---

## 📚 Comandos Úteis

### **Rodar todos os checks localmente:**
```bash
# Full pipeline local
mvn clean verify \
  sonar:sonar \
  org.owasp:dependency-check-maven:check \
  -Dsonar.host.url=$SONAR_URL \
  -Dsonar.token=$SONAR_TOKEN
```

### **Apenas coverage:**
```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

### **Apenas security scan:**
```bash
mvn org.owasp:dependency-check-maven:check
open target/dependency-check-report.html
```

### **Apenas SonarQube:**
```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=cliente-core \
  -Dsonar.host.url=$SONAR_URL \
  -Dsonar.token=$SONAR_TOKEN
```

---

## 🎓 Referências

- [SonarQube Documentation](https://docs.sonarqube.org/latest/)
- [SonarCloud Setup](https://sonarcloud.io/documentation)
- [OWASP Dependency Check](https://jeremylong.github.io/DependencyCheck/)
- [CodeQL Documentation](https://codeql.github.com/docs/)
- [JaCoCo Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)

---

**Última atualização:** 2025-11-03
**Versão:** 1.0
**Responsável:** Equipe DevSecOps Va Nessa Mudança
