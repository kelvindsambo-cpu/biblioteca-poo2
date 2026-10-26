# Biblioteca POO II

Aplicação desktop pequena de gestão de biblioteca para demonstrar **Java Swing, MVC, JDBC, Threads e POO**.

## Âmbito
Três módulos: **Livros**, **Leitores** e **Empréstimos**. Um funcionário opera a aplicação; não há login. A primeira versão não inclui multas, reservas, relatórios nem notificações.

## Tecnologias
Java 17, Swing, MySQL 8+, JDBC (MySQL Connector/J), Maven e `SwingWorker`.

## Começar
1. Instalar JDK 17, Maven e MySQL.
2. Executar `sql/biblioteca.sql` no MySQL.
3. Configurar as variáveis de ambiente `BIBLIOTECA_DB_URL`, `BIBLIOTECA_DB_USER` e `BIBLIOTECA_DB_PASSWORD` (ver `docs/05_AMBIENTE_E_GITHUB.md`). Não colocar credenciais no Git.
4. Na raiz, executar `mvn clean compile` e `mvn exec:java`.

**Estado inicial:** estrutura e documentação, ligação JDBC e janela com separadores vazios. Os módulos ainda não estão implementados.

## Documentação
- [01 — Proposta e âmbito](docs/01_PROPOSTA.md)
- [02 — Casos de uso e regras](docs/02_REQUISITOS.md)
- [03 — Arquitectura e contratos](docs/03_ARQUITECTURA_CONTRATOS.md)
- [04 — Divisão das tarefas](docs/04_TAREFAS.md)
- [05 — Ambiente e GitHub](docs/05_AMBIENTE_E_GITHUB.md)
- [06 — Testes e apresentação](docs/06_TESTES_APRESENTACAO.md)
- [07 — Mensagem para o grupo](docs/07_MENSAGEM_GRUPO.md)

## Critério de conclusão
CRUD de livros e leitores, empréstimo e devolução consistentes, dados persistidos no MySQL, interface responsiva durante consultas e arquitectura MVC compreensível.
