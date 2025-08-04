-- Criação da tabela users
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    matricula BIGINT NOT NULL
);

-- Inserção de registros na tabela users
INSERT INTO users (name, email, password, matricula) VALUES
('Teste', 'teste@teste.com','123','123123'),
('Teste2', 'teste2@teste.com','456','456456'),
('Teste3', 'teste3@teste.com','789','789789');
