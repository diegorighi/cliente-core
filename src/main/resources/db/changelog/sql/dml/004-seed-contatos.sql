-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Seed: Contatos dos Clientes
-- ========================================

-- Contatos Cliente PF 1 (Ana Silva)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (1, 'CELULAR', '(11) 98765-4321', true, true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (1, 'WHATSAPP', '(11) 98765-4321', true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (1, 'EMAIL', 'ana.silva@email.com', true, true);


-- Contatos Cliente PF 2 (João Santos)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (2, 'CELULAR', '(21) 99876-5432', true, true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (2, 'EMAIL', 'joao.santos@email.com', true, true);


-- Contatos Cliente PF 3 (Maria Oliveira)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (3, 'CELULAR', '(31) 98888-7777', true, true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (3, 'WHATSAPP', '(31) 98888-7777', true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (3, 'EMAIL', 'maria.oliveira@email.com', true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (3, 'TELEFONE_FIXO', '(31) 3333-4444', false, true);


-- Contatos Cliente PF 4 (Pedro Costa)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (4, 'CELULAR', '(11) 97777-6666', true, false, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (4, 'EMAIL', 'pedro.costa@email.com', false, true);


-- Contatos Cliente PF 5 (Carla Mendes - bloqueada)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (5, 'CELULAR', '(31) 96666-5555', true, false, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (5, 'EMAIL', 'carla.mendes@email.com', false, true);


-- Contatos Cliente PF 6 (Lucas Ferreira - ativo)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (6, 'CELULAR', '(11) 95555-4444', true, true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (6, 'WHATSAPP', '(11) 95555-4444', true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (6, 'EMAIL', 'lucas.ferreira@email.com', true, true);


-- Contatos Cliente PF 7 (Juliana Rocha)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (7, 'CELULAR', '(31) 94444-3333', true, true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (7, 'EMAIL', 'juliana.rocha@email.com', true, true);


-- Contatos Cliente PF 8 (Roberto Alves - VIP)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (8, 'CELULAR', '(11) 93333-2222', true, true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (8, 'WHATSAPP', '(11) 93333-2222', true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (8, 'EMAIL', 'roberto.alves@email.com', true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (8, 'TELEFONE_FIXO', '(11) 2222-3333', true, true);


-- Contatos Cliente PF 9 (Fernanda Lima)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (9, 'CELULAR', '(11) 92222-1111', true, true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (9, 'EMAIL', 'fernanda.lima@email.com', true, true);


-- Contatos Cliente PF 10 (Ricardo Souza)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (10, 'CELULAR', '(51) 91111-0000', true, true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (10, 'WHATSAPP', '(51) 91111-0000', true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (10, 'EMAIL', 'ricardo.souza@email.com', true, true);


-- Contatos Cliente PJ 11 (Móveis Estrela)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (11, 'TELEFONE_FIXO', '(11) 4444-5555', true, true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (11, 'CELULAR', '(11) 99999-8888', true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (11, 'EMAIL', 'contato@moveisestrela.com.br', true, true);


-- Contatos Cliente PJ 12 (Tech Solutions)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (12, 'TELEFONE_FIXO', '(11) 5555-6666', true, true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (12, 'EMAIL', 'compras@techsolutions.com.br', true, true);


-- Contatos Cliente PJ 13 (Construtora Nova Era)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (13, 'TELEFONE_FIXO', '(31) 6666-7777', true, true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (13, 'CELULAR', '(31) 98888-9999', true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (13, 'EMAIL', 'financeiro@construtoraera.com.br', true, true);


-- Contatos Cliente PJ 14 (Design Interiores MEI)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (14, 'CELULAR', '(11) 97777-8888', true, true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (14, 'WHATSAPP', '(11) 97777-8888', true, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (14, 'EMAIL', 'contato@designinteriores.com.br', true, true);


-- Contatos Cliente PJ 15 (Hotel Boa Vista)
INSERT INTO contatos (cliente_id, tipo_contato, valor, contato_principal, verificado, ativo)
VALUES (15, 'TELEFONE_FIXO', '(21) 7777-8888', true, false, true);

INSERT INTO contatos (cliente_id, tipo_contato, valor, verificado, ativo)
VALUES (15, 'EMAIL', 'gerencia@hotelboavista.com.br', false, true);
