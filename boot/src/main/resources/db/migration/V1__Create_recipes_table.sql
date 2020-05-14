CREATE TABLE IF NOT EXISTS recipe
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR NOT NULL,
    ingredients VARCHAR[],
    instructions TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);