# Plano de testes e demonstração

Antes de testar, a aplicação tem de estar configurada (ver [05](05_AMBIENTE_E_GITHUB.md)).

## 1. Testes automáticos das regras (sem base de dados)
Na raiz do projecto:

```powershell
mvn test
```

Correm 18 testes JUnit em `src/test/java/biblioteca/model` e `src/test/java/biblioteca/util`:

| Classe | O que verifica |
|---|---|
| `LivroTest` | RN01: título e autor obrigatórios, tamanho máximo, ISBN válido/inválido, intervalo do ano |
| `LeitorTest` | RN02: nome obrigatório, telefone opcional, formato do telefone |
| `EmprestimoTest` | RN11 a RN13: prazo, atraso, multa de 10 MT/dia, estado do empréstimo, multa paga/por pagar |
| `SenhaTest` | RN18: o hash do Java é igual ao `SHA2` do MySQL (o admin consegue entrar); cada sal é diferente |

Estes testes usam datas fixas, por isso dão o mesmo resultado em qualquer dia.

## 2. Teste dos DAOs contra o MySQL
O [`DaoSmokeTest`](../src/test/java/biblioteca/dao/DaoSmokeTest.java) testa a persistência directamente contra o MySQL, sem interface:

```powershell
mvn test-compile exec:java@smoke
```

O teste cria os seus próprios livros, leitores, empréstimos e reservas e **apaga-os no fim**, por isso pode ser corrido na base da apresentação sem deixar lixo. Está dividido em quatro grupos:
- **Livros, leitores, empréstimo e devolução:** inclui o rollback e a T05/T07/T08.
- **Prazos, atrasos e multas:** recua as datas na base para simular 6 dias de atraso; verifica a multa de 60 MT e as regras RN14 e RN15.
- **Reservas:** RN16 e RN17.
- **Concorrência:** duas threads a emprestar o mesmo livro ao mesmo tempo.

Deve terminar com `Todos os testes passaram.`. Qualquer `[FALHA]` indica um problema nos DAOs ou na base. Se aparecer um erro de ligação, ver "Problemas comuns" no doc 05.

## 3. Testes manuais na interface
Abrir a aplicação com `mvn exec:java`. Os dados de exemplo (`sql/dados_teste.sql`) já trazem um empréstimo em atraso (Ualalapi, com o Carlos), uma multa por pagar (Rui) e uma reserva (Clean Code, para a Ana).

| Teste | Passos | Esperado |
|---|---|---|
| T01 | Entrar com `admin` / password errada | "Utilizador ou password errados." e a janela continua aberta |
| T02 | Entrar com `admin` / `admin123` | Abre o Início com os números e o atraso do Carlos a vermelho |
| T03 | Livros: preencher título e autor, escolher uma imagem, **Registar** | "Livro registado..." e o livro aparece com a capa |
| T04 | Livros: deixar o título vazio, **Registar** | "O título do livro é obrigatório." e nada é gravado |
| T05 | Livros: ISBN `123`, **Registar** | Mensagem sobre os 10 ou 13 dígitos |
| T06 | Leitores: preencher nome, **Registar**; depois clicar na Ana | Leitor aparece; à direita vê-se o histórico da Ana |
| T07 | Empréstimos: livro + Ana + prazo 7, **Emprestar** | Mensagem com a data de devolução; o livro sai da lista de disponíveis |
| T08 | Empréstimos: tentar emprestar ao **Rui** | Recusado: tem 30 MT de multas por pagar (RN14) |
| T09 | Desmarcar **Mostrar só activos**, seleccionar a linha do Rui (Mensagem), **Registar pagamento de multa** | "Pagamento registado."; o Rui já pode levar livros |
| T10 | Empréstimos: tentar emprestar ao **Carlos** | Recusado: tem livros em atraso (RN15) |
| T11 | Seleccionar o empréstimo do Carlos (Ualalapi), **Devolver seleccionado** | "Devolução registada." com a multa calculada |
| T12 | Seleccionar o Clean Code (Joana), **Devolver**; tentar emprestá-lo à Joana | Aviso de que o livro está reservado para a Ana; a seguir, empréstimo à Joana recusado (RN16) |
| T13 | Emprestar o Clean Code à Ana | Aceite; em Reservas, a reserva da Ana passa a "Atendida" |
| T14 | Reservas: escolher um livro emprestado e um leitor, **Reservar**; repetir com outro leitor | A 1.ª é aceite; a 2.ª é recusada (uma reserva activa por livro) |
| T15 | Livros: seleccionar um livro com histórico, **Remover** | "Este livro tem histórico de empréstimos e não pode ser removido." |
| T16 | Qualquer tabela: clicar no cabeçalho "ID" duas vezes | Ordena 1, 2, ..., 10 e depois ao contrário; a selecção continua a apontar para o registo certo |
| T17 | Empréstimos: **Imprimir / PDF** e escolher "Microsoft Print to PDF" | É criado um PDF com a tabela, a data e o número da página |
| T18 | **Alterar password**, errar a actual | "A password actual está errada." |
| T19 | **Sair**, voltar a entrar | Volta à janela de entrada; os dados continuam lá |
| T20 | Num terminal novo: `$env:BIBLIOTECA_DB_PASSWORD="errada"` e depois `mvn exec:java` | A janela de entrada mostra "Não foi possível ligar à base de dados." sem fechar. Fechar esse terminal no fim |

## 4. Roteiro de apresentação (aprox. 8–10 minutos)
1. Explicar o problema e as tecnologias.
2. Entrar (login) e mostrar o Início: números, gráfico e atrasos.
3. Registar um livro com capa e um leitor.
4. Emprestar com prazo; tentar emprestar ao Rui (multa) e ao Carlos (atraso).
5. Devolver o livro atrasado e mostrar a multa; registar o pagamento.
6. Mostrar a reserva do Clean Code a funcionar (T12–T13).
7. Gerar um relatório em PDF.
8. Mostrar no código os pontos da secção 5.
9. Concluir com os conceitos de POO II demonstrados.

Antes de apresentar:
- correr `mvn test` e o teste dos DAOs;
- confirmar que a base tem os dados de exemplo (para recomeçar do zero: `DROP DATABASE biblioteca_poo2;` no MySQL e voltar a correr o script);
- fazer o roteiro uma vez completo.

## 5. Pontos do código para mostrar
1. **Camadas.** A View e o Controller só conhecem as interfaces `*Repository`; todo o SQL está nos DAOs.
2. **Encapsulamento.** Os setters de [`Livro`](../src/main/java/biblioteca/model/Livro.java) recusam dados inválidos, por isso um livro sem título não chega a existir. As regras de multa estão em [`Emprestimo`](../src/main/java/biblioteca/model/Emprestimo.java) e são testadas pelo JUnit.
3. **Excepções próprias.** `RegraNegocioException` e a subclasse `EmprestimoException` separam "regra violada" (mensagem para o funcionário) de "erro técnico" (`SQLException`). Ver `Tarefa.mensagem`.
4. **Transacção no empréstimo.** Em [`EmprestimoDAO.emprestar`](../src/main/java/biblioteca/dao/EmprestimoDAO.java):
   - `setAutoCommit(false)`;
   - `SELECT ... FOR UPDATE` bloqueia o livro;
   - verificações de disponibilidade, leitor, multas, atrasos e reserva;
   - `INSERT` e `UPDATE`;
   - `commit()`; qualquer erro faz `rollback()`.
5. **Threads.** [`Tarefa`](../src/main/java/biblioteca/controller/Tarefa.java) põe **todas** as operações num `SwingWorker`. Mostrar como um controller lê a View na EDT, chama o DAO em fundo e actualiza a View no `done()`.
6. **Swing desenhado à mão.** `FundoPanel`, `Botao`, `Cartao` e `GraficoBarras` sobrepõem `paintComponent` e desenham com `Graphics2D`.
7. **Passwords.** [`Senha`](../src/main/java/biblioteca/util/Senha.java): sal aleatório + SHA-256, e a comparação em tempo constante.
8. **Regras na base.** As chaves estrangeiras e os `CHECK` em [`sql/biblioteca.sql`](../sql/biblioteca.sql) garantem a consistência mesmo que alguém altere a base fora da aplicação.

Perguntas prováveis:
- *Porque não validar a disponibilidade só na interface?* Porque dois postos podem ver o livro disponível ao mesmo tempo; só o bloqueio na base garante a RN03.
- *Porque guardar a multa em vez de a calcular sempre?* Porque fica fixada no dia da devolução; mudar a tabela de multas no futuro não altera multas antigas.
- *Porque abrir uma ligação por operação?* É simples e seguro para uma aplicação pequena; com muitos utilizadores usava-se um pool de ligações.
- *O SHA-256 é suficiente?* Para este projecto sim, com sal. Em produção usava-se um algoritmo lento, próprio para passwords (bcrypt, Argon2).

## Definição de pronto
- Todas as operações funcionam no MySQL.
- Nenhuma operação parcial deixa um livro, empréstimo ou reserva incoerente.
- As interfaces não executam SQL.
- `mvn test` e o teste dos DAOs passam.
- O projecto compila a partir do repositório.
