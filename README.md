# Biblioteca POO II

Aplicação desktop de gestão de uma biblioteca pequena, desenvolvida em **Java Swing, MVC, JDBC e Threads** para a unidade curricular de Programação Orientada a Objectos II (Universidade Eduardo Mondlane, Faculdade de Engenharia, 2026).

**Relatório:** [docs/Relatorio_POO2_Biblioteca.pdf](docs/Relatorio_POO2_Biblioteca.pdf)

## Autores
Chindela, Jéssica Jorge · Condjo, Edson Nicolau · Lissenga, Wendy Carla · Nhantumbo, Nuno João · Saite, Cíntia de Carmen Sílvio · Sambo, Kelvin Damião

Docentes: PhD Eng. Ruben Manhiça (regente) e Eng. Helton Jossias (assistente).

## Funcionalidades
- **Entrada** com utilizador e password (guardada com sal + SHA-256) e alteração da password.
- **Início:** livros, empréstimos activos, atrasos, reservas e multas por pagar; os livros mais emprestados; a lista de atrasos com o telefone dos leitores.
- **Livros:** registar, editar, remover e pesquisar; ISBN, ano e imagem da capa.
- **Leitores:** registar, editar e remover; histórico de empréstimos de cada leitor.
- **Empréstimos:** prazo de 1 a 60 dias, devolução, multa de 10 MT por dia de atraso e registo do pagamento. Leitores com atrasos ou multas por pagar não podem levar livros.
- **Reservas** de livros emprestados: quando o livro volta, só quem o reservou o pode levar.
- **Relatórios:** qualquer tabela pode ser impressa ou guardada em PDF.

Todas as operações na base correm em fundo (`SwingWorker`), por isso a janela nunca congela. Emprestar, devolver e reservar são transacções com `SELECT ... FOR UPDATE`.

## Tecnologias
Java 17, Swing, MySQL 8+, JDBC (MySQL Connector/J), Maven, `SwingWorker` e JUnit 5. Não há outras bibliotecas: o fundo, os botões, o gráfico e as capas dos livros sem imagem são desenhados com `Graphics2D`, e os relatórios usam a impressão do próprio `JTable`.

## Como executar
1. Instalar o JDK 17 ou mais recente, o Maven e o MySQL 8.
2. Criar a base de dados e as variáveis `BIBLIOTECA_DB_*`. No Windows, na raiz do projecto, correr `powershell -ExecutionPolicy Bypass -File sql\preparar-mysql.ps1` e depois fechar e voltar a abrir o terminal e o IDE.
3. Na raiz, executar `mvn clean compile` e depois `mvn exec:java`.
4. Entrar com o utilizador **`admin`** e a password **`admin123`**, e mudar a password em **Alterar password**.

A base de dados está na pasta `sql/`: estrutura (`biblioteca.sql`), dados de exemplo (`dados_teste.sql`) e actualização de uma base antiga (`actualizar-v2.sql`). O passo a passo completo e a resolução de problemas estão em [04 — Instalação](docs/04_INSTALACAO.md).

## Testes
- `mvn test`: 18 testes JUnit das regras, sem base de dados.
- `mvn test-compile exec:java@smoke`: 40 verificações dos DAOs contra o MySQL, incluindo o *rollback* e a concorrência entre duas threads. Deve terminar com "Todos os testes passaram.".

Detalhes e testes manuais em [05 — Testes](docs/05_TESTES.md).

## Documentação
- [Relatório (PDF)](docs/Relatorio_POO2_Biblioteca.pdf)
- [01 — Proposta e âmbito](docs/01_PROPOSTA.md)
- [02 — Casos de uso e regras](docs/02_REQUISITOS.md)
- [03 — Arquitectura e contratos](docs/03_ARQUITECTURA_CONTRATOS.md)
- [04 — Instalação e execução](docs/04_INSTALACAO.md)
- [05 — Testes](docs/05_TESTES.md)
