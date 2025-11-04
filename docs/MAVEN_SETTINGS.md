# Maven Settings Configuration

## Overview

Este projeto utiliza **Maven Central** como repositório padrão, evitando dependências de repositórios corporativos específicos.

## Configuração Atual

**Local:** `/Users/diegorighi/Desenvolvimento/Infra/apache-maven-3.9.11/conf/settings.xml`

**Repositórios configurados:**
- `central-https`: https://repo.maven.apache.org/maven2 (releases)
- `central-snapshots`: https://central.sonatype.com/repository/maven-snapshots (snapshots)

## Arquivos Disponíveis

```
apache-maven-3.9.11/conf/
├── settings.xml                    # ✅ ATIVO - Maven Central (padrão)
├── settings_old.xml                # Backup Maven Central
└── settings_porto_seguro.xml       # Porto Seguro Nexus (não usar neste projeto)
```

## Como Trocar entre Configurações

### Usar Maven Central (Recomendado para Va Nessa Mudança)
```bash
cd /Users/diegorighi/Desenvolvimento/Infra/apache-maven-3.9.11/conf
cp settings_old.xml settings.xml
```

### Usar Porto Seguro Nexus (Projetos corporativos)
```bash
cd /Users/diegorighi/Desenvolvimento/Infra/apache-maven-3.9.11/conf
cp settings_porto_seguro.xml settings.xml
```

## Verificar Configuração Ativa

```bash
mvn help:effective-settings | grep -A 5 "<mirrors>"
```

Ou simplesmente rodar qualquer comando Maven e verificar os logs:
```bash
mvn dependency:tree 2>&1 | grep "Downloading from"
```

**Saída esperada (Maven Central):**
```
Downloading from central-https: https://repo.maven.apache.org/maven2/...
```

**Saída indesejada (Porto Seguro):**
```
Downloading from porto-repo: https://nexusportoprd.portoseguro.brasil/...
```

## Troubleshooting

### Problema: Maven ainda usa repositório errado
**Causa:** Cache local do Maven tem artefatos marcados com `porto-repo`

**Solução:**
```bash
# Opção 1: Limpar cache completo (cuidado - redownload tudo)
rm -rf ~/.m2/repository

# Opção 2: Limpar apenas dependências específicas
rm -rf ~/.m2/repository/org/springframework
rm -rf ~/.m2/repository/com/fasterxml

# Opção 3: Forçar re-download no build
mvn clean install -U
```

### Problema: "No plugin found for prefix 'spring-boot'"
**Causa:** settings.xml da Porto Seguro está ativo

**Solução:**
```bash
cd /Users/diegorighi/Desenvolvimento/Infra/apache-maven-3.9.11/conf
cp settings_old.xml settings.xml
mvn clean install
```

## Política do Projeto

**Va Nessa Mudança NÃO deve usar repositórios corporativos Porto Seguro.**

- ✅ Usar: Maven Central
- ❌ Evitar: Nexus Porto Seguro
- 📦 Todos os artefatos devem estar disponíveis em repositórios públicos

---

**Última atualização:** 2025-11-03  
**Responsável:** Tech Lead Va Nessa Mudança
