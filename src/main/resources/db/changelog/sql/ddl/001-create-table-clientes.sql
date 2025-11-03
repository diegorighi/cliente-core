-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Tabela: clientes (pai - herança JOINED)
-- ========================================

CREATE TABLE clientes (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Discriminator para herança JOINED (PF ou PJ)
    dtype VARCHAR(10) NOT NULL,

    -- Dados básicos
    email VARCHAR(150) NOT NULL,
    observacoes VARCHAR(1000),
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Classificação do cliente
    tipo_cliente VARCHAR(20) NOT NULL DEFAULT 'PROSPECTO',

    -- Marketing e origem
    origem_lead VARCHAR(30),
    utm_source VARCHAR(100),
    utm_campaign VARCHAR(100),
    utm_medium VARCHAR(100),

    -- Programa de indicação
    cliente_indicador_id BIGINT,
    data_indicacao TIMESTAMP,
    indicacao_recompensada BOOLEAN DEFAULT false,

    -- Métricas de transações
    total_compras_realizadas INTEGER DEFAULT 0,
    total_vendas_realizadas INTEGER DEFAULT 0,
    valor_total_comprado NUMERIC(15,2) DEFAULT 0.00,
    valor_total_vendido NUMERIC(15,2) DEFAULT 0.00,
    data_primeira_transacao TIMESTAMP,
    data_ultima_transacao TIMESTAMP,

    -- Bloqueio e segurança
    bloqueado BOOLEAN NOT NULL DEFAULT false,
    motivo_bloqueio VARCHAR(500),
    data_bloqueio TIMESTAMP,
    usuario_bloqueou VARCHAR(100),

    -- Auditoria
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_clientes_dtype CHECK (dtype IN ('PF', 'PJ')),
    CONSTRAINT chk_clientes_tipo_cliente CHECK (tipo_cliente IN (
        'CONSIGNANTE', 'COMPRADOR', 'AMBOS', 'PROSPECTO', 'PARCEIRO', 'INATIVO'
    )),
    CONSTRAINT chk_clientes_origem_lead CHECK (origem_lead IS NULL OR origem_lead IN (
        'GOOGLE_ADS', 'FACEBOOK_ADS', 'INSTAGRAM_ADS', 'INDICACAO',
        'GOOGLE_ORGANICO', 'REDES_SOCIAIS', 'WHATSAPP', 'BOCA_A_BOCA',
        'INFLUENCER', 'PARCEIRO', 'OUTRO'
    )),
    CONSTRAINT chk_clientes_metricas_positivas CHECK (
        total_compras_realizadas >= 0 AND
        total_vendas_realizadas >= 0 AND
        valor_total_comprado >= 0 AND
        valor_total_vendido >= 0
    )
);

-- Comentários na tabela e colunas críticas
COMMENT ON TABLE clientes IS 'Tabela pai para clientes (PF e PJ) com herança JOINED';
COMMENT ON COLUMN clientes.dtype IS 'Discriminador de herança: PF (Pessoa Física) ou PJ (Pessoa Jurídica)';
COMMENT ON COLUMN clientes.tipo_cliente IS 'Classificação do cliente: CONSIGNANTE (vende), COMPRADOR, AMBOS, PROSPECTO, PARCEIRO, INATIVO';
COMMENT ON COLUMN clientes.origem_lead IS 'Canal de aquisição do cliente (marketing)';
COMMENT ON COLUMN clientes.cliente_indicador_id IS 'FK para cliente que indicou este (programa de indicação)';
COMMENT ON COLUMN clientes.indicacao_recompensada IS 'Se o indicador já recebeu crédito pela indicação';
COMMENT ON COLUMN clientes.bloqueado IS 'Cliente bloqueado não pode realizar novas transações';
COMMENT ON COLUMN clientes.valor_total_comprado IS 'Valor total que o cliente gastou comprando na plataforma';
COMMENT ON COLUMN clientes.valor_total_vendido IS 'Valor total que o cliente recebeu vendendo na plataforma';
