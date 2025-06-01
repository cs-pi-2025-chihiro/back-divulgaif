INSERT INTO roles (name, created_at) VALUES
 ('IS_STUDENT', CURRENT_TIMESTAMP),
 ('IS_TEACHER', CURRENT_TIMESTAMP),
 ('IS_ADMIN', CURRENT_TIMESTAMP);

INSERT INTO users (name, email, cpf, rg, ra, phone, password, bio, date_of_birth, avatar_url, email_confirmed_at, created_at) VALUES
  -- Student
  ('Mateus Teste', 'mateusteste@teste.com', '202.108.850-25', null, null, null, '$2a$10$50IuCYfQIsWieK3IBZ1nauiEStJh639sJrkAdWBv18IIOYuYCEPZi', null, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO user_roles (user_id, role_id) VALUES
  (1, 1); -- IS_STUDENT