-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Tabela: enderecos
-- ========================================

CREATE TABLE enderecos (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- FK para Cliente
    cliente_id BIGINT NOT NULL,

    -- Endereço completo
    cep VARCHAR(9) NOT NULL,
    logradouro VARCHAR(200) NOT NULL,
    numero VARCHAR(10),
    complemento VARCHAR(100),
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    estado VARCHAR(2) NOT NULL,
    pais VARCHAR(50) NOT NULL DEFAULT 'Brasil',

    -- Tipo de endereço
    tipo_endereco VARCHAR(20),

    -- Controle
    endereco_principal BOOLEAN NOT NULL DEFAULT false,

    -- Controle de ativação
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Auditoria
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_enderecos_estado CHECK (estado IN (
        'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA',
        'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN',
        'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
    )),
    CONSTRAINT chk_enderecos_tipo CHECK (tipo_endereco IS NULL OR tipo_endereco IN (
        'RESIDENCIAL', 'COMERCIAL', 'ENTREGA', 'COBRANCA', 'COLETA'
    ))
);

-- Comentários
COMMENT ON TABLE enderecos IS 'Endereços dos clientes (residencial, comercial, entrega, cobrança, coleta)';
COMMENT ON COLUMN enderecos.tipo_endereco IS 'Tipo: RESIDENCIAL, COMERCIAL, ENTREGA (receber compras), COBRANCA (boletos), COLETA (buscar itens)';
COMMENT ON COLUMN enderecos.endereco_principal IS 'Apenas UM endereço pode ser principal por tipo';
COMMENT ON COLUMN enderecos.cep IS 'CEP formatado XXXXX-XXX';
COMMENT ON COLUMN enderecos.estado IS 'Sigla do estado (UF)';
