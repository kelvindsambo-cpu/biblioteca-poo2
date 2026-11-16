CREATE DATABASE IF NOT EXISTS biblioteca_poo2
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biblioteca_poo2;

CREATE TABLE IF NOT EXISTS livro (
  id INT AUTO_INCREMENT PRIMARY KEY,
  titulo VARCHAR(150) NOT NULL,
  autor VARCHAR(100) NOT NULL,
  disponivel BOOLEAN NOT NULL DEFAULT TRUE,
  isbn VARCHAR(20) NULL,
  ano SMALLINT NULL,
  capa VARCHAR(255) NULL          -- nome do ficheiro da imagem da capa
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS leitor (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(100) NOT NULL,
  telefone VARCHAR(20) NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS emprestimo (
  id INT AUTO_INCREMENT PRIMARY KEY,
  livro_id INT NOT NULL,
  leitor_id INT NOT NULL,
  data_emprestimo DATE NOT NULL,
  data_prevista DATE NOT NULL,    -- prazo de devolução (RN11)
  data_devolucao DATE NULL,       -- NULL = empréstimo activo
  multa INT NOT NULL DEFAULT 0,   -- em meticais, fixada na devolução (RN12)
  multa_paga BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_emprestimo_livro FOREIGN KEY (livro_id) REFERENCES livro(id),
  CONSTRAINT fk_emprestimo_leitor FOREIGN KEY (leitor_id) REFERENCES leitor(id),
  CONSTRAINT chk_datas CHECK (data_devolucao IS NULL OR data_devolucao >= data_emprestimo),
  CONSTRAINT chk_prazo CHECK (data_prevista >= data_emprestimo),
  CONSTRAINT chk_multa CHECK (multa >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reserva (
  id INT AUTO_INCREMENT PRIMARY KEY,
  livro_id INT NOT NULL,
  leitor_id INT NOT NULL,
  data_reserva DATE NOT NULL,
  estado VARCHAR(12) NOT NULL DEFAULT 'ACTIVA',
  CONSTRAINT fk_reserva_livro FOREIGN KEY (livro_id) REFERENCES livro(id),
  CONSTRAINT fk_reserva_leitor FOREIGN KEY (leitor_id) REFERENCES leitor(id),
  CONSTRAINT chk_estado CHECK (estado IN ('ACTIVA', 'ATENDIDA', 'CANCELADA'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS funcionario (
  id INT AUTO_INCREMENT PRIMARY KEY,
  utilizador VARCHAR(50) NOT NULL UNIQUE,
  nome VARCHAR(100) NOT NULL,
  sal CHAR(32) NOT NULL,
  password_hash CHAR(64) NOT NULL  -- SHA-256 de (sal + password); nunca a password em claro
) ENGINE=InnoDB;

-- Primeiro acesso: utilizador "admin", password "admin123" (alterar depois de entrar).
-- INSERT IGNORE: se o admin já existir, não mexe na password dele.
INSERT IGNORE INTO funcionario (utilizador, nome, sal, password_hash) VALUES
  ('admin', 'Administrador', '5f2b9c41d07e4a8c93b6e1f0a2d4c7e8',
   SHA2(CONCAT('5f2b9c41d07e4a8c93b6e1f0a2d4c7e8', 'admin123'), 256));
