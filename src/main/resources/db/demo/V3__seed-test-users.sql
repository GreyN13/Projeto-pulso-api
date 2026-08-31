-- Contas de demonstração. As senhas abaixo estão armazenadas exclusivamente como hashes BCrypt.
INSERT INTO usuario (nome, email, senha, criado_em) VALUES
    ('Gray Fox', 'gray.fox@pulso.com', '$2a$10$f.oiNbE22YhktsA1nNj7bea0oZbp9DCDEcDRaQ6gDpSBiHm1wXLv.', CURRENT_TIMESTAMP),
    ('Solid Snake', 'solid.snake@pulso.com', '$2a$10$10dYxwv2f4dGsPAqYlBA0ed6x88A.DAnLmOAdIFrlVJX5aUeQ7A0.', CURRENT_TIMESTAMP),
    ('Sam Porter', 'sam.porter@pulso.com', '$2a$10$1imXi4z.MYUi0KFUz.c4qeCDJ1CnEu3WHJ8qVLZA02Zta4gm5gJTC', CURRENT_TIMESTAMP),
    ('Raiden', 'raiden@pulso.com', '$2a$10$48RnT.ajMMm6w9qMH9sFQ.1QFUFTFTigSJB0mTGfIWvCOZLi7YNeC', CURRENT_TIMESTAMP),
    ('Kojima', 'kojima@pulso.com', '$2a$10$Mr0akXeSA.2D/1k4eBQjW./QalaVbdWuFazf5Z4OrcafvuS1Gog6K', CURRENT_TIMESTAMP);
