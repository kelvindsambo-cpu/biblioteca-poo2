CREATE DATABASE IF NOT EXISTS biblioteca_poo2
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biblioteca_poo2;

CREATE TABLE IF NOT EXISTS livro (
  id INT AUTO_INCREMENT PRIMARY KEY,
  titulo VARCHAR(150) NOT NULL,
  autor VARCHAR(100) NOT NULL,
  disponivel BOOLEAN NOT NULL DEFAULT TRUE
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
  data_devolucao DATE NULL,
  CONSTRAINT fk_emprestimo_livro FOREIGN KEY (livro_id) REFERENCES livro(id),
  CONSTRAINT fk_emprestimo_leitor FOREIGN KEY (leitor_id) REFERENCES leitor(id),
  CONSTRAINT chk_datas CHECK (data_devolucao IS NULL OR data_devolucao >= data_emprestimo)
) ENGINE=InnoDB;
