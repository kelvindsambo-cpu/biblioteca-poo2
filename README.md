# Biblioteca POO II

Aplicação desktop pequena de gestão de biblioteca para demonstrar **Java Swing, MVC, JDBC, Threads e POO**.

## Âmbito
Três módulos: **Livros**, **Leitores** e **Empréstimos**. Um funcionário opera a aplicação; não há login. A primeira versão não inclui multas, reservas, relatórios nem notificações.

## Tecnologias
Java 17, Swing, MySQL 8+, JDBC (MySQL Connector/J), Maven e `SwingWorker`.

## Começar
1. Instalar JDK 17 ou mais recente, Maven e MySQL 8.
2. Preparar a base de dados e as variáveis `BIBLIOTECA_DB_*`. No Windows, na raiz do projecto: `powershell -ExecutionPolicy Bypass -File sql\preparar-mysql.ps1`. Fechar e voltar a abrir o terminal e o IDE.
3. Na raiz, executar `mvn clean compile` e depois `mvn exec:java`.
4. Confirmar que tudo funciona com `mvn test-compile exec:java@smoke` (deve terminar com "Todos os testes passaram.").

Passo a passo e resolução de problemas: [05 — Ambiente e GitHub](docs/05_AMBIENTE_E_GITHUB.md). Não colocar credenciais no Git.

**Estado:** os três módulos estão implementados e integrados na janela principal.

## Usar a aplicação
- **Livros:** preencher título e autor e clicar **Registar**. Para editar ou remover, seleccionar a linha na tabela, alterar os campos e clicar **Editar** ou **Remover**. **Pesquisar** filtra a tabela; com a pesquisa vazia volta a lista completa.
- **Leitores:** igual aos livros, com nome (obrigatório) e telefone (opcional).
- **Empréstimos:** escolher o livro e o leitor nas listas e clicar **Emprestar**. A lista de livros só mostra os disponíveis. Para devolver, seleccionar o empréstimo na tabela e clicar **Devolver seleccionado**. Desmarcar **Mostrar só activos** mostra o histórico completo.
- Um livro ou leitor com empréstimos registados não pode ser removido (RN08/RN09).
- Ao mudar de separador, os dados são recarregados da base.

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
