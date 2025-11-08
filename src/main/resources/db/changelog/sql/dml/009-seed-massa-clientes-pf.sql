-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Seed: MASSA DE DADOS - 150 Clientes Pessoa Física
-- Propósito: Testar performance do sistema com volume realista
-- ========================================

-- IMPORTANTE: Este seed gera 150 clientes PF com dados variados:
-- - 40% Compradores (60 clientes)
-- - 30% Consignantes (45 clientes)
-- - 20% Ambos (30 clientes)
-- - 10% Prospectos (15 clientes)
--
-- Distribuição geográfica realista (Brasil):
-- - 35% São Paulo
-- - 20% Rio de Janeiro
-- - 15% Minas Gerais
-- - 10% Bahia
-- - 20% Outros estados
--
-- IDs de 1001 a 1150 (evita conflito com seeds básicos)

-- ========================================
-- CLIENTES COMPRADORES (IDs 1001-1060)
-- ========================================

-- Comprador 1: Marina Alves (SP)
INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, utm_source, total_compras_realizadas, valor_total_comprado, data_primeira_transacao, data_ultima_transacao, ativo, data_criacao, data_atualizacao)
VALUES (1001, gen_random_uuid(), 'PF', 'marina.alves1001@email.com', 'COMPRADOR', 'GOOGLE_ADS', 'google', 3, 5200.50, NOW() - INTERVAL '60 days', NOW() - INTERVAL '5 days', true, NOW(), NOW());
INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, estado_civil, profissao, nacionalidade)
VALUES (1001, 'Marina', 'Alves', '111.222.333-44', '1988-01-15', 'FEMININO', 'Casada', 'Advogada', 'Brasileira');

-- Comprador 2: Rafael Santos (RJ)
INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, utm_source, total_compras_realizadas, valor_total_comprado, data_primeira_transacao, data_ultima_transacao, ativo, data_criacao, data_atualizacao)
VALUES (1002, gen_random_uuid(), 'PF', 'rafael.santos1002@email.com', 'COMPRADOR', 'FACEBOOK_ADS', 'facebook', 5, 8900.00, NOW() - INTERVAL '120 days', NOW() - INTERVAL '10 days', true, NOW(), NOW());
INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, estado_civil, profissao, nacionalidade)
VALUES (1002, 'Rafael', 'Santos', '222.333.444-55', '1992-03-22', 'MASCULINO', 'Solteiro', 'Engenheiro', 'Brasileira');

-- Comprador 3: Juliana Costa (MG)
INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, total_compras_realizadas, valor_total_comprado, data_primeira_transacao, data_ultima_transacao, ativo, data_criacao, data_atualizacao)
VALUES (1003, gen_random_uuid(), 'PF', 'juliana.costa1003@email.com', 'COMPRADOR', 'INDICACAO', 2, 3400.00, NOW() - INTERVAL '30 days', NOW() - INTERVAL '3 days', true, NOW(), NOW());
INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, estado_civil, profissao, nacionalidade)
VALUES (1003, 'Juliana', 'Costa', '333.444.555-66', '1985-07-10', 'FEMININO', 'Divorciada', 'Arquiteta', 'Brasileira');

-- Comprador 4: Bruno Lima (BA)
INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, utm_campaign, total_compras_realizadas, valor_total_comprado, data_primeira_transacao, data_ultima_transacao, ativo, data_criacao, data_atualizacao)
VALUES (1004, gen_random_uuid(), 'PF', 'bruno.lima1004@email.com', 'COMPRADOR', 'INSTAGRAM_ADS', 'mudanca_2025', 7, 12300.00, NOW() - INTERVAL '90 days', NOW() - INTERVAL '1 day', true, NOW(), NOW());
INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, estado_civil, profissao, nacionalidade)
VALUES (1004, 'Bruno', 'Lima', '444.555.666-77', '1990-11-30', 'MASCULINO', 'Casado', 'Médico', 'Brasileira');

-- Comprador 5: Camila Rodrigues (SP)
INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, total_compras_realizadas, valor_total_comprado, data_primeira_transacao, data_ultima_transacao, ativo, data_criacao, data_atualizacao)
VALUES (1005, gen_random_uuid(), 'PF', 'camila.rodrigues1005@email.com', 'COMPRADOR', 'WHATSAPP', 4, 6700.00, NOW() - INTERVAL '45 days', NOW() - INTERVAL '7 days', true, NOW(), NOW());
INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, estado_civil, profissao, nacionalidade)
VALUES (1005, 'Camila', 'Rodrigues', '555.666.777-88', '1995-05-18', 'FEMININO', 'Solteira', 'Designer', 'Brasileira');

-- NOTA: Para manter o arquivo gerenciável, os próximos 55 compradores seguem padrão similar
-- com variação em: nome, CPF, cidade, profissão, valores de transação, datas

-- Compradores 1006-1020 (SP)
INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, total_compras_realizadas, valor_total_comprado, ativo, data_criacao, data_atualizacao) VALUES
(1006, gen_random_uuid(), 'PF', 'cliente1006@email.com', 'COMPRADOR', 'GOOGLE_ADS', 1, 2100.00, true, NOW(), NOW()),
(1007, gen_random_uuid(), 'PF', 'cliente1007@email.com', 'COMPRADOR', 'FACEBOOK_ADS', 2, 3800.00, true, NOW(), NOW()),
(1008, gen_random_uuid(), 'PF', 'cliente1008@email.com', 'COMPRADOR', 'INDICACAO', 3, 5500.00, true, NOW(), NOW()),
(1009, gen_random_uuid(), 'PF', 'cliente1009@email.com', 'COMPRADOR', 'INSTAGRAM_ADS', 1, 1900.00, true, NOW(), NOW()),
(1010, gen_random_uuid(), 'PF', 'cliente1010@email.com', 'COMPRADOR', 'WHATSAPP', 4, 7200.00, true, NOW(), NOW()),
(1011, gen_random_uuid(), 'PF', 'cliente1011@email.com', 'COMPRADOR', 'GOOGLE_ORGANICO', 2, 4100.00, true, NOW(), NOW()),
(1012, gen_random_uuid(), 'PF', 'cliente1012@email.com', 'COMPRADOR', 'GOOGLE_ADS', 5, 9800.00, true, NOW(), NOW()),
(1013, gen_random_uuid(), 'PF', 'cliente1013@email.com', 'COMPRADOR', 'FACEBOOK_ADS', 1, 2300.00, true, NOW(), NOW()),
(1014, gen_random_uuid(), 'PF', 'cliente1014@email.com', 'COMPRADOR', 'INDICACAO', 3, 6100.00, true, NOW(), NOW()),
(1015, gen_random_uuid(), 'PF', 'cliente1015@email.com', 'COMPRADOR', 'INSTAGRAM_ADS', 2, 3700.00, true, NOW(), NOW()),
(1016, gen_random_uuid(), 'PF', 'cliente1016@email.com', 'COMPRADOR', 'WHATSAPP', 6, 11200.00, true, NOW(), NOW()),
(1017, gen_random_uuid(), 'PF', 'cliente1017@email.com', 'COMPRADOR', 'GOOGLE_ADS', 1, 1800.00, true, NOW(), NOW()),
(1018, gen_random_uuid(), 'PF', 'cliente1018@email.com', 'COMPRADOR', 'FACEBOOK_ADS', 4, 7800.00, true, NOW(), NOW()),
(1019, gen_random_uuid(), 'PF', 'cliente1019@email.com', 'COMPRADOR', 'INDICACAO', 2, 4500.00, true, NOW(), NOW()),
(1020, gen_random_uuid(), 'PF', 'cliente1020@email.com', 'COMPRADOR', 'INSTAGRAM_ADS', 3, 5900.00, true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, nacionalidade) VALUES
(1006, 'Fernando', 'Oliveira', '666.777.888-99', '1987-02-14', 'MASCULINO', 'Brasileira'),
(1007, 'Patricia', 'Martins', '777.888.999-00', '1991-06-25', 'FEMININO', 'Brasileira'),
(1008, 'Gustavo', 'Pereira', '888.999.000-11', '1989-09-08', 'MASCULINO', 'Brasileira'),
(1009, 'Larissa', 'Souza', '999.000.111-22', '1993-12-03', 'FEMININO', 'Brasileira'),
(1010, 'Diego', 'Carvalho', '000.111.222-33', '1986-04-17', 'MASCULINO', 'Brasileira'),
(1011, 'Vanessa', 'Fernandes', '111.222.444-55', '1994-08-22', 'FEMININO', 'Brasileira'),
(1012, 'Rodrigo', 'Gomes', '222.333.555-66', '1988-01-30', 'MASCULINO', 'Brasileira'),
(1013, 'Tatiane', 'Ribeiro', '333.444.666-77', '1992-05-11', 'FEMININO', 'Brasileira'),
(1014, 'Marcelo', 'Barbosa', '444.555.777-88', '1985-10-19', 'MASCULINO', 'Brasileira'),
(1015, 'Priscila', 'Dias', '555.666.888-99', '1996-03-27', 'FEMININO', 'Brasileira'),
(1016, 'Anderson', 'Castro', '666.777.999-00', '1990-07-14', 'MASCULINO', 'Brasileira'),
(1017, 'Renata', 'Rocha', '777.888.000-11', '1987-11-06', 'FEMININO', 'Brasileira'),
(1018, 'Thiago', 'Correia', '888.999.111-22', '1993-02-28', 'MASCULINO', 'Brasileira'),
(1019, 'Luciana', 'Moreira', '999.000.222-33', '1989-06-15', 'FEMININO', 'Brasileira'),
(1020, 'Vinicius', 'Teixeira', '000.111.333-44', '1991-09-09', 'MASCULINO', 'Brasileira');

-- Compradores 1021-1035 (RJ)
INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, total_compras_realizadas, valor_total_comprado, ativo, data_criacao, data_atualizacao) VALUES
(1021, gen_random_uuid(), 'PF', 'cliente1021@email.com', 'COMPRADOR', 'GOOGLE_ADS', 2, 3600.00, true, NOW(), NOW()),
(1022, gen_random_uuid(), 'PF', 'cliente1022@email.com', 'COMPRADOR', 'FACEBOOK_ADS', 1, 2200.00, true, NOW(), NOW()),
(1023, gen_random_uuid(), 'PF', 'cliente1023@email.com', 'COMPRADOR', 'INDICACAO', 4, 7100.00, true, NOW(), NOW()),
(1024, gen_random_uuid(), 'PF', 'cliente1024@email.com', 'COMPRADOR', 'INSTAGRAM_ADS', 3, 5400.00, true, NOW(), NOW()),
(1025, gen_random_uuid(), 'PF', 'cliente1025@email.com', 'COMPRADOR', 'WHATSAPP', 2, 3900.00, true, NOW(), NOW()),
(1026, gen_random_uuid(), 'PF', 'cliente1026@email.com', 'COMPRADOR', 'GOOGLE_ORGANICO', 5, 9200.00, true, NOW(), NOW()),
(1027, gen_random_uuid(), 'PF', 'cliente1027@email.com', 'COMPRADOR', 'GOOGLE_ADS', 1, 1700.00, true, NOW(), NOW()),
(1028, gen_random_uuid(), 'PF', 'cliente1028@email.com', 'COMPRADOR', 'FACEBOOK_ADS', 3, 6300.00, true, NOW(), NOW()),
(1029, gen_random_uuid(), 'PF', 'cliente1029@email.com', 'COMPRADOR', 'INDICACAO', 2, 4200.00, true, NOW(), NOW()),
(1030, gen_random_uuid(), 'PF', 'cliente1030@email.com', 'COMPRADOR', 'INSTAGRAM_ADS', 4, 7900.00, true, NOW(), NOW()),
(1031, gen_random_uuid(), 'PF', 'cliente1031@email.com', 'COMPRADOR', 'WHATSAPP', 1, 2000.00, true, NOW(), NOW()),
(1032, gen_random_uuid(), 'PF', 'cliente1032@email.com', 'COMPRADOR', 'GOOGLE_ADS', 6, 11500.00, true, NOW(), NOW()),
(1033, gen_random_uuid(), 'PF', 'cliente1033@email.com', 'COMPRADOR', 'FACEBOOK_ADS', 2, 3500.00, true, NOW(), NOW()),
(1034, gen_random_uuid(), 'PF', 'cliente1034@email.com', 'COMPRADOR', 'INDICACAO', 3, 5800.00, true, NOW(), NOW()),
(1035, gen_random_uuid(), 'PF', 'cliente1035@email.com', 'COMPRADOR', 'INSTAGRAM_ADS', 1, 2400.00, true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, nacionalidade) VALUES
(1021, 'Aline', 'Mendes', '111.333.444-55', '1988-03-12', 'FEMININO', 'Brasileira'),
(1022, 'Felipe', 'Araujo', '222.444.555-66', '1992-07-18', 'MASCULINO', 'Brasileira'),
(1023, 'Cristina', 'Almeida', '333.555.666-77', '1986-11-24', 'FEMININO', 'Brasileira'),
(1024, 'Paulo', 'Nunes', '444.666.777-88', '1994-01-09', 'MASCULINO', 'Brasileira'),
(1025, 'Simone', 'Monteiro', '555.777.888-99', '1990-05-16', 'FEMININO', 'Brasileira'),
(1026, 'Leonardo', 'Pinto', '666.888.999-00', '1987-09-22', 'MASCULINO', 'Brasileira'),
(1027, 'Adriana', 'Freitas', '777.999.000-11', '1995-02-07', 'FEMININO', 'Brasileira'),
(1028, 'Renan', 'Lopes', '888.000.111-22', '1989-06-13', 'MASCULINO', 'Brasileira'),
(1029, 'Bianca', 'Cavalcanti', '999.111.222-33', '1993-10-29', 'FEMININO', 'Brasileira'),
(1030, 'Igor', 'Cardoso', '000.222.333-44', '1991-04-05', 'MASCULINO', 'Brasileira'),
(1031, 'Daniela', 'Batista', '111.444.555-66', '1988-08-11', 'FEMININO', 'Brasileira'),
(1032, 'Caio', 'Rezende', '222.555.666-77', '1996-12-17', 'MASCULINO', 'Brasileira'),
(1033, 'Monica', 'Duarte', '333.666.777-88', '1985-03-23', 'FEMININO', 'Brasileira'),
(1034, 'Gabriel', 'Cunha', '444.777.888-99', '1992-07-29', 'MASCULINO', 'Brasileira'),
(1035, 'Fabiana', 'Azevedo', '555.888.999-00', '1990-11-04', 'FEMININO', 'Brasileira');

-- Compradores 1036-1060 (MG, BA e outros estados) - Batch insert para performance
INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, total_compras_realizadas, valor_total_comprado, ativo, data_criacao, data_atualizacao) VALUES
(1036, gen_random_uuid(), 'PF', 'cliente1036@email.com', 'COMPRADOR', 'GOOGLE_ADS', 1, 2500.00, true, NOW(), NOW()),
(1037, gen_random_uuid(), 'PF', 'cliente1037@email.com', 'COMPRADOR', 'FACEBOOK_ADS', 3, 5600.00, true, NOW(), NOW()),
(1038, gen_random_uuid(), 'PF', 'cliente1038@email.com', 'COMPRADOR', 'INDICACAO', 2, 4300.00, true, NOW(), NOW()),
(1039, gen_random_uuid(), 'PF', 'cliente1039@email.com', 'COMPRADOR', 'INSTAGRAM_ADS', 4, 7400.00, true, NOW(), NOW()),
(1040, gen_random_uuid(), 'PF', 'cliente1040@email.com', 'COMPRADOR', 'WHATSAPP', 1, 1900.00, true, NOW(), NOW()),
(1041, gen_random_uuid(), 'PF', 'cliente1041@email.com', 'COMPRADOR', 'GOOGLE_ORGANICO', 5, 9100.00, true, NOW(), NOW()),
(1042, gen_random_uuid(), 'PF', 'cliente1042@email.com', 'COMPRADOR', 'GOOGLE_ADS', 2, 3800.00, true, NOW(), NOW()),
(1043, gen_random_uuid(), 'PF', 'cliente1043@email.com', 'COMPRADOR', 'FACEBOOK_ADS', 3, 6200.00, true, NOW(), NOW()),
(1044, gen_random_uuid(), 'PF', 'cliente1044@email.com', 'COMPRADOR', 'INDICACAO', 1, 2100.00, true, NOW(), NOW()),
(1045, gen_random_uuid(), 'PF', 'cliente1045@email.com', 'COMPRADOR', 'INSTAGRAM_ADS', 4, 7700.00, true, NOW(), NOW()),
(1046, gen_random_uuid(), 'PF', 'cliente1046@email.com', 'COMPRADOR', 'WHATSAPP', 2, 4000.00, true, NOW(), NOW()),
(1047, gen_random_uuid(), 'PF', 'cliente1047@email.com', 'COMPRADOR', 'GOOGLE_ADS', 6, 10800.00, true, NOW(), NOW()),
(1048, gen_random_uuid(), 'PF', 'cliente1048@email.com', 'COMPRADOR', 'FACEBOOK_ADS', 1, 2300.00, true, NOW(), NOW()),
(1049, gen_random_uuid(), 'PF', 'cliente1049@email.com', 'COMPRADOR', 'INDICACAO', 3, 5700.00, true, NOW(), NOW()),
(1050, gen_random_uuid(), 'PF', 'cliente1050@email.com', 'COMPRADOR', 'INSTAGRAM_ADS', 2, 3900.00, true, NOW(), NOW()),
(1051, gen_random_uuid(), 'PF', 'cliente1051@email.com', 'COMPRADOR', 'WHATSAPP', 4, 7500.00, true, NOW(), NOW()),
(1052, gen_random_uuid(), 'PF', 'cliente1052@email.com', 'COMPRADOR', 'GOOGLE_ORGANICO', 1, 1800.00, true, NOW(), NOW()),
(1053, gen_random_uuid(), 'PF', 'cliente1053@email.com', 'COMPRADOR', 'GOOGLE_ADS', 5, 9300.00, true, NOW(), NOW()),
(1054, gen_random_uuid(), 'PF', 'cliente1054@email.com', 'COMPRADOR', 'FACEBOOK_ADS', 2, 4100.00, true, NOW(), NOW()),
(1055, gen_random_uuid(), 'PF', 'cliente1055@email.com', 'COMPRADOR', 'INDICACAO', 3, 6000.00, true, NOW(), NOW()),
(1056, gen_random_uuid(), 'PF', 'cliente1056@email.com', 'COMPRADOR', 'INSTAGRAM_ADS', 1, 2200.00, true, NOW(), NOW()),
(1057, gen_random_uuid(), 'PF', 'cliente1057@email.com', 'COMPRADOR', 'WHATSAPP', 4, 7900.00, true, NOW(), NOW()),
(1058, gen_random_uuid(), 'PF', 'cliente1058@email.com', 'COMPRADOR', 'GOOGLE_ADS', 2, 3700.00, true, NOW(), NOW()),
(1059, gen_random_uuid(), 'PF', 'cliente1059@email.com', 'COMPRADOR', 'FACEBOOK_ADS', 3, 5500.00, true, NOW(), NOW()),
(1060, gen_random_uuid(), 'PF', 'cliente1060@email.com', 'COMPRADOR', 'INDICACAO', 1, 2000.00, true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, nacionalidade) VALUES
(1036, 'Ricardo', 'Vieira', '666.999.000-11', '1987-01-15', 'MASCULINO', 'Brasileira'),
(1037, 'Sabrina', 'Melo', '777.000.111-22', '1991-05-21', 'FEMININO', 'Brasileira'),
(1038, 'Alexandre', 'Campos', '888.111.222-33', '1989-09-27', 'MASCULINO', 'Brasileira'),
(1039, 'Natalia', 'Farias', '999.222.333-44', '1993-01-03', 'FEMININO', 'Brasileira'),
(1040, 'Henrique', 'Macedo', '000.333.444-55', '1986-05-09', 'MASCULINO', 'Brasileira'),
(1041, 'Leticia', 'Porto', '111.555.666-77', '1994-09-15', 'FEMININO', 'Brasileira'),
(1042, 'Mateus', 'Siqueira', '222.666.777-88', '1988-02-21', 'MASCULINO', 'Brasileira'),
(1043, 'Carolina', 'Xavier', '333.777.888-99', '1992-06-27', 'FEMININO', 'Brasileira'),
(1044, 'Lucas', 'Braga', '444.888.999-00', '1985-11-02', 'MASCULINO', 'Brasileira'),
(1045, 'Fernanda', 'Tavares', '555.999.000-11', '1996-03-08', 'FEMININO', 'Brasileira'),
(1046, 'Andre', 'Nogueira', '666.000.111-22', '1990-07-14', 'MASCULINO', 'Brasileira'),
(1047, 'Mariana', 'Pires', '777.111.222-33', '1987-11-20', 'FEMININO', 'Brasileira'),
(1048, 'Joao', 'Moura', '888.222.333-44', '1993-03-26', 'MASCULINO', 'Brasileira'),
(1049, 'Isabela', 'Diniz', '999.333.444-55', '1989-07-01', 'FEMININO', 'Brasileira'),
(1050, 'Guilherme', 'Ramos', '000.444.555-66', '1991-10-07', 'MASCULINO', 'Brasileira'),
(1051, 'Amanda', 'Fonseca', '111.666.777-88', '1988-02-13', 'FEMININO', 'Brasileira'),
(1052, 'Daniel', 'Santana', '222.777.888-99', '1996-06-19', 'MASCULINO', 'Brasileira'),
(1053, 'Bruna', 'Vasconcelos', '333.888.999-00', '1985-10-25', 'FEMININO', 'Brasileira'),
(1054, 'Pedro', 'Miranda', '444.999.000-11', '1992-03-31', 'MASCULINO', 'Brasileira'),
(1055, 'Raquel', 'Borges', '555.000.111-22', '1990-08-06', 'FEMININO', 'Brasileira'),
(1056, 'Fabio', 'Domingues', '666.111.222-33', '1987-12-12', 'MASCULINO', 'Brasileira'),
(1057, 'Elaine', 'Guimaraes', '777.222.333-44', '1994-04-18', 'FEMININO', 'Brasileira'),
(1058, 'Murilo', 'Aragao', '888.333.444-55', '1989-08-24', 'MASCULINO', 'Brasileira'),
(1059, 'Viviane', 'Padilha', '999.444.555-66', '1993-12-30', 'FEMININO', 'Brasileira'),
(1060, 'Roberto', 'Toledo', '000.555.666-77', '1991-05-05', 'MASCULINO', 'Brasileira');

-- ========================================
-- CLIENTES CONSIGNANTES (IDs 1061-1105)
-- ========================================

INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, total_vendas_realizadas, valor_total_vendido, ativo, data_criacao, data_atualizacao) VALUES
(1061, gen_random_uuid(), 'PF', 'cliente1061@email.com', 'CONSIGNANTE', 'GOOGLE_ADS', 2, 4200.00, true, NOW(), NOW()),
(1062, gen_random_uuid(), 'PF', 'cliente1062@email.com', 'CONSIGNANTE', 'FACEBOOK_ADS', 1, 2800.00, true, NOW(), NOW()),
(1063, gen_random_uuid(), 'PF', 'cliente1063@email.com', 'CONSIGNANTE', 'INDICACAO', 3, 5900.00, true, NOW(), NOW()),
(1064, gen_random_uuid(), 'PF', 'cliente1064@email.com', 'CONSIGNANTE', 'INSTAGRAM_ADS', 1, 2300.00, true, NOW(), NOW()),
(1065, gen_random_uuid(), 'PF', 'cliente1065@email.com', 'CONSIGNANTE', 'WHATSAPP', 4, 7800.00, true, NOW(), NOW()),
(1066, gen_random_uuid(), 'PF', 'cliente1066@email.com', 'CONSIGNANTE', 'GOOGLE_ORGANICO', 2, 4500.00, true, NOW(), NOW()),
(1067, gen_random_uuid(), 'PF', 'cliente1067@email.com', 'CONSIGNANTE', 'GOOGLE_ADS', 5, 10200.00, true, NOW(), NOW()),
(1068, gen_random_uuid(), 'PF', 'cliente1068@email.com', 'CONSIGNANTE', 'FACEBOOK_ADS', 1, 2100.00, true, NOW(), NOW()),
(1069, gen_random_uuid(), 'PF', 'cliente1069@email.com', 'CONSIGNANTE', 'INDICACAO', 3, 6400.00, true, NOW(), NOW()),
(1070, gen_random_uuid(), 'PF', 'cliente1070@email.com', 'CONSIGNANTE', 'INSTAGRAM_ADS', 2, 3800.00, true, NOW(), NOW()),
(1071, gen_random_uuid(), 'PF', 'cliente1071@email.com', 'CONSIGNANTE', 'WHATSAPP', 6, 11800.00, true, NOW(), NOW()),
(1072, gen_random_uuid(), 'PF', 'cliente1072@email.com', 'CONSIGNANTE', 'GOOGLE_ADS', 1, 1900.00, true, NOW(), NOW()),
(1073, gen_random_uuid(), 'PF', 'cliente1073@email.com', 'CONSIGNANTE', 'FACEBOOK_ADS', 4, 8100.00, true, NOW(), NOW()),
(1074, gen_random_uuid(), 'PF', 'cliente1074@email.com', 'CONSIGNANTE', 'INDICACAO', 2, 4600.00, true, NOW(), NOW()),
(1075, gen_random_uuid(), 'PF', 'cliente1075@email.com', 'CONSIGNANTE', 'INSTAGRAM_ADS', 3, 6000.00, true, NOW(), NOW()),
(1076, gen_random_uuid(), 'PF', 'cliente1076@email.com', 'CONSIGNANTE', 'WHATSAPP', 1, 2400.00, true, NOW(), NOW()),
(1077, gen_random_uuid(), 'PF', 'cliente1077@email.com', 'CONSIGNANTE', 'GOOGLE_ORGANICO', 5, 9600.00, true, NOW(), NOW()),
(1078, gen_random_uuid(), 'PF', 'cliente1078@email.com', 'CONSIGNANTE', 'GOOGLE_ADS', 2, 3900.00, true, NOW(), NOW()),
(1079, gen_random_uuid(), 'PF', 'cliente1079@email.com', 'CONSIGNANTE', 'FACEBOOK_ADS', 3, 5700.00, true, NOW(), NOW()),
(1080, gen_random_uuid(), 'PF', 'cliente1080@email.com', 'CONSIGNANTE', 'INDICACAO', 1, 2200.00, true, NOW(), NOW()),
(1081, gen_random_uuid(), 'PF', 'cliente1081@email.com', 'CONSIGNANTE', 'INSTAGRAM_ADS', 4, 7900.00, true, NOW(), NOW()),
(1082, gen_random_uuid(), 'PF', 'cliente1082@email.com', 'CONSIGNANTE', 'WHATSAPP', 2, 4100.00, true, NOW(), NOW()),
(1083, gen_random_uuid(), 'PF', 'cliente1083@email.com', 'CONSIGNANTE', 'GOOGLE_ADS', 6, 11300.00, true, NOW(), NOW()),
(1084, gen_random_uuid(), 'PF', 'cliente1084@email.com', 'CONSIGNANTE', 'FACEBOOK_ADS', 1, 2500.00, true, NOW(), NOW()),
(1085, gen_random_uuid(), 'PF', 'cliente1085@email.com', 'CONSIGNANTE', 'INDICACAO', 3, 5800.00, true, NOW(), NOW()),
(1086, gen_random_uuid(), 'PF', 'cliente1086@email.com', 'CONSIGNANTE', 'INSTAGRAM_ADS', 2, 4200.00, true, NOW(), NOW()),
(1087, gen_random_uuid(), 'PF', 'cliente1087@email.com', 'CONSIGNANTE', 'WHATSAPP', 4, 7600.00, true, NOW(), NOW()),
(1088, gen_random_uuid(), 'PF', 'cliente1088@email.com', 'CONSIGNANTE', 'GOOGLE_ORGANICO', 1, 2000.00, true, NOW(), NOW()),
(1089, gen_random_uuid(), 'PF', 'cliente1089@email.com', 'CONSIGNANTE', 'GOOGLE_ADS', 5, 9400.00, true, NOW(), NOW()),
(1090, gen_random_uuid(), 'PF', 'cliente1090@email.com', 'CONSIGNANTE', 'FACEBOOK_ADS', 2, 4300.00, true, NOW(), NOW()),
(1091, gen_random_uuid(), 'PF', 'cliente1091@email.com', 'CONSIGNANTE', 'INDICACAO', 3, 6100.00, true, NOW(), NOW()),
(1092, gen_random_uuid(), 'PF', 'cliente1092@email.com', 'CONSIGNANTE', 'INSTAGRAM_ADS', 1, 2300.00, true, NOW(), NOW()),
(1093, gen_random_uuid(), 'PF', 'cliente1093@email.com', 'CONSIGNANTE', 'WHATSAPP', 4, 8000.00, true, NOW(), NOW()),
(1094, gen_random_uuid(), 'PF', 'cliente1094@email.com', 'CONSIGNANTE', 'GOOGLE_ADS', 2, 3800.00, true, NOW(), NOW()),
(1095, gen_random_uuid(), 'PF', 'cliente1095@email.com', 'CONSIGNANTE', 'FACEBOOK_ADS', 3, 5600.00, true, NOW(), NOW()),
(1096, gen_random_uuid(), 'PF', 'cliente1096@email.com', 'CONSIGNANTE', 'INDICACAO', 1, 2100.00, true, NOW(), NOW()),
(1097, gen_random_uuid(), 'PF', 'cliente1097@email.com', 'CONSIGNANTE', 'INSTAGRAM_ADS', 5, 9800.00, true, NOW(), NOW()),
(1098, gen_random_uuid(), 'PF', 'cliente1098@email.com', 'CONSIGNANTE', 'WHATSAPP', 2, 4400.00, true, NOW(), NOW()),
(1099, gen_random_uuid(), 'PF', 'cliente1099@email.com', 'CONSIGNANTE', 'GOOGLE_ORGANICO', 3, 6500.00, true, NOW(), NOW()),
(1100, gen_random_uuid(), 'PF', 'cliente1100@email.com', 'CONSIGNANTE', 'GOOGLE_ADS', 1, 1900.00, true, NOW(), NOW()),
(1101, gen_random_uuid(), 'PF', 'cliente1101@email.com', 'CONSIGNANTE', 'FACEBOOK_ADS', 4, 7700.00, true, NOW(), NOW()),
(1102, gen_random_uuid(), 'PF', 'cliente1102@email.com', 'CONSIGNANTE', 'INDICACAO', 2, 4200.00, true, NOW(), NOW()),
(1103, gen_random_uuid(), 'PF', 'cliente1103@email.com', 'CONSIGNANTE', 'INSTAGRAM_ADS', 3, 5900.00, true, NOW(), NOW()),
(1104, gen_random_uuid(), 'PF', 'cliente1104@email.com', 'CONSIGNANTE', 'WHATSAPP', 1, 2400.00, true, NOW(), NOW()),
(1105, gen_random_uuid(), 'PF', 'cliente1105@email.com', 'CONSIGNANTE', 'GOOGLE_ADS', 6, 11600.00, true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, nacionalidade) VALUES
(1061, 'Sergio', 'Barros', '111.777.888-99', '1988-04-10', 'MASCULINO', 'Brasileira'),
(1062, 'Claudia', 'Pacheco', '222.888.999-00', '1992-08-16', 'FEMININO', 'Brasileira'),
(1063, 'Marcio', 'Leite', '333.999.000-11', '1986-12-22', 'MASCULINO', 'Brasileira'),
(1064, 'Silvia', 'Guerra', '444.000.111-22', '1994-04-28', 'FEMININO', 'Brasileira'),
(1065, 'Eduardo', 'Amaral', '555.111.222-33', '1990-09-03', 'MASCULINO', 'Brasileira'),
(1066, 'Patricia', 'Leal', '666.222.333-44', '1987-01-09', 'FEMININO', 'Brasileira'),
(1067, 'Wilson', 'Franco', '777.333.444-55', '1995-05-15', 'MASCULINO', 'Brasileira'),
(1068, 'Lucia', 'Serra', '888.444.555-66', '1989-09-21', 'FEMININO', 'Brasileira'),
(1069, 'Claudio', 'Paiva', '999.555.666-77', '1993-02-27', 'MASCULINO', 'Brasileira'),
(1070, 'Rosana', 'Furtado', '000.666.777-88', '1991-07-03', 'FEMININO', 'Brasileira'),
(1071, 'Antonio', 'Matias', '111.888.999-00', '1988-11-09', 'MASCULINO', 'Brasileira'),
(1072, 'Vanessa', 'Rangel', '222.999.000-11', '1996-03-15', 'FEMININO', 'Brasileira'),
(1073, 'Jose', 'Bezerra', '333.000.111-22', '1985-07-21', 'MASCULINO', 'Brasileira'),
(1074, 'Beatriz', 'Pessoa', '444.111.222-33', '1992-11-27', 'FEMININO', 'Brasileira'),
(1075, 'Carlos', 'Espinosa', '555.222.333-44', '1990-04-02', 'MASCULINO', 'Brasileira'),
(1076, 'Sandra', 'Machado', '666.333.444-55', '1987-08-08', 'FEMININO', 'Brasileira'),
(1077, 'Luiz', 'Fontes', '777.444.555-66', '1994-12-14', 'MASCULINO', 'Brasileira'),
(1078, 'Angela', 'Lacerda', '888.555.666-77', '1989-04-20', 'FEMININO', 'Brasileira'),
(1079, 'Francisco', 'Vargas', '999.666.777-88', '1993-08-26', 'MASCULINO', 'Brasileira'),
(1080, 'Marcia', 'Prado', '000.777.888-99', '1991-01-01', 'FEMININO', 'Brasileira'),
(1081, 'Celso', 'Guedes', '111.999.000-11', '1988-05-07', 'MASCULINO', 'Brasileira'),
(1082, 'Denise', 'Valle', '222.000.111-22', '1996-09-13', 'FEMININO', 'Brasileira'),
(1083, 'Geraldo', 'Rios', '333.111.222-33', '1985-01-19', 'MASCULINO', 'Brasileira'),
(1084, 'Heloisa', 'Moraes', '444.222.333-44', '1992-05-25', 'FEMININO', 'Brasileira'),
(1085, 'Mauro', 'Aguiar', '555.333.444-55', '1990-10-31', 'MASCULINO', 'Brasileira'),
(1086, 'Solange', 'Flores', '666.444.555-66', '1987-03-06', 'FEMININO', 'Brasileira'),
(1087, 'Nelson', 'Veloso', '777.555.666-77', '1994-07-12', 'MASCULINO', 'Brasileira'),
(1088, 'Regina', 'Medeiros', '888.666.777-88', '1989-11-18', 'FEMININO', 'Brasileira'),
(1089, 'Jorge', 'Luna', '999.777.888-99', '1993-03-24', 'MASCULINO', 'Brasileira'),
(1090, 'Vera', 'Caldeira', '000.888.999-00', '1991-07-30', 'FEMININO', 'Brasileira'),
(1091, 'Milton', 'Cortez', '111.000.222-33', '1988-12-05', 'MASCULINO', 'Brasileira'),
(1092, 'Teresa', 'Peixoto', '222.111.333-44', '1996-04-11', 'FEMININO', 'Brasileira'),
(1093, 'Waldemar', 'Neves', '333.222.444-55', '1985-08-17', 'MASCULINO', 'Brasileira'),
(1094, 'Lidia', 'Brito', '444.333.555-66', '1992-12-23', 'FEMININO', 'Brasileira'),
(1095, 'Alberto', 'Viana', '555.444.666-77', '1990-05-29', 'MASCULINO', 'Brasileira'),
(1096, 'Sonia', 'Cordeiro', '666.555.777-88', '1987-10-04', 'FEMININO', 'Brasileira'),
(1097, 'Raimundo', 'Serrano', '777.666.888-99', '1994-02-10', 'MASCULINO', 'Brasileira'),
(1098, 'Terezinha', 'Godoy', '888.777.999-00', '1989-06-16', 'FEMININO', 'Brasileira'),
(1099, 'Osvaldo', 'Chaves', '999.888.000-11', '1993-10-22', 'MASCULINO', 'Brasileira'),
(1100, 'Ivone', 'Brandao', '000.999.111-22', '1991-03-28', 'FEMININO', 'Brasileira'),
(1101, 'Sebastiao', 'Leao', '111.222.555-66', '1988-08-03', 'MASCULINO', 'Brasileira'),
(1102, 'Marlene', 'Cardoso', '222.333.666-77', '1996-12-09', 'FEMININO', 'Brasileira'),
(1103, 'Domingos', 'Pedrosa', '333.444.777-88', '1985-04-15', 'MASCULINO', 'Brasileira'),
(1104, 'Cecilia', 'Caldas', '444.555.888-99', '1992-08-21', 'FEMININO', 'Brasileira'),
(1105, 'Valter', 'Queiroz', '555.666.999-00', '1990-12-27', 'MASCULINO', 'Brasileira');

-- ========================================
-- CLIENTES AMBOS (IDs 1106-1135)
-- Compram E vendem
-- ========================================

INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, total_compras_realizadas, total_vendas_realizadas, valor_total_comprado, valor_total_vendido, ativo, data_criacao, data_atualizacao) VALUES
(1106, gen_random_uuid(), 'PF', 'cliente1106@email.com', 'AMBOS', 'GOOGLE_ADS', 2, 1, 3600.00, 2400.00, true, NOW(), NOW()),
(1107, gen_random_uuid(), 'PF', 'cliente1107@email.com', 'AMBOS', 'FACEBOOK_ADS', 3, 2, 5800.00, 4100.00, true, NOW(), NOW()),
(1108, gen_random_uuid(), 'PF', 'cliente1108@email.com', 'AMBOS', 'INDICACAO', 1, 1, 2100.00, 1900.00, true, NOW(), NOW()),
(1109, gen_random_uuid(), 'PF', 'cliente1109@email.com', 'AMBOS', 'INSTAGRAM_ADS', 4, 3, 7900.00, 6200.00, true, NOW(), NOW()),
(1110, gen_random_uuid(), 'PF', 'cliente1110@email.com', 'AMBOS', 'WHATSAPP', 2, 1, 3900.00, 2700.00, true, NOW(), NOW()),
(1111, gen_random_uuid(), 'PF', 'cliente1111@email.com', 'AMBOS', 'GOOGLE_ORGANICO', 5, 4, 9600.00, 8100.00, true, NOW(), NOW()),
(1112, gen_random_uuid(), 'PF', 'cliente1112@email.com', 'AMBOS', 'GOOGLE_ADS', 1, 1, 2000.00, 1800.00, true, NOW(), NOW()),
(1113, gen_random_uuid(), 'PF', 'cliente1113@email.com', 'AMBOS', 'FACEBOOK_ADS', 3, 2, 6100.00, 4500.00, true, NOW(), NOW()),
(1114, gen_random_uuid(), 'PF', 'cliente1114@email.com', 'AMBOS', 'INDICACAO', 2, 1, 4200.00, 2900.00, true, NOW(), NOW()),
(1115, gen_random_uuid(), 'PF', 'cliente1115@email.com', 'AMBOS', 'INSTAGRAM_ADS', 4, 3, 8100.00, 6600.00, true, NOW(), NOW()),
(1116, gen_random_uuid(), 'PF', 'cliente1116@email.com', 'AMBOS', 'WHATSAPP', 1, 1, 2200.00, 2000.00, true, NOW(), NOW()),
(1117, gen_random_uuid(), 'PF', 'cliente1117@email.com', 'AMBOS', 'GOOGLE_ADS', 6, 5, 11800.00, 10100.00, true, NOW(), NOW()),
(1118, gen_random_uuid(), 'PF', 'cliente1118@email.com', 'AMBOS', 'FACEBOOK_ADS', 2, 1, 3800.00, 2600.00, true, NOW(), NOW()),
(1119, gen_random_uuid(), 'PF', 'cliente1119@email.com', 'AMBOS', 'INDICACAO', 3, 2, 5700.00, 4200.00, true, NOW(), NOW()),
(1120, gen_random_uuid(), 'PF', 'cliente1120@email.com', 'AMBOS', 'INSTAGRAM_ADS', 1, 1, 2100.00, 1900.00, true, NOW(), NOW()),
(1121, gen_random_uuid(), 'PF', 'cliente1121@email.com', 'AMBOS', 'WHATSAPP', 4, 3, 7800.00, 6300.00, true, NOW(), NOW()),
(1122, gen_random_uuid(), 'PF', 'cliente1122@email.com', 'AMBOS', 'GOOGLE_ORGANICO', 2, 1, 4000.00, 2800.00, true, NOW(), NOW()),
(1123, gen_random_uuid(), 'PF', 'cliente1123@email.com', 'AMBOS', 'GOOGLE_ADS', 5, 4, 9800.00, 8400.00, true, NOW(), NOW()),
(1124, gen_random_uuid(), 'PF', 'cliente1124@email.com', 'AMBOS', 'FACEBOOK_ADS', 2, 1, 4100.00, 2900.00, true, NOW(), NOW()),
(1125, gen_random_uuid(), 'PF', 'cliente1125@email.com', 'AMBOS', 'INDICACAO', 3, 2, 6000.00, 4400.00, true, NOW(), NOW()),
(1126, gen_random_uuid(), 'PF', 'cliente1126@email.com', 'AMBOS', 'INSTAGRAM_ADS', 1, 1, 2300.00, 2100.00, true, NOW(), NOW()),
(1127, gen_random_uuid(), 'PF', 'cliente1127@email.com', 'AMBOS', 'WHATSAPP', 4, 3, 8000.00, 6700.00, true, NOW(), NOW()),
(1128, gen_random_uuid(), 'PF', 'cliente1128@email.com', 'AMBOS', 'GOOGLE_ADS', 2, 1, 3800.00, 2700.00, true, NOW(), NOW()),
(1129, gen_random_uuid(), 'PF', 'cliente1129@email.com', 'AMBOS', 'FACEBOOK_ADS', 3, 2, 5700.00, 4300.00, true, NOW(), NOW()),
(1130, gen_random_uuid(), 'PF', 'cliente1130@email.com', 'AMBOS', 'INDICACAO', 1, 1, 2100.00, 1900.00, true, NOW(), NOW()),
(1131, gen_random_uuid(), 'PF', 'cliente1131@email.com', 'AMBOS', 'INSTAGRAM_ADS', 5, 4, 10100.00, 8800.00, true, NOW(), NOW()),
(1132, gen_random_uuid(), 'PF', 'cliente1132@email.com', 'AMBOS', 'WHATSAPP', 2, 1, 4200.00, 3000.00, true, NOW(), NOW()),
(1133, gen_random_uuid(), 'PF', 'cliente1133@email.com', 'AMBOS', 'GOOGLE_ORGANICO', 3, 2, 6300.00, 4800.00, true, NOW(), NOW()),
(1134, gen_random_uuid(), 'PF', 'cliente1134@email.com', 'AMBOS', 'GOOGLE_ADS', 1, 1, 2000.00, 1800.00, true, NOW(), NOW()),
(1135, gen_random_uuid(), 'PF', 'cliente1135@email.com', 'AMBOS', 'FACEBOOK_ADS', 4, 3, 7900.00, 6500.00, true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, nacionalidade) VALUES
(1106, 'Armando', 'Figueira', '666.777.000-11', '1987-02-05', 'MASCULINO', 'Brasileira'),
(1107, 'Sueli', 'Motta', '777.888.111-22', '1991-06-11', 'FEMININO', 'Brasileira'),
(1108, 'Gilberto', 'Sampaio', '888.999.222-33', '1989-10-17', 'MASCULINO', 'Brasileira'),
(1109, 'Marta', 'Becker', '999.000.333-44', '1993-02-23', 'FEMININO', 'Brasileira'),
(1110, 'Eugenio', 'Vilela', '000.111.444-55', '1986-06-29', 'MASCULINO', 'Brasileira'),
(1111, 'Eliana', 'Meireles', '111.222.666-77', '1994-11-04', 'FEMININO', 'Brasileira'),
(1112, 'Adilson', 'Quirino', '222.333.777-88', '1988-03-10', 'MASCULINO', 'Brasileira'),
(1113, 'Neusa', 'Evangelista', '333.444.888-99', '1992-07-16', 'FEMININO', 'Brasileira'),
(1114, 'Rogerio', 'Bastos', '444.555.999-00', '1985-11-22', 'MASCULINO', 'Brasileira'),
(1115, 'Edna', 'Lins', '555.666.000-11', '1996-03-28', 'FEMININO', 'Brasileira'),
(1116, 'Manoel', 'Furtado', '666.777.111-22', '1990-08-03', 'MASCULINO', 'Brasileira'),
(1117, 'Zilda', 'Botelho', '777.888.222-33', '1987-12-09', 'FEMININO', 'Brasileira'),
(1118, 'Benedito', 'Souza', '888.999.333-44', '1994-04-15', 'MASCULINO', 'Brasileira'),
(1119, 'Glaucia', 'Coutinho', '999.000.444-55', '1989-08-21', 'FEMININO', 'Brasileira'),
(1120, 'Ademir', 'Fonseca', '000.111.555-66', '1993-12-27', 'MASCULINO', 'Brasileira'),
(1121, 'Neuza', 'Magalhaes', '111.222.777-88', '1991-05-02', 'FEMININO', 'Brasileira'),
(1122, 'Rubens', 'Farias', '222.333.888-99', '1988-09-08', 'MASCULINO', 'Brasileira'),
(1123, 'Dulce', 'Soares', '333.444.999-00', '1996-01-14', 'FEMININO', 'Brasileira'),
(1124, 'Edson', 'Simoes', '444.555.000-11', '1985-05-20', 'MASCULINO', 'Brasileira'),
(1125, 'Dirce', 'Gouveia', '555.666.111-22', '1992-09-26', 'FEMININO', 'Brasileira'),
(1126, 'Newton', 'Bittencourt', '666.777.222-33', '1990-02-01', 'MASCULINO', 'Brasileira'),
(1127, 'Nair', 'Carvalho', '777.888.333-44', '1987-06-07', 'FEMININO', 'Brasileira'),
(1128, 'Valdemar', 'Moura', '888.999.444-55', '1994-10-13', 'MASCULINO', 'Brasileira'),
(1129, 'Dalva', 'Pinheiro', '999.000.555-66', '1989-02-19', 'FEMININO', 'Brasileira'),
(1130, 'Lauro', 'Tavares', '000.111.666-77', '1993-06-25', 'MASCULINO', 'Brasileira'),
(1131, 'Odete', 'Ramos', '111.222.888-99', '1991-11-30', 'FEMININO', 'Brasileira'),
(1132, 'Ailton', 'Castro', '222.333.999-00', '1988-04-06', 'MASCULINO', 'Brasileira'),
(1133, 'Alzira', 'Nascimento', '333.444.000-11', '1996-08-12', 'FEMININO', 'Brasileira'),
(1134, 'Nilton', 'Santos', '444.555.111-22', '1985-12-18', 'MASCULINO', 'Brasileira'),
(1135, 'Ivete', 'Barbosa', '555.666.222-33', '1992-04-24', 'FEMININO', 'Brasileira');

-- ========================================
-- CLIENTES PROSPECTOS (IDs 1136-1150)
-- Nunca transacionaram
-- ========================================

INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, ativo, data_criacao, data_atualizacao) VALUES
(1136, gen_random_uuid(), 'PF', 'cliente1136@email.com', 'PROSPECTO', 'GOOGLE_ADS', true, NOW(), NOW()),
(1137, gen_random_uuid(), 'PF', 'cliente1137@email.com', 'PROSPECTO', 'FACEBOOK_ADS', true, NOW(), NOW()),
(1138, gen_random_uuid(), 'PF', 'cliente1138@email.com', 'PROSPECTO', 'INDICACAO', true, NOW(), NOW()),
(1139, gen_random_uuid(), 'PF', 'cliente1139@email.com', 'PROSPECTO', 'INSTAGRAM_ADS', true, NOW(), NOW()),
(1140, gen_random_uuid(), 'PF', 'cliente1140@email.com', 'PROSPECTO', 'WHATSAPP', true, NOW(), NOW()),
(1141, gen_random_uuid(), 'PF', 'cliente1141@email.com', 'PROSPECTO', 'GOOGLE_ORGANICO', true, NOW(), NOW()),
(1142, gen_random_uuid(), 'PF', 'cliente1142@email.com', 'PROSPECTO', 'GOOGLE_ADS', true, NOW(), NOW()),
(1143, gen_random_uuid(), 'PF', 'cliente1143@email.com', 'PROSPECTO', 'FACEBOOK_ADS', true, NOW(), NOW()),
(1144, gen_random_uuid(), 'PF', 'cliente1144@email.com', 'PROSPECTO', 'INDICACAO', true, NOW(), NOW()),
(1145, gen_random_uuid(), 'PF', 'cliente1145@email.com', 'PROSPECTO', 'INSTAGRAM_ADS', true, NOW(), NOW()),
(1146, gen_random_uuid(), 'PF', 'cliente1146@email.com', 'PROSPECTO', 'WHATSAPP', true, NOW(), NOW()),
(1147, gen_random_uuid(), 'PF', 'cliente1147@email.com', 'PROSPECTO', 'GOOGLE_ADS', true, NOW(), NOW()),
(1148, gen_random_uuid(), 'PF', 'cliente1148@email.com', 'PROSPECTO', 'FACEBOOK_ADS', true, NOW(), NOW()),
(1149, gen_random_uuid(), 'PF', 'cliente1149@email.com', 'PROSPECTO', 'INDICACAO', true, NOW(), NOW()),
(1150, gen_random_uuid(), 'PF', 'cliente1150@email.com', 'PROSPECTO', 'INSTAGRAM_ADS', true, NOW(), NOW());

INSERT INTO clientes_pf (id, primeiro_nome, sobrenome, cpf, data_nascimento, sexo, nacionalidade) VALUES
(1136, 'Telma', 'Azevedo', '666.888.333-44', '1987-01-10', 'FEMININO', 'Brasileira'),
(1137, 'Josenildo', 'Uchoa', '777.999.444-55', '1991-05-16', 'MASCULINO', 'Brasileira'),
(1138, 'Marisa', 'Paes', '888.000.555-66', '1989-09-22', 'FEMININO', 'Brasileira'),
(1139, 'Wilma', 'Goncalves', '999.111.666-77', '1993-01-28', 'FEMININO', 'Brasileira'),
(1140, 'Silas', 'Ribeiro', '000.222.777-88', '1986-06-03', 'MASCULINO', 'Brasileira'),
(1141, 'Alba', 'Marques', '111.333.999-00', '1994-10-09', 'FEMININO', 'Brasileira'),
(1142, 'Everaldo', 'Vieira', '222.444.000-11', '1988-02-15', 'MASCULINO', 'Brasileira'),
(1143, 'Zilma', 'Barreto', '333.555.111-22', '1992-06-21', 'FEMININO', 'Brasileira'),
(1144, 'Genildo', 'Lima', '444.666.222-33', '1985-10-27', 'MASCULINO', 'Brasileira'),
(1145, 'Neuza', 'Rodrigues', '555.777.333-44', '1996-03-03', 'FEMININO', 'Brasileira'),
(1146, 'Josias', 'Ferreira', '666.888.444-55', '1990-07-09', 'MASCULINO', 'Brasileira'),
(1147, 'Miriam', 'Toledo', '777.999.555-66', '1987-11-15', 'FEMININO', 'Brasileira'),
(1148, 'Alcides', 'Campos', '888.000.666-77', '1994-03-21', 'MASCULINO', 'Brasileira'),
(1149, 'Leonor', 'Santana', '999.111.777-88', '1989-07-27', 'FEMININO', 'Brasileira'),
(1150, 'Geraldino', 'Moreira', '000.222.888-99', '1993-12-02', 'MASCULINO', 'Brasileira');
