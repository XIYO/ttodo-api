-- V2.1: Create Todo Mutation tables for synchronization support

-- Drop existing mutation tables if they exist
DROP TABLE IF EXISTS todo_instance_mutations CASCADE;
DROP TABLE IF EXISTS todo_definition_mutations CASCADE;

-- Create todo_definition_mutations table
CREATE TABLE todo_definition_mutations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    definition_id UUID NOT NULL,
    user_id UUID NOT NULL,
    mutation_type VARCHAR(20) NOT NULL CHECK (mutation_type IN ('CREATE', 'UPDATE', 'DELETE', 'RESTORE')),
    mutation_data JSONB NOT NULL,
    client_id VARCHAR(255),
    sync_status VARCHAR(20) DEFAULT 'PENDING' CHECK (sync_status IN ('PENDING', 'SYNCED', 'FAILED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    synced_at TIMESTAMP,

    CONSTRAINT fk_todo_def_mut_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- Create todo_instance_mutations table
CREATE TABLE todo_instance_mutations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instance_id UUID NOT NULL,
    definition_id UUID NOT NULL,
    user_id UUID NOT NULL,
    mutation_type VARCHAR(20) NOT NULL CHECK (mutation_type IN ('CREATE', 'UPDATE', 'DELETE', 'RESTORE', 'STATUS_CHANGE')),
    mutation_data JSONB NOT NULL,
    client_id VARCHAR(255),
    sync_status VARCHAR(20) DEFAULT 'PENDING' CHECK (sync_status IN ('PENDING', 'SYNCED', 'FAILED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    synced_at TIMESTAMP,

    CONSTRAINT fk_todo_inst_mut_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_todo_inst_mut_definition FOREIGN KEY (definition_id)
        REFERENCES todo_definitions(id) ON DELETE CASCADE
);

-- Create indexes for efficient querying
CREATE INDEX idx_todo_def_mut_user ON todo_definition_mutations(user_id);
CREATE INDEX idx_todo_def_mut_definition ON todo_definition_mutations(definition_id);
CREATE INDEX idx_todo_def_mut_status ON todo_definition_mutations(sync_status) WHERE sync_status = 'PENDING';
CREATE INDEX idx_todo_def_mut_created ON todo_definition_mutations(created_at DESC);
CREATE INDEX idx_todo_def_mut_client ON todo_definition_mutations(client_id);

CREATE INDEX idx_todo_inst_mut_user ON todo_instance_mutations(user_id);
CREATE INDEX idx_todo_inst_mut_instance ON todo_instance_mutations(instance_id);
CREATE INDEX idx_todo_inst_mut_definition ON todo_instance_mutations(definition_id);
CREATE INDEX idx_todo_inst_mut_status ON todo_instance_mutations(sync_status) WHERE sync_status = 'PENDING';
CREATE INDEX idx_todo_inst_mut_created ON todo_instance_mutations(created_at DESC);
CREATE INDEX idx_todo_inst_mut_client ON todo_instance_mutations(client_id);

-- Function to automatically log definition mutations
CREATE OR REPLACE FUNCTION log_todo_definition_mutation()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO todo_definition_mutations(definition_id, user_id, mutation_type, mutation_data)
        VALUES (NEW.id, NEW.owner_id, 'CREATE', to_jsonb(NEW));
    ELSIF TG_OP = 'UPDATE' THEN
        -- Check if it's a restore (deleted_at changed from NOT NULL to NULL)
        IF OLD.deleted_at IS NOT NULL AND NEW.deleted_at IS NULL THEN
            INSERT INTO todo_definition_mutations(definition_id, user_id, mutation_type, mutation_data)
            VALUES (NEW.id, NEW.owner_id, 'RESTORE', to_jsonb(NEW));
        -- Check if it's a soft delete (deleted_at changed from NULL to NOT NULL)
        ELSIF OLD.deleted_at IS NULL AND NEW.deleted_at IS NOT NULL THEN
            INSERT INTO todo_definition_mutations(definition_id, user_id, mutation_type, mutation_data)
            VALUES (NEW.id, NEW.owner_id, 'DELETE', to_jsonb(NEW));
        ELSE
            -- Regular update
            INSERT INTO todo_definition_mutations(definition_id, user_id, mutation_type, mutation_data)
            VALUES (NEW.id, NEW.owner_id, 'UPDATE', jsonb_build_object(
                'old', to_jsonb(OLD),
                'new', to_jsonb(NEW)
            ));
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to automatically log instance mutations
CREATE OR REPLACE FUNCTION log_todo_instance_mutation()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO todo_instance_mutations(instance_id, definition_id, user_id, mutation_type, mutation_data)
        VALUES (NEW.id, NEW.definition_id, NEW.user_id, 'CREATE', to_jsonb(NEW));
    ELSIF TG_OP = 'UPDATE' THEN
        -- Check if it's a restore (deleted_at changed from NOT NULL to NULL)
        IF OLD.deleted_at IS NOT NULL AND NEW.deleted_at IS NULL THEN
            INSERT INTO todo_instance_mutations(instance_id, definition_id, user_id, mutation_type, mutation_data)
            VALUES (NEW.id, NEW.definition_id, NEW.user_id, 'RESTORE', to_jsonb(NEW));
        -- Check if it's a soft delete (deleted_at changed from NULL to NOT NULL)
        ELSIF OLD.deleted_at IS NULL AND NEW.deleted_at IS NOT NULL THEN
            INSERT INTO todo_instance_mutations(instance_id, definition_id, user_id, mutation_type, mutation_data)
            VALUES (NEW.id, NEW.definition_id, NEW.user_id, 'DELETE', to_jsonb(NEW));
        -- Check if it's a status change
        ELSIF OLD.status_id != NEW.status_id THEN
            INSERT INTO todo_instance_mutations(instance_id, definition_id, user_id, mutation_type, mutation_data)
            VALUES (NEW.id, NEW.definition_id, NEW.user_id, 'STATUS_CHANGE', jsonb_build_object(
                'old_status', OLD.status_id,
                'new_status', NEW.status_id,
                'data', to_jsonb(NEW)
            ));
        ELSE
            -- Regular update
            INSERT INTO todo_instance_mutations(instance_id, definition_id, user_id, mutation_type, mutation_data)
            VALUES (NEW.id, NEW.definition_id, NEW.user_id, 'UPDATE', jsonb_build_object(
                'old', to_jsonb(OLD),
                'new', to_jsonb(NEW)
            ));
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers to automatically log mutations (optional - can be enabled/disabled)
-- Uncomment these if you want automatic mutation logging
-- CREATE TRIGGER log_todo_definition_mutations_trigger
--     AFTER INSERT OR UPDATE ON todo_definitions
--     FOR EACH ROW
--     EXECUTE FUNCTION log_todo_definition_mutation();

-- CREATE TRIGGER log_todo_instance_mutations_trigger
--     AFTER INSERT OR UPDATE ON todo_instances
--     FOR EACH ROW
--     EXECUTE FUNCTION log_todo_instance_mutation();

-- Add comments for documentation
COMMENT ON TABLE todo_definition_mutations IS 'Tracks all mutations to todo definitions for synchronization';
COMMENT ON TABLE todo_instance_mutations IS 'Tracks all mutations to todo instances for synchronization';
COMMENT ON COLUMN todo_definition_mutations.mutation_type IS 'Type of mutation: CREATE, UPDATE, DELETE, RESTORE';
COMMENT ON COLUMN todo_instance_mutations.mutation_type IS 'Type of mutation: CREATE, UPDATE, DELETE, RESTORE, STATUS_CHANGE';
COMMENT ON COLUMN todo_definition_mutations.mutation_data IS 'JSON data containing the mutation details';
COMMENT ON COLUMN todo_definition_mutations.client_id IS 'Identifier of the client that made the mutation';
COMMENT ON COLUMN todo_definition_mutations.sync_status IS 'Synchronization status: PENDING, SYNCED, FAILED';