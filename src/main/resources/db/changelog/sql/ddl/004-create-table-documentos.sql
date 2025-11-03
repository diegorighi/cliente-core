-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Tabela: documentos
-- ========================================

CREATE TABLE documentos (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- FK para Cliente
    cliente_id BIGINT NOT NULL,

    -- Tipo de documento
    tipo_documento VARCHAR(30) NOT NULL,

    -- Dados do documento
    numero VARCHAR(50) NOT NULL,
    orgao_emissor VARCHAR(50),
    data_emissao DATE,
    data_validade DATE,

    -- Status e controle
    status_documento VARCHAR(30) NOT NULL DEFAULT 'AGUARDANDO_VERIFICACAO',
    documento_principal BOOLEAN NOT NULL DEFAULT false,

    -- Observações
    observacoes VARCHAR(500),

    -- Controle de ativação
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Auditoria
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_documentos_tipo CHECK (tipo_documento IN (
        'CPF', 'RG', 'CNH', 'PASSAPORTE', 'CNPJ',
        'INSCRICAO_ESTADUAL', 'INSCRICAO_MUNICIPAL',
        'CERTIDAO_NASCIMENTO', 'TITULO_ELEITOR', 'CARTEIRA_TRABALHO', 'OUTRO'
    )),
    CONSTRAINT chk_documentos_status CHECK (status_documento IN (
        'VALIDO', 'EXPIRADO', 'AGUARDANDO_VERIFICACAO', 'VERIFICADO', 'REJEITADO'
    )),
    CONSTRAINT chk_documentos_data_validade CHECK (
        data_validade IS NULL OR
        data_emissao IS NULL OR
        data_validade >= data_emissao
    )
);

-- Comentários
COMMENT ON TABLE documentos IS 'Documentos associados aos clientes (CPF, RG, CNH, CNPJ, etc)';
COMMENT ON COLUMN documentos.tipo_documento IS 'Tipo: CPF, RG, CNH, PASSAPORTE, CNPJ, etc';
COMMENT ON COLUMN documentos.status_documento IS 'Status de validação: VALIDO, EXPIRADO, AGUARDANDO_VERIFICACAO, VERIFICADO, REJEITADO';
COMMENT ON COLUMN documentos.documento_principal IS 'Apenas UM documento pode ser principal por cliente';
COMMENT ON COLUMN documentos.data_validade IS 'Data de validade (para CNH, Passaporte, etc). Trigger atualiza status_documento quando expirar';
