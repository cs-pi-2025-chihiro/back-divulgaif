INSERT INTO roles (name, created_at) VALUES
 ('IS_STUDENT', CURRENT_TIMESTAMP),
 ('IS_TEACHER', CURRENT_TIMESTAMP),
 ('IS_ADMIN', CURRENT_TIMESTAMP);

INSERT INTO users (name, email, cpf, rg, ra, phone, password, bio, date_of_birth, avatar_url, email_confirmed_at, created_at) VALUES
  ('Mateus Teste', 'mateusteste@teste.com', '202.108.850-25', null, null, null, '$2a$10$50IuCYfQIsWieK3IBZ1nauiEStJh639sJrkAdWBv18IIOYuYCEPZi', null, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id) VALUES
  (1, 1); -- IS_STUDENT

INSERT INTO users (name, email, cpf, rg, ra, phone, password, bio, date_of_birth, avatar_url, email_confirmed_at, created_at) VALUES
  ('Test Student 1', 'student1@test.com', '123.456.789-01', null, '2023001', null, '$2a$10$50IuCYfQIsWieK3IBZ1nauiEStJh639sJrkAdWBv18IIOYuYCEPZi', null, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Test Student 2', 'student2@test.com', '123.456.789-02', null, '2023002', null, '$2a$10$50IuCYfQIsWieK3IBZ1nauiEStJh639sJrkAdWBv18IIOYuYCEPZi', null, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Test Teacher', 'teacher@test.com', '123.456.789-03', null, null, null, '$2a$10$50IuCYfQIsWieK3IBZ1nauiEStJh639sJrkAdWBv18IIOYuYCEPZi', null, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id) VALUES
  (2, 1),
  (3, 1),
  (4, 2);

INSERT INTO work_status (name, created_at) VALUES
   ('DRAFT', CURRENT_TIMESTAMP),
   ('SUBMITTED', CURRENT_TIMESTAMP),
   ('PENDING_CHANGES', CURRENT_TIMESTAMP),
   ('PUBLISHED', CURRENT_TIMESTAMP),
   ('REJECTED', CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO work_types (name, created_at, updated_at) VALUES
   ('ARTICLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
   ('SEARCH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
   ('DISSERTATION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
   ('EXTENSION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
   ('FINAL_THESIS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;
