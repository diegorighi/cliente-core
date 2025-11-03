-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Foreign Keys e Constraints
-- ========================================

-- ====================
-- FOREIGN KEYS: clientes_pf
-- ====================

-- FK para tabela pai (herança JOINED)
ALTER TABLE clientes_pf
ADD CONSTRAINT fk_clientes_pf_cliente
FOREIGN KEY (id) REFERENCES clientes(id)
ON DELETE CASCADE
ON UPDATE CASCADE;


-- ====================
-- FOREIGN KEYS: clientes_pj
-- ====================

-- FK para tabela pai (herança JOINED)
ALTER TABLE clientes_pj
ADD CONSTRAINT fk_clientes_pj_cliente
FOREIGN KEY (id) REFERENCES clientes(id)
ON DELETE CASCADE
ON UPDATE CASCADE;


-- ====================
-- FOREIGN KEYS: clientes (auto-referência)
-- ====================

-- FK para programa de indicação (cliente que indicou)
ALTER TABLE clientes
ADD CONSTRAINT fk_clientes_indicador
FOREIGN KEY (cliente_indicador_id) REFERENCES clientes(id)
ON DELETE SET NULL
ON UPDATE CASCADE;


-- ====================
-- FOREIGN KEYS: documentos
-- ====================

ALTER TABLE documentos
ADD CONSTRAINT fk_documentos_cliente
FOREIGN KEY (cliente_id) REFERENCES clientes(id)
ON DELETE CASCADE
ON UPDATE CASCADE;


-- ====================
-- FOREIGN KEYS: contatos
-- ====================

ALTER TABLE contatos
ADD CONSTRAINT fk_contatos_cliente
FOREIGN KEY (cliente_id) REFERENCES clientes(id)
ON DELETE CASCADE
ON UPDATE CASCADE;


-- ====================
-- FOREIGN KEYS: enderecos
-- ====================

ALTER TABLE enderecos
ADD CONSTRAINT fk_enderecos_cliente
FOREIGN KEY (cliente_id) REFERENCES clientes(id)
ON DELETE CASCADE
ON UPDATE CASCADE;


-- ====================
-- FOREIGN KEYS: dados_bancarios
-- ====================

ALTER TABLE dados_bancarios
ADD CONSTRAINT fk_dados_bancarios_cliente
FOREIGN KEY (cliente_id) REFERENCES clientes(id)
ON DELETE CASCADE
ON UPDATE CASCADE;


-- ====================
-- FOREIGN KEYS: preferencias_cliente
-- ====================

ALTER TABLE preferencias_cliente
ADD CONSTRAINT fk_preferencias_cliente
FOREIGN KEY (cliente_id) REFERENCES clientes(id)
ON DELETE CASCADE
ON UPDATE CASCADE;


-- ====================
-- FOREIGN KEYS: auditoria_cliente
-- ====================

ALTER TABLE auditoria_cliente
ADD CONSTRAINT fk_auditoria_cliente
FOREIGN KEY (cliente_id) REFERENCES clientes(id)
ON DELETE CASCADE
ON UPDATE CASCADE;


-- ====================
-- UNIQUE CONSTRAINTS
-- ====================

-- Garantir que apenas UM documento pode ser principal por cliente
-- (Não criamos constraint aqui pois seria complexo - validação será feita na aplicação)

-- Garantir que apenas UM contato pode ser principal por cliente
-- (Não criamos constraint aqui pois seria complexo - validação será feita na aplicação)

-- Garantir que apenas UM endereço pode ser principal por tipo por cliente
-- (Não criamos constraint aqui pois seria complexo - validação será feita na aplicação)

-- Garantir que apenas UMA conta bancária pode ser principal por cliente
-- (Não criamos constraint aqui pois seria complexo - validação será feita na aplicação)


-- ====================
-- COMENTÁRIOS
-- ====================

COMMENT ON CONSTRAINT fk_clientes_pf_cliente ON clientes_pf IS 'FK para clientes (herança JOINED) - CASCADE deleta PF se cliente pai for deletado';
COMMENT ON CONSTRAINT fk_clientes_pj_cliente ON clientes_pj IS 'FK para clientes (herança JOINED) - CASCADE deleta PJ se cliente pai for deletado';
COMMENT ON CONSTRAINT fk_clientes_indicador ON clientes IS 'FK auto-referência para programa de indicação - SET NULL se indicador for deletado';
COMMENT ON CONSTRAINT fk_documentos_cliente ON documentos IS 'FK para clientes - CASCADE deleta documentos se cliente for deletado';
COMMENT ON CONSTRAINT fk_contatos_cliente ON contatos IS 'FK para clientes - CASCADE deleta contatos se cliente for deletado';
COMMENT ON CONSTRAINT fk_enderecos_cliente ON enderecos IS 'FK para clientes - CASCADE deleta endereços se cliente for deletado';
COMMENT ON CONSTRAINT fk_dados_bancarios_cliente ON dados_bancarios IS 'FK para clientes - CASCADE deleta dados bancários se cliente for deletado';
COMMENT ON CONSTRAINT fk_preferencias_cliente ON preferencias_cliente IS 'FK para clientes (OneToOne) - CASCADE deleta preferências se cliente for deletado';
COMMENT ON CONSTRAINT fk_auditoria_cliente ON auditoria_cliente IS 'FK para clientes - CASCADE deleta auditoria se cliente for deletado';
