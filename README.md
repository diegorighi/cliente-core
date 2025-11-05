# 🧑 cliente-core

## Descrição
Microserviço responsável pelo gerenciamento completo de clientes (Pessoa Física e Jurídica) da plataforma Va Nessa Mudança.

Este serviço é o núcleo do cadastro de clientes, gerenciando informações pessoais, documentos, contatos e endereços tanto para pessoas físicas quanto jurídicas.

## 🚀 Quick Start

```bash
./setup-local.sh
```

Este comando único configura todo o ambiente local (PostgreSQL + DynamoDB + aplicação + testes).

**Tempo:** ~2 minutos | **Documentação completa:** [Como Subir Local Stack](docs/setup/COMO_SUBIR_LOCAL_STACK.md)

## 📚 Documentação

### Setup & Desenvolvimento
- **[Como Subir Local Stack](docs/setup/COMO_SUBIR_LOCAL_STACK.md)** - Guia completo de setup (COMECE AQUI)
- **[Resumo do Setup](docs/setup/SETUP_LOCAL_SUMMARY.md)** - O que foi implementado
- **[Desenvolvimento Local](docs/setup/LOCAL_DEVELOPMENT.md)** - Workflows diários

### Arquitetura & Features
- **[Virtual Threads](docs/development/VIRTUAL_THREADS.md)** - Java 21 concurrency
- **[DynamoDB Cache](docs/cache/DYNAMODB_CACHE_SUMMARY.md)** - Estratégia de cache
- **[Liquibase Structure](docs/LIQUIBASE_STRUCTURE.md)** - Schema management
- **[Integration Architecture](docs/INTEGRATION_ARCHITECTURE.md)** - Kafka + Step Functions

### Quality & Testing
- **[CI/CD Strategy](docs/CI-CD-STRATEGY.md)** - Pipeline & workflows
- **[QA Test Plans](docs/qa/)** - Planos de teste detalhados
- **[SonarQube Setup](docs/SONARQUBE-SETUP.md)** - Code quality

### Deploy & Infrastructure
- **[Terraform Modules](terraform/)** - AWS infrastructure as code
- **[Free Tier Guide](terraform/FREE_TIER_GUIDE.md)** - Deploy gratuito AWS

---

## 🏗️ Tecnologias

- **Java:** 21
- **Spring Boot:** 3.5.7
- **PostgreSQL:** (latest)
- **JPA/Hibernate:** ORM para persistência
- **Lombok:** Redução de boilerplate
- **Spring DevTools:** Hot reload em desenvolvimento

---

## ⚙️ Configuração

### Porta
- **Desenvolvimento:** 8081
- **Base URL:** `/api/clientes`

### application.yml (Principal)
```yaml
spring:
  application:
    name: cliente-core
  profiles:
    active: dev

server:
  port: 8081
  servlet:
    context-path: /api/clientes

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### Perfis de Ambiente (Profiles)

O cliente-core utiliza múltiplos perfis Spring para diferentes cenários:

| Perfil | Banco de Dados | Uso | Comando |
|--------|----------------|-----|---------|
| **local** | H2 in-memory | Desenvolvimento rápido sem Docker | `mvn spring-boot:run -Dspring-boot.run.profiles=local` |
| **dev** | PostgreSQL (localhost:5432) | Desenvolvimento com dados persistentes | `mvn spring-boot:run` (padrão) |
| **test** | H2 in-memory | Testes unitários rápidos | `mvn test` (automático) |
| **integration** | PostgreSQL TestContainers | Testes E2E realistas | Usado por `AbstractIntegrationTest` |
| **prod** | PostgreSQL RDS (AWS) | Produção | Configurado via variáveis de ambiente |

#### application-local.yml (H2 - Sem Docker)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:clientedb;MODE=PostgreSQL
    driver-class-name: org.h2.Driver

  h2:
    console:
      enabled: true
      path: /h2-console  # Acesse: http://localhost:8081/api/clientes/h2-console

  liquibase:
    drop-first: true  # Recria schema a cada startup
```

**Vantagens:**
- ✅ Startup em 5-10 segundos (vs 30s com PostgreSQL)
- ✅ Não requer Docker/PostgreSQL instalado
- ✅ Console H2 para inspeção de dados
- ✅ Ideal para desenvolvimento rápido de features

#### application-dev.yml (PostgreSQL Local)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vanessa_mudanca_clientes
    username: user
    password: senha123
    hikari:
      maximum-pool-size: 50  # Otimizado para Virtual Threads

  jpa:
    show-sql: true

  liquibase:
    contexts: dev  # Inclui seeds de teste
```

**Vantagens:**
- ✅ Ambiente idêntico à produção
- ✅ Dados persistentes entre restarts
- ✅ Testa migrações Liquibase reais

#### application-test.yml (H2 - Testes Unitários)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL

  liquibase:
    contexts: test
    drop-first: true

  jpa:
    show-sql: false
```

**Vantagens:**
- ✅ Testes 5-10x mais rápidos
- ✅ Isolamento total entre testes
- ✅ Sem dependências externas

#### application-integration.yml (PostgreSQL TestContainers)
```yaml
spring:
  liquibase:
    contexts: ddl-only  # Sem seeds, apenas estrutura
```

**Vantagens:**
- ✅ Testes E2E em ambiente real PostgreSQL
- ✅ Container compartilhado (singleton pattern)
- ✅ Valida compatibilidade com produção

#### 🚀 Guia Rápido: Qual Perfil Usar?

**Cenário 1: Desenvolvimento rápido de uma feature nova**
```bash
# Use perfil 'local' com H2 (startup rápido, sem Docker)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Acesse H2 Console: http://localhost:8081/api/clientes/h2-console
# JDBC URL: jdbc:h2:mem:clientedb
# User: sa
# Password: (deixe em branco)
```

**Cenário 2: Testar migrações Liquibase ou dados persistentes**
```bash
# Use perfil 'dev' com PostgreSQL local
docker run --name postgres-dev -e POSTGRES_DB=vanessa_mudanca_clientes \
  -e POSTGRES_USER=user -e POSTGRES_PASSWORD=senha123 \
  -p 5432:5432 -d postgres:16-alpine

mvn spring-boot:run  # Usa perfil 'dev' por padrão
```

**Cenário 3: Rodar testes unitários rapidamente**
```bash
# Perfil 'test' com H2 é usado automaticamente
mvn test

# Ou rodar teste específico
mvn test -Dtest=CreateClientePFServiceTest
```

**Cenário 4: Rodar testes de integração E2E**
```bash
# Requer Docker rodando (TestContainers)
mvn test -Dtest=UpdateClientePFIntegrationTest

# AbstractIntegrationTest usa perfil 'integration' automaticamente
```

**Cenário 5: Build completo com todos os testes**
```bash
# H2 para unit tests + PostgreSQL TestContainers para integration tests
mvn clean install
```

### Virtual Threads (Java 21)

O cliente-core utiliza **Virtual Threads** do Java 21 para melhorar drasticamente o throughput e reduzir latência em operações I/O-bound (banco de dados).

**Configuração:**
```yaml
spring:
  threads:
    virtual:
      enabled: true  # Ativa Virtual Threads automaticamente
```

**Benefícios:**
- **5-10x mais throughput**: De ~100 req/s para ~500-1000 req/s
- **50x mais usuários simultâneos**: De 200 para 10.000+
- **Redução de latência P95**: De 500ms para ~150ms (sob carga)
- **Pool de conexões otimizado**: Aumentado de 10 para 50 conexões

**Como funciona:**
- Spring Boot automaticamente usa Virtual Threads para todas as requisições HTTP
- Quando aguardando I/O (queries no banco), a Virtual Thread é "parked"
- O carrier thread (OS thread) é liberado para processar outra Virtual Thread
- Resultado: Milhares de requisições simultâneas sem esgotar threads do OS

**Monitoramento:**
```bash
# Métricas Actuator
curl http://localhost:8081/api/clientes/actuator/metrics/jvm.threads.virtual
curl http://localhost:8081/api/clientes/actuator/metrics/jvm.threads.platform
curl http://localhost:8081/api/clientes/actuator/metrics/jvm.threads.peak
```

**Referências:**
- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
- [Spring Boot 3.2+ Virtual Threads](https://spring.io/blog/2023/09/09/all-together-now-spring-boot-3-2-graalvm-native-images-java-21-and-virtual)
- Documentação completa: `VIRTUAL_THREADS.md`

---

## 📁 Estrutura de Camadas

```
cliente-core/src/main/java/br/com/vanessa_mudanca/cliente_core/
├── domain/
│   ├── entity/          # Entidades JPA
│   │   ├── Cliente.java               (abstrata - base)
│   │   ├── ClientePF.java             (Pessoa Física)
│   │   ├── ClientePJ.java             (Pessoa Jurídica)
│   │   ├── Documento.java
│   │   ├── Contato.java
│   │   ├── Endereco.java
│   │   ├── DadosBancarios.java        (Novo)
│   │   ├── PreferenciaCliente.java    (Novo)
│   │   └── AuditoriaCliente.java      (Novo)
│   └── enums/           # Enumerações
│       ├── SexoEnum.java
│       ├── EstadoEnum.java
│       ├── TipoDocumentoEnum.java
│       ├── TipoContatoEnum.java
│       ├── TipoClienteEnum.java       (Novo)
│       ├── OrigemLeadEnum.java        (Novo)
│       ├── TipoEnderecoEnum.java      (Novo)
│       ├── StatusDocumentoEnum.java   (Novo)
│       └── TipoChavePixEnum.java      (Novo)
├── repository/          # Repositórios JPA (a implementar)
├── service/             # Lógica de negócio (a implementar)
├── controller/          # API REST (a implementar)
└── dto/                # Data Transfer Objects (a implementar)
```

---

## 🗂️ Entidades

### Cliente (Abstrata)
**Tabela:** `clientes`
**Estratégia de herança:** JOINED (tabelas separadas para PF e PJ)

**Campos Básicos:**
- `id` (Long, PK, auto-increment)
- `email` (String, 150)
- `observacoes` (String, 1000)
- `ativo` (Boolean, default: true)
- `dataCriacao` (LocalDateTime, auto)
- `dataAtualizacao` (LocalDateTime, auto)

**Classificação:**
- `tipoCliente` (TipoClienteEnum) - Consignante, Comprador, Ambos, Prospecto, Parceiro, Inativo

**Marketing:**
- `origemLead` (OrigemLeadEnum) - Como conheceu o Va Nessa
- `utmSource` (String, 100) - Origem da campanha
- `utmCampaign` (String, 100) - Nome da campanha
- `utmMedium` (String, 100) - Meio da campanha

**Indicação:**
- `clienteIndicador` (ManyToOne → Cliente) - Quem indicou este cliente
- `dataIndicacao` (LocalDateTime)
- `indicacaoRecompensada` (Boolean) - Se já ganhou crédito pela indicação

**Métricas:**
- `totalComprasRealizadas` (Integer)
- `totalVendasRealizadas` (Integer)
- `valorTotalComprado` (Double)
- `valorTotalVendido` (Double)
- `dataPrimeiraTransacao` (LocalDateTime)
- `dataUltimaTransacao` (LocalDateTime)

**Bloqueio/Segurança:**
- `bloqueado` (Boolean, default: false)
- `motivoBloqueio` (String, 500)
- `dataBloqueio` (LocalDateTime)
- `usuarioBloqueou` (String, 100)

**Soft Delete (Deleção Lógica):**
- `dataDelecao` (LocalDateTime) - Data/hora da deleção lógica
- `motivoDelecao` (String, 500) - Motivo da exclusão
- `usuarioDeletou` (String, 100) - Usuário que realizou a deleção

> **IMPORTANTE:** Este microserviço implementa **soft delete pattern**. Clientes NÃO são deletados fisicamente do banco de dados. Quando deletados:
> - Campo `ativo` é alterado para `false`
> - Campos `dataDelecao`, `motivoDelecao` e `usuarioDeletou` são preenchidos
> - Dados são preservados para auditoria e podem ser restaurados
> - Queries normais filtram automaticamente clientes deletados (via métodos `findActive*`)

**Relacionamentos:**
- `listaDocumentos` (OneToMany → Documento)
- `listaContatos` (OneToMany → Contato)
- `listaEnderecos` (OneToMany → Endereco)
- `listaDadosBancarios` (OneToMany → DadosBancarios)
- `preferenciaCliente` (OneToOne → PreferenciaCliente)

**Métodos auxiliares:**
- `adicionarDocumento(Documento)`
- `removerDocumento(Documento)`
- `adicionarContato(Contato)`
- `removerContato(Contato)`
- `adicionarEndereco(Endereco)`
- `removerEndereco(Endereco)`
- `adicionarDadosBancarios(DadosBancarios)`
- `removerDadosBancarios(DadosBancarios)`
- `deletar(String motivo, String usuario)` - Realiza soft delete
- `restaurar(String usuario)` - Restaura cliente deletado
- `isDeletado()` - Verifica se cliente está deletado

---

### ClientePF (Pessoa Física)
**Tabela:** `clientes_pf`
**Herda de:** Cliente
**Discriminator:** "PF"

**Campos específicos:**
- `primeiroNome` (String, 100, obrigatório)
- `nomeDoMeio` (String, 100)
- `sobrenome` (String, 100, obrigatório)
- `cpf` (String, 14, único)
- `rg` (String, 20)
- `dataNascimento` (LocalDate)
- `sexo` (SexoEnum)
- `nomeMae` (String, 200)
- `nomePai` (String, 200)
- `estadoCivil` (String, 30)
- `profissao` (String, 100)
- `nacionalidade` (String, 50, default: "Brasileira")
- `naturalidade` (String, 100)

**Métodos auxiliares:**
- `getNomeCompleto()` - Retorna nome completo formatado
- `getIdade()` - Calcula idade baseada na data de nascimento

---

### ClientePJ (Pessoa Jurídica)
**Tabela:** `clientes_pj`
**Herda de:** Cliente
**Discriminator:** "PJ"

**Campos específicos:**
- `razaoSocial` (String, 200, obrigatório)
- `nomeFantasia` (String, 200)
- `cnpj` (String, 18, único, obrigatório)
- `inscricaoEstadual` (String, 20)
- `inscricaoMunicipal` (String, 20)
- `dataAbertura` (LocalDate)
- `porteEmpresa` (String, 50)
- `naturezaJuridica` (String, 100)
- `atividadePrincipal` (String, 200)
- `capitalSocial` (Double)
- `nomeResponsavel` (String, 200)
- `cpfResponsavel` (String, 14)
- `cargoResponsavel` (String, 100)
- `site` (String, 200)

**Métodos auxiliares:**
- `getNomeExibicao()` - Retorna nome fantasia ou razão social

---

### Documento
**Tabela:** `documentos`

**Campos:**
- `id` (Long, PK, auto-increment)
- `tipoDocumento` (TipoDocumentoEnum, obrigatório)
- `numero` (String, 50, obrigatório)
- `orgaoEmissor` (String, 50)
- `dataEmissao` (LocalDate)
- `dataValidade` (LocalDate)
- `observacoes` (String, 500)
- `statusDocumento` (StatusDocumentoEnum, default: AGUARDANDO_VERIFICACAO)
- `documentoPrincipal` (Boolean, default: false)
- `ativo` (Boolean, default: true)
- `dataCriacao` (LocalDateTime, auto)
- `dataAtualizacao` (LocalDateTime, auto)

**Métodos auxiliares:**
- `isExpirado()` - Verifica se documento está vencido (compara dataValidade com data atual)

**Comportamento automático:**
- No @PreUpdate, se `isExpirado()` retornar true, `statusDocumento` é automaticamente atualizado para EXPIRADO

**Relacionamento:**
- `cliente_id` (FK para Cliente)

---

### Contato
**Tabela:** `contatos`

**Campos:**
- `id` (Long, PK, auto-increment)
- `tipoContato` (TipoContatoEnum, obrigatório)
- `valor` (String, 100, obrigatório)
- `observacoes` (String, 500)
- `contatoPrincipal` (Boolean, default: false)
- `verificado` (Boolean, default: false)
- `ativo` (Boolean, default: true)
- `dataCriacao` (LocalDateTime, auto)
- `dataAtualizacao` (LocalDateTime, auto)

**Relacionamento:**
- `cliente_id` (FK para Cliente)

---

### Endereco
**Tabela:** `enderecos`

**Campos:**
- `id` (Long, PK, auto-increment)
- `cep` (String, 9, obrigatório)
- `logradouro` (String, 200, obrigatório)
- `numero` (String, 10)
- `complemento` (String, 100)
- `bairro` (String, 100, obrigatório)
- `cidade` (String, 100, obrigatório)
- `estado` (EstadoEnum, obrigatório)
- `pais` (String, 50, default: "Brasil")
- `tipoEndereco` (TipoEnderecoEnum) - Residencial, Comercial, Entrega, Cobrança, Coleta
- `enderecoPrincipal` (Boolean, default: false)
- `ativo` (Boolean, default: true)
- `dataCriacao` (LocalDateTime, auto)
- `dataAtualizacao` (LocalDateTime, auto)

**Relacionamento:**
- `cliente_id` (FK para Cliente)

---

---

### DadosBancarios
**Tabela:** `dados_bancarios`

**Campos:**
- `id` (Long, PK, auto-increment)
- `tipoConta` (String, 20) - "Corrente" ou "Poupança"
- `banco` (String, 100)
- `agencia` (String, 10)
- `conta` (String, 20)
- `digitoConta` (String, 2)
- `chavePix` (String, 100)
- `tipoChavePix` (TipoChavePixEnum) - CPF, CNPJ, Email, Telefone, Aleatória
- `dadosVerificados` (Boolean, default: false)
- `contaPrincipal` (Boolean, default: false)
- `ativo` (Boolean, default: true)
- `dataCriacao` (LocalDateTime, auto)
- `dataAtualizacao` (LocalDateTime, auto)

**Relacionamento:**
- `cliente_id` (FK para Cliente) @ManyToOne

**Regra de Negócio:**
- Apenas uma conta pode ser `contaPrincipal = true` por cliente
- Dados bancários são críticos para repasses de vendas

---

### PreferenciaCliente
**Tabela:** `preferencias_cliente`

**Campos:**
- `id` (Long, PK, auto-increment)
- `aceitaComunicacaoEmail` (Boolean, default: true)
- `aceitaComunicacaoSMS` (Boolean, default: true)
- `aceitaComunicacaoWhatsApp` (Boolean, default: true)
- `aceitaComunicacaoTelefone` (Boolean, default: false)
- `aceitaNewsletters` (Boolean, default: false)
- `aceitaOfertas` (Boolean, default: true)
- `aceitaPesquisas` (Boolean, default: false)
- `dataConsentimento` (LocalDateTime) - Quando aceitou LGPD
- `ipConsentimento` (String, 50) - IP de onde aceitou
- `consentimentoAtivo` (Boolean, default: true)
- `dataCriacao` (LocalDateTime, auto)
- `dataAtualizacao` (LocalDateTime, auto)

**Relacionamento:**
- `cliente_id` (FK para Cliente) @OneToOne

**Regra de Negócio:**
- Compliance com LGPD
- Respeitar preferências para evitar spam
- Registro de consentimento para auditoria

---

### AuditoriaCliente
**Tabela:** `auditoria_cliente`

**Campos:**
- `id` (Long, PK, auto-increment)
- `campoAlterado` (String, 100) - Nome do campo modificado
- `valorAnterior` (String, 500) - Valor antes da alteração
- `valorNovo` (String, 500) - Valor após alteração
- `usuarioResponsavel` (String, 100) - Quem fez a mudança
- `dataAlteracao` (LocalDateTime)
- `motivoAlteracao` (String, 500)
- `ipOrigem` (String, 50)
- `dataCriacao` (LocalDateTime, auto)

**Relacionamento:**
- `cliente_id` (FK para Cliente) @ManyToOne

**Regra de Negócio:**
- Registro append-only (nunca deletar)
- Rastreamento de fraudes e alterações suspeitas
- Compliance e resolução de conflitos

---

## 🏷️ Enumerações

### SexoEnum
- `MASCULINO` ("M", "Masculino")
- `FEMININO` ("F", "Feminino")
- `OUTRO` ("O", "Outro")
- `NAO_INFORMADO` ("N", "Não Informado")

### EstadoEnum
Todos os estados brasileiros (AC, AL, AP, AM, BA, CE, DF, ES, GO, MA, MT, MS, MG, PA, PB, PR, PE, PI, RJ, RN, RS, RO, RR, SC, SP, SE, TO)

### TipoDocumentoEnum
- `CPF` - Cadastro de Pessoa Física
- `RG` - Registro Geral
- `CNH` - Carteira Nacional de Habilitação
- `PASSAPORTE` - Passaporte
- `CNPJ` - Cadastro Nacional de Pessoa Jurídica
- `INSCRICAO_ESTADUAL` - Inscrição Estadual
- `INSCRICAO_MUNICIPAL` - Inscrição Municipal
- `CERTIDAO_NASCIMENTO` - Certidão de Nascimento
- `TITULO_ELEITOR` - Título de Eleitor
- `CARTEIRA_TRABALHO` - Carteira de Trabalho
- `OUTRO` - Outro

### TipoContatoEnum
- `CELULAR` - Celular
- `TELEFONE_FIXO` - Telefone Fixo
- `EMAIL` - E-mail
- `WHATSAPP` - WhatsApp
- `TELEGRAM` - Telegram
- `OUTRO` - Outro

### TipoClienteEnum ⭐ NOVO
- `CONSIGNANTE` - Pessoa que VAI VENDER (deixa item em consignação)
- `COMPRADOR` - Pessoa que VAI COMPRAR
- `AMBOS` - Vende E compra
- `PROSPECTO` - Ainda não fez nenhuma transação
- `PARCEIRO` - Prestador de serviço (transportadora, instalador)
- `INATIVO` - Desativado

### OrigemLeadEnum ⭐ NOVO
- `GOOGLE_ADS` - Google Ads
- `FACEBOOK_ADS` - Facebook Ads
- `INSTAGRAM_ADS` - Instagram Ads
- `INDICACAO` - Indicação de outro cliente
- `GOOGLE_ORGANICO` - Busca orgânica Google
- `REDES_SOCIAIS` - Redes sociais (orgânico)
- `WHATSAPP` - WhatsApp
- `BOCA_A_BOCA` - Boca a boca
- `INFLUENCER` - Influenciador
- `PARCEIRO` - Parceiro comercial
- `OUTRO` - Outro

### TipoEnderecoEnum ⭐ NOVO
- `RESIDENCIAL` - Endereço residencial
- `COMERCIAL` - Endereço comercial
- `ENTREGA` - Específico para receber compras
- `COBRANCA` - Para boletos e cobranças
- `COLETA` - Onde buscar itens para consignação

### StatusDocumentoEnum ⭐ NOVO
- `VALIDO` - Documento válido e ativo
- `EXPIRADO` - Documento vencido (data passou)
- `AGUARDANDO_VERIFICACAO` - Aguardando validação
- `VERIFICADO` - Verificado e aprovado
- `REJEITADO` - Documento rejeitado

### TipoChavePixEnum ⭐ NOVO
- `CPF` - Chave PIX tipo CPF
- `CNPJ` - Chave PIX tipo CNPJ
- `EMAIL` - Chave PIX tipo e-mail
- `TELEFONE` - Chave PIX tipo telefone
- `ALEATORIA` - Chave PIX aleatória

---

## 🔌 Endpoints

### Status: ✅ IMPLEMENTADO (CRUD Completo + Soft Delete)

#### Clientes PF
- ✅ `GET /v1/clientes/pf` - Listar clientes PF (paginado)
- ✅ `GET /v1/clientes/pf/{publicId}` - Buscar cliente PF por UUID público
- ✅ `GET /v1/clientes/pf/cpf/{cpf}` - Buscar por CPF
- ✅ `POST /v1/clientes/pf` - Criar cliente PF
- ✅ `PUT /v1/clientes/pf/{publicId}` - Atualizar cliente PF (suporta atualização parcial)
- ✅ `DELETE /v1/clientes/pf/{publicId}` - **Soft delete** cliente PF
  - **Query params obrigatórios:** `motivo` (String), `usuario` (String)
  - **Retorno:** 204 No Content
  - **Exceções:** 404 (não encontrado), 409 (já deletado)
- ✅ `POST /v1/clientes/pf/{publicId}/restaurar` - Restaurar cliente PF deletado
  - **Query param obrigatório:** `usuario` (String)
  - **Retorno:** 204 No Content
  - **Exceção:** 404 (não encontrado)

#### Clientes PJ
- ✅ `GET /v1/clientes/pj` - Listar clientes PJ (paginado)
- ✅ `GET /v1/clientes/pj/{publicId}` - Buscar cliente PJ por UUID público
- ✅ `GET /v1/clientes/pj/cnpj/{cnpj}` - Buscar por CNPJ
- ✅ `POST /v1/clientes/pj` - Criar cliente PJ
- ✅ `PUT /v1/clientes/pj/{publicId}` - Atualizar cliente PJ (suporta atualização parcial)
- ✅ `DELETE /v1/clientes/pj/{publicId}` - **Soft delete** cliente PJ
  - **Query params obrigatórios:** `motivo` (String), `usuario` (String)
  - **Retorno:** 204 No Content
  - **Exceções:** 404 (não encontrado), 409 (já deletado)
- ✅ `POST /v1/clientes/pj/{publicId}/restaurar` - Restaurar cliente PJ deletado
  - **Query param obrigatório:** `usuario` (String)
  - **Retorno:** 204 No Content
  - **Exceção:** 404 (não encontrado)

#### Documentos
- `POST /api/clientes/{clienteId}/documentos` - Adicionar documento
- `PUT /api/clientes/{clienteId}/documentos/{documentoId}` - Atualizar documento
- `DELETE /api/clientes/{clienteId}/documentos/{documentoId}` - Remover documento

#### Contatos
- `POST /api/clientes/{clienteId}/contatos` - Adicionar contato
- `PUT /api/clientes/{clienteId}/contatos/{contatoId}` - Atualizar contato
- `DELETE /api/clientes/{clienteId}/contatos/{contatoId}` - Remover contato

#### Endereços
- `POST /api/clientes/{clienteId}/enderecos` - Adicionar endereço
- `PUT /api/clientes/{clienteId}/enderecos/{enderecoId}` - Atualizar endereço
- `DELETE /api/clientes/{clienteId}/enderecos/{enderecoId}` - Remover endereço

#### Dados Bancários ⭐ NOVO
- `POST /api/clientes/{clienteId}/dados-bancarios` - Adicionar dados bancários
- `PUT /api/clientes/{clienteId}/dados-bancarios/{dadosId}` - Atualizar dados bancários
- `DELETE /api/clientes/{clienteId}/dados-bancarios/{dadosId}` - Remover dados bancários
- `PUT /api/clientes/{clienteId}/dados-bancarios/{dadosId}/definir-principal` - Definir conta principal

#### Preferências ⭐ NOVO
- `GET /api/clientes/{clienteId}/preferencias` - Obter preferências do cliente
- `PUT /api/clientes/{clienteId}/preferencias` - Atualizar preferências
- `POST /api/clientes/{clienteId}/preferencias/consentimento` - Registrar consentimento LGPD

#### Auditoria ⭐ NOVO
- `GET /api/clientes/{clienteId}/auditoria` - Listar histórico de alterações
- `GET /api/clientes/{clienteId}/auditoria/{campo}` - Histórico de um campo específico

---

## 📋 Regras de Negócio

### Validações
1. **CPF:** Deve ser único e válido (algoritmo de validação)
2. **CNPJ:** Deve ser único e válido (algoritmo de validação)
3. **Email:** Formato válido, único por cliente
4. **Documento Principal:** Apenas 1 documento pode ser principal por cliente
5. **Contato Principal:** Apenas 1 contato pode ser principal por cliente
6. **Endereço Principal:** Apenas 1 endereço pode ser principal por tipo
7. **Conta Bancária Principal:** Apenas 1 conta pode ser principal por cliente ⭐ NOVO
8. **CEP:** Formato válido (XXXXX-XXX)
9. **Data Nascimento:** Não pode ser futura
10. **Soft Delete:** Ao deletar, marcar `ativo = false` ao invés de remover
11. **Chave PIX:** Validar formato conforme tipo de chave ⭐ NOVO
12. **Documento Expirado:** Atualização automática de status quando dataValidade passar ⭐ NOVO

### Negócio
1. Cliente PF obrigatoriamente deve ter: `primeiroNome` e `sobrenome`
2. Cliente PJ obrigatoriamente deve ter: `razaoSocial` e `cnpj`
3. Todo cliente deve ter pelo menos 1 contato ativo
4. Documentos expirados devem ser sinalizados automaticamente
5. Endereços podem ser validados via API de CEP (ViaCEP)
6. **Cliente consignante** deve ter dados bancários cadastrados para receber repasses ⭐ NOVO
7. **Preferências de comunicação** devem ser respeitadas (LGPD) ⭐ NOVO
8. **Auditoria** deve registrar todas alterações críticas (CPF, CNPJ, dados bancários) ⭐ NOVO
9. **Bloqueio de cliente** impede novas transações ⭐ NOVO
10. **Programa de indicação:** Cliente que indica recebe recompensa apenas uma vez por indicado ⭐ NOVO

### Soft Delete (Deleção Lógica) ✅ IMPLEMENTADO
1. **Não há deleção física:** Clientes NUNCA são removidos do banco de dados
2. **Preservação de dados:** Todos os dados são mantidos para auditoria e conformidade legal
3. **Restauração:** Clientes deletados podem ser restaurados a qualquer momento
4. **Validação de duplicidade:** Não é possível deletar cliente já deletado (retorna 409 Conflict)
5. **Queries filtradas automáticas:**
   - Métodos `findActive*` retornam apenas clientes ativos
   - Métodos sem prefixo `Active` retornam TODOS os clientes (incluindo deletados)
6. **Auditoria obrigatória:** Motivo e usuário responsável são obrigatórios na deleção
7. **Integridade referencial:** Relacionamentos são preservados mesmo após deleção

---

## 🔗 Dependências Externas

### A Implementar
- **ViaCEP API** - Validação e preenchimento automático de endereços
- **API de Validação CPF/CNPJ** - Validação em tempo real
- **API de SMS** - Verificação de contatos telefônicos
- **API de Email** - Verificação de emails

---

## 🚀 Como Rodar

### Pré-requisitos
1. **PostgreSQL** rodando na porta 5432
2. **Java 21** instalado
3. **Maven** instalado

### Passo 1: Criar banco de dados
```bash
psql -U postgres
CREATE DATABASE vanessa_mudanca_clientes;
\q
```

### Passo 2: Executar aplicação
```bash
cd cliente-core
mvn spring-boot:run
```

**O que acontece:**
1. Spring Boot inicia
2. **Liquibase** executa automaticamente:
   - Cria 9 tabelas (clientes, clientes_pf, clientes_pj, documentos, contatos, enderecos, dados_bancarios, preferencias_cliente, auditoria_cliente)
   - Cria ~50 índices otimizados para RDS PostgreSQL
   - Cria foreign keys e constraints
   - **Insere seeds** de teste (15 clientes + documentos + contatos + endereços + dados bancários)
3. Aplicação fica disponível em: `http://localhost:8081/api/clientes`

### Passo 3: Verificar estrutura do banco
```bash
psql -U postgres -d vanessa_mudanca_clientes
\dt  # Listar tabelas
\di  # Listar índices
```

Ou use o script de verificação:
```bash
psql -U postgres -d vanessa_mudanca_clientes -f verify-database-structure.sql
```

### Health Check
```bash
curl http://localhost:8081/api/clientes/actuator/health
```

### ⚠️ Troubleshooting Liquibase

**Erro: "Validation Failed"**
```bash
# Limpar histórico Liquibase (apenas desenvolvimento)
psql -U postgres -d vanessa_mudanca_clientes
DROP TABLE databasechangelog;
DROP TABLE databasechangeloglock;
\q

# Reiniciar aplicação
mvn spring-boot:run
```

**Ver logs do Liquibase:**
```bash
mvn spring-boot:run | grep liquibase
```

**Desabilitar seeds (apenas estrutura):**
Edite `application-dev.yml` e mude:
```yaml
liquibase:
  contexts: dev  # Mude para: contexts: ddl-only
```

---

## 🧪 Testes

### Status: ✅ 155 TESTES IMPLEMENTADOS

**Cobertura Atual:**
- ✅ **Validações de CPF/CNPJ** (26 testes - DocumentoValidator)
- ✅ **Services de Cliente PF** (28 testes)
- ✅ **Services de Cliente PJ** (30 testes)
- ✅ **Soft Delete** (21 testes - DeleteClienteService + Repository)
- ✅ **Controllers REST** (6 testes - ClientePF e ClientePJ)
- ✅ **Utilitários** (42 testes - MaskingUtil, CorrelationId)
- ✅ **Integração** (2 testes - Application Context)

**Executar testes:**
```bash
# Todos os testes
mvn test

# Teste específico
mvn test -Dtest=DeleteClienteServiceTest

# Com cobertura
mvn clean verify
```

**Total: 155 testes passando** ✅

---

## 💡 Exemplos de Uso - Soft Delete

### Exemplo 1: Deletar Cliente PF

**Request:**
```bash
DELETE http://localhost:8081/api/clientes/v1/clientes/pf/550e8400-e29b-41d4-a716-446655440000?motivo=Cliente%20solicitou%20exclus%C3%A3o&usuario=admin
```

**Response:**
```
204 No Content
```

**O que acontece:**
1. Campo `ativo` alterado para `false`
2. Campo `dataDelecao` preenchido com timestamp atual
3. Campo `motivoDelecao` = "Cliente solicitou exclusão"
4. Campo `usuarioDeletou` = "admin"
5. Dados preservados no banco para auditoria

### Exemplo 2: Restaurar Cliente Deletado

**Request:**
```bash
POST http://localhost:8081/api/clientes/v1/clientes/pf/550e8400-e29b-41d4-a716-446655440000/restaurar?usuario=supervisor
```

**Response:**
```
204 No Content
```

**O que acontece:**
1. Campo `ativo` alterado para `true`
2. Campos `dataDelecao`, `motivoDelecao`, `usuarioDeletou` limpos (null)
3. Cliente volta a aparecer nas queries normais

### Exemplo 3: Uso de Queries Filtradas no Código

```java
// Buscar APENAS clientes ativos (uso normal em APIs públicas)
Optional<ClientePF> ativo = clientePFRepository.findActiveByCpf("12345678909");

// Buscar TODOS (incluindo deletados) - para auditoria/restauração
Optional<ClientePF> qualquer = clientePFRepository.findByCpf("12345678909");

// Verificar se CPF está em uso por cliente ATIVO
boolean cpfEmUso = clientePFRepository.existsActiveByCpf("12345678909");
```

### Exemplo 4: Tratamento de Erros

**Tentativa de deletar cliente já deletado:**
```bash
DELETE .../550e8400-e29b-41d4-a716-446655440000?motivo=Teste&usuario=admin
```

**Response:**
```json
{
  "timestamp": "2025-11-03T20:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Cliente com PublicId 550e8400-e29b-41d4-a716-446655440000 já foi deletado anteriormente",
  "path": "/v1/clientes/pf/550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 📊 Banco de Dados

### Gerenciamento de Schema
O banco de dados é gerenciado pelo **Liquibase** para garantir versionamento completo e evitar alterações manuais.

**IMPORTANTE:** ❌ **NUNCA** use `ALTER TABLE` diretamente no banco de dados. Todas as alterações devem passar pelo Liquibase.

### Estrutura de Tabelas
```
clientes (tabela pai)
├── clientes_pf (herança JOINED)
└── clientes_pj (herança JOINED)

documentos (OneToMany com clientes)
contatos (OneToMany com clientes)
enderecos (OneToMany com clientes)
dados_bancarios (OneToMany com clientes) ⭐ NOVO
preferencias_cliente (OneToOne com clientes) ⭐ NOVO
auditoria_cliente (OneToMany com clientes) ⭐ NOVO
```

### Arquivos Liquibase
```
src/main/resources/db/changelog/
├── db-changelog-master.xml              # Orquestrador principal
└── sql/
    ├── ddl/                             # Scripts de estrutura
    │   ├── 001-create-table-clientes.sql
    │   ├── 002-create-table-clientes-pf.sql
    │   ├── 003-create-table-clientes-pj.sql
    │   ├── 004-create-table-documentos.sql
    │   ├── 005-create-table-contatos.sql
    │   ├── 006-create-table-enderecos.sql
    │   ├── 007-create-table-dados-bancarios.sql
    │   ├── 008-create-table-preferencias-cliente.sql
    │   ├── 009-create-table-auditoria-cliente.sql
    │   ├── 010-create-indexes.sql       # ~50 índices otimizados
    │   └── 011-create-constraints.sql   # Foreign keys
    └── dml/                             # Dados de teste (seeds)
        ├── 001-seed-clientes-pf.sql     # 10 clientes PF
        ├── 002-seed-clientes-pj.sql     # 5 clientes PJ
        ├── 003-seed-documentos.sql
        ├── 004-seed-contatos.sql
        ├── 005-seed-enderecos.sql
        ├── 006-seed-dados-bancarios.sql
        ├── 007-seed-preferencias.sql
        └── 008-seed-auditoria.sql
```

### Índices Implementados (~50 índices)

**Tabela clientes:**
- `clientes.email` (UNIQUE parcial WHERE ativo = true)
- `clientes.tipo_cliente` (INDEX)
- `clientes.origem_lead` (INDEX)
- `clientes.bloqueado` (INDEX parcial WHERE bloqueado = true)
- `clientes.cliente_indicador_id + indicacao_recompensada` (INDEX composto)
- `clientes.data_criacao` (INDEX DESC)

**Tabela clientes_pf:**
- `clientes_pf.cpf` (UNIQUE)
- `clientes_pf.data_nascimento` (INDEX)
- **GIN full-text search** para busca por nome (português)

**Tabela clientes_pj:**
- `clientes_pj.cnpj` (UNIQUE)
- `clientes_pj.porte_empresa` (INDEX)
- **GIN full-text search** para razão social/fantasia (português)

**Tabela documentos:**
- `documentos.cliente_id + ativo` (INDEX composto)
- `documentos.tipo_documento` (INDEX)
- `documentos.status_documento` (INDEX)
- `documentos.numero` (INDEX)

**Tabela contatos:**
- `contatos.cliente_id + ativo` (INDEX composto)
- `contatos.tipo_contato` (INDEX)
- `contatos.valor` (INDEX)

**Tabela enderecos:**
- `enderecos.cliente_id + ativo` (INDEX composto)
- `enderecos.cep` (INDEX)
- `enderecos.cidade + estado` (INDEX composto)
- `enderecos.tipo_endereco` (INDEX)

**Tabela dados_bancarios:**
- `dados_bancarios.cliente_id + ativo` (INDEX composto)
- `dados_bancarios.conta_principal` (INDEX parcial WHERE conta_principal = true)
- `dados_bancarios.chave_pix` (INDEX)

**Tabela preferencias_cliente:**
- `preferencias_cliente.cliente_id` (UNIQUE)
- `preferencias_cliente.consentimento_ativo` (INDEX)

**Tabela auditoria_cliente:**
- `auditoria_cliente.cliente_id + data_alteracao` (INDEX composto DESC)
- `auditoria_cliente.campo_alterado` (INDEX)
- `auditoria_cliente.usuario_responsavel` (INDEX)

### Documentação Completa
Para mais detalhes sobre a estrutura do banco de dados, consulte:
- **Estrutura técnica:** `LIQUIBASE_STRUCTURE.md`
- **Guia rápido:** `LIQUIBASE_QUICKSTART.md`
- **Script de verificação:** `verify-database-structure.sql`

---

## 📝 Histórico de Mudanças

### 2025-11-03 - Versão 0.2.0 ⭐ SOFT DELETE IMPLEMENTADO
- ✅ **Soft Delete Pattern** implementado completamente:
  - Campos `dataDelecao`, `motivoDelecao`, `usuarioDeletou` adicionados à entidade Cliente
  - Métodos de domínio: `deletar()`, `restaurar()`, `isDeletado()`
  - Liquibase changeset `013-add-soft-delete-columns.sql` com índices otimizados
- ✅ **Use Cases e Services:**
  - `DeleteClienteUseCase` com operações `deletar()` e `restaurar()`
  - `DeleteClienteService` com logging estruturado (MDC)
  - `ClienteJaDeletadoException` para validação de duplicidade
- ✅ **Endpoints REST:**
  - `DELETE /v1/clientes/{pf|pj}/{publicId}` - Soft delete com motivo e usuário
  - `POST /v1/clientes/{pf|pj}/{publicId}/restaurar` - Restauração de clientes
  - Retorno 204 No Content, exceções 404/409 adequadas
- ✅ **Queries Filtradas (Dual-Method Pattern):**
  - Métodos `findActive*` retornam apenas registros ativos
  - Métodos sem prefixo retornam TODOS (incluindo deletados)
  - Implementado em: ClientePF e ClientePJ repositories
  - Exemplos: `findActiveByCpf()`, `findActiveByPublicId()`, `existsActiveByCpf()`
- ✅ **Testes Completos (21 novos testes):**
  - 12 testes unitários (DeleteClienteServiceTest)
  - 9 testes de integração (ClientePFRepositoryAdapterSoftDeleteTest)
  - **Total: 155 testes passando** (aumento de 134 → 155)
- ✅ **Documentação atualizada:**
  - README com seções de Soft Delete
  - Regras de negócio documentadas
  - Endpoints documentados com exemplos

### 2025-11-02 - Versão 0.1.0 (Noite)
- ✅ **Liquibase** implementado para gerenciamento de schema PostgreSQL
- ✅ **20 scripts SQL** criados (11 DDL + 8 DML seeds + 1 master XML)
- ✅ **~50 índices otimizados** para RDS PostgreSQL:
  - Índices parciais (WHERE)
  - Índices compostos (múltiplas colunas)
  - Índices GIN para full-text search em português
  - Índices DESC para ordenação
- ✅ **Seeds de teste** com 15 clientes (10 PF + 5 PJ):
  - Dados realistas (nomes, CPF/CNPJ, endereços)
  - Programa de indicação (clientes indicadores/indicados)
  - Histórico de transações (compras/vendas)
  - Múltiplos contatos (celular, email, WhatsApp)
  - Dados bancários e PIX
  - Preferências LGPD com consentimento
  - Auditoria de alterações
- ✅ **Documentação completa**:
  - `LIQUIBASE_STRUCTURE.md` - Estrutura técnica detalhada
  - `LIQUIBASE_QUICKSTART.md` - Guia rápido de uso
  - `verify-database-structure.sql` - Script de verificação
- ✅ **Foreign Keys** com ON DELETE/UPDATE apropriados
- ✅ **CHECK constraints** para enums (ao invés de ENUM nativo PostgreSQL)
- ✅ **Comentários** em tabelas e colunas críticas
- ✅ **application-dev.yml** configurado com Liquibase
- ✅ **pom.xml** atualizado com dependência Liquibase
- ✅ README atualizado com instruções de uso e troubleshooting

### 2025-11-02 - Versão 0.0.2 (Tarde)
- ✅ **5 Novos Enums:** TipoClienteEnum, OrigemLeadEnum, TipoEnderecoEnum, StatusDocumentoEnum, TipoChavePixEnum
- ✅ **3 Novas Entidades:** DadosBancarios, PreferenciaCliente, AuditoriaCliente
- ✅ **Melhorias na Entidade Cliente:**
  - Adicionados campos de classificação (tipoCliente)
  - Adicionados campos de marketing (origemLead, UTM)
  - Adicionados campos de indicação (clienteIndicador)
  - Adicionados campos de métricas (totais de compras/vendas)
  - Adicionados campos de bloqueio/segurança
  - Relacionamentos com DadosBancarios e PreferenciaCliente
- ✅ **Melhorias na Entidade Documento:**
  - Campo statusDocumento com atualização automática para EXPIRADO
  - Método isExpirado()
- ✅ **Melhorias na Entidade Endereco:**
  - Campo tipoEndereco (Residencial, Comercial, Entrega, Cobrança, Coleta)
- ✅ **Compliance LGPD:** Entidade PreferenciaCliente com consentimento
- ✅ **Auditoria:** Rastreamento de alterações críticas
- ✅ **Dados Bancários:** Suporte a múltiplas contas e PIX
- ✅ README atualizado com todas as novas funcionalidades

### 2025-11-02 - Versão 0.0.1 (Manhã)
- ✅ Estrutura inicial do projeto
- ✅ Configuração de `application.yml` e `application-dev.yml`
- ✅ Criação de enums: `SexoEnum`, `EstadoEnum`, `TipoDocumentoEnum`, `TipoContatoEnum`
- ✅ Criação de entidades base: `Cliente`, `ClientePF`, `ClientePJ`
- ✅ Criação de entidades auxiliares: `Documento`, `Contato`, `Endereco`
- ✅ Configuração de herança JOINED para clientes
- ✅ Implementação de timestamps automáticos
- ✅ README inicial criado

---

## 🔗 Integração com Outros Microserviços

### Arquitetura Híbrida: Step Functions + Kafka

O cliente-core utiliza **arquitetura híbrida** para integração:

- **AWS Step Functions**: Cliente-core é **chamado** por outros MS (validação síncrona)
- **Apache Kafka (MSK)**: Cliente-core **publica/consome** eventos (propagação assíncrona)

**📄 Documentação Completa:** `docs/INTEGRATION_ARCHITECTURE.md`

### Papel do cliente-core

| Padrão | Uso | Exemplo |
|--------|-----|---------|
| **Step Functions** | ❌ NÃO inicia | Cliente-core é apenas CRUD |
| **Step Functions** | ✅ É chamado | `venda-core` valida se cliente existe antes de criar venda |
| **Kafka Producer** | ✅ Publica eventos | Notifica quando cliente é criado/atualizado |
| **Kafka Consumer** | ✅ Consome eventos | Atualiza métricas quando venda é concluída |

### Eventos Kafka Publicados

**Topic:** `cliente-events`

| Evento | Quando | Consumidores |
|--------|--------|--------------|
| `ClientePFCriado` | POST /v1/clientes/pf (sucesso) | analytics-core, notificacao-core, auditoria-core |
| `ClientePJCriado` | POST /v1/clientes/pj (sucesso) | analytics-core, notificacao-core, auditoria-core |
| `ClientePFAtualizado` | PUT /v1/clientes/pf/{id} (sucesso) | auditoria-core, analytics-core |
| `ClienteDeletado` | DELETE /v1/clientes/{id} (futuro) | auditoria-core |

### Eventos Kafka Consumidos

**Topic:** `venda-events`
**Consumer Group:** `cliente-core-metrics-group`

| Evento | Ação |
|--------|------|
| `VendaConcluida` | Incrementa `totalVendasRealizadas` (vendedor) e `totalComprasRealizadas` (comprador) |
| `VendaCancelada` | Rollback das métricas (decrementa contadores) |

### Correlation ID

Todos os eventos e chamadas HTTP incluem **Correlation ID** para rastreamento:

- Header HTTP: `X-Correlation-ID`
- Payload Kafka: `event.correlationId`
- Logs CloudWatch: Campo `correlationId` em todos os logs

**Query CloudWatch (rastreamento completo):**
```sql
fields @timestamp, @message, correlationId, service
| filter correlationId = "abc-123"
| sort @timestamp asc
```

### Idempotência

**Kafka:** Implementado via tabela `eventos_processados` (evita processar evento duplicado)
**HTTP:** Via header `X-Idempotency-Key` (a ser implementado em Feature DELETE)

---

## 🎯 Próximos Passos

1. **Repositories** - Criar interfaces JPA com queries customizadas
2. **Services** - Implementar lógica de negócio e validações
3. **DTOs** - Criar objetos de transferência para API
4. **Controllers** - Implementar endpoints REST
5. **Validators** - Validação de CPF/CNPJ
6. **Exception Handling** - Tratamento global de exceções
7. **Testes Unitários** - Cobertura mínima de 80%
8. **Testes de Integração** - Validar fluxos completos
9. **Documentação OpenAPI** - Swagger/Springdoc
10. **Integração ViaCEP** - Validação de endereços

---

## 📞 Contato

Para dúvidas ou sugestões sobre este microserviço, consulte o time de desenvolvimento.

---

**Última atualização:** 2025-11-02
**Versão:** 0.1.0-SNAPSHOT
**Mantido por:** Equipe Va Nessa Mudança

---

## 📊 Estatísticas do Microserviço

- **Entidades:** 9 (Cliente, ClientePF, ClientePJ, Documento, Contato, Endereco, DadosBancarios, PreferenciaCliente, AuditoriaCliente)
- **Enums:** 9 (SexoEnum, EstadoEnum, TipoDocumentoEnum, TipoContatoEnum, TipoClienteEnum, OrigemLeadEnum, TipoEnderecoEnum, StatusDocumentoEnum, TipoChavePixEnum)
- **Tabelas:** 9 (todas gerenciadas por Liquibase)
- **Scripts SQL:** 20 (11 DDL + 8 DML seeds + 1 master XML)
- **Índices:** ~50 (otimizados para RDS PostgreSQL)
- **Foreign Keys:** 9 (com ON DELETE/UPDATE apropriados)
- **Seeds:** 15 clientes + ~150 registros relacionados
- **Relacionamentos:** 8 (listaDocumentos, listaContatos, listaEnderecos, listaDadosBancarios, preferenciaCliente, clienteIndicador, auditoria, dados bancários)
- **Compliance:** LGPD implementado (PreferenciaCliente)
- **Auditoria:** Sim (AuditoriaCliente)
- **Soft Delete:** Sim (campo ativo)
- **Timestamps:** Sim (dataCriacao, dataAtualizacao)
- **Versionamento de Schema:** Liquibase
- **Status:** ✅ Banco de dados completo - Pronto para implementação de Repositories, Services e Controllers
