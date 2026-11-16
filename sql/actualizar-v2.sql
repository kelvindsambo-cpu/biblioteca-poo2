-- Actualiza uma base criada com a versão 1 (sem prazos, multas, capas nem reservas).
-- Correr UMA vez, depois de biblioteca.sql (que cria as tabelas novas e o admin).
-- O script preparar-mysql.ps1 faz isto sozinho quando detecta uma base antiga.
USE biblioteca_poo2;

ALTER TABLE livro
  ADD COLUMN isbn VARCHAR(20) NULL,
  ADD COLUMN ano SMALLINT NULL,
  ADD COLUMN capa VARCHAR(255) NULL;

ALTER TABLE emprestimo
  ADD COLUMN data_prevista DATE NULL AFTER data_emprestimo,
  ADD COLUMN multa INT NOT NULL DEFAULT 0,
  ADD COLUMN multa_paga BOOLEAN NOT NULL DEFAULT FALSE;

-- Empréstimos antigos ficam com o prazo padrão de 14 dias
UPDATE emprestimo SET data_prevista = DATE_ADD(data_emprestimo, INTERVAL 14 DAY)
  WHERE data_prevista IS NULL;

ALTER TABLE emprestimo
  MODIFY data_prevista DATE NOT NULL,
  ADD CONSTRAINT chk_prazo CHECK (data_prevista >= data_emprestimo),
  ADD CONSTRAINT chk_multa CHECK (multa >= 0);

-- Capas para os livros de exemplo que já existiam
UPDATE livro SET capa = 'terra-sonambula.png', ano = 1992 WHERE titulo = 'Terra Sonâmbula' AND capa IS NULL;
UPDATE livro SET capa = 'ultimo-voo-do-flamingo.png', ano = 2000 WHERE titulo = 'O Último Voo do Flamingo' AND capa IS NULL;
UPDATE livro SET capa = 'ualalapi.png', ano = 1987 WHERE titulo = 'Ualalapi' AND capa IS NULL;
UPDATE livro SET capa = 'balada-de-amor-ao-vento.png', ano = 1990 WHERE titulo = 'Balada de Amor ao Vento' AND capa IS NULL;
UPDATE livro SET capa = 'clean-code.png', ano = 2008, isbn = '978-0132350884' WHERE titulo = 'Clean Code' AND capa IS NULL;
