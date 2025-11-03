# 🧑 cliente-core

## Descrição
Microserviço responsável pelo gerenciamento completo de clientes (Pessoa Física e Jurídica) da plataforma Va Nessa Mudança.

Este serviço é o núcleo do cadastro de clientes, gerenciando informações pessoais, documentos, contatos e endereços tanto para pessoas físicas quanto jurídicas.

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

### application-dev.yml (Desenvolvimento Local)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vanessa_mudanca_clientes
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    br.com.vanessa_mudanca: DEBUG
```

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

### Status: 🚧 A IMPLEMENTAR

Os seguintes endpoints serão implementados:

#### Clientes PF
- `GET /api/clientes/pf` - Listar clientes PF
- `GET /api/clientes/pf/{id}` - Buscar cliente PF por ID
- `POST /api/clientes/pf` - Criar cliente PF
- `PUT /api/clientes/pf/{id}` - Atualizar cliente PF
- `DELETE /api/clientes/pf/{id}` - Deletar (soft delete) cliente PF
- `GET /api/clientes/pf/cpf/{cpf}` - Buscar por CPF

#### Clientes PJ
- `GET /api/clientes/pj` - Listar clientes PJ
- `GET /api/clientes/pj/{id}` - Buscar cliente PJ por ID
- `POST /api/clientes/pj` - Criar cliente PJ
- `PUT /api/clientes/pj/{id}` - Atualizar cliente PJ
- `DELETE /api/clientes/pj/{id}` - Deletar (soft delete) cliente PJ
- `GET /api/clientes/pj/cnpj/{cnpj}` - Buscar por CNPJ

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

### Status: 🚧 A IMPLEMENTAR

Serão implementados testes para:
- Validações de CPF/CNPJ
- Regras de negócio de documentos/contatos/endereços
- Endpoints REST
- Integração com banco de dados

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

### 2025-11-02 - Versão 0.1.0 (Noite) ⭐ NOVA VERSÃO
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
