-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Seed: 5 Clientes Pessoa Jurídica
-- ========================================

-- Cliente PJ 1: Móveis Estrela LTDA (Consignante)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, total_vendas_realizadas, valor_total_vendido, data_primeira_transacao, ativo, data_criacao, data_atualizacao)
VALUES (11, 'PJ', 'contato@moveisestrela.com.br', 'CONSIGNANTE', 'PARCEIRO', 12, 85000.00, NOW() - INTERVAL '120 days', true, NOW(), NOW());

INSERT INTO clientes_pj (id, razao_social, nome_fantasia, cnpj, inscricao_estadual, data_abertura, porte_empresa, natureza_juridica, atividade_principal, capital_social, nome_responsavel, cpf_responsavel, cargo_responsavel, site)
VALUES (11, 'MÓVEIS ESTRELA COMÉRCIO LTDA', 'Móveis Estrela', '12.345.678/0001-90', '123.456.789.012', '2015-03-10', 'EPP', 'Sociedade Limitada', 'Comércio varejista de móveis', 100000.00, 'Carlos Alberto Silva', '123.456.789-10', 'Sócio Diretor', 'www.moveisestrela.com.br');


-- Cliente PJ 2: Tech Solutions S.A. (Comprador corporativo)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, utm_source, total_compras_realizadas, valor_total_comprado, data_primeira_transacao, data_ultima_transacao, ativo, data_criacao, data_atualizacao)
VALUES (12, 'PJ', 'compras@techsolutions.com.br', 'COMPRADOR', 'GOOGLE_ADS', 'google', 6, 45000.00, NOW() - INTERVAL '60 days', NOW() - INTERVAL '5 days', true, NOW(), NOW());

INSERT INTO clientes_pj (id, razao_social, nome_fantasia, cnpj, inscricao_estadual, data_abertura, porte_empresa, natureza_juridica, atividade_principal, capital_social, nome_responsavel, cpf_responsavel, cargo_responsavel, site)
VALUES (12, 'TECH SOLUTIONS TECNOLOGIA S.A.', 'Tech Solutions', '23.456.789/0001-01', '234.567.890.123', '2018-07-15', 'MEDIO', 'Sociedade Anônima', 'Desenvolvimento de software', 500000.00, 'Mariana Costa Santos', '234.567.890-12', 'Gerente de Compras', 'www.techsolutions.com.br');


-- Cliente PJ 3: Construtora Nova Era (Ambos - compra E vende)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, total_compras_realizadas, total_vendas_realizadas, valor_total_comprado, valor_total_vendido, ativo, data_criacao, data_atualizacao)
VALUES (13, 'PJ', 'financeiro@construtoraera.com.br', 'AMBOS', 'INDICACAO', 4, 8, 28000.00, 52000.00, true, NOW(), NOW());

INSERT INTO clientes_pj (id, razao_social, nome_fantasia, cnpj, inscricao_estadual, inscricao_municipal, data_abertura, porte_empresa, natureza_juridica, atividade_principal, capital_social, nome_responsavel, cpf_responsavel, cargo_responsavel)
VALUES (13, 'CONSTRUTORA NOVA ERA LTDA', 'Nova Era Construções', '34.567.890/0001-12', '345.678.901.234', '8765432', '2010-01-20', 'EPP', 'Sociedade Limitada', 'Construção de edifícios', 250000.00, 'José Roberto Oliveira', '345.678.901-23', 'Diretor Executivo');


-- Cliente PJ 4: Design Interiores MEI (Parceiro)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, observacoes, ativo, data_criacao, data_atualizacao)
VALUES (14, 'PJ', 'contato@designinteriores.com.br', 'PARCEIRO', 'BOCA_A_BOCA', 'Parceiro de design de interiores - pode indicar clientes', true, NOW(), NOW());

INSERT INTO clientes_pj (id, razao_social, nome_fantasia, cnpj, data_abertura, porte_empresa, natureza_juridica, atividade_principal, nome_responsavel, cpf_responsavel, cargo_responsavel, site)
VALUES (14, 'PATRICIA ROCHA DESIGN', 'Design Interiores', '45.678.901/0001-23', '2020-05-10', 'MEI', 'Empresário Individual', 'Design de interiores', 'Patrícia Rocha Lima', '456.789.012-34', 'Proprietária', 'www.designinteriores.com.br');


-- Cliente PJ 5: Hotel Boa Vista (Prospecto corporativo)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, utm_source, utm_campaign, observacoes, ativo, data_criacao, data_atualizacao)
VALUES (15, 'PJ', 'gerencia@hotelboavista.com.br', 'PROSPECTO', 'GOOGLE_ADS', 'google', 'campanha-hotelaria', 'Interessado em mobiliário para renovação de 50 quartos', true, NOW(), NOW());

INSERT INTO clientes_pj (id, razao_social, nome_fantasia, cnpj, inscricao_estadual, inscricao_municipal, data_abertura, porte_empresa, natureza_juridica, atividade_principal, capital_social, nome_responsavel, cpf_responsavel, cargo_responsavel, site)
VALUES (15, 'HOTEL BOA VISTA TURISMO LTDA', 'Hotel Boa Vista', '56.789.012/0001-34', '456.789.012.345', '7654321', '2005-11-08', 'MEDIO', 'Sociedade Limitada', 'Hotelaria', 800000.00, 'Fernando Alves Mendes', '567.890.123-45', 'Gerente Geral', 'www.hotelboavista.com.br');


-- Ajustar sequence para próximo ID
SELECT setval('clientes_id_seq', 15, true);
