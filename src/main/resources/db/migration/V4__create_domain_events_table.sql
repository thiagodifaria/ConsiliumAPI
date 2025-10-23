
CREATE TABLE domain_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    event_type VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,

    event_data JSONB NOT NULL,

    metadata JSONB,

    version BIGINT NOT NULL DEFAULT 1,

    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_event_type CHECK (event_type ~ '^[A-Z_]+$'),
    CONSTRAINT chk_aggregate_type CHECK (aggregate_type IN ('TASK', 'PROJECT', 'USER'))
);

CREATE INDEX idx_domain_events_aggregate ON domain_events(aggregate_type, aggregate_id);
CREATE INDEX idx_domain_events_type ON domain_events(event_type);
CREATE INDEX idx_domain_events_occurred_at ON domain_events(occurred_at DESC);

CREATE INDEX idx_domain_events_aggregate_time ON domain_events(aggregate_id, occurred_at DESC);

COMMENT ON TABLE domain_events IS 'Event Store: armazena todos eventos de domínio do sistema (immutable, append-only)';
COMMENT ON COLUMN domain_events.event_type IS 'Tipo do evento (TASK_CREATED, TASK_STATUS_CHANGED, PROJECT_CREATED, etc)';
COMMENT ON COLUMN domain_events.aggregate_type IS 'Tipo da entidade raiz (TASK, PROJECT, USER)';
COMMENT ON COLUMN domain_events.aggregate_id IS 'ID da entidade afetada pelo evento';
COMMENT ON COLUMN domain_events.event_data IS 'Dados completos do evento em JSON';
COMMENT ON COLUMN domain_events.metadata IS 'Metadados contextuais (user_id, ip, correlation_id, etc)';
COMMENT ON COLUMN domain_events.version IS 'Versão do evento para controle de concorrência';
COMMENT ON COLUMN domain_events.occurred_at IS 'Timestamp de quando o evento ocorreu';