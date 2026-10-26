-- Dados de exemplo para testes e apresentação. Executar depois de biblioteca.sql.
USE biblioteca_poo2;

INSERT INTO livro (titulo, autor) VALUES
  ('Terra Sonâmbula', 'Mia Couto'),
  ('O Último Voo do Flamingo', 'Mia Couto'),
  ('Ualalapi', 'Ungulani Ba Ka Khosa'),
  ('Balada de Amor ao Vento', 'Paulina Chiziane'),
  ('Clean Code', 'Robert C. Martin');

INSERT INTO leitor (nome, telefone) VALUES
  ('Ana Machava', '841234567'),
  ('Carlos Mondlane', NULL),
  ('Joana Sitoe', '829876543');
