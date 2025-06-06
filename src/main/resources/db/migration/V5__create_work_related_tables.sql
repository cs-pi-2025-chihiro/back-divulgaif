CREATE TABLE work_status
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE work_types
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE authors
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    type VARCHAR(100),
    user_id INTEGER NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE labels
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    color VARCHAR(7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE links
(
    id SERIAL PRIMARY KEY,
    url VARCHAR(500) NOT NULL,
    title VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE works
(
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    content TEXT,
    principal_link VARCHAR(500),
    meta_tag VARCHAR(500),
    status_id INTEGER NOT NULL,
    type_id INTEGER,
    teacher_id INTEGER NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    submitted_at TIMESTAMP NULL,
    approved_at TIMESTAMP NULL,
    FOREIGN KEY (status_id) REFERENCES work_status (id) ON DELETE RESTRICT,
    FOREIGN KEY (type_id) REFERENCES work_types (id) ON DELETE SET NULL,
    FOREIGN KEY (teacher_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE histories
(
    id SERIAL PRIMARY KEY,
    message TEXT NOT NULL,
    work_id INTEGER NOT NULL,
    teacher_id INTEGER NULL,
    status_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (work_id) REFERENCES works (id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE work_authors
(
    id SERIAL NOT NULL,
    work_id INTEGER NOT NULL,
    author_id INTEGER NOT NULL,
    is_primary BOOLEAN NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    PRIMARY KEY (work_id, author_id),
    FOREIGN KEY (work_id) REFERENCES works (id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors (id) ON DELETE CASCADE
);

CREATE TABLE work_labels
(
    id SERIAL NOT NULL,
    work_id INTEGER NOT NULL,
    label_id INTEGER NOT NULL,
    PRIMARY KEY (work_id, label_id),
    FOREIGN KEY (work_id) REFERENCES works (id) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES labels (id) ON DELETE CASCADE
);

CREATE TABLE work_links
(
    id SERIAL NOT NULL,
    work_id INTEGER NOT NULL,
    link_id INTEGER NOT NULL,
    PRIMARY KEY (work_id, link_id),
    FOREIGN KEY (work_id) REFERENCES works (id) ON DELETE CASCADE,
    FOREIGN KEY (link_id) REFERENCES links (id) ON DELETE CASCADE
);

CREATE INDEX idx_authors_user_id ON authors(user_id);
CREATE INDEX idx_authors_type ON authors(type);
CREATE INDEX idx_works_status_id ON works(status_id);
CREATE INDEX idx_works_type_id ON works(type_id);
CREATE INDEX idx_works_teacher_id ON works(teacher_id);
CREATE INDEX idx_works_submitted_at ON works(submitted_at);
CREATE INDEX idx_histories_work_id ON histories(work_id);
CREATE INDEX idx_histories_teacher_id ON histories(teacher_id);
CREATE INDEX idx_work_authors_primary ON work_authors(work_id, is_primary);

CREATE UNIQUE INDEX index_unique_work_status_name
    ON work_status (name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX index_unique_work_type_name
    ON work_types (name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX index_unique_label_name
    ON labels (name);

CREATE TRIGGER update_work_status_updated_at BEFORE UPDATE ON work_status
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_work_types_updated_at BEFORE UPDATE ON work_types
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_authors_updated_at BEFORE UPDATE ON authors
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_labels_updated_at BEFORE UPDATE ON labels
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_links_updated_at BEFORE UPDATE ON links
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_works_updated_at BEFORE UPDATE ON works
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

INSERT INTO work_status (name) VALUES
   ('DRAFT'),
   ('SUBMITTED'),
   ('PENDING_CHANGES'),
   ('PUBLISHED'),
   ('REJECTED');

CREATE VIEW work_with_primary_author AS
SELECT
    w.*,
    ws.name as status_name,
    wt.name as type_name,
    u_teacher.name as teacher_name,
    a.name as primary_author_name,
    a.email as primary_author_email,
    u_author.name as primary_author_user_name
FROM works w
     LEFT JOIN work_status ws ON w.status_id = ws.id
     LEFT JOIN work_types wt ON w.type_id = wt.id
     LEFT JOIN users u_teacher ON w.teacher_id = u_teacher.id
     LEFT JOIN work_authors wa ON w.id = wa.work_id AND wa.is_primary = true
     LEFT JOIN authors a ON wa.author_id = a.id
     LEFT JOIN users u_author ON a.user_id = u_author.id;