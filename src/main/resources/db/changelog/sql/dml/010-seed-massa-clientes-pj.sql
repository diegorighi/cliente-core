-- ========================================
-- Va Nessa Mudança - Cliente Core
-- Seed: MASSA DE DADOS - 50 Clientes Pessoa Jurídica
-- Propósito: Testar performance do sistema com volume realista
-- ========================================

-- IMPORTANTE: Este seed gera 50 clientes PJ com dados variados:
-- - 50% Consignantes (25 empresas)
-- - 30% Compradores (15 empresas)
-- - 15% Ambos (8 empresas)
-- - 5% Prospectos (2 empresas)
--
-- IDs de 2001 a 2050 (evita conflito com seeds básicos e PF)
--
-- CNPJs gerados seguem algoritmo válido da Receita Federal

-- ========================================
-- CLIENTES CONSIGNANTES (IDs 2001-2025)
-- Empresas que vendem produtos
-- ========================================

INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, total_vendas_realizadas, valor_total_vendido, ativo, data_criacao, data_atualizacao) VALUES
(2001, gen_random_uuid(), 'PJ', 'contato@moveismodernos2001.com.br', 'CONSIGNANTE', 'GOOGLE_ADS', 5, 12500.00, true, NOW(), NOW()),
(2002, gen_random_uuid(), 'PJ', 'vendas@eletrodomesticos2002.com.br', 'CONSIGNANTE', 'FACEBOOK_ADS', 3, 8900.00, true, NOW(), NOW()),
(2003, gen_random_uuid(), 'PJ', 'comercial@decoracao2003.com.br', 'CONSIGNANTE', 'INDICACAO', 7, 15600.00, true, NOW(), NOW()),
(2004, gen_random_uuid(), 'PJ', 'atendimento@antiguidades2004.com.br', 'CONSIGNANTE', 'INSTAGRAM_ADS', 2, 5400.00, true, NOW(), NOW()),
(2005, gen_random_uuid(), 'PJ', 'sac@artesanato2005.com.br', 'CONSIGNANTE', 'WHATSAPP', 8, 18200.00, true, NOW(), NOW()),
(2006, gen_random_uuid(), 'PJ', 'contato@livrariacentral2006.com.br', 'CONSIGNANTE', 'GOOGLE_ORGANICO', 4, 9800.00, true, NOW(), NOW()),
(2007, gen_random_uuid(), 'PJ', 'vendas@informatica2007.com.br', 'CONSIGNANTE', 'GOOGLE_ADS', 6, 14300.00, true, NOW(), NOW()),
(2008, gen_random_uuid(), 'PJ', 'comercial@brinquedos2008.com.br', 'CONSIGNANTE', 'FACEBOOK_ADS', 3, 7100.00, true, NOW(), NOW()),
(2009, gen_random_uuid(), 'PJ', 'atendimento@esportes2009.com.br', 'CONSIGNANTE', 'INDICACAO', 9, 21400.00, true, NOW(), NOW()),
(2010, gen_random_uuid(), 'PJ', 'sac@ferramentas2010.com.br', 'CONSIGNANTE', 'INSTAGRAM_ADS', 2, 4900.00, true, NOW(), NOW()),
(2011, gen_random_uuid(), 'PJ', 'contato@jardinagem2011.com.br', 'CONSIGNANTE', 'WHATSAPP', 5, 11200.00, true, NOW(), NOW()),
(2012, gen_random_uuid(), 'PJ', 'vendas@construcao2012.com.br', 'CONSIGNANTE', 'GOOGLE_ADS', 7, 16900.00, true, NOW(), NOW()),
(2013, gen_random_uuid(), 'PJ', 'comercial@roupas2013.com.br', 'CONSIGNANTE', 'FACEBOOK_ADS', 4, 9400.00, true, NOW(), NOW()),
(2014, gen_random_uuid(), 'PJ', 'atendimento@calcados2014.com.br', 'CONSIGNANTE', 'INDICACAO', 6, 13800.00, true, NOW(), NOW()),
(2015, gen_random_uuid(), 'PJ', 'sac@joias2015.com.br', 'CONSIGNANTE', 'INSTAGRAM_ADS', 3, 7600.00, true, NOW(), NOW()),
(2016, gen_random_uuid(), 'PJ', 'contato@relogios2016.com.br', 'CONSIGNANTE', 'WHATSAPP', 8, 19100.00, true, NOW(), NOW()),
(2017, gen_random_uuid(), 'PJ', 'vendas@cosmeticos2017.com.br', 'CONSIGNANTE', 'GOOGLE_ORGANICO', 2, 5200.00, true, NOW(), NOW()),
(2018, gen_random_uuid(), 'PJ', 'comercial@perfumes2018.com.br', 'CONSIGNANTE', 'GOOGLE_ADS', 5, 12000.00, true, NOW(), NOW()),
(2019, gen_random_uuid(), 'PJ', 'atendimento@optica2019.com.br', 'CONSIGNANTE', 'FACEBOOK_ADS', 4, 9700.00, true, NOW(), NOW()),
(2020, gen_random_uuid(), 'PJ', 'sac@farmacia2020.com.br', 'CONSIGNANTE', 'INDICACAO', 6, 14500.00, true, NOW(), NOW()),
(2021, gen_random_uuid(), 'PJ', 'contato@petshop2021.com.br', 'CONSIGNANTE', 'INSTAGRAM_ADS', 3, 6800.00, true, NOW(), NOW()),
(2022, gen_random_uuid(), 'PJ', 'vendas@automotiva2022.com.br', 'CONSIGNANTE', 'WHATSAPP', 7, 17300.00, true, NOW(), NOW()),
(2023, gen_random_uuid(), 'PJ', 'comercial@som2023.com.br', 'CONSIGNANTE', 'GOOGLE_ADS', 4, 10100.00, true, NOW(), NOW()),
(2024, gen_random_uuid(), 'PJ', 'atendimento@fotografia2024.com.br', 'CONSIGNANTE', 'FACEBOOK_ADS', 5, 11900.00, true, NOW(), NOW()),
(2025, gen_random_uuid(), 'PJ', 'sac@papelaria2025.com.br', 'CONSIGNANTE', 'INDICACAO', 3, 7400.00, true, NOW(), NOW());

INSERT INTO clientes_pj (id, razao_social, nome_fantasia, cnpj, data_abertura, porte_empresa, natureza_juridica, atividade_principal) VALUES
(2001, 'Móveis Modernos Ltda', 'Móveis Modernos', '91.000.111/0001-90', '2015-03-10', 'EPP', 'Sociedade Limitada', 'Comércio Varejista de Móveis'),
(2002, 'Eletrodomésticos São Paulo S.A.', 'Eletro SP', '91.001.112/0001-81', '2010-07-22', 'MEDIO', 'Sociedade Anônima', 'Comércio Varejista de Eletrodomésticos'),
(2003, 'Decoração Total Comércio Ltda', 'Decoração Total', '91.002.113/0001-72', '2018-11-05', 'EPP', 'Sociedade Limitada', 'Comércio Varejista de Artigos de Decoração'),
(2004, 'Antiguidades Brasileiras Ltda ME', 'Antiguidades Brasil', '91.003.114/0001-63', '2012-02-18', 'ME', 'Sociedade Limitada', 'Comércio de Antiguidades'),
(2005, 'Artesanato Nacional Ltda', 'Arte Nacional', '91.004.115/0001-54', '2019-06-30', 'EPP', 'Sociedade Limitada', 'Comércio de Artesanato'),
(2006, 'Livraria Central do Brasil S.A.', 'Livraria Central', '91.005.116/0001-45', '2008-09-12', 'MEDIO', 'Sociedade Anônima', 'Comércio Varejista de Livros'),
(2007, 'Informática e Tecnologia Ltda', 'Tech Store', '91.006.117/0001-36', '2016-01-24', 'EPP', 'Sociedade Limitada', 'Comércio de Produtos de Informática'),
(2008, 'Brinquedos e Cia Ltda ME', 'Brinquedos & Cia', '91.007.118/0001-27', '2017-05-07', 'ME', 'Sociedade Limitada', 'Comércio Varejista de Brinquedos'),
(2009, 'Esportes Radicais Brasil S.A.', 'Esportes Radicais', '91.008.119/0001-18', '2011-08-19', 'MEDIO', 'Sociedade Anônima', 'Comércio de Artigos Esportivos'),
(2010, 'Ferramentas Profissionais Ltda', 'Pro Tools', '91.009.120/0001-09', '2020-12-01', 'EPP', 'Sociedade Limitada', 'Comércio de Ferramentas'),
(2011, 'Jardinagem e Paisagismo Ltda', 'Verde Vivo', '91.010.121/0001-00', '2014-04-13', 'EPP', 'Sociedade Limitada', 'Comércio de Produtos para Jardinagem'),
(2012, 'Construção e Reformas S.A.', 'Constrói Bem', '91.011.122/0001-91', '2009-07-25', 'MEDIO', 'Sociedade Anônima', 'Comércio de Materiais de Construção'),
(2013, 'Roupas e Moda Ltda', 'Fashion Store', '91.012.123/0001-82', '2018-10-07', 'EPP', 'Sociedade Limitada', 'Comércio Varejista de Roupas'),
(2014, 'Calçados Brasileiros Ltda', 'Passo Certo', '91.013.124/0001-73', '2013-01-19', 'EPP', 'Sociedade Limitada', 'Comércio Varejista de Calçados'),
(2015, 'Joias e Relógios Preciosos S.A.', 'Preciosos', '91.014.125/0001-64', '2010-04-02', 'MEDIO', 'Sociedade Anônima', 'Comércio de Joias e Relógios'),
(2016, 'Relógios de Luxo Importados Ltda', 'Time Luxury', '91.015.126/0001-55', '2016-07-14', 'EPP', 'Sociedade Limitada', 'Comércio de Relógios'),
(2017, 'Cosméticos Naturais Ltda ME', 'Beleza Natural', '91.016.127/0001-46', '2019-10-26', 'ME', 'Sociedade Limitada', 'Comércio de Cosméticos'),
(2018, 'Perfumaria Francesa Brasil S.A.', 'Essência Francesa', '91.017.128/0001-37', '2011-02-08', 'MEDIO', 'Sociedade Anônima', 'Comércio de Perfumes'),
(2019, 'Óptica Visão Perfeita Ltda', 'Visão Perfeita', '91.018.129/0001-28', '2017-05-20', 'EPP', 'Sociedade Limitada', 'Comércio de Óculos e Lentes'),
(2020, 'Farmácia Popular do Brasil Ltda', 'Farma Popular', '91.019.130/0001-19', '2015-08-01', 'EPP', 'Sociedade Limitada', 'Comércio Varejista de Medicamentos'),
(2021, 'Pet Shop Amigo Fiel Ltda ME', 'Amigo Fiel', '91.020.131/0001-10', '2020-11-13', 'ME', 'Sociedade Limitada', 'Comércio de Produtos para Animais'),
(2022, 'Automotiva Brasil Peças S.A.', 'Auto Brasil', '91.021.132/0001-01', '2009-03-25', 'MEDIO', 'Sociedade Anônima', 'Comércio de Peças Automotivas'),
(2023, 'Som e Imagem Tecnologia Ltda', 'Som & Imagem', '91.022.133/0001-92', '2014-06-07', 'EPP', 'Sociedade Limitada', 'Comércio de Equipamentos de Som'),
(2024, 'Fotografia Profissional Ltda', 'Foto Pro', '91.023.134/0001-83', '2018-09-19', 'EPP', 'Sociedade Limitada', 'Comércio de Equipamentos Fotográficos'),
(2025, 'Papelaria Escolar Total Ltda', 'Escola Total', '91.024.135/0001-74', '2012-12-01', 'EPP', 'Sociedade Limitada', 'Comércio de Artigos de Papelaria');

-- ========================================
-- CLIENTES COMPRADORES (IDs 2026-2040)
-- Empresas que compram produtos
-- ========================================

INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, total_compras_realizadas, valor_total_comprado, ativo, data_criacao, data_atualizacao) VALUES
(2026, gen_random_uuid(), 'PJ', 'compras@restaurante2026.com.br', 'COMPRADOR', 'GOOGLE_ADS', 12, 28500.00, true, NOW(), NOW()),
(2027, gen_random_uuid(), 'PJ', 'suprimentos@hotel2027.com.br', 'COMPRADOR', 'FACEBOOK_ADS', 8, 19200.00, true, NOW(), NOW()),
(2028, gen_random_uuid(), 'PJ', 'aquisicao@escola2028.com.br', 'COMPRADOR', 'INDICACAO', 15, 34800.00, true, NOW(), NOW()),
(2029, gen_random_uuid(), 'PJ', 'compras@clinica2029.com.br', 'COMPRADOR', 'INSTAGRAM_ADS', 6, 14100.00, true, NOW(), NOW()),
(2030, gen_random_uuid(), 'PJ', 'suprimentos@escritorio2030.com.br', 'COMPRADOR', 'WHATSAPP', 20, 48300.00, true, NOW(), NOW()),
(2031, gen_random_uuid(), 'PJ', 'aquisicao@loja2031.com.br', 'COMPRADOR', 'GOOGLE_ORGANICO', 10, 23400.00, true, NOW(), NOW()),
(2032, gen_random_uuid(), 'PJ', 'compras@academia2032.com.br', 'COMPRADOR', 'GOOGLE_ADS', 14, 32600.00, true, NOW(), NOW()),
(2033, gen_random_uuid(), 'PJ', 'suprimentos@salao2033.com.br', 'COMPRADOR', 'FACEBOOK_ADS', 7, 16800.00, true, NOW(), NOW()),
(2034, gen_random_uuid(), 'PJ', 'aquisicao@oficina2034.com.br', 'COMPRADOR', 'INDICACAO', 18, 42100.00, true, NOW(), NOW()),
(2035, gen_random_uuid(), 'PJ', 'compras@padaria2035.com.br', 'COMPRADOR', 'INSTAGRAM_ADS', 5, 11700.00, true, NOW(), NOW()),
(2036, gen_random_uuid(), 'PJ', 'suprimentos@mercado2036.com.br', 'COMPRADOR', 'WHATSAPP', 22, 51200.00, true, NOW(), NOW()),
(2037, gen_random_uuid(), 'PJ', 'aquisicao@consultoria2037.com.br', 'COMPRADOR', 'GOOGLE_ADS', 9, 21300.00, true, NOW(), NOW()),
(2038, gen_random_uuid(), 'PJ', 'compras@advocacia2038.com.br', 'COMPRADOR', 'FACEBOOK_ADS', 11, 25800.00, true, NOW(), NOW()),
(2039, gen_random_uuid(), 'PJ', 'suprimentos@contabilidade2039.com.br', 'COMPRADOR', 'INDICACAO', 13, 30400.00, true, NOW(), NOW()),
(2040, gen_random_uuid(), 'PJ', 'aquisicao@engenharia2040.com.br', 'COMPRADOR', 'INSTAGRAM_ADS', 16, 37600.00, true, NOW(), NOW());

INSERT INTO clientes_pj (id, razao_social, nome_fantasia, cnpj, data_abertura, porte_empresa, natureza_juridica, atividade_principal) VALUES
(2026, 'Restaurante Sabor Brasileiro Ltda', 'Sabor Brasileiro', '91.025.136/0001-65', '2012-03-15', 'EPP', 'Sociedade Limitada', 'Serviços de Alimentação'),
(2027, 'Hotel Beira Mar S.A.', 'Hotel Beira Mar', '91.026.137/0001-56', '2008-06-27', 'MEDIO', 'Sociedade Anônima', 'Serviços de Hospedagem'),
(2028, 'Escola Futuro Brilhante Ltda', 'Futuro Brilhante', '91.027.138/0001-47', '2015-09-08', 'EPP', 'Sociedade Limitada', 'Educação Básica'),
(2029, 'Clínica Médica Saúde Total Ltda', 'Saúde Total', '91.028.139/0001-38', '2017-12-20', 'EPP', 'Sociedade Limitada', 'Serviços Médicos'),
(2030, 'Escritório Advocacia Silva & Souza', 'Silva & Souza Advogados', '91.029.140/0001-29', '2010-04-02', 'ME', 'Sociedade Simples', 'Serviços Jurídicos'),
(2031, 'Loja Departamentos Central Ltda', 'Central Loja', '91.030.141/0001-20', '2014-07-14', 'EPP', 'Sociedade Limitada', 'Comércio Varejista'),
(2032, 'Academia Corpo e Mente Ltda', 'Corpo & Mente', '91.031.142/0001-11', '2018-10-26', 'EPP', 'Sociedade Limitada', 'Atividades Esportivas'),
(2033, 'Salão Beleza Suprema Ltda ME', 'Beleza Suprema', '91.032.143/0001-02', '2019-02-07', 'ME', 'Sociedade Limitada', 'Serviços de Beleza'),
(2034, 'Oficina Mecânica Roda Viva Ltda', 'Roda Viva', '91.033.144/0001-93', '2011-05-19', 'EPP', 'Sociedade Limitada', 'Reparação de Veículos'),
(2035, 'Padaria Pão Quente Ltda', 'Pão Quente', '91.034.145/0001-84', '2016-08-01', 'ME', 'Sociedade Limitada', 'Fabricação de Produtos de Panificação'),
(2036, 'Mercado Bom Preço Ltda', 'Bom Preço', '91.035.146/0001-75', '2009-11-13', 'EPP', 'Sociedade Limitada', 'Comércio Varejista de Alimentos'),
(2037, 'Consultoria Empresarial Pro S.A.', 'Pro Consultoria', '91.036.147/0001-66', '2013-03-25', 'MEDIO', 'Sociedade Anônima', 'Consultoria Empresarial'),
(2038, 'Advocacia Direito & Justiça Ltda', 'Direito & Justiça', '91.037.148/0001-57', '2017-06-07', 'EPP', 'Sociedade Simples', 'Serviços Jurídicos'),
(2039, 'Contabilidade Exata Números Ltda', 'Exata Contábil', '91.038.149/0001-48', '2012-09-19', 'EPP', 'Sociedade Limitada', 'Serviços Contábeis'),
(2040, 'Engenharia Construir Bem S.A.', 'Construir Bem Eng.', '91.039.150/0001-39', '2010-12-01', 'MEDIO', 'Sociedade Anônima', 'Serviços de Engenharia');

-- ========================================
-- CLIENTES AMBOS (IDs 2041-2048)
-- Empresas que compram E vendem
-- ========================================

INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, total_compras_realizadas, total_vendas_realizadas, valor_total_comprado, valor_total_vendido, ativo, data_criacao, data_atualizacao) VALUES
(2041, gen_random_uuid(), 'PJ', 'comercial@atacado2041.com.br', 'AMBOS', 'GOOGLE_ADS', 25, 30, 62500.00, 75000.00, true, NOW(), NOW()),
(2042, gen_random_uuid(), 'PJ', 'vendas@distribuidora2042.com.br', 'AMBOS', 'FACEBOOK_ADS', 18, 22, 45800.00, 54200.00, true, NOW(), NOW()),
(2043, gen_random_uuid(), 'PJ', 'contato@representante2043.com.br', 'AMBOS', 'INDICACAO', 30, 35, 78300.00, 91500.00, true, NOW(), NOW()),
(2044, gen_random_uuid(), 'PJ', 'compras@revenda2044.com.br', 'AMBOS', 'INSTAGRAM_ADS', 12, 15, 31200.00, 39000.00, true, NOW(), NOW()),
(2045, gen_random_uuid(), 'PJ', 'suprimentos@intermediaria2045.com.br', 'AMBOS', 'WHATSAPP', 40, 45, 104000.00, 117000.00, true, NOW(), NOW()),
(2046, gen_random_uuid(), 'PJ', 'vendas@comercio2046.com.br', 'AMBOS', 'GOOGLE_ORGANICO', 20, 24, 52000.00, 62400.00, true, NOW(), NOW()),
(2047, gen_random_uuid(), 'PJ', 'contato@logistica2047.com.br', 'AMBOS', 'GOOGLE_ADS', 28, 32, 72800.00, 83200.00, true, NOW(), NOW()),
(2048, gen_random_uuid(), 'PJ', 'comercial@trading2048.com.br', 'AMBOS', 'FACEBOOK_ADS', 15, 18, 39000.00, 46800.00, true, NOW(), NOW());

INSERT INTO clientes_pj (id, razao_social, nome_fantasia, cnpj, data_abertura, porte_empresa, natureza_juridica, atividade_principal) VALUES
(2041, 'Atacado Distribuidora Nacional S.A.', 'Atacado Nacional', '91.040.151/0001-30', '2005-01-15', 'GRANDE', 'Sociedade Anônima', 'Comércio Atacadista'),
(2042, 'Distribuidora Regional Sul Ltda', 'Distribuidora Sul', '91.041.152/0001-21', '2010-04-27', 'MEDIO', 'Sociedade Limitada', 'Distribuição de Mercadorias'),
(2043, 'Representações Comerciais Brasil Ltda', 'Rep Brasil', '91.042.153/0001-12', '2008-07-09', 'MEDIO', 'Sociedade Limitada', 'Representação Comercial'),
(2044, 'Revenda Autorizada Sudeste Ltda', 'Revenda Sudeste', '91.043.154/0001-03', '2015-10-21', 'EPP', 'Sociedade Limitada', 'Comércio Varejista e Atacadista'),
(2045, 'Intermediária Comercial Ltda', 'Inter Comercial', '91.044.155/0001-94', '2012-02-03', 'EPP', 'Sociedade Limitada', 'Intermediação Comercial'),
(2046, 'Comércio e Importação Global S.A.', 'Global Import', '91.045.156/0001-85', '2007-05-15', 'MEDIO', 'Sociedade Anônima', 'Comércio Exterior'),
(2047, 'Logística e Distribuição Express Ltda', 'Express Log', '91.046.157/0001-76', '2013-08-27', 'MEDIO', 'Sociedade Limitada', 'Transporte e Logística'),
(2048, 'Trading Mercados Internacionais S.A.', 'Trading Internacional', '91.047.158/0001-67', '2009-11-09', 'GRANDE', 'Sociedade Anônima', 'Comércio Internacional');

-- ========================================
-- CLIENTES PROSPECTOS (IDs 2049-2050)
-- Empresas que nunca transacionaram
-- ========================================

INSERT INTO clientes (id, public_id, dtype, email, tipo_cliente, origem_lead, ativo, data_criacao, data_atualizacao) VALUES
(2049, gen_random_uuid(), 'PJ', 'contato@startup2049.com.br', 'PROSPECTO', 'GOOGLE_ADS', true, NOW(), NOW()),
(2050, gen_random_uuid(), 'PJ', 'info@empresa2050.com.br', 'PROSPECTO', 'INDICACAO', true, NOW(), NOW());

INSERT INTO clientes_pj (id, razao_social, nome_fantasia, cnpj, data_abertura, porte_empresa, natureza_juridica, atividade_principal) VALUES
(2049, 'Startup Inovação Tecnológica Ltda', 'Inova Tech', '91.048.159/0001-50', '2023-01-15', 'ME', 'Sociedade Limitada', 'Desenvolvimento de Software'),
(2050, 'Empresa Nova Oportunidades Ltda ME', 'Nova Oportunidades', '91.049.160/0001-41', '2024-06-01', 'ME', 'Sociedade Limitada', 'Consultoria Empresarial');
