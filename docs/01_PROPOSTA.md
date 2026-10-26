# Proposta do projecto

## Problema
Uma biblioteca pequena precisa de registar livros, leitores e empréstimos sem utilizar folhas de papel.

## Objectivo
Construir uma aplicação desktop funcional e demonstrável para a disciplina de Programação Orientada a Objectos II.

## Funcionalidades incluídas
- Livros: adicionar, listar, pesquisar, editar e remover quando não existir histórico.
- Leitores: adicionar, listar, editar e remover quando não existir histórico.
- Empréstimos: registar, devolver, listar activos e consultar histórico.

## Fora do âmbito
Autenticação, multas, prazos obrigatórios, reservas, múltiplas bibliotecas, PDF, email e dashboards.

## Critérios académicos
- Encapsulamento nas entidades; responsabilidades separadas.
- Swing com eventos e `JTable`.
- MVC: View sem SQL; Controller coordena interacções; DAO persiste.
- JDBC com `PreparedStatement`, `ResultSet` e transacções.
- `SwingWorker` para consultas sem bloquear a interface.
- Tratamento de erros e validação de dados.
- Interfaces/herança apenas quando justificadas; não inventar hierarquias artificiais.

## Limite de dimensão
Três tabelas, três painéis, três controllers, três DAOs. Novas funcionalidades exigem consenso do grupo.
