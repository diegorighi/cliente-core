# 🤖 GitHub Actions Workflows

## 📋 Workflows Disponíveis

| Workflow | Arquivo | Trigger | Duração | Propósito |
|----------|---------|---------|---------|-----------|
| **CI Básico** | `ci.yml` | Push em `feature/*` | ~3min | Build + testes unitários |
| **Developer + Smoke Test** | `developer-with-jmeter.yml` | Push em `developer` | ~7min | CI + JMeter (10 users) |
| **Release + Load Test** | `release-with-loadtest.yml` | Push em `release` | ~10min | CI + JMeter (100 users) |
| **Code Quality** | `code-quality.yml` | Push/PR em `developer/release/main` | ~8min | SonarQube + OWASP + CodeQL |
| **Production Deploy** | `main-deploy.yml` | Push em `main` | ~15min | Deploy AWS ECS |

---

## 🔑 Secrets Necessários

Configure em: **Settings → Secrets and variables → Actions**

```bash
# SonarQube
SONAR_TOKEN              # Token do SonarCloud
SONAR_HOST_URL           # https://sonarcloud.io

# AWS (para deploy em produção)
AWS_ACCESS_KEY_ID        # (ou use OIDC role)
AWS_SECRET_ACCESS_KEY    # (ou use OIDC role)
```

---

## 🎯 Project Keys

**SonarCloud:**
- **Organization:** va-nessa-mudanca
- **Project Key:** va-nessa-mudanca-cliente-core
- **Dashboard:** https://sonarcloud.io/dashboard?id=va-nessa-mudanca-cliente-core

⚠️ **Importante:** SonarCloud não aceita underscores. Use hífens no Project Key.

---

## 📚 Documentação Completa

- **Setup SonarQube:** [`docs/SONARQUBE-SETUP.md`](../docs/SONARQUBE-SETUP.md)
- **Pipeline CI/CD:** [`docs/CI-CD-STRATEGY.md`](../docs/CI-CD-STRATEGY.md)
- **JMeter Tests:** [`.jmeter/README.md`](../.jmeter/README.md)

---

## 🚀 Como Usar

### **1. Feature Development:**
```bash
git checkout -b feature/nova-funcionalidade
# ... desenvolver ...
git push origin feature/nova-funcionalidade
# ✅ Workflow "ci.yml" roda automaticamente
```

### **2. Developer (Homologação):**
```bash
git checkout developer
git merge feature/nova-funcionalidade
git push origin developer
# ✅ Workflows rodam automaticamente:
#    - code-quality.yml (SonarQube + Security)
#    - developer-with-jmeter.yml (Smoke test)
```

### **3. Release (Pre-Prod):**
```bash
git checkout release
git merge developer
git push origin release
# ✅ Workflows rodam automaticamente:
#    - code-quality.yml
#    - release-with-loadtest.yml (Load test 100 users)
```

### **4. Production:**
```bash
git checkout main
git merge release
git push origin main
# ✅ Workflow main-deploy.yml aguarda aprovação manual
#    (2 aprovadores requeridos)
```

---

## ✅ Quality Gates

Todos os workflows têm **quality gates** que bloqueiam o merge se falharem:

**Code Quality:**
- ✅ SonarQube Quality Gate PASS
- ✅ Coverage >= 80%
- ✅ Sem CVE HIGH/CRITICAL (OWASP)
- ✅ Sem vulnerabilidades HIGH/CRITICAL (CodeQL)

**Performance:**
- ✅ JMeter Smoke: 0% error rate
- ✅ JMeter Load: P95 < 500ms

---

**Última atualização:** 2025-11-03
