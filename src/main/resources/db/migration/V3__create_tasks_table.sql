CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    due_date DATE,
    deleted BOOLEAN NOT NULL DEFAULT false,
    project_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT chk_task_status CHECK (status IN ('TODO', 'DOING', 'DONE')),
    CONSTRAINT chk_task_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);

CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_priority ON tasks(priority);
CREATE INDEX idx_task_project_id ON tasks(project_id);
CREATE INDEX idx_task_deleted ON tasks(deleted);
CREATE INDEX idx_task_project_deleted ON tasks(project_id, deleted);

COMMENT ON TABLE tasks IS 'Tabela de tarefas vinculadas a projetos';
COMMENT ON COLUMN tasks.id IS 'Identificador único da tarefa (UUID)';
COMMENT ON COLUMN tasks.title IS 'Título da tarefa (5-150 caracteres)';
COMMENT ON COLUMN tasks.description IS 'Descrição detalhada da tarefa';
COMMENT ON COLUMN tasks.status IS 'Status atual da tarefa (TODO, DOING, DONE)';
COMMENT ON COLUMN tasks.priority IS 'Prioridade da tarefa (LOW, MEDIUM, HIGH)';
COMMENT ON COLUMN tasks.due_date IS 'Data limite para conclusão (opcional)';
COMMENT ON COLUMN tasks.deleted IS 'Flag de soft delete';
COMMENT ON COLUMN tasks.project_id IS 'Referência ao projeto';