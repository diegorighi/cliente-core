-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Tabela: clientes_pj (Pessoa Jurídica)
-- Herança JOINED de clientes
-- ========================================

CREATE TABLE clientes_pj (
    -- FK para tabela pai (herança JOINED)
    id BIGINT PRIMARY KEY,

    -- Identificação da empresa
    razao_social VARCHAR(200) NOT NULL,
    nome_fantasia VARCHAR(200),
    cnpj VARCHAR(18) NOT NULL UNIQUE,

    -- Inscrições
    inscricao_estadual VARCHAR(20),
    inscricao_municipal VARCHAR(20),

    -- Dados da empresa
    data_abertura DATE,
    porte_empresa VARCHAR(50),
    natureza_juridica VARCHAR(100),
    atividade_principal VARCHAR(200),
    capital_social NUMERIC(15,2),

    -- Responsável legal
    nome_responsavel VARCHAR(200),
    cpf_responsavel VARCHAR(14),
    cargo_responsavel VARCHAR(100),

    -- Contato online
    site VARCHAR(200),

    -- Constraints
    CONSTRAINT chk_clientes_pj_porte CHECK (porte_empresa IS NULL OR porte_empresa IN (
        'MEI', 'ME', 'EPP', 'MEDIO', 'GRANDE'
    )),
    CONSTRAINT chk_clientes_pj_capital_positivo CHECK (
        capital_social IS NULL OR capital_social >= 0
    ),
    CONSTRAINT chk_clientes_pj_data_abertura CHECK (
        data_abertura IS NULL OR data_abertura <= CURRENT_DATE
    )
);

-- Comentários
COMMENT ON TABLE clientes_pj IS 'Clientes Pessoa Jurídica (herda de clientes com JOINED)';
COMMENT ON COLUMN clientes_pj.id IS 'FK para clientes.id (chave primária compartilhada)';
COMMENT ON COLUMN clientes_pj.cnpj IS 'CNPJ formatado XX.XXX.XXX/XXXX-XX (único no sistema)';
COMMENT ON COLUMN clientes_pj.razao_social IS 'Razão social oficial da empresa';
COMMENT ON COLUMN clientes_pj.nome_fantasia IS 'Nome fantasia (usado em exibições)';
COMMENT ON COLUMN clientes_pj.porte_empresa IS 'Porte: MEI, ME (Microempresa), EPP (Pequeno Porte), MEDIO, GRANDE';
COMMENT ON COLUMN clientes_pj.cpf_responsavel IS 'CPF do representante legal da empresa';
