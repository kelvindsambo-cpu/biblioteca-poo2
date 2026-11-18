# Proposta do projecto

## Problema
Uma biblioteca pequena precisa de registar livros, leitores e empréstimos sem usar folhas de papel, saber quem está atrasado e quanto deve, e guardar livros para quem os pediu.

## Objectivo
Construir uma aplicação desktop funcional e demonstrável para a disciplina de Programação Orientada a Objectos II.

## Funcionalidades
**Versão 1**
- Livros: adicionar, listar, pesquisar, editar e remover quando não existir histórico.
- Leitores: adicionar, listar, editar e remover quando não existir histórico.
- Empréstimos: registar, devolver, listar activos e consultar histórico.

**Versão 2**
- Entrada na aplicação com utilizador e password; alteração da password.
- Prazo de devolução por empréstimo, detecção de atrasos e multa de 10 MT por dia.
- Leitores com atrasos ou multas por pagar não podem levar livros.
- Reservas de livros emprestados.
- Separador Início com estatísticas, gráfico dos livros mais emprestados e lista de atrasos.
- Relatórios: impressão das tabelas, incluindo para PDF através da impressora do sistema.
- Livros com ISBN, ano e capa; capas ilustradas para os livros de exemplo.
- Pesquisa nos empréstimos, histórico por leitor e ordenação das tabelas.
- Interface nova: menu lateral, cartões e fundo desenhado.

## Fora do âmbito
Vários níveis de permissão entre funcionários, múltiplas bibliotecas, envio de email ou SMS, pagamentos electrónicos.

## Critérios académicos
- Encapsulamento nas entidades: os setters validam os dados.
- Responsabilidades separadas.
- Swing com eventos, `JTable`, `CardLayout` e componentes desenhados com `paintComponent`.
- MVC: a View não tem SQL; o Controller coordena as interacções; o DAO persiste.
- JDBC com `PreparedStatement`, `ResultSet` e transacções com `SELECT ... FOR UPDATE`.
- `SwingWorker` para todas as operações na base, tanto leituras como escritas.
- Excepções próprias para as regras de negócio (`RegraNegocioException` e `EmprestimoException`).
- Herança só quando justificada:
  - `PainelBase` junta o que os painéis têm em comum;
  - `EmprestimoException` é um caso de `RegraNegocioException`.
- Testes automáticos (JUnit) das regras.

## Limite de dimensão
Cinco tabelas, cinco separadores e um controller por separador.
