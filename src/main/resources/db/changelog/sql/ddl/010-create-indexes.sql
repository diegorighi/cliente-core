-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Índices Otimizados para PostgreSQL RDS
-- ========================================

-- ====================
-- TABELA: clientes
-- ====================

-- Índice único em email (busca frequente)
CREATE UNIQUE INDEX idx_clientes_email ON clientes(email) WHERE ativo = true;

-- Índice em dtype (discriminador de herança)
CREATE INDEX idx_clientes_dtype ON clientes(dtype);

-- Índice em tipo_cliente (filtros de negócio: CONSIGNANTE, COMPRADOR, etc)
CREATE INDEX idx_clientes_tipo_cliente ON clientes(tipo_cliente);

-- Índice em origem_lead (análise de marketing)
CREATE INDEX idx_clientes_origem_lead ON clientes(origem_lead);

-- Índice em bloqueado (filtrar clientes ativos/bloqueados)
CREATE INDEX idx_clientes_bloqueado ON clientes(bloqueado) WHERE bloqueado = true;

-- Índice composto para programa de indicação
CREATE INDEX idx_clientes_indicador ON clientes(cliente_indicador_id, indicacao_recompensada);

-- Índice em data_criacao (ordenação por data de cadastro)
CREATE INDEX idx_clientes_data_criacao ON clientes(data_criacao DESC);

-- Índice em ativo (soft delete)
CREATE INDEX idx_clientes_ativo ON clientes(ativo);

-- Índice composto para relatórios de vendas/compras
CREATE INDEX idx_clientes_metricas ON clientes(total_vendas_realizadas, total_compras_realizadas) WHERE ativo = true;


-- ====================
-- TABELA: clientes_pf
-- ====================

-- Índice ÚNICO em CPF (crítico para performance)
-- Já definido como UNIQUE na tabela, mas explicitado aqui
-- CREATE UNIQUE INDEX idx_clientes_pf_cpf ON clientes_pf(cpf);

-- Índice em data_nascimento (para queries por faixa etária)
CREATE INDEX idx_clientes_pf_data_nascimento ON clientes_pf(data_nascimento);

-- Índice em sexo (para segmentação)
CREATE INDEX idx_clientes_pf_sexo ON clientes_pf(sexo);

-- Índice para busca por nome completo (GIN para ILIKE/full-text)
CREATE INDEX idx_clientes_pf_nome_completo ON clientes_pf
USING GIN (to_tsvector('portuguese',
    COALESCE(primeiro_nome, '') || ' ' ||
    COALESCE(nome_do_meio, '') || ' ' ||
    COALESCE(sobrenome, '')
));


-- ====================
-- TABELA: clientes_pj
-- ====================

-- Índice ÚNICO em CNPJ (crítico para performance)
-- Já definido como UNIQUE na tabela, mas explicitado aqui
-- CREATE UNIQUE INDEX idx_clientes_pj_cnpj ON clientes_pj(cnpj);

-- Índice em porte_empresa (para segmentação)
CREATE INDEX idx_clientes_pj_porte ON clientes_pj(porte_empresa);

-- Índice para busca por razão social (GIN para ILIKE/full-text)
CREATE INDEX idx_clientes_pj_razao_social ON clientes_pj
USING GIN (to_tsvector('portuguese',
    COALESCE(razao_social, '') || ' ' ||
    COALESCE(nome_fantasia, '')
));


-- ====================
-- TABELA: documentos
-- ====================

-- Índice composto em cliente_id + ativo (buscar documentos ativos de um cliente)
CREATE INDEX idx_documentos_cliente_ativo ON documentos(cliente_id, ativo);

-- Índice em tipo_documento (filtrar por tipo: CPF, RG, CNH, etc)
CREATE INDEX idx_documentos_tipo ON documentos(tipo_documento);

-- Índice em status_documento (filtrar por status: VALIDO, EXPIRADO, etc)
CREATE INDEX idx_documentos_status ON documentos(status_documento);

-- Índice em numero (buscar por número do documento)
CREATE INDEX idx_documentos_numero ON documentos(numero);

-- Índice em data_validade (para identificar documentos próximos de vencer)
CREATE INDEX idx_documentos_data_validade ON documentos(data_validade)
WHERE data_validade IS NOT NULL AND ativo = true;

-- Índice em documento_principal (buscar documento principal do cliente)
CREATE INDEX idx_documentos_principal ON documentos(cliente_id, documento_principal)
WHERE documento_principal = true AND ativo = true;


-- ====================
-- TABELA: contatos
-- ====================

-- Índice composto em cliente_id + ativo (buscar contatos ativos de um cliente)
CREATE INDEX idx_contatos_cliente_ativo ON contatos(cliente_id, ativo);

-- Índice em tipo_contato (filtrar por tipo: CELULAR, EMAIL, WHATSAPP, etc)
CREATE INDEX idx_contatos_tipo ON contatos(tipo_contato);

-- Índice em valor (buscar por telefone ou email específico)
CREATE INDEX idx_contatos_valor ON contatos(valor);

-- Índice em contato_principal (buscar contato principal do cliente)
CREATE INDEX idx_contatos_principal ON contatos(cliente_id, contato_principal)
WHERE contato_principal = true AND ativo = true;

-- Índice em verificado (filtrar contatos verificados)
CREATE INDEX idx_contatos_verificado ON contatos(verificado) WHERE verificado = true;


-- ====================
-- TABELA: enderecos
-- ====================

-- Índice composto em cliente_id + ativo (buscar endereços ativos de um cliente)
CREATE INDEX idx_enderecos_cliente_ativo ON enderecos(cliente_id, ativo);

-- Índice em CEP (buscar por CEP - útil para logística)
CREATE INDEX idx_enderecos_cep ON enderecos(cep);

-- Índice em tipo_endereco (filtrar por tipo: RESIDENCIAL, ENTREGA, COLETA, etc)
CREATE INDEX idx_enderecos_tipo ON enderecos(tipo_endereco);

-- Índice em cidade + estado (queries regionais)
CREATE INDEX idx_enderecos_cidade_estado ON enderecos(cidade, estado);

-- Índice em endereco_principal (buscar endereço principal do cliente)
CREATE INDEX idx_enderecos_principal ON enderecos(cliente_id, endereco_principal)
WHERE endereco_principal = true AND ativo = true;


-- ====================
-- TABELA: dados_bancarios
-- ====================

-- Índice composto em cliente_id + ativo (buscar dados bancários ativos de um cliente)
CREATE INDEX idx_dados_bancarios_cliente_ativo ON dados_bancarios(cliente_id, ativo);

-- Índice em conta_principal (buscar conta principal do cliente)
CREATE INDEX idx_dados_bancarios_principal ON dados_bancarios(cliente_id, conta_principal)
WHERE conta_principal = true AND ativo = true;

-- Índice em dados_verificados (filtrar contas verificadas)
CREATE INDEX idx_dados_bancarios_verificados ON dados_bancarios(dados_verificados)
WHERE dados_verificados = true;

-- Índice em tipo_chave_pix (buscar por tipo de chave PIX)
CREATE INDEX idx_dados_bancarios_tipo_pix ON dados_bancarios(tipo_chave_pix);

-- Índice em chave_pix (buscar por chave PIX específica)
CREATE INDEX idx_dados_bancarios_chave_pix ON dados_bancarios(chave_pix)
WHERE chave_pix IS NOT NULL;


-- ====================
-- TABELA: preferencias_cliente
-- ====================

-- Índice em cliente_id (já é UNIQUE, mas explicitado para clareza)
-- CREATE UNIQUE INDEX idx_preferencias_cliente_id ON preferencias_cliente(cliente_id);

-- Índice em consentimento_ativo (filtrar clientes com consentimento ativo)
CREATE INDEX idx_preferencias_consentimento ON preferencias_cliente(consentimento_ativo);

-- Índice em data_consentimento (para auditoria LGPD)
CREATE INDEX idx_preferencias_data_consentimento ON preferencias_cliente(data_consentimento DESC);


-- ====================
-- TABELA: auditoria_cliente
-- ====================

-- Índice composto em cliente_id + data_alteracao (buscar histórico de um cliente)
CREATE INDEX idx_auditoria_cliente_data ON auditoria_cliente(cliente_id, data_alteracao DESC);

-- Índice em campo_alterado (buscar alterações de um campo específico)
CREATE INDEX idx_auditoria_campo ON auditoria_cliente(campo_alterado);

-- Índice em usuario_responsavel (rastrear alterações por usuário)
CREATE INDEX idx_auditoria_usuario ON auditoria_cliente(usuario_responsavel);

-- Índice em data_alteracao (ordenação cronológica de auditoria)
CREATE INDEX idx_auditoria_data_alteracao ON auditoria_cliente(data_alteracao DESC);


-- ====================
-- COMENTÁRIOS
-- ====================

COMMENT ON INDEX idx_clientes_email IS 'Índice único em email para busca rápida (apenas clientes ativos)';
COMMENT ON INDEX idx_clientes_tipo_cliente IS 'Índice para filtrar por tipo: CONSIGNANTE, COMPRADOR, AMBOS, etc';
COMMENT ON INDEX idx_clientes_bloqueado IS 'Índice parcial para clientes bloqueados (otimização)';
COMMENT ON INDEX idx_clientes_pf_nome_completo IS 'Índice GIN full-text para busca por nome (português)';
COMMENT ON INDEX idx_clientes_pj_razao_social IS 'Índice GIN full-text para busca por razão social/fantasia (português)';
COMMENT ON INDEX idx_documentos_data_validade IS 'Índice parcial para documentos com validade (identificar expirações)';
COMMENT ON INDEX idx_enderecos_cep IS 'Índice em CEP para queries de logística e agrupamento regional';
COMMENT ON INDEX idx_auditoria_cliente_data IS 'Índice composto para histórico cronológico de alterações por cliente';
