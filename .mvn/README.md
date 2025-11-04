# Maven Configuration - Cliente-Core

## ⚠️ IMPORTANTE

**Este microserviço HERDA a configuração Maven da RAIZ do monorepo.**

```
va-nessa-mudanca/.mvn/maven.config  ← Configuração HERDADA
cliente-core/.mvn/                  ← Apenas wrapper + docs
```

**NÃO há `maven.config` neste diretório!** A configuração vem de `../../.mvn/maven.config`.

---

## Propósito deste Diretório

Este `.mvn/` contém:
- ✅ **Maven Wrapper** (`./mvnw`) - Build sem Maven instalado
- ✅ **README.md** (este arquivo) - Documentação específica do MS
- ❌ **maven.config** - AUSENTE (herda da raiz)

## Problema Resolvido (Nível Monorepo)

**Situação:** Maven global configurado para usar Nexus Porto Seguro (corporativo).
**Necessidade:** Va Nessa Mudança deve usar Maven Central (público).
**Solução:** `.mvn/maven.config` na RAIZ força Maven Central para TODOS os MS.

---

## Como Funciona a Herança

### Maven 3.3.1+ - Busca Hierárquica

Quando você roda Maven em `cliente-core/`:

```bash
cd va-nessa-mudanca/cliente-core
mvn clean install
```

Maven busca `.mvn/maven.config`:
1. `cliente-core/.mvn/maven.config` ❌ Não existe
2. `va-nessa-mudanca/.mvn/maven.config` ✅ ENCONTRADO!

**Resultado:** Usa `settings_old.xml` (Maven Central) automaticamente.

### Arquivo Herdado (na raiz)

**Localização:** `../../.mvn/maven.config`

**Conteúdo:**
```
-s /Users/diegorighi/Desenvolvimento/Infra/apache-maven-3.9.11/conf/settings_old.xml
```

**Efeito para cliente-core:**
- ✅ Usa Maven Central (repo.maven.apache.org)
- ✅ Ignora Nexus Porto Seguro
- ✅ Mesma config que TODOS os MS do monorepo

---

## Estrutura de Settings Maven

**Localização Global:** `/Users/diegorighi/Desenvolvimento/Infra/apache-maven-3.9.11/conf/`

```
apache-maven-3.9.11/conf/
├── settings.xml              # PORTO SEGURO (global default)
├── settings_old.xml          # MAVEN CENTRAL (usado por .mvn/maven.config)
└── settings_central.xml      # Backup Maven Central
```

### Por Que Não Trocar o Global?

**Problema:** Você trabalha em projetos Porto Seguro E Va Nessa Mudança.

**Solução:**
- `settings.xml` global → Porto Seguro (seus projetos corporativos)
- `.mvn/maven.config` → Maven Central (apenas Va Nessa Mudança)

---

## Testando a Configuração

### Verificar Settings Ativo

```bash
cd /Users/diegorighi/Desenvolvimento/va-nessa-mudanca/cliente-core
mvn help:effective-settings | grep -A 5 "<mirrors>"
```

**Saída Esperada:**
```xml
<mirror>
  <id>central-https</id>
  <mirrorOf>central</mirrorOf>
  <name>Maven Central HTTPS</name>
  <url>https://repo.maven.apache.org/maven2</url>
</mirror>
```

**Saída INDESEJADA (se .mvn/maven.config não funcionar):**
```xml
<mirror>
  <id>porto-repo</id>
  <url>https://nexusportoprd.portoseguro.brasil/...</url>
</mirror>
```

### Testar Download

```bash
mvn dependency:tree 2>&1 | grep "Downloading from"
```

**Esperado:**
```
Downloading from central-https: https://repo.maven.apache.org/maven2/...
```

**Indesejado:**
```
Downloading from porto-repo: https://nexusportoprd.portoseguro.brasil/...
```

---

## Alternativa: Script Wrapper (Backup)

Se `.mvn/maven.config` não funcionar (Maven < 3.3.1), use o script:

```bash
./mvn-vanessa.sh clean install
./mvn-vanessa.sh test
./mvn-vanessa.sh spring-boot:run
```

O script `mvn-vanessa.sh` força uso de `-s settings_old.xml` explicitamente.

---

## Troubleshooting

### Problema: Maven ainda usa porto-repo

**Causa 1:** Cache do Maven tem artefatos marcados com `porto-repo`

**Solução:**
```bash
mvn clean install -U  # Força re-download
```

**Causa 2:** `.mvn/maven.config` não está sendo lido

**Solução:**
```bash
# Verificar versão do Maven (precisa >= 3.3.1)
mvn --version

# Usar script wrapper como fallback
./mvn-vanessa.sh clean install
```

### Problema: Plugin não encontrado

**Causa:** Settings errado (Porto Seguro bloqueando download)

**Solução:**
```bash
# Testar manualmente com settings correto
mvn -s /Users/.../conf/settings_old.xml clean install
```

---

## Outros Microserviços

Quando criar novos microserviços em **Va Nessa Mudança**:

```bash
cd va-nessa-mudanca/novo-microservico
mkdir .mvn
cat > .mvn/maven.config << 'EOF'
-s /Users/diegorighi/Desenvolvimento/Infra/apache-maven-3.9.11/conf/settings_old.xml
EOF
```

**Todos os microserviços** do monorepo devem usar Maven Central!

---

## Referências

- [Maven Config File Documentation](https://maven.apache.org/configure.html)
- [Maven Settings Reference](https://maven.apache.org/settings.html)
- `../docs/MAVEN_SETTINGS.md` - Documentação completa

---

**Criado:** 2025-11-03
**Autor:** Tech Lead Va Nessa Mudança
**Versão:** 1.0
