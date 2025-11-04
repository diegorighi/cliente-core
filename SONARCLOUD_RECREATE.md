# SonarCloud - Recriar Projeto com Repositório Correto

## ⚠️ NOTA: Esta é uma Configuração Temporária

Este projeto **será migrado** no futuro para `va-nessa-mudanca/cliente-core` (organização da empresa).

**POR AGORA**: Vamos configurar com `diegorighi/cliente-core` (repositório atual).

**NO FUTURO**: Quando migrar, siga o guia `MIGRATION_GUIDE.md` (já preparado).

---

## Situação Atual

- **Repositório GitHub (atual)**: `diegorighi/cliente-core` ✅
- **Repositório GitHub (futuro)**: `va-nessa-mudanca/cliente-core` (após migração)
- **Projeto SonarCloud**: `va-nessa-mudanca/cliente-core` ❌ (ERRADO - foi criado antecipadamente)

**Problema**: O projeto no SonarCloud está vinculado a um repositório futuro que **ainda não existe** (`va-nessa-mudanca/cliente-core`), mas o repositório **atual** é `diegorighi/cliente-core`.

**Solução**: Recriar o projeto no SonarCloud vinculando ao repositório **atual** (`diegorighi/cliente-core`). Quando fizer a migração, basta seguir o `MIGRATION_GUIDE.md`.

---

## Passo a Passo (10 minutos)

### 1. Deletar o Projeto Antigo no SonarCloud

1. Acesse: https://sonarcloud.io/
2. Login with GitHub
3. Vá em **My Projects**
4. Procure pelo projeto `cliente-core` (ou `va-nessa-mudanca_cliente-core`)
5. Clique nos **3 pontinhos** (⋮) ao lado do projeto
6. Escolha **Delete**
7. Confirme digitando o nome do projeto
8. Clique em **Delete**

✅ Projeto antigo deletado!

---

### 2. Criar Novo Projeto Vinculado ao Repositório Correto

1. No SonarCloud, clique no **+** (canto superior direito)
2. Escolha **Analyze new project**
3. Você verá uma lista de repositórios do GitHub
4. **Procure por**: `cliente-core`
5. **IMPORTANTE**: Selecione **`diegorighi/cliente-core`** ✅ (NÃO `va-nessa-mudanca/cliente-core`)
6. Marque a checkbox ao lado de `diegorighi/cliente-core`
7. Clique em **Set Up** (botão azul no canto superior direito)

---

### 3. Verificar os Valores Sugeridos

O SonarCloud vai sugerir automaticamente:

**Organization**:
```
diegorighi
```

**Project Key**:
```
diegorighi_cliente-core
```

✅ **Aceite esses valores!** Eles já estão configurados no workflow.

Clique em **Set Up** para continuar.

---

### 4. Escolher Método de Análise

1. Escolha **With GitHub Actions**
2. SonarCloud vai mostrar:
   - Instruções (pode ignorar, já fizemos isso!)
   - **Token** para copiar

3. Clique em **Continue**
4. Clique em **Generate token** (ou use um token existente)
5. **COPIE O TOKEN** (você só verá uma vez!)

---

### 5. Configurar o Secret no GitHub

1. Vá no repositório: https://github.com/diegorighi/cliente-core
2. Clique em **Settings** (aba no topo)
3. No menu lateral esquerdo: **Secrets and variables** → **Actions**
4. **Se já existe `SONAR_TOKEN`**:
   - Clique em **Update** no token existente
   - Cole o novo token
   - Clique em **Update secret**

5. **Se não existe**:
   - Clique em **New repository secret**
   - **Name**: `SONAR_TOKEN`
   - **Secret**: cole o token copiado
   - Clique em **Add secret**

---

### 6. Verificar o Workflow

O workflow **já está configurado** com os valores corretos:

```yaml
env:
  SONAR_ORGANIZATION: diegorighi           # ✅ Correto
  SONAR_PROJECT_KEY: diegorighi_cliente-core  # ✅ Correto
```

**Não precisa mudar nada!** 🎉

---

### 7. Testar a Configuração

Faça um commit para testar:

```bash
git add .
git commit -m "ci: Recreate SonarCloud project with correct repository"
git push origin developer
```

Depois:

1. Vá em **Actions**: https://github.com/diegorighi/cliente-core/actions
2. Veja o workflow **Code Quality and Security** rodando
3. Clique no workflow para ver os detalhes
4. O job **SonarCloud Analysis** deve passar ✅

---

### 8. Verificar no SonarCloud

Depois que o workflow rodar com sucesso:

1. Volte no SonarCloud: https://sonarcloud.io/
2. Vá em **My Projects**
3. Você verá o projeto **cliente-core** com:
   - **Organization**: `diegorighi` ✅
   - **Repository**: `diegorighi/cliente-core` ✅
   - **Quality Gate**: Status da análise
   - **Coverage**: ~85% ✅

**URL do Dashboard**:
```
https://sonarcloud.io/dashboard?id=diegorighi_cliente-core
```

---

## Checklist

- [ ] **Passo 1**: Deletar projeto antigo `va-nessa-mudanca/cliente-core` no SonarCloud
- [ ] **Passo 2**: Criar novo projeto vinculando `diegorighi/cliente-core`
- [ ] **Passo 3**: Verificar que organization=`diegorighi` e key=`diegorighi_cliente-core`
- [ ] **Passo 4**: Copiar SONAR_TOKEN
- [ ] **Passo 5**: Configurar/atualizar secret `SONAR_TOKEN` no GitHub
- [ ] **Passo 6**: Verificar que workflow está com valores corretos ✅ (já está!)
- [ ] **Passo 7**: Fazer commit e push para testar
- [ ] **Passo 8**: Verificar que workflow passou e projeto aparece no SonarCloud

---

## Troubleshooting

### Não vejo o repositório `diegorighi/cliente-core` na lista

**Causa**: SonarCloud não tem permissão para acessar o repositório

**Solução**:
1. No SonarCloud, vá em **My Account** → **Security** → **GitHub**
2. Clique em **Configure** ao lado de GitHub
3. Será redirecionado para GitHub
4. Em **Organization access**, procure por `diegorighi`
5. Clique em **Grant** se necessário
6. Volte no SonarCloud e tente novamente

---

### Workflow continua falhando

**Verifique**:
1. `SONAR_TOKEN` está configurado no GitHub? (Settings → Secrets)
2. Token foi copiado corretamente? (sem espaços extras)
3. Esperou 1-2 minutos após configurar o secret?

**Solução**: Tente gerar novo token no SonarCloud e atualizar o secret

---

### Projeto aparece duplicado

Se você vir dois projetos (`va-nessa-mudanca_cliente-core` e `diegorighi_cliente-core`):

1. **Delete o antigo** `va-nessa-mudanca_cliente-core`
2. **Mantenha apenas** `diegorighi_cliente-core`

---

## Valores Finais Corretos

| Propriedade | Valor |
|-------------|-------|
| **GitHub Repository** | `diegorighi/cliente-core` |
| **SonarCloud Organization** | `diegorighi` |
| **SonarCloud Project Key** | `diegorighi_cliente-core` |
| **SonarCloud Dashboard** | https://sonarcloud.io/dashboard?id=diegorighi_cliente-core |

---

**Tempo estimado**: 10 minutos
**Última atualização**: 2025-11-04
