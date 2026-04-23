-- Creates the users table used for authentication and account lifecycle metadata.
CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_login TIMESTAMP NULL
);

-- Creates the tasks table linked to users, including lifecycle dates and domain constraints.
CREATE TABLE IF NOT EXISTS tarefas (
    id UUID PRIMARY KEY,
    id_usuario UUID NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    descricao TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    prioridade VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    data_vencimento TIMESTAMP NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_conclusao TIMESTAMP NULL,
    CONSTRAINT fk_tarefas_usuarios FOREIGN KEY (id_usuario) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT chk_tarefas_status CHECK (status IN ('PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDA')),
    CONSTRAINT chk_tarefas_prioridade CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA')),
    CONSTRAINT chk_tarefas_conclusao_status CHECK (
        (data_conclusao IS NULL AND status <> 'CONCLUIDA')
        OR (data_conclusao IS NOT NULL AND status = 'CONCLUIDA')
    )
);

-- Optimizes common task filtering operations by owner, status, and priority.
CREATE INDEX IF NOT EXISTS idx_tarefas_id_usuario ON tarefas (id_usuario);
CREATE INDEX IF NOT EXISTS idx_tarefas_status ON tarefas (status);
CREATE INDEX IF NOT EXISTS idx_tarefas_prioridade ON tarefas (prioridade);

