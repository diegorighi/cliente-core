-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Seed: Endereços dos Clientes
-- ========================================

-- Endereços Cliente PF 1 (Ana Silva)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (1, '01310-100', 'Avenida Paulista', '1000', 'Apto 501', 'Bela Vista', 'São Paulo', 'SP', 'RESIDENCIAL', true, true);

INSERT INTO enderecos (cliente_id, cep, logradouro, numero, bairro, cidade, estado, tipo_endereco, ativo)
VALUES (1, '01310-200', 'Rua Augusta', '500', 'Consolação', 'São Paulo', 'SP', 'COMERCIAL', true);


-- Endereços Cliente PF 2 (João Santos)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (2, '20040-020', 'Avenida Rio Branco', '200', 'Sala 1001', 'Centro', 'Rio de Janeiro', 'RJ', 'RESIDENCIAL', true, true);


-- Endereços Cliente PF 3 (Maria Oliveira)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (3, '30130-100', 'Rua da Bahia', '1200', 'Apto 302', 'Centro', 'Belo Horizonte', 'MG', 'RESIDENCIAL', true, true);

INSERT INTO enderecos (cliente_id, cep, logradouro, numero, bairro, cidade, estado, tipo_endereco, ativo)
VALUES (3, '30130-150', 'Avenida Afonso Pena', '800', 'Centro', 'Belo Horizonte', 'MG', 'ENTREGA', true);

INSERT INTO enderecos (cliente_id, cep, logradouro, numero, bairro, cidade, estado, tipo_endereco, ativo)
VALUES (3, '30130-200', 'Rua dos Carijós', '450', 'Centro', 'Belo Horizonte', 'MG', 'COBRANCA', true);


-- Endereços Cliente PF 4 (Pedro Costa)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (4, '01452-000', 'Rua Haddock Lobo', '300', 'Apto 801', 'Cerqueira César', 'São Paulo', 'SP', 'RESIDENCIAL', true, true);


-- Endereços Cliente PF 5 (Carla Mendes - bloqueada)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (5, '30220-000', 'Rua Paraíba', '1500', 'Casa', 'Funcionários', 'Belo Horizonte', 'MG', 'RESIDENCIAL', true, true);


-- Endereços Cliente PF 6 (Lucas Ferreira - ativo)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (6, '05426-100', 'Avenida Faria Lima', '2000', 'Apto 1202', 'Itaim Bibi', 'São Paulo', 'SP', 'RESIDENCIAL', true, true);

INSERT INTO enderecos (cliente_id, cep, logradouro, numero, bairro, cidade, estado, tipo_endereco, ativo)
VALUES (6, '05426-200', 'Rua Funchal', '500', 'Vila Olímpia', 'São Paulo', 'SP', 'ENTREGA', true);


-- Endereços Cliente PF 7 (Juliana Rocha)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (7, '30360-000', 'Rua Padre Rolim', '800', 'Apto 201', 'Santa Efigênia', 'Belo Horizonte', 'MG', 'RESIDENCIAL', true, true);


-- Endereços Cliente PF 8 (Roberto Alves - VIP)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (8, '01452-002', 'Rua Oscar Freire', '2500', 'Cobertura', 'Jardins', 'São Paulo', 'SP', 'RESIDENCIAL', true, true);

INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, ativo)
VALUES (8, '01310-300', 'Avenida Paulista', '3000', 'Conjunto 501', 'Bela Vista', 'São Paulo', 'SP', 'COMERCIAL', true);

INSERT INTO enderecos (cliente_id, cep, logradouro, numero, bairro, cidade, estado, tipo_endereco, ativo)
VALUES (8, '01452-003', 'Alameda Lorena', '1000', 'Jardins', 'São Paulo', 'SP', 'COLETA', true);


-- Endereços Cliente PF 9 (Fernanda Lima)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (9, '04543-011', 'Avenida Juscelino Kubitschek', '1500', 'Apto 403', 'Vila Nova Conceição', 'São Paulo', 'SP', 'RESIDENCIAL', true, true);


-- Endereços Cliente PF 10 (Ricardo Souza)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (10, '90040-000', 'Rua dos Andradas', '1200', 'Sala 501', 'Centro Histórico', 'Porto Alegre', 'RS', 'RESIDENCIAL', true, true);


-- Endereços Cliente PJ 11 (Móveis Estrela)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (11, '03302-000', 'Avenida Celso Garcia', '5000', 'Loja', 'Tatuapé', 'São Paulo', 'SP', 'COMERCIAL', true, true);

INSERT INTO enderecos (cliente_id, cep, logradouro, numero, bairro, cidade, estado, tipo_endereco, ativo)
VALUES (11, '03303-000', 'Rua Serra de Juréa', '100', 'Tatuapé', 'São Paulo', 'SP', 'COLETA', true);


-- Endereços Cliente PJ 12 (Tech Solutions)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (12, '04711-130', 'Avenida das Nações Unidas', '12000', 'Torre A - 10º andar', 'Brooklin', 'São Paulo', 'SP', 'COMERCIAL', true, true);

INSERT INTO enderecos (cliente_id, cep, logradouro, numero, bairro, cidade, estado, tipo_endereco, ativo)
VALUES (12, '04711-140', 'Rua Fidêncio Ramos', '300', 'Vila Olímpia', 'São Paulo', 'SP', 'ENTREGA', true);


-- Endereços Cliente PJ 13 (Construtora Nova Era)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (13, '30310-000', 'Avenida do Contorno', '4000', 'Sala 801', 'Funcionários', 'Belo Horizonte', 'MG', 'COMERCIAL', true, true);

INSERT INTO enderecos (cliente_id, cep, logradouro, numero, bairro, cidade, estado, tipo_endereco, ativo)
VALUES (13, '30310-100', 'Rua Curitiba', '800', 'Centro', 'Belo Horizonte', 'MG', 'COBRANCA', true);


-- Endereços Cliente PJ 14 (Design Interiores MEI)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, complemento, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (14, '01327-001', 'Rua Bela Cintra', '500', 'Conjunto 302', 'Consolação', 'São Paulo', 'SP', 'COMERCIAL', true, true);


-- Endereços Cliente PJ 15 (Hotel Boa Vista)
INSERT INTO enderecos (cliente_id, cep, logradouro, numero, bairro, cidade, estado, tipo_endereco, endereco_principal, ativo)
VALUES (15, '22070-000', 'Avenida Atlântica', '3000', 'Copacabana', 'Rio de Janeiro', 'RJ', 'COMERCIAL', true, true);
