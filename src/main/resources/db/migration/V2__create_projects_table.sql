CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_project_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_project_name ON projects(name);

COMMENT ON TABLE projects IS 'Tabela de projetos';
COMMENT ON COLUMN projects.id IS 'Identificador único do projeto (UUID)';
COMMENT ON COLUMN projects.name IS 'Nome único do projeto (3-100 caracteres)';
COMMENT ON COLUMN projects.description IS 'Descrição detalhada do projeto';
COMMENT ON COLUMN projects.start_date IS 'Data de início do projeto';
COMMENT ON COLUMN projects.end_date IS 'Data de término do projeto (opcional)';