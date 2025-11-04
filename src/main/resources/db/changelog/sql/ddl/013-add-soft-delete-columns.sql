-- ==============================================================================
-- Liquibase Changeset: Adicionar colunas de Soft Delete na tabela clientes
-- ==============================================================================
-- Author: Tech Lead
-- Date: 2025-11-03
-- Description: Adiciona campos data_delecao, motivo_delecao e usuario_deletou
--              para implementar soft delete pattern
-- ==============================================================================

-- changeset tech-lead:012-add-soft-delete-columns

-- Adicionar coluna data_delecao
ALTER TABLE clientes
ADD COLUMN data_delecao TIMESTAMP NULL;

-- Adicionar coluna motivo_delecao
ALTER TABLE clientes
ADD COLUMN motivo_delecao VARCHAR(500) NULL;

-- Adicionar coluna usuario_deletou
ALTER TABLE clientes
ADD COLUMN usuario_deletou VARCHAR(100) NULL;

-- Comentários nas colunas
COMMENT ON COLUMN clientes.data_delecao IS 'Data e hora em que o cliente foi deletado (soft delete)';
COMMENT ON COLUMN clientes.motivo_delecao IS 'Motivo da deleção do cliente';
COMMENT ON COLUMN clientes.usuario_deletou IS 'Usuário que realizou a deleção';

-- Criar índice para buscar clientes deletados
CREATE INDEX idx_clientes_data_delecao ON clientes(data_delecao) WHERE data_delecao IS NOT NULL;

-- Criar índice composto para buscar clientes ativos (não deletados)
CREATE INDEX idx_clientes_ativo_data_delecao ON clientes(ativo, data_delecao) WHERE ativo = true AND data_delecao IS NULL;

-- rollback ALTER TABLE clientes DROP COLUMN data_delecao;
-- rollback ALTER TABLE clientes DROP COLUMN motivo_delecao;
-- rollback ALTER TABLE clientes DROP COLUMN usuario_deletou;
-- rollback DROP INDEX IF EXISTS idx_clientes_data_delecao;
-- rollback DROP INDEX IF EXISTS idx_clientes_ativo_data_delecao;
