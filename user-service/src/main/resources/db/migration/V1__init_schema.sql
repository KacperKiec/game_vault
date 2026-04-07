CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE game_list (
    user_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    list_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, game_id),
    CONSTRAINT fk_user_game FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    date DATE NOT NULL,
    CONSTRAINT fk_user_review FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    type VARCHAR(100) NOT NULL,
    title VARCHAR(50) NOT NULL,
    content VARCHAR(255) NOT NULL,
    metadata JSONB NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    ws_sent BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_user_notification FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO users (username, password, email, role)
VALUES (
    'admin',
    '$2a$10$8.UnVuG9HHgU.nLpYIghLuZz109tXv2qX2Iasw8E6IByV.yN5X5U6', -- hasło: admin
    'admin@example.com',
    'ADMIN'
       )
ON CONFLICT (username) DO NOTHING;