-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Tabela: preferencias_cliente (LGPD)
-- ========================================

CREATE TABLE preferencias_cliente (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- FK para Cliente (OneToOne)
    cliente_id BIGINT NOT NULL UNIQUE,

    -- Preferências de comunicação
    aceita_comunicacao_email BOOLEAN NOT NULL DEFAULT true,
    aceita_comunicacao_sms BOOLEAN NOT NULL DEFAULT true,
    aceita_comunicacao_whatsapp BOOLEAN NOT NULL DEFAULT true,
    aceita_comunicacao_telefone BOOLEAN NOT NULL DEFAULT false,

    -- Marketing
    aceita_newsletters BOOLEAN NOT NULL DEFAULT false,
    aceita_ofertas BOOLEAN NOT NULL DEFAULT true,
    aceita_pesquisas BOOLEAN NOT NULL DEFAULT false,

    -- Consentimento LGPD
    data_consentimento TIMESTAMP,
    ip_consentimento VARCHAR(50),
    consentimento_ativo BOOLEAN NOT NULL DEFAULT true,

    -- Auditoria
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Comentários
COMMENT ON TABLE preferencias_cliente IS 'Preferências de comunicação e consentimento LGPD (OneToOne com clientes)';
COMMENT ON COLUMN preferencias_cliente.cliente_id IS 'FK UNIQUE para clientes (relacionamento OneToOne)';
COMMENT ON COLUMN preferencias_cliente.aceita_comunicacao_email IS 'Cliente aceita receber emails transacionais e marketing';
COMMENT ON COLUMN preferencias_cliente.aceita_comunicacao_whatsapp IS 'Cliente aceita receber mensagens via WhatsApp';
COMMENT ON COLUMN preferencias_cliente.data_consentimento IS 'Quando o cliente aceitou os termos LGPD';
COMMENT ON COLUMN preferencias_cliente.ip_consentimento IS 'IP de onde o consentimento foi dado (auditoria LGPD)';
COMMENT ON COLUMN preferencias_cliente.consentimento_ativo IS 'Se o consentimento ainda está válido (pode ser revogado)';
