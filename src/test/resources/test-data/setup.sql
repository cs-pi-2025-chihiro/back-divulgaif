INSERT INTO roles (name, created_at) VALUES
 ('IS_STUDENT', CURRENT_TIMESTAMP),
 ('IS_TEACHER', CURRENT_TIMESTAMP),
 ('IS_ADMIN', CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (id, name, email, cpf, rg, ra, phone, password, bio, date_of_birth, avatar_url, email_confirmed_at, created_at, secondary_email, user_type) VALUES
  (1, 'Mateus Teste', 'mateusteste@teste.com', '202.108.850-25', null, null, null, '$2a$10$50IuCYfQIsWieK3IBZ1nauiEStJh639sJrkAdWBv18IIOYuYCEPZi', null, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null, 'Aluno'),
  (2, 'Test Student 1', 'student1@test.com', '123.456.789-01', null, '2023001', null, '$2a$10$50IuCYfQIsWieK3IBZ1nauiEStJh639sJrkAdWBv18IIOYuYCEPZi', null, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null, 'Aluno'),
  (3, 'Test Student 2', 'student2@test.com', '123.456.789-02', null, '2023002', null, '$2a$10$50IuCYfQIsWieK3IBZ1nauiEStJh639sJrkAdWBv18IIOYuYCEPZi', null, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null, 'Aluno'),
  (4, 'Test Teacher', 'teacher@test.com', '123.456.789-03', null, null, null, '$2a$10$50IuCYfQIsWieK3IBZ1nauiEStJh639sJrkAdWBv18IIOYuYCEPZi', null, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null, 'Servidor (Docente)')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id) VALUES
  (1, 1),
  (2, 1),
  (3, 1),
  (4, 2)
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO work_status (id, name, created_at) VALUES
   (1, 'DRAFT', CURRENT_TIMESTAMP),
   (2, 'SUBMITTED', CURRENT_TIMESTAMP),
   (3, 'PENDING_CHANGES', CURRENT_TIMESTAMP),
   (4, 'PUBLISHED', CURRENT_TIMESTAMP),
   (5, 'REJECTED', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO work_types (id, name, created_at, updated_at) VALUES
   (1, 'ARTICLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
   (2, 'SEARCH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
   (3, 'DISSERTATION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
   (4, 'EXTENSION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
   (5, 'FINAL_THESIS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO authors (id, name, email, type, user_id, created_at, updated_at) VALUES
    (1, 'Mateus Teste', 'mateusteste@teste.com', 'CADASTRADO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Test Student 1', 'student1@test.com', 'CADASTRADO', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Test Student 2', 'student2@test.com', 'CADASTRADO', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'Test Teacher', 'teacher@test.com', 'CADASTRADO', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'Joao Silva', 'joao.silva@email.com', 'SEM CADASTRO', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO labels (id, name, color, created_at, updated_at) VALUES
    (1, 'Tecnologia', '#FF5733', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Java', '#007396', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO links (id, url, name, description, created_at, updated_at) VALUES
    (1, 'https://github.com/user/repo', 'GitHub Repository', 'Repository with project source code', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO works (id, title, description, content, principal_link, meta_tag, work_status_id, work_type_id, teacher_id, image_url, created_at, updated_at, submitted_at, approved_at) VALUES
    (1, 'Desenvolvimento de Sistema Web', 'Sistema web para gerenciamento de projetos acadêmicos', 'Conteúdo detalhado do trabalho...', 'https://projeto-principal.com', 'sistema, web, gestão', 4, 1, 4, 'https://example.com/image.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO work_authors(work_id, author_id) VALUES
    (1, 1),
    (1, 2),
    (1, 3)
ON CONFLICT (work_id, author_id) DO NOTHING;

INSERT INTO work_labels(work_id, label_id) VALUES
    (1, 1)
ON CONFLICT (work_id, label_id) DO NOTHING;

INSERT INTO work_links(work_id, link_id) VALUES
    (1, 1)
ON CONFLICT (work_id, link_id) DO NOTHING;

SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('work_status_id_seq', (SELECT MAX(id) FROM work_status));
SELECT setval('work_types_id_seq', (SELECT MAX(id) FROM work_types));
SELECT setval('authors_id_seq', (SELECT MAX(id) FROM authors));
SELECT setval('labels_id_seq', (SELECT MAX(id) FROM labels));
SELECT setval('links_id_seq', (SELECT MAX(id) FROM links));
SELECT setval('works_id_seq', (SELECT MAX(id) FROM works));

INSERT INTO works (id, title, description, content, work_status_id, work_type_id, teacher_id, created_at, updated_at) VALUES
(2, 'Trabalho para Editar', 'Descrição inicial', 'Conteúdo inicial', 1, 1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO work_authors(work_id, author_id) VALUES
    (2, 2)
ON CONFLICT (work_id, author_id) DO NOTHING;