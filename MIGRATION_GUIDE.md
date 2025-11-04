# Migration Guide - diegorighi → va-nessa-mudanca

## Status Atual vs Futuro

**ATUAL (Temporário)**:
- Repositório: `diegorighi/cliente-core` (pessoal)
- SonarCloud Org: `diegorighi`
- SonarCloud Key: `diegorighi_cliente-core`

**FUTURO (Após Migração)**:
- Repositório: `va-nessa-mudanca/cliente-core` (empresa)
- SonarCloud Org: `va-nessa-mudanca`
- SonarCloud Key: `va-nessa-mudanca_cliente-core`

---

## Quando Fazer a Migração?

Execute esta migração quando:
1. A organização `va-nessa-mudanca` for criada no GitHub
2. O repositório for transferido de `diegorighi` para `va-nessa-mudanca`

---

## Checklist de Migração (Quando Chegar o Momento)

### Fase 1: GitHub Repository Transfer

- [ ] **GitHub**: Criar organização `va-nessa-mudanca` (se ainda não existe)
- [ ] **GitHub**: Settings → Danger Zone → Transfer ownership
  - Transfer to: `va-nessa-mudanca`
  - New name: `cliente-core` (manter o mesmo)
- [ ] **GitHub**: Confirmar transferência
- [ ] **Local**: Atualizar remote URL:
  ```bash
  git remote set-url origin git@github.com:va-nessa-mudanca/cliente-core.git
  git remote -v  # Verificar
  ```

### Fase 2: SonarCloud

- [ ] **SonarCloud**: Deletar projeto antigo `diegorighi_cliente-core`
  - https://sonarcloud.io/ → My Projects
  - 3 pontinhos → Delete
- [ ] **SonarCloud**: Criar novo projeto
  - + → Analyze new project
  - Selecionar: `va-nessa-mudanca/cliente-core`
  - Organization: `va-nessa-mudanca`
  - Project Key: `va-nessa-mudanca_cliente-core`
- [ ] **SonarCloud**: Copiar novo SONAR_TOKEN
- [ ] **GitHub**: Atualizar secret `SONAR_TOKEN`
  - Settings → Secrets → Actions → Update `SONAR_TOKEN`

### Fase 3: GitHub Actions Workflow

- [ ] **Workflow**: Atualizar `.github/workflows/code-quality.yml`:
  ```yaml
  env:
    SONAR_ORGANIZATION: va-nessa-mudanca
    SONAR_PROJECT_KEY: va-nessa-mudanca_cliente-core
  ```
- [ ] **Commit**: Commitar as mudanças
  ```bash
  git add .github/workflows/code-quality.yml
  git commit -m "ci: Update SonarCloud for va-nessa-mudanca organization"
  git push origin developer
  ```

### Fase 4: Outros Workflows (se aplicável)

- [ ] Atualizar outros workflows que referenciam o repositório
- [ ] Atualizar badges no README.md
- [ ] Atualizar links de documentação

### Fase 5: Verificação

- [ ] **GitHub Actions**: Verificar que workflow passa
- [ ] **SonarCloud**: Verificar dashboard
  - https://sonarcloud.io/dashboard?id=va-nessa-mudanca_cliente-core
- [ ] **README**: Atualizar badges (se existirem)

---

## Atualização de Badges (Após Migração)

### Antes (diegorighi):
```markdown
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=diegorighi_cliente-core&metric=alert_status)](https://sonarcloud.io/dashboard?id=diegorighi_cliente-core)
```

### Depois (va-nessa-mudanca):
```markdown
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=va-nessa-mudanca_cliente-core&metric=alert_status)](https://sonarcloud.io/dashboard?id=va-nessa-mudanca_cliente-core)
```

---

## Script de Migração Automática

Quando chegar o momento, você pode usar este script:

```bash
#!/bin/bash
# migration-script.sh - Execute APÓS transferir o repositório no GitHub

echo "🔄 Migrando cliente-core para va-nessa-mudanca..."

# 1. Atualizar remote URL
echo "📍 Atualizando remote URL..."
git remote set-url origin git@github.com:va-nessa-mudanca/cliente-core.git
git remote -v

# 2. Atualizar workflow
echo "📝 Atualizando workflow..."
sed -i '' 's/SONAR_ORGANIZATION: diegorighi/SONAR_ORGANIZATION: va-nessa-mudanca/' .github/workflows/code-quality.yml
sed -i '' 's/SONAR_PROJECT_KEY: diegorighi_cliente-core/SONAR_PROJECT_KEY: va-nessa-mudanca_cliente-core/' .github/workflows/code-quality.yml

# 3. Commit e push
echo "✅ Commitando mudanças..."
git add .github/workflows/code-quality.yml
git commit -m "ci: Migrate to va-nessa-mudanca organization"
git push origin developer

echo "✅ Migração local concluída!"
echo ""
echo "⚠️  PRÓXIMOS PASSOS MANUAIS:"
echo "1. Deletar projeto diegorighi_cliente-core no SonarCloud"
echo "2. Criar novo projeto va-nessa-mudanca_cliente-core no SonarCloud"
echo "3. Atualizar SONAR_TOKEN no GitHub Secrets"
echo "4. Verificar que Actions está passando"
```

Salvar como `migration-script.sh` e executar:
```bash
chmod +x migration-script.sh
./migration-script.sh
```

---

## Por Agora (Fase Atual)

**O que fazer AGORA**:
1. Use `diegorighi` como organização
2. Siga o guia `SONARCLOUD_RECREATE.md` com os valores:
   - Organization: `diegorighi`
   - Project Key: `diegorighi_cliente-core`
3. Configure tudo para funcionar corretamente

**O que fazer DEPOIS (quando migrar)**:
1. Siga este guia `MIGRATION_GUIDE.md`
2. Transfira o repositório no GitHub
3. Recrie o projeto no SonarCloud
4. Atualize o workflow

---

## Arquivos que Mudarão na Migração

| Arquivo | Campo | De | Para |
|---------|-------|----|----- |
| `.github/workflows/code-quality.yml` | `SONAR_ORGANIZATION` | `diegorighi` | `va-nessa-mudanca` |
| `.github/workflows/code-quality.yml` | `SONAR_PROJECT_KEY` | `diegorighi_cliente-core` | `va-nessa-mudanca_cliente-core` |
| `.git/config` | `remote.origin.url` | `diegorighi/cliente-core` | `va-nessa-mudanca/cliente-core` |
| `README.md` | Badges (se existirem) | `diegorighi_cliente-core` | `va-nessa-mudanca_cliente-core` |

---

## Tempo Estimado de Migração

- **Transferência do repositório**: 2 minutos
- **Atualizar remote local**: 1 minuto
- **Recriar projeto SonarCloud**: 5 minutos
- **Atualizar workflow**: 2 minutos
- **Testar**: 5 minutos

**Total**: ~15 minutos

---

## Links Úteis

- **GitHub Transfer Docs**: https://docs.github.com/en/repositories/creating-and-managing-repositories/transferring-a-repository
- **SonarCloud Docs**: https://docs.sonarcloud.io/
- **Este Guia**: `MIGRATION_GUIDE.md`

---

**Última atualização**: 2025-11-04
**Status**: Preparado para migração futura
