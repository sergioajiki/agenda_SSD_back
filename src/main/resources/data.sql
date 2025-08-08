-- Criação da tabela users
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    matricula BIGINT NOT NULL
);

-- Criação da tabela meetings
CREATE TABLE IF NOT EXISTS meetings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    update_date TIMESTAMP NOT NULL,
    meet_date DATE NOT NULL,
    time_start TIME NOT NULL,
    time_end TIME NOT NULL,
    meeting_room VARCHAR(100) NOT NULL,
    user_id INT NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Inserção de registros na tabela users
INSERT INTO users (name, email, password, matricula) VALUES
('Teste', 'teste@teste.com','123','123123'),
('Teste2', 'teste2@teste.com','456','456456'),
('Teste3', 'teste3@teste.com','789','789789');

INSERT INTO meetings (title, update_date, meet_date, time_start, time_end, meeting_room, user_id) VALUES
('Reunião de Estratégia', '2025-08-08 08:00:00', '2025-08-09', '09:00:00', '10:00:00', 'CIEGES', 1),
('Planejamento Semanal', '2025-08-08 08:30:00', '2025-08-10', '11:00:00', '12:00:00', 'APOIO', 2),
('Alinhamento de Equipe', '2025-08-08 09:00:00', '2025-08-11', '13:00:00', '14:00:00', 'CIEGES', 3),
('Feedback de Projeto', '2025-08-08 09:30:00', '2025-08-12', '15:00:00', '16:00:00', 'APOIO', 1),
('Workshop Interno', '2025-08-08 10:00:00', '2025-08-13', '09:00:00', '11:00:00', 'CIEGES', 2),
('Discussão Técnica', '2025-08-08 10:30:00', '2025-08-14', '14:00:00', '15:30:00', 'APOIO', 3),
('Revisão de Metas', '2025-08-08 11:00:00', '2025-08-15', '08:00:00', '09:00:00', 'CIEGES', 1),
('Apresentação de Resultados', '2025-08-08 11:30:00', '2025-08-16', '10:00:00', '11:00:00', 'APOIO', 2),
('Planejamento de Sprint', '2025-08-08 12:00:00', '2025-08-17', '13:30:00', '15:00:00', 'CIEGES', 3),
('Capacitação', '2025-08-08 12:30:00', '2025-08-18', '16:00:00', '17:30:00', 'APOIO', 1);
