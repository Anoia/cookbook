CREATE TABLE IF NOT EXISTS recipe
(
    id           BIGSERIAL PRIMARY KEY,
    name         TEXT                        NOT NULL,
    description  TEXT                        NOT NULL DEFAULT '',
    ingredients  TEXT[]                      NOT NULL DEFAULT array[]::text[],
    instructions TEXT                        NOT NULL DEFAULT '',
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);


CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$
BEGIN
    IF row (NEW.*) IS DISTINCT FROM row (OLD.*) THEN
        NEW.updated_at = (NOW() AT TIME ZONE 'utc');
        RETURN NEW;
    ELSE
        RETURN OLD;
    END IF;
END;
$function$;

DROP TRIGGER IF EXISTS store_recipe_updated_at ON recipe;
CREATE TRIGGER store_recipe_updated_at
    BEFORE UPDATE
    ON recipe
    FOR EACH ROW
EXECUTE PROCEDURE set_updated_at();