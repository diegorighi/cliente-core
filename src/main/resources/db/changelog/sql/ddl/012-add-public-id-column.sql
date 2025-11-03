-- liquibase formatted sql
-- changeset vanessa:012-add-public-id-column context:ddl-only,dev

-- Adiciona coluna public_id do tipo UUID na tabela clientes
-- Essa coluna será usada como identificador público para expor nas APIs,
-- evitando expor o ID sequencial interno do banco de dados
ALTER TABLE clientes
    ADD COLUMN public_id UUID;

-- Gera UUIDs para registros existentes
UPDATE clientes
SET public_id = gen_random_uuid()
WHERE public_id IS NULL;

-- Torna a coluna NOT NULL após popular os valores existentes
ALTER TABLE clientes
    ALTER COLUMN public_id SET NOT NULL;

-- Adiciona constraint de unicidade
ALTER TABLE clientes
    ADD CONSTRAINT uk_clientes_public_id UNIQUE (public_id);

-- Cria índice otimizado para buscas por public_id
-- Usando HASH index pois UUIDs não precisam de ordenação
CREATE INDEX idx_clientes_public_id ON clientes USING HASH (public_id);

-- Comentários para documentação
COMMENT ON COLUMN clientes.public_id IS 'Identificador público UUID usado nas APIs externas para evitar exposição de IDs sequenciais';
COMMENT ON INDEX idx_clientes_public_id IS 'Índice HASH para buscas rápidas por UUID (não requer ordenação)';
