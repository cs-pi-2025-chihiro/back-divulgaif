INSERT INTO roles (name, created_at) VALUES
 ('IS_STUDENT', CURRENT_TIMESTAMP),
 ('IS_TEACHER', CURRENT_TIMESTAMP),
 ('IS_ADMIN', CURRENT_TIMESTAMP);

INSERT INTO users (name, email, cpf, rg, ra, phone, password, bio, date_of_birth, avatar_url, email_confirmed_at, created_at) VALUES
  -- Student
  ('João Silva Santos', 'joao.silva@ifpr.edu.br', '12345678901', '123456789', 'RA2024001', '(41) 99999-1234', '$2a$10$test.password.hash.1', 'Estudante de Informática do IFPR', '2001-05-15 00:00:00', 'https://avatar.example.com/joao.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  -- Teacher
  ('Prof. Maria Oliveira', 'maria.oliveira@ifpr.edu.br', '98765432109', '987654321', NULL, '(41) 99999-5678', '$2a$10$test.password.hash.2', 'Professora de Desenvolvimento Web', '1985-08-22 00:00:00', 'https://avatar.example.com/maria.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('Carlos Admin', 'carlos.admin@ifpr.edu.br', '11122233344', '111222333', NULL, '(41) 99999-9999', '$2a$10$test.password.hash.3', 'Administrador do sistema', '1980-12-03 00:00:00', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

  ('Ana Costa', 'ana.costa@ifpr.edu.br', '55566677788', '555666777', 'RA2024002', '(41) 88888-1111', '$2a$10$test.password.hash.4', 'Estudante de Design', '2002-03-10 00:00:00', NULL, NULL, CURRENT_TIMESTAMP),

  ('Pedro Fernandes', 'pedro.fernandes@ifpr.edu.br', '99988877766', '999888777', 'RA2024003', NULL, '$2a$10$test.password.hash.5', NULL, '2000-11-28 00:00:00', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id) VALUES
  (1, 1), -- IS_STUDENT

  (2, 2); -- IS_TEACHER

-- Update Pedro with a forgot password token (for testing password reset flows)
UPDATE users SET forgot_password_token = 'test-forgot-token-12345' WHERE id = 5;