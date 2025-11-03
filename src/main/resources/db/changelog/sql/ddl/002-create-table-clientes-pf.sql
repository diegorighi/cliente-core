-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Tabela: clientes_pf (Pessoa Física)
-- Herança JOINED de clientes
-- ========================================

CREATE TABLE clientes_pf (
    -- FK para tabela pai (herança JOINED)
    id BIGINT PRIMARY KEY,

    -- Nome completo
    primeiro_nome VARCHAR(100) NOT NULL,
    nome_do_meio VARCHAR(100),
    sobrenome VARCHAR(100) NOT NULL,

    -- Documentos principais
    cpf VARCHAR(14) UNIQUE,
    rg VARCHAR(20),

    -- Dados pessoais
    data_nascimento DATE,
    sexo VARCHAR(15),

    -- Filiação
    nome_mae VARCHAR(200),
    nome_pai VARCHAR(200),

    -- Outros dados
    estado_civil VARCHAR(30),
    profissao VARCHAR(100),
    nacionalidade VARCHAR(50) DEFAULT 'Brasileira',
    naturalidade VARCHAR(100),

    -- Constraints
    CONSTRAINT chk_clientes_pf_sexo CHECK (sexo IS NULL OR sexo IN (
        'MASCULINO', 'FEMININO', 'OUTRO', 'NAO_INFORMADO'
    )),
    CONSTRAINT chk_clientes_pf_data_nascimento CHECK (
        data_nascimento IS NULL OR data_nascimento <= CURRENT_DATE
    )
);

-- Comentários
COMMENT ON TABLE clientes_pf IS 'Clientes Pessoa Física (herda de clientes com JOINED)';
COMMENT ON COLUMN clientes_pf.id IS 'FK para clientes.id (chave primária compartilhada)';
COMMENT ON COLUMN clientes_pf.cpf IS 'CPF formatado XXX.XXX.XXX-XX (único no sistema)';
COMMENT ON COLUMN clientes_pf.primeiro_nome IS 'Primeiro nome do cliente';
COMMENT ON COLUMN clientes_pf.sobrenome IS 'Sobrenome do cliente';
COMMENT ON COLUMN clientes_pf.data_nascimento IS 'Data de nascimento (para cálculo de idade)';
