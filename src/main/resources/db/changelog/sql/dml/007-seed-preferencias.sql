-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Seed: Preferências LGPD dos Clientes
-- ========================================

-- Preferências Cliente PF 1 (Ana Silva)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (1, true, true, true, false, true, true, false, NOW() - INTERVAL '90 days', '187.123.45.67', true);


-- Preferências Cliente PF 2 (João Santos)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (2, true, false, true, false, false, true, false, NOW() - INTERVAL '80 days', '201.12.34.56', true);


-- Preferências Cliente PF 3 (Maria Oliveira)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (3, true, true, true, true, true, true, true, NOW() - INTERVAL '120 days', '177.98.76.54', true);


-- Preferências Cliente PF 4 (Pedro Costa)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (4, true, true, true, false, false, false, false, NOW() - INTERVAL '5 days', '192.168.1.100', true);


-- Preferências Cliente PF 5 (Carla Mendes - bloqueada)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (5, false, false, false, false, false, false, false, NOW() - INTERVAL '60 days', '179.45.67.89', false);


-- Preferências Cliente PF 6 (Lucas Ferreira - ativo)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (6, true, true, true, false, true, true, false, NOW() - INTERVAL '100 days', '200.11.22.33', true);


-- Preferências Cliente PF 7 (Juliana Rocha)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (7, true, false, true, false, true, true, false, NOW() - INTERVAL '15 days', '189.77.88.99', true);


-- Preferências Cliente PF 8 (Roberto Alves - VIP)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (8, true, true, true, true, true, true, true, NOW() - INTERVAL '200 days', '177.55.44.33', true);


-- Preferências Cliente PF 9 (Fernanda Lima)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (9, true, true, true, false, false, true, false, NOW() - INTERVAL '3 days', '190.123.234.111', true);


-- Preferências Cliente PF 10 (Ricardo Souza)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (10, true, false, true, false, false, true, false, NOW() - INTERVAL '20 days', '201.88.99.100', true);


-- Preferências Cliente PJ 11 (Móveis Estrela)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (11, true, false, true, true, true, true, false, NOW() - INTERVAL '150 days', '187.200.100.50', true);


-- Preferências Cliente PJ 12 (Tech Solutions)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (12, true, false, false, true, false, true, true, NOW() - INTERVAL '70 days', '200.150.100.75', true);


-- Preferências Cliente PJ 13 (Construtora Nova Era)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (13, true, true, true, false, true, true, false, NOW() - INTERVAL '180 days', '179.100.50.25', true);


-- Preferências Cliente PJ 14 (Design Interiores MEI)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (14, true, false, true, false, true, true, true, NOW() - INTERVAL '50 days', '189.150.75.125', true);


-- Preferências Cliente PJ 15 (Hotel Boa Vista)
INSERT INTO preferencias_cliente (cliente_id, aceita_comunicacao_email, aceita_comunicacao_sms, aceita_comunicacao_whatsapp, aceita_comunicacao_telefone, aceita_newsletters, aceita_ofertas, aceita_pesquisas, data_consentimento, ip_consentimento, consentimento_ativo)
VALUES (15, true, true, false, true, true, true, false, NOW() - INTERVAL '10 days', '201.100.200.150', true);
