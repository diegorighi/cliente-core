-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Tabela: contatos
-- ========================================

CREATE TABLE contatos (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- FK para Cliente
    cliente_id BIGINT NOT NULL,

    -- Tipo de contato
    tipo_contato VARCHAR(20) NOT NULL,

    -- Valor do contato (telefone, email, etc)
    valor VARCHAR(100) NOT NULL,

    -- Controle
    contato_principal BOOLEAN NOT NULL DEFAULT false,
    verificado BOOLEAN NOT NULL DEFAULT false,

    -- Observações
    observacoes VARCHAR(500),

    -- Controle de ativação
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Auditoria
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_contatos_tipo CHECK (tipo_contato IN (
        'CELULAR', 'TELEFONE_FIXO', 'EMAIL', 'WHATSAPP', 'TELEGRAM', 'OUTRO'
    ))
);

-- Comentários
COMMENT ON TABLE contatos IS 'Contatos dos clientes (celular, email, WhatsApp, etc)';
COMMENT ON COLUMN contatos.tipo_contato IS 'Tipo: CELULAR, TELEFONE_FIXO, EMAIL, WHATSAPP, TELEGRAM, OUTRO';
COMMENT ON COLUMN contatos.valor IS 'Valor do contato: número de telefone, endereço de email, etc';
COMMENT ON COLUMN contatos.contato_principal IS 'Apenas UM contato pode ser principal por cliente';
COMMENT ON COLUMN contatos.verificado IS 'Indica se o contato foi verificado (via SMS, email, etc)';
