-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Seed: Dados Bancários e PIX
-- ========================================

-- Dados bancários Cliente PF 1 (Ana Silva - Consignante)
INSERT INTO dados_bancarios (cliente_id, tipo_conta, banco, agencia, conta, digito_conta, chave_pix, tipo_chave_pix, dados_verificados, conta_principal, ativo)
VALUES (1, 'CORRENTE', 'Banco do Brasil', '1234', '56789', '0', '123.456.789-10', 'CPF', true, true, true);

INSERT INTO dados_bancarios (cliente_id, tipo_conta, banco, agencia, conta, digito_conta, chave_pix, tipo_chave_pix, dados_verificados, ativo)
VALUES (1, 'POUPANCA', 'Caixa Econômica Federal', '0987', '12345', '6', 'ana.silva@email.com', 'EMAIL', false, true);


-- Dados bancários Cliente PF 2 (João Santos - Comprador, não precisa)
-- Compradores não precisam de dados bancários


-- Dados bancários Cliente PF 3 (Maria Oliveira - Ambos)
INSERT INTO dados_bancarios (cliente_id, tipo_conta, banco, agencia, conta, digito_conta, chave_pix, tipo_chave_pix, dados_verificados, conta_principal, ativo)
VALUES (3, 'CORRENTE', 'Itaú Unibanco', '5678', '98765', '4', '345.678.901-23', 'CPF', true, true, true);

INSERT INTO dados_bancarios (cliente_id, tipo_conta, banco, agencia, conta, digito_conta, chave_pix, tipo_chave_pix, dados_verificados, ativo)
VALUES (3, 'CORRENTE', 'Nubank', '0001', '11223344', '5', '(31) 98888-7777', 'TELEFONE', true, true);


-- Dados bancários Cliente PF 4 (Pedro Costa - Prospecto, não precisa ainda)
-- Prospectos não têm dados bancários


-- Dados bancários Cliente PF 5 (Carla Mendes - Consignante bloqueada)
INSERT INTO dados_bancarios (cliente_id, tipo_conta, banco, agencia, conta, digito_conta, chave_pix, tipo_chave_pix, dados_verificados, conta_principal, ativo)
VALUES (5, 'CORRENTE', 'Bradesco', '2345', '67890', '1', '567.890.123-45', 'CPF', false, true, true);


-- Dados bancários Cliente PF 6 (Lucas Ferreira - Comprador, não precisa)
-- Compradores não precisam de dados bancários


-- Dados bancários Cliente PF 7 (Juliana Rocha - Consignante nova)
INSERT INTO dados_bancarios (cliente_id, tipo_conta, banco, agencia, conta, digito_conta, chave_pix, tipo_chave_pix, dados_verificados, conta_principal, ativo)
VALUES (7, 'CORRENTE', 'Santander', '3456', '54321', '9', 'juliana.rocha@email.com', 'EMAIL', true, true, true);


-- Dados bancários Cliente PF 8 (Roberto Alves - VIP Ambos)
INSERT INTO dados_bancarios (cliente_id, tipo_conta, banco, agencia, conta, digito_conta, chave_pix, tipo_chave_pix, dados_verificados, conta_principal, ativo)
VALUES (8, 'CORRENTE', 'Banco do Brasil', '9876', '11111', '2', '890.123.456-78', 'CPF', true, true, true);

INSERT INTO dados_bancarios (cliente_id, tipo_conta, banco, agencia, conta, digito_conta, chave_pix, tipo_chave_pix, dados_verificados, ativo)
VALUES (8, 'POUPANCA', 'Itaú Unibanco', '5432', '22222', '3', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'ALEATORIA', true, true);


-- Dados bancários Cliente PF 9 (Fernanda Lima - Prospecto, não precisa)
-- Prospectos não têm dados bancários


-- Dados bancários Cliente PF 10 (Ricardo Souza - Consignante)
INSERT INTO dados_bancarios (cliente_id, tipo_conta, banco, agencia, conta, digito_conta, chave_pix, tipo_chave_pix, dados_verificados, conta_principal, ativo)
VALUES (10, 'CORRENTE', 'Banco Inter', '0001', '99999', '8', '(51) 91111-0000', 'TELEFONE', true, true, true);


-- Dados bancários Cliente PJ 11 (Móveis Estrela - Consignante)
INSERT INTO dados_bancarios (cliente_id, tipo_conta, banco, agencia, conta, digito_conta, chave_pix, tipo_chave_pix, dados_verificados, conta_principal, ativo)
VALUES (11, 'CORRENTE', 'Banco do Brasil', '1111', '88888', '0', '12.345.678/0001-90', 'CNPJ', true, true, true);

INSERT INTO dados_bancarios (cliente_id, tipo_conta, banco, agencia, conta, digito_conta, chave_pix, tipo_chave_pix, dados_verificados, ativo)
VALUES (11, 'CORRENTE', 'Caixa Econômica Federal', '2222', '77777', '6', 'contato@moveisestrela.com.br', 'EMAIL', true, true);


-- Dados bancários Cliente PJ 12 (Tech Solutions - Comprador, não precisa)
-- Compradores corporativos não precisam de dados bancários


-- Dados bancários Cliente PJ 13 (Construtora Nova Era - Ambos)
INSERT INTO dados_bancarios (cliente_id, tipo_conta, banco, agencia, conta, digito_conta, chave_pix, tipo_chave_pix, dados_verificados, conta_principal, ativo)
VALUES (13, 'CORRENTE', 'Itaú Unibanco', '3333', '66666', '4', '34.567.890/0001-12', 'CNPJ', true, true, true);


-- Dados bancários Cliente PJ 14 (Design Interiores - Parceiro)
INSERT INTO dados_bancarios (cliente_id, tipo_conta, banco, agencia, conta, digito_conta, chave_pix, tipo_chave_pix, dados_verificados, conta_principal, ativo)
VALUES (14, 'CORRENTE', 'Nubank', '0001', '55555', '5', '45.678.901/0001-23', 'CNPJ', true, true, true);


-- Dados bancários Cliente PJ 15 (Hotel Boa Vista - Prospecto, não precisa ainda)
-- Prospectos corporativos não têm dados bancários
