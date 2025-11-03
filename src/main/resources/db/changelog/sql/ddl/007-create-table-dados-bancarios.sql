-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Tabela: dados_bancarios
-- ========================================

CREATE TABLE dados_bancarios (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- FK para Cliente
    cliente_id BIGINT NOT NULL,

    -- Dados da conta bancária
    tipo_conta VARCHAR(20),
    banco VARCHAR(100),
    agencia VARCHAR(10),
    conta VARCHAR(20),
    digito_conta VARCHAR(2),

    -- PIX
    chave_pix VARCHAR(100),
    tipo_chave_pix VARCHAR(20),

    -- Controle
    dados_verificados BOOLEAN NOT NULL DEFAULT false,
    conta_principal BOOLEAN NOT NULL DEFAULT false,

    -- Controle de ativação
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Auditoria
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_dados_bancarios_tipo_conta CHECK (tipo_conta IS NULL OR tipo_conta IN (
        'CORRENTE', 'POUPANCA'
    )),
    CONSTRAINT chk_dados_bancarios_tipo_chave_pix CHECK (tipo_chave_pix IS NULL OR tipo_chave_pix IN (
        'CPF', 'CNPJ', 'EMAIL', 'TELEFONE', 'ALEATORIA'
    ))
);

-- Comentários
COMMENT ON TABLE dados_bancarios IS 'Dados bancários dos clientes para repasses de vendas (conta + PIX)';
COMMENT ON COLUMN dados_bancarios.tipo_conta IS 'Tipo de conta: CORRENTE ou POUPANCA';
COMMENT ON COLUMN dados_bancarios.tipo_chave_pix IS 'Tipo de chave PIX: CPF, CNPJ, EMAIL, TELEFONE, ALEATORIA';
COMMENT ON COLUMN dados_bancarios.conta_principal IS 'Apenas UMA conta pode ser principal por cliente';
COMMENT ON COLUMN dados_bancarios.dados_verificados IS 'Se os dados bancários foram validados (via microdeposito, etc)';
