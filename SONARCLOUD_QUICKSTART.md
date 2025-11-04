# SonarCloud - Quick Start (5 minutos)

## ⚠️ IMPORTANTE: Faça isso ANTES de rodar o workflow

O SonarCloud precisa que você crie o projeto **antes** de rodar a análise no GitHub Actions.

---

## Passo a Passo (5 minutos)

### 1. Acesse o SonarCloud

1. Vá em https://sonarcloud.io/
2. Clique em **Log in**
3. Escolha **Log in with GitHub**
4. Autorize o SonarCloud a acessar sua conta GitHub

---

### 2. Importe o Repositório

1. No canto superior direito, clique no **+** (símbolo de adicionar)
2. Clique em **Analyze new project**
3. Você verá uma lista de repositórios do GitHub
4. **Procure por**: `cliente-core`
5. Marque a checkbox ao lado de `diegorighi/cliente-core`
6. Clique em **Set Up** (botão azul no canto superior direito)

---

### 3. Configure o Projeto

Na tela de configuração:

**Project Key** (gerado automaticamente):
```
diegorighi_cliente-core
```
✅ **Este é o valor correto** que já está configurado no workflow!

**Organization** (sua organização):
```
diegorighi
```
✅ **Este é o seu username do GitHub** que já está configurado no workflow!

**Não mude esses valores!** Eles devem bater com o workflow.

Clique em **Set Up** para continuar.

---

### 4. Escolha o Método de Análise

1. Escolha **With GitHub Actions** (opção recomendada)
2. SonarCloud vai mostrar instruções - **IGNORE**, já fizemos isso!
3. O importante é copiar o **SONAR_TOKEN**:
   - Clique em **Continue**
   - Clique em **Generate token** ou use um existente
   - **Copie o token** (você só verá uma vez!)

---

### 5. Configure o Secret no GitHub

1. Vá no seu repositório: https://github.com/diegorighi/cliente-core
2. Clique em **Settings** (aba no topo)
3. No menu lateral esquerdo, clique em **Secrets and variables** → **Actions**
4. Clique em **New repository secret**
5. Preencha:
   - **Name**: `SONAR_TOKEN`
   - **Secret**: cole o token que você copiou do SonarCloud
6. Clique em **Add secret**

---

### 6. Teste a Configuração

Agora faça um commit para testar:

```bash
# Fazer commit das alterações do workflow
git add .github/workflows/code-quality.yml
git commit -m "ci: Configure SonarCloud with correct organization"
git push origin developer
```

Depois:

1. Vá em **Actions** no GitHub: https://github.com/diegorighi/cliente-core/actions
2. Veja o workflow **Code Quality and Security** rodando
3. Clique no workflow para ver os detalhes
4. O job **SonarCloud Analysis** deve passar ✅

---

## Verificação

Depois que o workflow rodar com sucesso:

1. Volte no SonarCloud: https://sonarcloud.io/
2. Clique em **My Projects**
3. Você verá o projeto **cliente-core** com:
   - **Quality Gate**: Passed ✅ (ou Failed ❌ se houver issues)
   - **Coverage**: 85%+ ✅
   - **Bugs, Vulnerabilities, Code Smells**: detalhes da análise

---

## Troubleshooting

### Erro: "Could not find a default branch"

**Causa**: Projeto não foi criado no SonarCloud

**Solução**: Siga os passos 1-3 acima para criar o projeto

---

### Erro: "Invalid SONAR_TOKEN"

**Causa**: Token não foi configurado ou está errado

**Solução**:
1. Gere novo token no SonarCloud (passo 4)
2. Configure o secret `SONAR_TOKEN` no GitHub (passo 5)
3. Aguarde 1-2 minutos para o secret ser propagado
4. Rode o workflow novamente

---

### Erro: "Project key mismatch"

**Causa**: O project key no workflow não bate com o do SonarCloud

**Verificar**:
1. SonarCloud → My Projects → cliente-core → Project Information
2. Copie o **Project Key** exatamente como está
3. Atualize em `.github/workflows/code-quality.yml`:
   ```yaml
   SONAR_PROJECT_KEY: diegorighi_cliente-core  # Use o valor correto
   ```

---

## Valores Configurados no Workflow

Para referência, estes são os valores **já configurados** no workflow:

```yaml
env:
  SONAR_ORGANIZATION: diegorighi              # ✅ Seu GitHub username
  SONAR_PROJECT_KEY: diegorighi_cliente-core  # ✅ Gerado pelo SonarCloud
```

**URL do SonarCloud**:
```
https://sonarcloud.io/dashboard?id=diegorighi_cliente-core
```

---

## Próximos Passos

Depois que o SonarCloud estiver funcionando:

1. ✅ Configure também o `NVD_API_KEY` (ver `GITHUB_SECRETS_SETUP.md`)
2. ✅ Monitore a qualidade do código no dashboard do SonarCloud
3. ✅ Configure Quality Gates personalizados se necessário
4. ✅ Integre o badge do SonarCloud no README:

```markdown
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=diegorighi_cliente-core&metric=alert_status)](https://sonarcloud.io/dashboard?id=diegorighi_cliente-core)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=diegorighi_cliente-core&metric=coverage)](https://sonarcloud.io/dashboard?id=diegorighi_cliente-core)
```

---

**Tempo estimado**: 5 minutos
**Última atualização**: 2025-11-04
