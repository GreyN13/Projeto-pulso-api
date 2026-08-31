-- Contas de demonstração. A senha de acesso está documentada no README e é armazenada somente como BCrypt.
INSERT INTO usuario (nome, email, senha, criado_em) VALUES
    ('Gray Fox', 'gray.fox@pulso.com', '$2a$10$Q8Abg5N/2fgVS9qTT1BLTeiQ0t1iVwovU/rcITYaaJNbq3Khwdb3C', CURRENT_TIMESTAMP),
    ('Solid Snake', 'solid.snake@pulso.com', '$2a$10$6nADZsSZrlnsmEYepGeJAeneh/IjFeXj8ZIfYb8UErpOmBvcZid.e', CURRENT_TIMESTAMP),
    ('Sam Porter', 'sam.porter@pulso.com', '$2a$10$ikyPYUOijc/ij0byZi6Qf.2/NaWR8QHpPGZUv3ealCKaOA03iPGjG', CURRENT_TIMESTAMP),
    ('Raiden', 'raiden@pulso.com', '$2a$10$8IygjPRjOZny1SXPuiLvt.hClVEhEno0pFJJ/xooDDsH8KpLfyQky', CURRENT_TIMESTAMP),
    ('Kojima', 'kojima@pulso.com', '$2a$10$dws9oi5UuT2sz7S6eyz//uf77SSSoKpUJRz81tm1FkkUTXKQ8ESYy', CURRENT_TIMESTAMP);
