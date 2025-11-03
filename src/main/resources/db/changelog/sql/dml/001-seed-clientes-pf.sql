-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Seed: 10 Clientes Pessoa Física
-- ========================================

-- Cliente PF 1: Ana Silva (Consignante - vai vender)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, utm_source, total_vendas_realizadas, valor_total_vendido, ativo, data_criacao, data_atualizacao)
VALUES (1, 'PF', 'ana.silva@email.com', 'CONSIGNANTE', 'GOOGLE_ADS', 'google', 2, 4500.00, true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, estado_civil, profissao, nacionalidade)
VALUES (1, 'Ana', 'Silva', '123.456.789-10', '1985-03-15', 'FEMININO', 'Casada', 'Arquiteta', 'Brasileira');


-- Cliente PF 2: João Santos (Comprador)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, total_compras_realizadas, valor_total_comprado, ativo, data_criacao, data_atualizacao)
VALUES (2, 'PF', 'joao.santos@email.com', 'COMPRADOR', 'INDICACAO', 3, 8200.00, true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, nome_do_meio, sobrenome, cpf, data_nascimento, sexo, estado_civil, profissao, nacionalidade)
VALUES (2, 'João', 'Carlos', 'Santos', '234.567.890-12', '1978-07-22', 'MASCULINO', 'Solteiro', 'Engenheiro', 'Brasileira');


-- Cliente PF 3: Maria Oliveira (Ambos - compra E vende)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, utm_campaign, total_compras_realizadas, total_vendas_realizadas, valor_total_comprado, valor_total_vendido, cliente_indicador_id, data_indicacao, indicacao_recompensada, ativo, data_criacao, data_atualizacao)
VALUES (3, 'PF', 'maria.oliveira@email.com', 'AMBOS', 'FACEBOOK_ADS', 'mudanca-2025', 5, 3, 12000.00, 6300.00, 1, NOW() - INTERVAL '30 days', true, true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, nome_mae, estado_civil, profissao, nacionalidade)
VALUES (3, 'Maria', 'Oliveira', '345.678.901-23', '1990-11-08', 'FEMININO', 'Rosa Oliveira', 'Divorciada', 'Designer', 'Brasileira');


-- Cliente PF 4: Pedro Costa (Prospecto - ainda não transacionou)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, ativo, data_criacao, data_atualizacao)
VALUES (4, 'PF', 'pedro.costa@email.com', 'PROSPECTO', 'WHATSAPP', true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, estado_civil, profissao, nacionalidade)
VALUES (4, 'Pedro', 'Costa', '456.789.012-34', '1995-05-20', 'MASCULINO', 'Solteiro', 'Desenvolvedor', 'Brasileira');


-- Cliente PF 5: Carla Mendes (Consignante - bloqueada)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, bloqueado, motivo_bloqueio, data_bloqueio, usuario_bloqueou, ativo, data_criacao, data_atualizacao)
VALUES (5, 'PF', 'carla.mendes@email.com', 'CONSIGNANTE', 'GOOGLE_ORGANICO', true, 'Documentos pendentes de verificação', NOW() - INTERVAL '5 days', 'admin@vanessa.com', true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, rg, data_nascimento, sexo, estado_civil, profissao, nacionalidade)
VALUES (5, 'Carla', 'Mendes', '567.890.123-45', 'MG-12.345.678', '1982-09-12', 'FEMININO', 'Casada', 'Médica', 'Brasileira');


-- Cliente PF 6: Lucas Ferreira (Comprador ativo)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, utm_source, utm_medium, total_compras_realizadas, valor_total_comprado, data_primeira_transacao, data_ultima_transacao, ativo, data_criacao, data_atualizacao)
VALUES (6, 'PF', 'lucas.ferreira@email.com', 'COMPRADOR', 'INSTAGRAM_ADS', 'instagram', 'cpc', 8, 15400.00, NOW() - INTERVAL '90 days', NOW() - INTERVAL '2 days', true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, nome_do_meio, sobrenome, cpf, data_nascimento, sexo, nome_pai, nome_mae, estado_civil, profissao, nacionalidade)
VALUES (6, 'Lucas', 'Eduardo', 'Ferreira', '678.901.234-56', '1988-12-30', 'MASCULINO', 'José Ferreira', 'Lucia Ferreira', 'Casado', 'Advogado', 'Brasileira');


-- Cliente PF 7: Juliana Rocha (Consignante nova)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, total_vendas_realizadas, valor_total_vendido, data_primeira_transacao, ativo, data_criacao, data_atualizacao)
VALUES (7, 'PF', 'juliana.rocha@email.com', 'CONSIGNANTE', 'BOCA_A_BOCA', 1, 2800.00, NOW() - INTERVAL '10 days', true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, estado_civil, profissao, nacionalidade, naturalidade)
VALUES (7, 'Juliana', 'Rocha', '789.012.345-67', '1992-04-18', 'FEMININO', 'Solteira', 'Professora', 'Brasileira', 'Belo Horizonte - MG');


-- Cliente PF 8: Roberto Alves (Ambos - cliente VIP)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, total_compras_realizadas, total_vendas_realizadas, valor_total_comprado, valor_total_vendido, data_primeira_transacao, data_ultima_transacao, ativo, data_criacao, data_atualizacao)
VALUES (8, 'PF', 'roberto.alves@email.com', 'AMBOS', 'PARCEIRO', 15, 8, 35000.00, 18500.00, NOW() - INTERVAL '180 days', NOW() - INTERVAL '1 day', true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, estado_civil, profissao, nacionalidade)
VALUES (8, 'Roberto', 'Alves', '890.123.456-78', '1975-08-05', 'MASCULINO', 'Casado', 'Empresário', 'Brasileira');


-- Cliente PF 9: Fernanda Lima (Prospecto com interesse)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, utm_source, utm_campaign, observacoes, ativo, data_criacao, data_atualizacao)
VALUES (9, 'PF', 'fernanda.lima@email.com', 'PROSPECTO', 'FACEBOOK_ADS', 'facebook', 'campanha-verao', 'Interessada em móveis planejados', true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, estado_civil, profissao, nacionalidade)
VALUES (9, 'Fernanda', 'Lima', '901.234.567-89', '1998-01-25', 'FEMININO', 'Solteira', 'Estudante', 'Brasileira');


-- Cliente PF 10: Ricardo Souza (Consignante indicado)
INSERT INTO clientes (id, dtype, email, tipo_cliente, origem_lead, cliente_indicador_id, data_indicacao, indicacao_recompensada, total_vendas_realizadas, valor_total_vendido, ativo, data_criacao, data_atualizacao)
VALUES (10, 'PF', 'ricardo.souza@email.com', 'CONSIGNANTE', 'INDICACAO', 8, NOW() - INTERVAL '15 days', false, 1, 3200.00, true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, estado_civil, profissao, nacionalidade)
VALUES (10, 'Ricardo', 'Souza', '012.345.678-90', '1987-06-14', 'MASCULINO', 'Casado', 'Contador', 'Brasileira');


-- Ajustar sequence para próximo ID
SELECT setval('clientes_id_seq', 10, true);
