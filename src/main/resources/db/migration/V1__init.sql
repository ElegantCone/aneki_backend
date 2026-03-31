CREATE TABLE users (
    id uuid PRIMARY KEY not null,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE jokes (
    id uuid PRIMARY KEY not null,
    user_id uuid NOT NULL,
    content TEXT NOT NULL,
    created_at bigint NOT NULL,
    updated_at bigint NOT NULL,
    CONSTRAINT fk_jokes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_jokes_created_at ON jokes (created_at DESC);
CREATE INDEX idx_jokes_user_id ON jokes (user_id);
