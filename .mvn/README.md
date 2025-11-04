# Maven Configuration Override

## Propósito

Este diretório `.mvn/` contém configurações específicas do projeto **Va Nessa Mudança** que sobrescrevem as configurações globais do Maven.

## Problema Resolvido

**Situação:** Maven global configurado para usar Nexus Porto Seguro (corporativo).
**Necessidade:** Va Nessa Mudança deve usar Maven Central (público).
**Solução:** Maven config file força uso de settings alternativo.

---

## Arquivo: maven.config

**Conteúdo:**
```
-s /Users/diegorighi/Desenvolvimento/Infra/apache-maven-3.9.11/conf/settings_old.xml
```

**O que faz:**
- Força Maven a usar `settings_old.xml` (Maven Central)
- Ignora `settings.xml` global (Porto Seguro)
- Aplicado automaticamente a TODOS os comandos Maven neste projeto

---

## Como Funciona

### Maven 3.3.1+ (.mvn/maven.config)

Quando você roda **qualquer comando Maven** dentro de `cliente-core/`:

```bash
mvn clean install
mvn test
mvn spring-boot:run
```

O Maven automaticamente:
1. Detecta o diretório `.mvn/`
2. Lê o arquivo `maven.config`
3. Aplica `-s /path/to/settings_old.xml`
4. Usa Maven Central ao invés de Porto Seguro

**Equivalente manual:**
```bash
mvn -s /Users/.../settings_old.xml clean install
```

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
