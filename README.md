# Biblioteca POO II

Aplicação desktop de gestão de uma biblioteca pequena, para demonstrar **Java Swing, MVC, JDBC, Threads e POO**.

## Âmbito
- **Versão 1:** Livros, Leitores e Empréstimos.
- **Versão 2:** entrada com utilizador e password, prazos de devolução, atrasos e multas, reservas, painel de Início com estatísticas, relatórios (impressão ou PDF), capas dos livros e uma interface nova com menu lateral e fundo desenhado.

## Tecnologias
Java 17, Swing, MySQL 8+, JDBC (MySQL Connector/J), Maven, `SwingWorker` e JUnit 5. Não há outras bibliotecas: o fundo, os botões, o gráfico e as capas de reserva são desenhados com `Graphics2D`, e os relatórios usam a impressão do próprio `JTable`.

## Começar
1. Instalar o JDK 17 ou mais recente, o Maven e o MySQL 8.
2. Preparar a base de dados e as variáveis `BIBLIOTECA_DB_*`. No Windows, na raiz do projecto, correr `powershell -ExecutionPolicy Bypass -File sql\preparar-mysql.ps1` e depois fechar e voltar a abrir o terminal e o IDE. Se a base já existir da versão 1, o script actualiza-a sozinho.
3. Na raiz, executar `mvn clean compile` e depois `mvn exec:java`.
4. Entrar com o utilizador **`admin`** e a password **`admin123`**. Mudar a password logo a seguir, em **Alterar password**.
5. Confirmar que tudo funciona:
   - `mvn test` corre os testes das regras, sem base de dados;
   - `mvn test-compile exec:java@smoke` testa os DAOs contra o MySQL e deve terminar com "Todos os testes passaram.".

O passo a passo e a resolução de problemas estão em [05 — Ambiente e GitHub](docs/05_AMBIENTE_E_GITHUB.md). Não colocar credenciais no Git.

## Usar a aplicação
- **Início:** números principais (livros, empréstimos, atrasos, multas por pagar), os livros mais emprestados e a lista de atrasos com o telefone dos leitores.
- **Livros:** preencher título e autor (ISBN e ano são opcionais) e clicar **Registar**. **Escolher imagem...** define a capa; sem imagem, a aplicação desenha uma capa com o título. Para editar ou remover, seleccionar a linha, alterar e clicar **Guardar alterações** ou **Remover**.
- **Leitores:** nome (obrigatório) e telefone (opcional). Ao seleccionar um leitor aparece o histórico de empréstimos dele.
- **Empréstimos:** escolher o livro, o leitor e o prazo (14 dias por defeito) e clicar **Emprestar**. Para devolver, seleccionar a linha e clicar **Devolver seleccionado**. Se houver atraso, a multa (10 MT por dia) fica registada. Quando o leitor pagar, usar **Registar pagamento de multa**. As linhas a vermelho estão em atraso ou têm multa por pagar.
- **Reservas:** um leitor pode reservar um livro que está emprestado. Quando o livro for devolvido, só esse leitor o pode levar.
- Em todas as tabelas: clicar no cabeçalho ordena, e **Imprimir / PDF** gera um relatório. Para obter um PDF, escolher a impressora "Microsoft Print to PDF".
- Ao abrir um separador, os dados são recarregados da base. Todas as operações na base correm em fundo, por isso a janela nunca congela.

## Documentação
- [01 — Proposta e âmbito](docs/01_PROPOSTA.md)
- [02 — Casos de uso e regras](docs/02_REQUISITOS.md)
- [03 — Arquitectura e contratos](docs/03_ARQUITECTURA_CONTRATOS.md)
- [04 — Divisão das tarefas](docs/04_TAREFAS.md)
- [05 — Ambiente e GitHub](docs/05_AMBIENTE_E_GITHUB.md)
- [06 — Testes e apresentação](docs/06_TESTES_APRESENTACAO.md)
- [07 — Mensagem para o grupo](docs/07_MENSAGEM_GRUPO.md)

## Critério de conclusão
- CRUD de livros e leitores.
- Empréstimo, devolução e reserva consistentes, feitos em transacções.
- Multas calculadas e registadas.
- Dados persistidos no MySQL.
- Interface responsiva durante as operações.
- Arquitectura MVC compreensível.
- Testes automáticos a passar.
