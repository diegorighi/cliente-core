-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Tabela: auditoria_cliente
-- ========================================

CREATE TABLE auditoria_cliente (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- FK para Cliente
    cliente_id BIGINT NOT NULL,

    -- Dados da alteração
    campo_alterado VARCHAR(100) NOT NULL,
    valor_anterior VARCHAR(500),
    valor_novo VARCHAR(500),

    -- Rastreamento
    usuario_responsavel VARCHAR(100),
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo_alteracao VARCHAR(500),
    ip_origem VARCHAR(50),

    -- Auditoria (append-only, sem data_atualizacao)
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Comentários
COMMENT ON TABLE auditoria_cliente IS 'Histórico de alterações nos clientes (append-only, compliance e fraude)';
COMMENT ON COLUMN auditoria_cliente.campo_alterado IS 'Nome do campo que foi modificado (ex: cpf, email, dados_bancarios)';
COMMENT ON COLUMN auditoria_cliente.valor_anterior IS 'Valor antes da alteração';
COMMENT ON COLUMN auditoria_cliente.valor_novo IS 'Valor após a alteração';
COMMENT ON COLUMN auditoria_cliente.usuario_responsavel IS 'Usuário que realizou a alteração (email ou ID)';
COMMENT ON COLUMN auditoria_cliente.ip_origem IS 'IP de onde a alteração foi feita (segurança)';
COMMENT ON COLUMN auditoria_cliente.data_alteracao IS 'Timestamp exato da alteração';
