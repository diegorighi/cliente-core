-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Script de Verificação do Banco de Dados
-- ========================================

-- Execute este script após rodar o Liquibase para validar
-- que toda a estrutura foi criada corretamente.

\echo '========================================'
\echo 'VERIFICAÇÃO DA ESTRUTURA DO BANCO'
\echo '========================================'
\echo ''

-- 1. Verificar tabelas criadas
\echo '1. TABELAS CRIADAS:'
SELECT
    table_name,
    CASE
        WHEN table_name = 'clientes' THEN '✓ Tabela pai (herança JOINED)'
        WHEN table_name = 'clientes_pf' THEN '✓ Pessoa Física (herança)'
        WHEN table_name = 'clientes_pj' THEN '✓ Pessoa Jurídica (herança)'
        WHEN table_name = 'documentos' THEN '✓ Documentos dos clientes'
        WHEN table_name = 'contatos' THEN '✓ Contatos (telefone, email, WhatsApp)'
        WHEN table_name = 'enderecos' THEN '✓ Endereços completos'
        WHEN table_name = 'dados_bancarios' THEN '✓ Dados bancários e PIX'
        WHEN table_name = 'preferencias_cliente' THEN '✓ Preferências LGPD'
        WHEN table_name = 'auditoria_cliente' THEN '✓ Histórico de alterações'
        ELSE 'Tabela não esperada'
    END AS descricao
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
  AND table_name NOT LIKE 'databasechange%'
ORDER BY
    CASE table_name
        WHEN 'clientes' THEN 1
        WHEN 'clientes_pf' THEN 2
        WHEN 'clientes_pj' THEN 3
        WHEN 'documentos' THEN 4
        WHEN 'contatos' THEN 5
        WHEN 'enderecos' THEN 6
        WHEN 'dados_bancarios' THEN 7
        WHEN 'preferencias_cliente' THEN 8
        WHEN 'auditoria_cliente' THEN 9
    END;

\echo ''
\echo '2. CONTAGEM DE COLUNAS POR TABELA:'
SELECT
    table_name,
    COUNT(*) AS total_colunas
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name IN ('clientes', 'clientes_pf', 'clientes_pj', 'documentos', 'contatos', 'enderecos', 'dados_bancarios', 'preferencias_cliente', 'auditoria_cliente')
GROUP BY table_name
ORDER BY
    CASE table_name
        WHEN 'clientes' THEN 1
        WHEN 'clientes_pf' THEN 2
        WHEN 'clientes_pj' THEN 3
        WHEN 'documentos' THEN 4
        WHEN 'contatos' THEN 5
        WHEN 'enderecos' THEN 6
        WHEN 'dados_bancarios' THEN 7
        WHEN 'preferencias_cliente' THEN 8
        WHEN 'auditoria_cliente' THEN 9
    END;

\echo ''
\echo '3. ÍNDICES CRIADOS:'
SELECT
    tablename AS tabela,
    indexname AS indice,
    CASE
        WHEN indexdef LIKE '%UNIQUE%' THEN 'UNIQUE'
        WHEN indexdef LIKE '%gin%' THEN 'GIN (full-text)'
        WHEN indexdef LIKE '%WHERE%' THEN 'PARCIAL'
        ELSE 'BTREE'
    END AS tipo
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename IN ('clientes', 'clientes_pf', 'clientes_pj', 'documentos', 'contatos', 'enderecos', 'dados_bancarios', 'preferencias_cliente', 'auditoria_cliente')
ORDER BY tablename, indexname;

\echo ''
\echo '4. FOREIGN KEYS (INTEGRIDADE REFERENCIAL):'
SELECT
    tc.table_name AS tabela_origem,
    kcu.column_name AS coluna,
    ccu.table_name AS tabela_destino,
    rc.update_rule AS on_update,
    rc.delete_rule AS on_delete
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
  AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
  AND ccu.table_schema = tc.table_schema
JOIN information_schema.referential_constraints AS rc
  ON rc.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = 'public'
ORDER BY tc.table_name;

\echo ''
\echo '5. CONSTRAINTS (CHECK, UNIQUE):'
SELECT
    tc.table_name AS tabela,
    tc.constraint_name AS constraint,
    tc.constraint_type AS tipo,
    cc.check_clause AS condicao
FROM information_schema.table_constraints AS tc
LEFT JOIN information_schema.check_constraints AS cc
  ON tc.constraint_name = cc.constraint_name
WHERE tc.table_schema = 'public'
  AND tc.constraint_type IN ('CHECK', 'UNIQUE')
  AND tc.table_name IN ('clientes', 'clientes_pf', 'clientes_pj', 'documentos', 'contatos', 'enderecos', 'dados_bancarios', 'preferencias_cliente', 'auditoria_cliente')
ORDER BY tc.table_name, tc.constraint_type, tc.constraint_name;

\echo ''
\echo '6. DADOS (SEEDS) INSERIDOS:'
SELECT
    'Clientes PF' AS tipo,
    COUNT(*) AS quantidade
FROM clientes WHERE dtype = 'PF'
UNION ALL
SELECT
    'Clientes PJ' AS tipo,
    COUNT(*) AS quantidade
FROM clientes WHERE dtype = 'PJ'
UNION ALL
SELECT
    'Documentos' AS tipo,
    COUNT(*) AS quantidade
FROM documentos
UNION ALL
SELECT
    'Contatos' AS tipo,
    COUNT(*) AS quantidade
FROM contatos
UNION ALL
SELECT
    'Endereços' AS tipo,
    COUNT(*) AS quantidade
FROM enderecos
UNION ALL
SELECT
    'Dados Bancários' AS tipo,
    COUNT(*) AS quantidade
FROM dados_bancarios
UNION ALL
SELECT
    'Preferências LGPD' AS tipo,
    COUNT(*) AS quantidade
FROM preferencias_cliente
UNION ALL
SELECT
    'Registros de Auditoria' AS tipo,
    COUNT(*) AS quantidade
FROM auditoria_cliente;

\echo ''
\echo '7. CLIENTES POR TIPO:'
SELECT
    tipo_cliente,
    COUNT(*) AS quantidade
FROM clientes
GROUP BY tipo_cliente
ORDER BY quantidade DESC;

\echo ''
\echo '8. DOCUMENTOS POR STATUS:'
SELECT
    status_documento,
    COUNT(*) AS quantidade
FROM documentos
GROUP BY status_documento
ORDER BY quantidade DESC;

\echo ''
\echo '9. CONTATOS VERIFICADOS:'
SELECT
    tipo_contato,
    SUM(CASE WHEN verificado THEN 1 ELSE 0 END) AS verificados,
    SUM(CASE WHEN NOT verificado THEN 1 ELSE 0 END) AS nao_verificados,
    COUNT(*) AS total
FROM contatos
GROUP BY tipo_contato
ORDER BY total DESC;

\echo ''
\echo '10. ENDEREÇOS POR TIPO:'
SELECT
    tipo_endereco,
    COUNT(*) AS quantidade
FROM enderecos
WHERE tipo_endereco IS NOT NULL
GROUP BY tipo_endereco
ORDER BY quantidade DESC;

\echo ''
\echo '11. DADOS BANCÁRIOS POR TIPO DE CHAVE PIX:'
SELECT
    tipo_chave_pix,
    COUNT(*) AS quantidade
FROM dados_bancarios
WHERE tipo_chave_pix IS NOT NULL
GROUP BY tipo_chave_pix
ORDER BY quantidade DESC;

\echo ''
\echo '12. CONSENTIMENTO LGPD ATIVO:'
SELECT
    CASE WHEN consentimento_ativo THEN 'Ativo' ELSE 'Inativo' END AS status,
    COUNT(*) AS quantidade
FROM preferencias_cliente
GROUP BY consentimento_ativo;

\echo ''
\echo '13. CHANGESETS LIQUIBASE EXECUTADOS:'
SELECT
    id AS changeset_id,
    author,
    filename,
    dateexecuted,
    orderexecuted AS ordem
FROM databasechangelog
ORDER BY orderexecuted;

\echo ''
\echo '14. ESPAÇO OCUPADO POR TABELA:'
SELECT
    schemaname AS schema,
    tablename AS tabela,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS tamanho_total,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) AS tamanho_dados,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) - pg_relation_size(schemaname||'.'||tablename)) AS tamanho_indices
FROM pg_tables
WHERE schemaname = 'public'
  AND tablename IN ('clientes', 'clientes_pf', 'clientes_pj', 'documentos', 'contatos', 'enderecos', 'dados_bancarios', 'preferencias_cliente', 'auditoria_cliente')
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

\echo ''
\echo '========================================'
\echo 'VERIFICAÇÃO CONCLUÍDA!'
\echo '========================================'
\echo ''
\echo 'Se todos os números batem com o esperado:'
\echo '- 9 tabelas (clientes, clientes_pf, clientes_pj, documentos, contatos, enderecos, dados_bancarios, preferencias_cliente, auditoria_cliente)'
\echo '- 10 Clientes PF'
\echo '- 5 Clientes PJ'
\echo '- ~35 Documentos'
\echo '- ~45 Contatos'
\echo '- ~25 Endereços'
\echo '- ~15 Dados Bancários'
\echo '- 15 Preferências LGPD'
\echo '- ~15 Registros de Auditoria'
\echo ''
\echo 'Então o Liquibase foi executado com SUCESSO! ✅'
\echo ''
