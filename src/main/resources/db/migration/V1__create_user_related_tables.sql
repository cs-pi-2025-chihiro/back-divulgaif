CREATE TABLE roles
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE users
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    cpf VARCHAR(20) NULL,
    ra VARCHAR(20) NULL,
    password VARCHAR(255) NOT NULL,
    bio VARCHAR(500) NULL,
    phone VARCHAR(255) NULL,
    avatar_url VARCHAR(255) NULL,
    email_confirmed_at TIMESTAMP NULL,
    forgot_password_token VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE user_roles
(
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,

    CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE UNIQUE INDEX index_unique_email
    ON users (email)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX index_unique_cpf
    ON users (cpf)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX index_unique_ra
    ON users (ra)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX index_unique_role_name
    ON roles (name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX index_unique_user_role
    ON user_roles (user_id, role_id);
