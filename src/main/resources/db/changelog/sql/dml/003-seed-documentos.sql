-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Seed: Documentos dos Clientes
-- ========================================

-- Documentos Cliente PF 1 (Ana Silva)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (1, 'CPF', '123.456.789-10', 'VERIFICADO', true, true);

INSERT INTO documentos (cliente_id, tipo_documento, numero, orgao_emissor, data_emissao, status_documento, ativo)
VALUES (1, 'RG', '12.345.678-9', 'SSP/SP', '2005-02-10', 'VERIFICADO', true);

INSERT INTO documentos (cliente_id, tipo_documento, numero, orgao_emissor, data_emissao, data_validade, status_documento, ativo)
VALUES (1, 'CNH', '12345678901', 'DETRAN/SP', '2020-05-15', '2030-05-15', 'VALIDO', true);


-- Documentos Cliente PF 2 (João Santos)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (2, 'CPF', '234.567.890-12', 'VERIFICADO', true, true);

INSERT INTO documentos (cliente_id, tipo_documento, numero, orgao_emissor, data_emissao, status_documento, ativo)
VALUES (2, 'RG', '23.456.789-0', 'SSP/RJ', '2000-08-22', 'VERIFICADO', true);


-- Documentos Cliente PF 3 (Maria Oliveira)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (3, 'CPF', '345.678.901-23', 'VERIFICADO', true, true);

INSERT INTO documentos (cliente_id, tipo_documento, numero, orgao_emissor, data_emissao, data_validade, status_documento, ativo)
VALUES (3, 'CNH', '34567890123', 'DETRAN/MG', '2018-11-10', '2028-11-10', 'VALIDO', true);


-- Documentos Cliente PF 4 (Pedro Costa)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (4, 'CPF', '456.789.012-34', 'AGUARDANDO_VERIFICACAO', true, true);


-- Documentos Cliente PF 5 (Carla Mendes - bloqueada)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (5, 'CPF', '567.890.123-45', 'AGUARDANDO_VERIFICACAO', true, true);

INSERT INTO documentos (cliente_id, tipo_documento, numero, orgao_emissor, data_emissao, status_documento, ativo)
VALUES (5, 'RG', 'MG-12.345.678', 'SSP/MG', '2002-09-15', 'AGUARDANDO_VERIFICACAO', true);


-- Documentos Cliente PF 6 (Lucas Ferreira)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (6, 'CPF', '678.901.234-56', 'VERIFICADO', true, true);

INSERT INTO documentos (cliente_id, tipo_documento, numero, orgao_emissor, data_emissao, data_validade, status_documento, ativo)
VALUES (6, 'CNH', '67890123456', 'DETRAN/SP', '2019-01-05', '2029-01-05', 'VALIDO', true);


-- Documentos Cliente PF 7 (Juliana Rocha)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (7, 'CPF', '789.012.345-67', 'VERIFICADO', true, true);


-- Documentos Cliente PF 8 (Roberto Alves - VIP)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (8, 'CPF', '890.123.456-78', 'VERIFICADO', true, true);

INSERT INTO documentos (cliente_id, tipo_documento, numero, orgao_emissor, data_emissao, status_documento, ativo)
VALUES (8, 'RG', '89.012.345-6', 'SSP/SP', '1995-08-10', 'VERIFICADO', true);

INSERT INTO documentos (cliente_id, tipo_documento, numero, orgao_emissor, data_emissao, data_validade, status_documento, ativo)
VALUES (8, 'CNH', '89012345678', 'DETRAN/SP', '2021-03-20', '2031-03-20', 'VALIDO', true);


-- Documentos Cliente PF 9 (Fernanda Lima)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (9, 'CPF', '901.234.567-89', 'VERIFICADO', true, true);


-- Documentos Cliente PF 10 (Ricardo Souza)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (10, 'CPF', '012.345.678-90', 'VERIFICADO', true, true);

INSERT INTO documentos (cliente_id, tipo_documento, numero, orgao_emissor, data_emissao, status_documento, ativo)
VALUES (10, 'RG', '01.234.567-8', 'SSP/RS', '2007-06-20', 'VERIFICADO', true);


-- Documentos Cliente PJ 11 (Móveis Estrela)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (11, 'CNPJ', '12.345.678/0001-90', 'VERIFICADO', true, true);

INSERT INTO documentos (cliente_id, tipo_documento, numero, data_emissao, status_documento, ativo)
VALUES (11, 'INSCRICAO_ESTADUAL', '123.456.789.012', '2015-03-15', 'VERIFICADO', true);


-- Documentos Cliente PJ 12 (Tech Solutions)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (12, 'CNPJ', '23.456.789/0001-01', 'VERIFICADO', true, true);

INSERT INTO documentos (cliente_id, tipo_documento, numero, data_emissao, status_documento, ativo)
VALUES (12, 'INSCRICAO_ESTADUAL', '234.567.890.123', '2018-07-20', 'VERIFICADO', true);


-- Documentos Cliente PJ 13 (Construtora Nova Era)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (13, 'CNPJ', '34.567.890/0001-12', 'VERIFICADO', true, true);

INSERT INTO documentos (cliente_id, tipo_documento, numero, data_emissao, status_documento, ativo)
VALUES (13, 'INSCRICAO_ESTADUAL', '345.678.901.234', '2010-01-25', 'VERIFICADO', true);

INSERT INTO documentos (cliente_id, tipo_documento, numero, data_emissao, status_documento, ativo)
VALUES (13, 'INSCRICAO_MUNICIPAL', '8765432', '2010-01-25', 'VERIFICADO', true);


-- Documentos Cliente PJ 14 (Design Interiores MEI)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (14, 'CNPJ', '45.678.901/0001-23', 'VERIFICADO', true, true);


-- Documentos Cliente PJ 15 (Hotel Boa Vista)
INSERT INTO documentos (cliente_id, tipo_documento, numero, status_documento, documento_principal, ativo)
VALUES (15, 'CNPJ', '56.789.012/0001-34', 'AGUARDANDO_VERIFICACAO', true, true);
