USE blue_tech;

-- =========================================================
-- USUÁRIOS (generalização)
-- =========================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    tipo ENUM('ALUNO','ORIENTADOR','COORDENADOR') NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tipo (tipo)
) ENGINE=InnoDB;

-- =========================================================
-- ALUNO (especialização)
-- =========================================================
CREATE TABLE IF NOT EXISTS aluno (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    ra VARCHAR(20) NOT NULL UNIQUE,
    idade INT NULL,
    historico_academico TEXT NULL,
    historico_profissional TEXT NULL,
    motivacao TEXT NULL,
    link_github VARCHAR(255) NULL,
    link_linkedin VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_aluno_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_aluno_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- PROFESSOR (especialização)
-- =========================================================
CREATE TABLE IF NOT EXISTS professor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    cargo VARCHAR(150) NULL,
    tipo_tg ENUM('TG1','TG2','AMBOS','NENHUM') NOT NULL DEFAULT 'NENHUM',
    coordenador BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_professor_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_professor_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- RELAÇÃO: ORIENTA (Professor → Aluno)
-- =========================================================
CREATE TABLE IF NOT EXISTS orienta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professor_id BIGINT NOT NULL,
    aluno_id BIGINT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fim DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_orientacao_prof_aluno UNIQUE (professor_id, aluno_id),
    CONSTRAINT fk_orienta_professor FOREIGN KEY (professor_id)
        REFERENCES professor(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_orienta_aluno FOREIGN KEY (aluno_id)
        REFERENCES aluno(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- PORTFÓLIO DE TG (1 por aluno)
-- =========================================================
CREATE TABLE IF NOT EXISTS tg_portifolio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aluno_id BIGINT NOT NULL,
    professor_id BIGINT NOT NULL, -- orientador responsável
    titulo VARCHAR(200) NULL,
    tema VARCHAR(200) NULL,
    status ENUM('EM_ANDAMENTO','CONCLUIDO') NOT NULL DEFAULT 'EM_ANDAMENTO',
    percentual_conclusao DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    data_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_conclusao DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_tg_aluno UNIQUE (aluno_id),
    CONSTRAINT fk_portifolio_aluno FOREIGN KEY (aluno_id)
        REFERENCES aluno(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_portifolio_professor FOREIGN KEY (professor_id)
        REFERENCES professor(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_portifolio_aluno (aluno_id),
    INDEX idx_portifolio_professor (professor_id)
) ENGINE=InnoDB;

-- =========================================================
-- SEÇÕES (1 a 6 APIs do TG)
-- =========================================================
CREATE TABLE IF NOT EXISTS tg_secao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portifolio_id BIGINT NOT NULL,
    api_numero TINYINT NOT NULL CHECK (api_numero BETWEEN 1 AND 6),
    status ENUM('PENDENTE','CONCLUIDA') NOT NULL DEFAULT 'PENDENTE',
    versao_validada BOOLEAN NOT NULL DEFAULT FALSE,
    data_validacao DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_portifolio_api UNIQUE (portifolio_id, api_numero),
    CONSTRAINT fk_secao_portifolio FOREIGN KEY (portifolio_id)
        REFERENCES tg_portifolio(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_secao_portifolio (portifolio_id, api_numero)
) ENGINE=InnoDB;

-- =========================================================
-- VERSÕES (iterações de uma seção)
-- =========================================================
CREATE TABLE IF NOT EXISTS tg_versao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    secao_id BIGINT NOT NULL,
    numero_versao INT NOT NULL,
    conteudo_md LONGTEXT NOT NULL,
    aceita BOOLEAN NOT NULL DEFAULT FALSE,
    comentario_orientador TEXT NULL,
    avaliado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_secao_versao UNIQUE (secao_id, numero_versao),
    CONSTRAINT fk_versao_secao FOREIGN KEY (secao_id)
        REFERENCES tg_secao(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_versao_secao (secao_id, numero_versao)
) ENGINE=InnoDB;

-- =========================================================
-- FEEDBACK (do orientador sobre uma versão)
-- =========================================================
CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    versao_id BIGINT NOT NULL,
    professor_id BIGINT NOT NULL,
    status ENUM('ACEITO','AJUSTES','REJEITADO') NOT NULL,
    comentario TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_versao FOREIGN KEY (versao_id)
        REFERENCES tg_versao(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_feedback_professor FOREIGN KEY (professor_id)
        REFERENCES professor(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_feedback_versao (versao_id),
    INDEX idx_feedback_professor (professor_id)
) ENGINE=InnoDB;

-- =========================================================
-- MENSAGENS (comunicação aluno ↔ professor)
-- =========================================================
CREATE TABLE IF NOT EXISTS mensagem (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portifolio_id BIGINT NOT NULL,
    remetente_id BIGINT NOT NULL,
    destinatario_id BIGINT NOT NULL,
    conteudo TEXT NOT NULL,
    tipo ENUM('TEXTO','SISTEMA') NOT NULL DEFAULT 'TEXTO',
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_mensagem_portifolio FOREIGN KEY (portifolio_id)
        REFERENCES tg_portifolio(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_mensagem_remetente FOREIGN KEY (remetente_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_mensagem_destinatario FOREIGN KEY (destinatario_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_mensagem_portifolio (portifolio_id),
    INDEX idx_mensagem_remetente (remetente_id),
    INDEX idx_mensagem_destinatario (destinatario_id)
) ENGINE=InnoDB;

-- =========================================================
-- DEFESA (apresentação final)
-- =========================================================
CREATE TABLE IF NOT EXISTS defesa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portifolio_id BIGINT NOT NULL,
    data_hora DATETIME NOT NULL,
    sala VARCHAR(100),
    status ENUM('AGENDADA','REALIZADA','CANCELADA') NOT NULL DEFAULT 'AGENDADA',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_defesa_portifolio FOREIGN KEY (portifolio_id)
        REFERENCES tg_portifolio(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_defesa_portifolio (portifolio_id)
) ENGINE=InnoDB;
