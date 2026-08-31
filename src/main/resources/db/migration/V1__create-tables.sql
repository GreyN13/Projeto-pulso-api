CREATE TABLE usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    criado_em DATETIME NOT NULL,
    CONSTRAINT uk_usuario_email UNIQUE (email)
);

CREATE TABLE artista (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    biografia TEXT,
    imagem_url VARCHAR(500)
);

CREATE TABLE album (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(150) NOT NULL,
    capa_url VARCHAR(500),
    ano INT NOT NULL,
    artista_id BIGINT NOT NULL,
    CONSTRAINT fk_album_artista FOREIGN KEY (artista_id) REFERENCES artista(id)
);

CREATE TABLE musica (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(150) NOT NULL,
    arquivo_url VARCHAR(500) NOT NULL,
    duracao_segundos INT NOT NULL,
    album_id BIGINT NOT NULL,
    CONSTRAINT fk_musica_album FOREIGN KEY (album_id) REFERENCES album(id)
);

CREATE TABLE playlist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(80) NOT NULL,
    descricao VARCHAR(500),
    criada_em DATETIME NOT NULL,
    CONSTRAINT fk_playlist_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE TABLE favorito (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    musica_id BIGINT NOT NULL,
    criado_em DATETIME NOT NULL,
    CONSTRAINT fk_favorito_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_favorito_musica FOREIGN KEY (musica_id) REFERENCES musica(id),
    CONSTRAINT uk_favorito_usuario_musica UNIQUE (usuario_id, musica_id)
);

CREATE TABLE reproducao (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    musica_id BIGINT NOT NULL,
    reproduzida_em DATETIME NOT NULL,
    CONSTRAINT fk_reproducao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_reproducao_musica FOREIGN KEY (musica_id) REFERENCES musica(id)
);

CREATE TABLE playlist_musica (
    playlist_id BIGINT NOT NULL,
    musica_id BIGINT NOT NULL,
    PRIMARY KEY (playlist_id, musica_id),
    CONSTRAINT fk_playlist_musica_playlist FOREIGN KEY (playlist_id) REFERENCES playlist(id),
    CONSTRAINT fk_playlist_musica_musica FOREIGN KEY (musica_id) REFERENCES musica(id)
);

CREATE INDEX idx_musica_titulo ON musica(titulo);
CREATE INDEX idx_artista_nome ON artista(nome);
CREATE INDEX idx_reproducao_usuario_data ON reproducao(usuario_id, reproduzida_em);
