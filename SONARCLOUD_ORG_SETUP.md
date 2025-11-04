# SonarCloud - Criar Organização e Projeto

## Situação Atual

Você está criando a organização `va-nessa-mudanca` no SonarCloud **antes** de migrar o repositório GitHub.

Esta é uma abordagem válida! O SonarCloud permitirá vincular repositórios externos.

---

## Passo 1: Criar Organização no SonarCloud

1. Acesse: https://sonarcloud.io/
2. Login with GitHub
3. Clique no **+** (canto superior direito)
4. Escolha **Create new organization**

### Preencha os Campos:

**Name** (Nome de Exibição):
```
Va Nessa Mudança
```
ou
```
VaNessaMudanca
```
*Este é o nome amigável que aparecerá na interface*

**Key** (Chave Técnica) ⚠️ **IMPORTANTE**:
```
va-nessa-mudanca
```
*Esta key será usada nas URLs e configurações - deve ser exatamente isso!*

**Billing Plan**:
- Escolha **Free Plan** (suficiente para projetos públicos)
- Ou o plano adequado para sua situação

5. Clique em **Create Organization**

---

## Passo 2: Vincular Repositório Externo

Agora você precisa vincular o repositório `diegorighi/cliente-core` à organização `va-nessa-mudanca`.

### Opção A: Importar Repositório Manualmente

1. Na organização `va-nessa-mudanca`, clique em **+** → **Analyze new project**
2. Você verá: "No repositories available"
3. Clique em **Import a new repository** ou **Add an external repository**
4. Escolha **GitHub**
5. Procure por: `cliente-core`
6. Selecione: `diegorighi/cliente-core`
7. Clique em **Set Up**

### Opção B: Conceder Acesso à Organização SonarCloud

Se a Opção A não funcionar:

1. No SonarCloud, vá em **Administration** → **Organization settings**
2. Vá em **GitHub** → **Configure GitHub App**
3. Será redirecionado para GitHub
4. Em **Repository access**, selecione:
   - **Only select repositories** → Escolha `diegorighi/cliente-core`
   - OU **All repositories**
5. Clique em **Save**
6. Volte no SonarCloud e tente importar novamente

---

## Passo 3: Configurar o Projeto

Ao importar `diegorighi/cliente-core`:

**Organization** (já selecionada):
```
va-nessa-mudanca
```

**Project Key** (gerado automaticamente):
```
va-nessa-mudanca_cliente-core
```

✅ **Aceite esses valores!** Já estão configurados no workflow.

Clique em **Set Up**.

---

## Passo 4: Obter SONAR_TOKEN

1. Escolha **With GitHub Actions**
2. Clique em **Continue**
3. Clique em **Generate Token** (ou use um existente)
4. **COPIE O TOKEN** (você só verá uma vez!)

---

## Passo 5: Configurar Secret no GitHub

1. Vá em: https://github.com/diegorighi/cliente-core
2. **Settings** → **Secrets and variables** → **Actions**
3. **New repository secret** (ou Update se já existe):
   - **Name**: `SONAR_TOKEN`
   - **Secret**: cole o token copiado
4. Clique em **Add secret** (ou **Update secret**)

---

## Passo 6: Testar

```bash
git add .github/workflows/code-quality.yml
git commit -m "ci: Configure SonarCloud with va-nessa-mudanca organization"
git push origin developer
```

Depois:
1. GitHub → **Actions**
2. Ver workflow **Code Quality and Security**
3. Job **SonarCloud Analysis** deve passar ✅

---

## Verificação

Após o workflow rodar:

1. SonarCloud → https://sonarcloud.io/
2. **My Organizations** → `va-nessa-mudanca`
3. **Projects** → Você verá `cliente-core`
4. Dashboard: https://sonarcloud.io/dashboard?id=va-nessa-mudanca_cliente-core

---

## Valores Configurados

| Campo | Valor |
|-------|-------|
| **SonarCloud Org Name** | `Va Nessa Mudança` (display name) |
| **SonarCloud Org Key** | `va-nessa-mudanca` (technical key) |
| **Project Key** | `va-nessa-mudanca_cliente-core` |
| **GitHub Repository** | `diegorighi/cliente-core` (atual) |
| **Future GitHub Repo** | `va-nessa-mudanca/cliente-core` (após transfer) |

---

## Quando Transferir o Repositório GitHub

No futuro, quando transferir de `diegorighi/cliente-core` → `va-nessa-mudanca/cliente-core`:

✅ **Não precisa fazer nada no SonarCloud!**

O binding continuará funcionando automaticamente porque:
1. A organização já é `va-nessa-mudanca`
2. O project key já é `va-nessa-mudanca_cliente-core`
3. O SonarCloud detectará automaticamente a transferência do repo

Você só precisará:
```bash
# Atualizar remote local
git remote set-url origin git@github.com:va-nessa-mudanca/cliente-core.git
```

---

## Troubleshooting

### Erro: "Organization key already exists"

**Causa**: Alguém já criou essa organização

**Solução**:
- Você é membro dessa org? Entre nela
- Não é membro? Use outra key: `va-nessa-mudanca-2` ou `vanessa-mudanca`

### Erro: "Cannot import repository"

**Causa**: SonarCloud não tem acesso ao repo

**Solução**:
1. GitHub → Settings → Applications → SonarCloud
2. Configure repository access
3. Grant access a `diegorighi/cliente-core`

### Erro: "Invalid SONAR_TOKEN"

**Causa**: Token não configurado ou expirado

**Solução**:
1. SonarCloud → My Account → Security → Generate new token
2. GitHub → Settings → Secrets → Update `SONAR_TOKEN`

---

## Diagrama de Setup

```
GitHub (agora):           diegorighi/cliente-core
                                    ↓
SonarCloud Org:           va-nessa-mudanca ✅
SonarCloud Project:       va-nessa-mudanca_cliente-core ✅
                                    ↓
GitHub (futuro):          va-nessa-mudanca/cliente-core
                          ↑
                    (transfer automático)
```

---

**Última atualização**: 2025-11-04
**Status**: Pronto para criar organização
