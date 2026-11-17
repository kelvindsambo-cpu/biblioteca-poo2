-- Dados de exemplo para testes e apresentação. Executar depois de biblioteca.sql, numa base nova.
-- As datas são relativas a hoje, para haver sempre um empréstimo em atraso para mostrar.
USE biblioteca_poo2;

INSERT INTO livro (titulo, autor, ano, isbn, capa) VALUES
  ('Terra Sonâmbula', 'Mia Couto', 1992, NULL, 'terra-sonambula.png'),
  ('O Último Voo do Flamingo', 'Mia Couto', 2000, NULL, 'ultimo-voo-do-flamingo.png'),
  ('Ualalapi', 'Ungulani Ba Ka Khosa', 1987, NULL, 'ualalapi.png'),
  ('Balada de Amor ao Vento', 'Paulina Chiziane', 1990, NULL, 'balada-de-amor-ao-vento.png'),
  ('Niketche', 'Paulina Chiziane', 2002, NULL, 'niketche.png'),
  ('Mayombe', 'Pepetela', 1980, NULL, 'mayombe.png'),
  ('Os Lusíadas', 'Luís Vaz de Camões', 1572, NULL, 'os-lusiadas.png'),
  ('Dom Casmurro', 'Machado de Assis', 1899, NULL, 'dom-casmurro.png'),
  ('Mensagem', 'Fernando Pessoa', 1934, NULL, 'mensagem.png'),
  ('Clean Code', 'Robert C. Martin', 2008, '978-0132350884', 'clean-code.png');

INSERT INTO leitor (nome, telefone) VALUES
  ('Ana Machava', '841234567'),
  ('Carlos Mondlane', NULL),
  ('Joana Sitoe', '829876543'),
  ('Rui Cossa', '861112233');

-- Histórico: devolvido a tempo, sem multa
INSERT INTO emprestimo (livro_id, leitor_id, data_emprestimo, data_prevista, data_devolucao)
SELECT l.id, r.id, CURDATE() - INTERVAL 60 DAY, CURDATE() - INTERVAL 46 DAY, CURDATE() - INTERVAL 50 DAY
FROM livro l, leitor r WHERE l.titulo = 'Terra Sonâmbula' AND r.nome = 'Ana Machava';

INSERT INTO emprestimo (livro_id, leitor_id, data_emprestimo, data_prevista, data_devolucao)
SELECT l.id, r.id, CURDATE() - INTERVAL 40 DAY, CURDATE() - INTERVAL 26 DAY, CURDATE() - INTERVAL 30 DAY
FROM livro l, leitor r WHERE l.titulo = 'Terra Sonâmbula' AND r.nome = 'Joana Sitoe';

-- Histórico: devolvido 2 dias atrasado, multa de 20 MT já paga
INSERT INTO emprestimo (livro_id, leitor_id, data_emprestimo, data_prevista, data_devolucao, multa, multa_paga)
SELECT l.id, r.id, CURDATE() - INTERVAL 30 DAY, CURDATE() - INTERVAL 16 DAY, CURDATE() - INTERVAL 14 DAY, 20, TRUE
FROM livro l, leitor r WHERE l.titulo = 'Niketche' AND r.nome = 'Ana Machava';

-- Histórico: devolvido 3 dias atrasado, multa de 30 MT POR PAGAR (o Rui não pode levar livros: RN14)
INSERT INTO emprestimo (livro_id, leitor_id, data_emprestimo, data_prevista, data_devolucao, multa, multa_paga)
SELECT l.id, r.id, CURDATE() - INTERVAL 25 DAY, CURDATE() - INTERVAL 11 DAY, CURDATE() - INTERVAL 8 DAY, 30, FALSE
FROM livro l, leitor r WHERE l.titulo = 'Mensagem' AND r.nome = 'Rui Cossa';

-- Activo e EM ATRASO há 6 dias
INSERT INTO emprestimo (livro_id, leitor_id, data_emprestimo, data_prevista)
SELECT l.id, r.id, CURDATE() - INTERVAL 20 DAY, CURDATE() - INTERVAL 6 DAY
FROM livro l, leitor r WHERE l.titulo = 'Ualalapi' AND r.nome = 'Carlos Mondlane';

-- Activo dentro do prazo
INSERT INTO emprestimo (livro_id, leitor_id, data_emprestimo, data_prevista)
SELECT l.id, r.id, CURDATE() - INTERVAL 3 DAY, CURDATE() + INTERVAL 11 DAY
FROM livro l, leitor r WHERE l.titulo = 'Clean Code' AND r.nome = 'Joana Sitoe';

UPDATE livro SET disponivel = FALSE WHERE titulo IN ('Ualalapi', 'Clean Code');

-- A Ana reservou o Clean Code: quando a Joana o devolver, só a Ana o pode levar (RN16)
INSERT INTO reserva (livro_id, leitor_id, data_reserva, estado)
SELECT l.id, r.id, CURDATE() - INTERVAL 1 DAY, 'ACTIVA'
FROM livro l, leitor r WHERE l.titulo = 'Clean Code' AND r.nome = 'Ana Machava';
