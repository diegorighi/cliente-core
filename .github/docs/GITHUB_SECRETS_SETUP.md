# GitHub Secrets - Configuração para CI/CD

Este documento explica como configurar os secrets necessários para os workflows do GitHub Actions funcionarem corretamente.

## Secrets Necessários

### 1. SONAR_TOKEN (SonarCloud)

**Onde usar**: Workflow `code-quality.yml` - Job `sonarcloud`

**Como obter**:

1. Acesse [SonarCloud](https://sonarcloud.io/)
2. Faça login com sua conta GitHub
3. Vá em **My Account** → **Security**
4. Em **Generate Tokens**, crie um novo token:
   - **Name**: `GitHub Actions - cliente-core`
   - **Type**: `User Token`
   - **Expiration**: escolha o período (recomendado: 90 dias)
5. Clique em **Generate** e **copie o token** (você só verá uma vez!)

**Como configurar no GitHub**:

1. Vá em **Settings** → **Secrets and variables** → **Actions**
2. Clique em **New repository secret**
3. **Name**: `SONAR_TOKEN`
4. **Secret**: cole o token copiado do SonarCloud
5. Clique em **Add secret**

---

### 2. SONAR_ORGANIZATION (SonarCloud)

**Status**: ✅ **JÁ CONFIGURADO** no arquivo `code-quality.yml`

```yaml
env:
  SONAR_ORGANIZATION: va-nessa-mudanca
```

**Como atualizar** (se necessário):

1. Abra `.github/workflows/code-quality.yml`
2. Localize a linha `SONAR_ORGANIZATION: va-nessa-mudanca`
3. Altere para o nome da sua organização no SonarCloud

**Onde encontrar o nome da organização**:
- SonarCloud → **My Organizations** → veja o **Key** da organização

---

### 3. NVD_API_KEY (OWASP Dependency Check)

**Onde usar**: Workflow `code-quality.yml` - Job `dependency-check`

**Como obter**:

1. Acesse [NVD API Key Request](https://nvd.nist.gov/developers/request-an-api-key)
2. Preencha o formulário:
   - **Email**: seu email profissional
   - **Organization**: Va Nessa Mudança
   - **Purpose**: Security vulnerability scanning for CI/CD pipeline
3. Aguarde o email com a API Key (geralmente instantâneo)
4. Copie a API Key do email

**Como configurar no GitHub**:

1. Vá em **Settings** → **Secrets and variables** → **Actions**
2. Clique em **New repository secret**
3. **Name**: `NVD_API_KEY`
4. **Secret**: cole a API Key do email do NVD
5. Clique em **Add secret**

**IMPORTANTE**: A API Key é **gratuita** e permite até **50 requisições por 30 segundos** (mais que suficiente para CI/CD).

---

### 4. GITHUB_TOKEN

**Status**: ✅ **AUTOMÁTICO** - Não precisa configurar!

O `GITHUB_TOKEN` é gerado automaticamente pelo GitHub Actions em cada workflow run. É usado para:
- Decorar Pull Requests com comentários do SonarCloud
- Fazer checkout do código
- Upload de artifacts

**Permissões** (já configuradas no workflow):
- `contents: read`
- `security-events: write` (para CodeQL)

---

## Checklist de Configuração

- [ ] **SONAR_TOKEN** configurado no GitHub Secrets
- [x] **SONAR_ORGANIZATION** definido em `code-quality.yml` (valor: `va-nessa-mudanca`)
- [ ] **NVD_API_KEY** configurado no GitHub Secrets
- [x] **GITHUB_TOKEN** (automático, nada a fazer)

---

## Como Criar Projeto no SonarCloud

Se o projeto ainda não existe no SonarCloud:

1. Acesse [SonarCloud](https://sonarcloud.io/)
2. Clique em **+** → **Analyze new project**
3. Selecione o repositório: **va-nessa-mudanca/cliente-core**
4. Clique em **Set Up**
5. Escolha **With GitHub Actions**
6. Configure o **Project Key**: `va-nessa-mudanca-cliente-core` (já configurado no workflow)
7. Configure a **Organization**: `va-nessa-mudanca` (já configurado no workflow)
8. Copie o **SONAR_TOKEN** e adicione aos GitHub Secrets (passo 1 acima)

---

## Testando a Configuração

### Teste Local (opcional)

```bash
# SonarCloud (local)
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=va-nessa-mudanca-cliente-core \
  -Dsonar.organization=va-nessa-mudanca \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=SEU_TOKEN_AQUI

# OWASP (local)
mvn verify -Dowasp.skip=false
```

### Teste no GitHub Actions

1. Faça um commit e push para `developer`:
   ```bash
   git add .
   git commit -m "test: Configure SonarCloud and OWASP"
   git push origin developer
   ```

2. Vá em **Actions** no GitHub
3. Veja o workflow **Code Quality and Security** rodando
4. Verifique os 3 jobs:
   - ✅ **SonarCloud Analysis** - deve passar se SONAR_TOKEN está configurado
   - ✅ **OWASP Dependency Check** - deve passar se NVD_API_KEY está configurado
   - ✅ **CodeQL Security** - deve sempre passar (não depende de secrets)

---

## Troubleshooting

### Erro: "You must define the following mandatory properties: sonar.organization"

**Causa**: O workflow não está encontrando a variável `SONAR_ORGANIZATION`

**Solução**:
1. Verifique se `.github/workflows/code-quality.yml` tem:
   ```yaml
   env:
     SONAR_ORGANIZATION: va-nessa-mudanca
   ```
2. Confirme que o nome da organização está correto no SonarCloud

---

### Erro: "Error updating the NVD Data; the NVD returned a 403 or 404"

**Causa**: Falta configurar `NVD_API_KEY` ou API Key inválida

**Solução**:
1. Obtenha nova API Key em https://nvd.nist.gov/developers/request-an-api-key
2. Configure no GitHub Secrets (nome exato: `NVD_API_KEY`)
3. Aguarde alguns minutos para o secret ser propagado

---

### Erro: "Invalid SONAR_TOKEN"

**Causa**: Token expirado ou copiado incorretamente

**Solução**:
1. Gere novo token no SonarCloud → My Account → Security
2. Atualize o secret `SONAR_TOKEN` no GitHub
3. Rode o workflow novamente

---

## Links Úteis

- [SonarCloud](https://sonarcloud.io/)
- [SonarCloud - GitHub Integration](https://docs.sonarcloud.io/getting-started/github/)
- [NVD API Key Request](https://nvd.nist.gov/developers/request-an-api-key)
- [GitHub Actions Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [OWASP Dependency Check](https://jeremylong.github.io/DependencyCheck/)

---

**Última atualização**: 2025-11-04
