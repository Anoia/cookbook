CREATE TABLE IF NOT EXISTS foodstuff
(
    foodstuff_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    foodstuff_name TEXT                     NOT NULL CHECK (foodstuff_name <> ''),
    description    TEXT                     NOT NULL DEFAULT '',
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW()),
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW()),
    CONSTRAINT foodstuff_name_unique UNIQUE (foodstuff_name)
);

DROP TRIGGER IF EXISTS store_foodstuff_updated_at ON foodstuff;
CREATE TRIGGER store_foodstuff_updated_at
    BEFORE UPDATE
    ON foodstuff
    FOR EACH ROW
EXECUTE PROCEDURE set_updated_at();

CREATE UNIQUE INDEX foodstuff_name_unique_idx on foodstuff(lower(foodstuff_name))