CREATE SCHEMA IF NOT EXISTS aneki;
SET search_path TO aneki;

CREATE TABLE IF NOT EXISTS users
(
    id       uuid PRIMARY KEY NOT NULL,
    name     VARCHAR(255)     NOT NULL,
    email    VARCHAR(320)     NOT NULL UNIQUE,
    password VARCHAR(255)     NOT NULL
);

CREATE TABLE IF NOT EXISTS jokes
(
    id         uuid PRIMARY KEY NOT NULL,
    user_id    uuid             NOT NULL,
    content    TEXT             NOT NULL,
    created_at bigint           NOT NULL,
    updated_at bigint           NOT NULL,
    CONSTRAINT fk_jokes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_jokes_created_at ON jokes (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_jokes_user_id ON jokes (user_id);
