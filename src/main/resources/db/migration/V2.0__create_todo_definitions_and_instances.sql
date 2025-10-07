-- V2.0: Create Todo Definitions and Instances tables
-- This is a breaking change that replaces the old todo structure

-- Drop existing todo-related tables and views if they exist
DROP VIEW IF EXISTS todo_view CASCADE;
DROP TABLE IF EXISTS todo_instances CASCADE;
DROP TABLE IF EXISTS todo_definitions CASCADE;

-- Create todo_definitions table (metadata/template)
CREATE TABLE todo_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority_id INTEGER DEFAULT 1 CHECK (priority_id BETWEEN 0 AND 2),
    category_id UUID,
    tags TEXT[], -- Array of strings for tags
    recurrence_rule JSONB, -- NULL = single occurrence
    is_recurring BOOLEAN GENERATED ALWAYS AS (recurrence_rule IS NOT NULL) STORED,
    base_date DATE,
    base_time TIME,
    is_collaborative BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_todo_def_owner FOREIGN KEY (owner_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_todo_def_category FOREIGN KEY (category_id)
        REFERENCES category(id) ON DELETE SET NULL
);

-- Create todo_instances table (actual execution/state)
CREATE TABLE todo_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    definition_id UUID NOT NULL,
    user_id UUID NOT NULL,
    sequence_number INTEGER NOT NULL DEFAULT 1,

    -- Override fields (NULL means use definition values)
    title VARCHAR(255),
    description TEXT,
    priority_id INTEGER CHECK (priority_id BETWEEN 0 AND 2),
    category_id UUID,
    tags TEXT[],

    -- Instance-specific fields
    due_date DATE NOT NULL,
    due_time TIME,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_todo_inst_definition FOREIGN KEY (definition_id)
        REFERENCES todo_definitions(id) ON DELETE CASCADE,
    CONSTRAINT fk_todo_inst_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_todo_inst_category FOREIGN KEY (category_id)
        REFERENCES category(id) ON DELETE SET NULL,
    CONSTRAINT uq_definition_sequence UNIQUE(definition_id, sequence_number)
);

-- Create indexes for better performance
CREATE INDEX idx_todo_def_owner ON todo_definitions(owner_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_todo_def_category ON todo_definitions(category_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_todo_def_recurring ON todo_definitions(is_recurring) WHERE deleted_at IS NULL;
CREATE INDEX idx_todo_def_active ON todo_definitions(is_active) WHERE deleted_at IS NULL;
CREATE INDEX idx_todo_def_created_at ON todo_definitions(created_at DESC);
CREATE INDEX idx_todo_def_deleted_at ON todo_definitions(deleted_at);

CREATE INDEX idx_todo_inst_definition ON todo_instances(definition_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_todo_inst_user ON todo_instances(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_todo_inst_completed ON todo_instances(completed) WHERE deleted_at IS NULL;
CREATE INDEX idx_todo_inst_due_date ON todo_instances(due_date, due_time) WHERE deleted_at IS NULL;
CREATE INDEX idx_todo_inst_pinned ON todo_instances(is_pinned) WHERE deleted_at IS NULL;
CREATE INDEX idx_todo_inst_category ON todo_instances(category_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_todo_inst_deleted_at ON todo_instances(deleted_at);

-- Create a unified view for easy querying
CREATE OR REPLACE VIEW todo_view AS
SELECT
    -- Instance fields
    i.id,
    i.definition_id,
    i.user_id,
    i.sequence_number,
    i.due_date,
    i.due_time,
    i.completed,
    i.completed_at,
    i.is_pinned,
    i.display_order,
    i.created_at,
    i.updated_at,

    -- Merged fields (instance overrides definition)
    COALESCE(i.title, d.title) AS title,
    COALESCE(i.description, d.description) AS description,
    COALESCE(i.priority_id, d.priority_id) AS priority_id,
    COALESCE(i.category_id, d.category_id) AS category_id,
    COALESCE(i.tags, d.tags) AS tags,

    -- Definition fields
    d.owner_id,
    d.recurrence_rule,
    d.is_recurring,
    d.base_date,
    d.base_time,
    d.is_collaborative,
    d.is_active,

    -- Joined fields
    u.nickname AS owner_nickname,
    c.name AS category_name,

    -- Computed fields
    CASE
        WHEN i.id IS NOT NULL THEN TRUE
        ELSE FALSE
    END AS has_instance
FROM
    todo_instances i
    INNER JOIN todo_definitions d ON i.definition_id = d.id
    INNER JOIN users u ON d.owner_id = u.id
    LEFT JOIN category c ON COALESCE(i.category_id, d.category_id) = c.id
WHERE
    i.deleted_at IS NULL
    AND d.deleted_at IS NULL;

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_todo_definitions_updated_at
    BEFORE UPDATE ON todo_definitions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_todo_instances_updated_at
    BEFORE UPDATE ON todo_instances
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE todo_definitions IS 'Todo definitions (templates/metadata) - defines what needs to be done';
COMMENT ON TABLE todo_instances IS 'Todo instances (actual executions) - tracks when and how tasks are done';
COMMENT ON VIEW todo_view IS 'Unified read-only view combining definitions and instances';

COMMENT ON COLUMN todo_definitions.recurrence_rule IS 'JSON recurrence rule following RFC 5545 standard. NULL means single occurrence';
COMMENT ON COLUMN todo_instances.completed IS 'TRUE if todo is completed, FALSE otherwise';
COMMENT ON COLUMN todo_instances.sequence_number IS 'For recurring todos: which occurrence this is. For single todos: always 1';