CREATE TABLE IF NOT EXISTS foodstuff
(
    id          BIGSERIAL PRIMARY KEY,
    name        TEXT                     NOT NULL CHECK (name <> ''),
    description TEXT                     NOT NULL DEFAULT '',
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW()),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW()),
    CONSTRAINT foodstuff_name_unique UNIQUE (name)
);

DROP TRIGGER IF EXISTS store_foodstuff_updated_at ON foodstuff;
CREATE TRIGGER store_foodstuff_updated_at
    BEFORE UPDATE
    ON foodstuff
    FOR EACH ROW
EXECUTE PROCEDURE set_updated_at();