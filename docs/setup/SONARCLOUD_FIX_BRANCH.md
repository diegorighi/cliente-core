# SonarCloud - Fix "Could not find a default branch"

## Erro Completo

```
Could not find a default branch for project with key 'va-nessa-mudanca-cliente-core'.
Make sure project exists.
```

## Causa Provável

O projeto **existe** no SonarCloud (`va-nessa-mudanca/cliente-core`), mas:
1. O SonarCloud não consegue acessar o repositório do GitHub
2. O binding entre SonarCloud e GitHub está quebrado
3. O repositório vinculado ao projeto está incorreto

---

## Solução: Verificar e Corrigir o Binding

### Passo 1: Verificar o Projeto no SonarCloud

1. Acesse: https://sonarcloud.io/
2. Login with GitHub
3. Vá em **My Projects**
4. Procure por `cliente-core` (pode estar listado como `va-nessa-mudanca_cliente-core`)
5. Clique no projeto

### Passo 2: Verificar Project Information

1. No projeto, clique em **Project Settings** (canto superior direito)
2. Clique em **General Settings**
3. Verifique:
   - **Project Key**: deve ser `va-nessa-mudanca_cliente-core` ✅
   - **Organization**: deve ser `va-nessa-mudanca` ✅

### Passo 3: Verificar o GitHub Repository Binding

1. Ainda em **Project Settings**, procure a aba **GitHub**
2. Ou vá em **Administration** → **Analysis Method** → **GitHub Actions**
3. Verifique qual repositório está vinculado

**Problema comum**: O projeto pode estar vinculado ao repositório errado!

### Passo 4: Reconectar o Repositório (Se necessário)

Se o binding estiver errado:

1. No SonarCloud, vá em **Project Settings** → **General Settings**
2. Procure por **Project Links** ou **Repository**
3. Você pode precisar **deletar** o projeto e **recriar**

**OU**

1. No SonarCloud, vá em **My Projects**
2. Clique nos **3 pontinhos** ao lado do projeto `cliente-core`
3. Escolha **Delete**
4. Confirme a exclusão
5. Siga os passos em `SONARCLOUD_QUICKSTART.md` para recriar

---

## Solução Alternativa: Criar Novo Projeto com Repositório Correto

### 1. Delete o Projeto Atual (se o binding estiver errado)

1. SonarCloud → **My Projects**
2. Procure `cliente-core`
3. Clique nos **3 pontinhos** → **Delete**
4. Confirme

### 2. Importe o Repositório Correto

1. No SonarCloud, clique no **+** → **Analyze new project**
2. **IMPORTANTE**: Procure pelo repositório correto:
   - Se o GitHub é `diegorighi/cliente-core` → selecione esse
   - Se o GitHub é `va-nessa-mudanca/cliente-core` → selecione esse
3. Marque a checkbox
4. Clique em **Set Up**

### 3. Configure o Project Key

O SonarCloud vai sugerir automaticamente:
- Se repo é `diegorighi/cliente-core` → key será `diegorighi_cliente-core`
- Se repo é `va-nessa-mudanca/cliente-core` → key será `va-nessa-mudanca_cliente-core`

**Aceite o padrão sugerido!**

### 4. Atualize o Workflow

Depois de criar o projeto, atualize `.github/workflows/code-quality.yml`:

```yaml
env:
  SONAR_ORGANIZATION: [valor correto]       # Ex: diegorighi ou va-nessa-mudanca
  SONAR_PROJECT_KEY: [valor correto]        # Ex: diegorighi_cliente-core
```

---

## Qual é o Repositório Real no GitHub?

Execute este comando para verificar:

```bash
cd /Users/diegorighi/Desenvolvimento/va-nessa-mudanca/cliente-core
git remote -v
```

**Exemplo de output**:
```
origin  https://github.com/diegorighi/cliente-core.git (fetch)
origin  https://github.com/diegorighi/cliente-core.git (push)
```

**Neste exemplo**, o repositório é `diegorighi/cliente-core`, então:
- **Organization**: `diegorighi`
- **Project Key**: `diegorighi_cliente-core`

---

## Verificar Default Branch no GitHub

1. Vá no repositório: https://github.com/[seu-username]/cliente-core
2. Clique em **Settings** (aba no topo)
3. No menu lateral, procure **Branches**
4. Veja qual é a **Default branch** (geralmente `main` ou `developer`)

**O SonarCloud espera encontrar commits nessa branch!**

Se a default branch for `developer`, certifique-se de ter feito push para ela:

```bash
git push origin developer
```

---

## Teste Rápido (Depois de Corrigir)

```bash
# Fazer commit para testar
git add .github/workflows/code-quality.yml
git commit -m "ci: Fix SonarCloud organization and project key"
git push origin developer  # ou main, dependendo da default branch
```

Depois:
1. GitHub → **Actions**
2. Ver workflow **Code Quality and Security**
3. Job **SonarCloud Analysis** deve passar ✅

---

## Resumo dos Valores Corretos

Baseado no repositório real do GitHub, use:

| Repositório GitHub | Organization | Project Key |
|-------------------|--------------|-------------|
| `diegorighi/cliente-core` | `diegorighi` | `diegorighi_cliente-core` |
| `va-nessa-mudanca/cliente-core` | `va-nessa-mudanca` | `va-nessa-mudanca_cliente-core` |

**IMPORTANTE**: Os valores no workflow **DEVEM** bater com os do projeto no SonarCloud!

---

## Comandos de Verificação

```bash
# Ver qual repositório remoto está configurado
git remote -v

# Ver qual é a branch atual
git branch --show-current

# Ver quais branches existem remotamente
git branch -r

# Ver o último commit
git log -1
```

---

**Última atualização**: 2025-11-04
