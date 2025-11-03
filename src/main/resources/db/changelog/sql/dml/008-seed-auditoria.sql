-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Seed: Histórico de Auditoria
-- ========================================

-- Auditoria Cliente PF 1 (Ana Silva)
INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (1, 'email', 'ana.silva.old@email.com', 'ana.silva@email.com', 'admin@vanessa.com', NOW() - INTERVAL '85 days', 'Cliente solicitou atualização de email', '187.123.45.67');

INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (1, 'tipo_cliente', 'PROSPECTO', 'CONSIGNANTE', 'sistema@vanessa.com', NOW() - INTERVAL '80 days', 'Cliente realizou primeira venda', '0.0.0.0');


-- Auditoria Cliente PF 3 (Maria Oliveira - cliente ativo)
INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (3, 'tipo_cliente', 'CONSIGNANTE', 'AMBOS', 'sistema@vanessa.com', NOW() - INTERVAL '60 days', 'Cliente realizou primeira compra', '0.0.0.0');

INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (3, 'indicacao_recompensada', 'false', 'true', 'financeiro@vanessa.com', NOW() - INTERVAL '25 days', 'Crédito de R$50 concedido ao indicador', '192.168.10.5');


-- Auditoria Cliente PF 5 (Carla Mendes - bloqueada)
INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (5, 'bloqueado', 'false', 'true', 'compliance@vanessa.com', NOW() - INTERVAL '5 days', 'Bloqueio preventivo por documentação pendente', '192.168.10.10');

INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (5, 'motivo_bloqueio', NULL, 'Documentos pendentes de verificação', 'compliance@vanessa.com', NOW() - INTERVAL '5 days', 'Detalhamento do bloqueio', '192.168.10.10');


-- Auditoria Cliente PF 6 (Lucas Ferreira - comprador ativo)
INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (6, 'tipo_cliente', 'PROSPECTO', 'COMPRADOR', 'sistema@vanessa.com', NOW() - INTERVAL '90 days', 'Cliente realizou primeira compra', '0.0.0.0');


-- Auditoria Cliente PF 8 (Roberto Alves - VIP)
INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (8, 'tipo_cliente', 'COMPRADOR', 'AMBOS', 'sistema@vanessa.com', NOW() - INTERVAL '150 days', 'Cliente iniciou vendas também', '0.0.0.0');

INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (8, 'observacoes', NULL, 'Cliente VIP - atendimento prioritário', 'comercial@vanessa.com', NOW() - INTERVAL '100 days', 'Marcação como cliente VIP', '192.168.10.15');


-- Auditoria Cliente PF 10 (Ricardo Souza)
INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (10, 'tipo_cliente', 'PROSPECTO', 'CONSIGNANTE', 'sistema@vanessa.com', NOW() - INTERVAL '14 days', 'Cliente realizou primeira venda por indicação', '0.0.0.0');


-- Auditoria Cliente PJ 11 (Móveis Estrela)
INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (11, 'nome_responsavel', 'Carlos Silva', 'Carlos Alberto Silva', 'admin@vanessa.com', NOW() - INTERVAL '50 days', 'Correção do nome completo do responsável', '187.200.100.50');

INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (11, 'email', 'contato.old@moveisestrela.com.br', 'contato@moveisestrela.com.br', 'admin@vanessa.com', NOW() - INTERVAL '45 days', 'Atualização de domínio de email', '187.200.100.50');


-- Auditoria Cliente PJ 12 (Tech Solutions)
INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (12, 'tipo_cliente', 'PROSPECTO', 'COMPRADOR', 'sistema@vanessa.com', NOW() - INTERVAL '60 days', 'Cliente realizou primeira compra corporativa', '0.0.0.0');


-- Auditoria Cliente PJ 13 (Construtora Nova Era)
INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (13, 'tipo_cliente', 'CONSIGNANTE', 'AMBOS', 'sistema@vanessa.com', NOW() - INTERVAL '90 days', 'Cliente passou a comprar também', '0.0.0.0');

INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (13, 'capital_social', '200000.00', '250000.00', 'admin@vanessa.com', NOW() - INTERVAL '30 days', 'Atualização de capital social conforme alteração contratual', '179.100.50.25');


-- Auditoria Cliente PJ 14 (Design Interiores MEI)
INSERT INTO auditoria_cliente (cliente_id, campo_alterado, valor_anterior, valor_novo, usuario_responsavel, data_alteracao, motivo_alteracao, ip_origem)
VALUES (14, 'tipo_cliente', 'PROSPECTO', 'PARCEIRO', 'comercial@vanessa.com', NOW() - INTERVAL '40 days', 'Cliente firmou parceria de indicação', '189.150.75.125');
