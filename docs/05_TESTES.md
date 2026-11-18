# Testes

Antes de testar, a aplicação tem de estar configurada (ver [04 — Instalação](04_INSTALACAO.md)).

## 1. Testes automáticos das regras (sem base de dados)
Na raiz do projecto:

```powershell
mvn test
```

Correm 18 testes JUnit em `src/test/java/biblioteca/model` e `src/test/java/biblioteca/util`:

| Classe | O que verifica |
|---|---|
| `LivroTest` | RN01: título e autor obrigatórios, tamanho máximo, ISBN válido e inválido, intervalo do ano |
| `LeitorTest` | RN02: nome obrigatório, telefone opcional, formato do telefone |
| `EmprestimoTest` | RN11 a RN13: prazo, atraso, multa de 10 MT/dia, estado do empréstimo, multa paga ou por pagar |
| `SenhaTest` | RN18: o hash do Java é igual ao `SHA2` do MySQL (o admin consegue entrar); cada sal é diferente |

Estes testes usam datas fixas, por isso dão o mesmo resultado em qualquer dia.

## 2. Teste dos DAOs contra o MySQL
O [`DaoSmokeTest`](../src/test/java/biblioteca/dao/DaoSmokeTest.java) testa a persistência directamente contra o MySQL, sem interface:

```powershell
mvn test-compile exec:java@smoke
```

No IDE, também se pode correr o método `main` de `DaoSmokeTest`.

O teste cria os seus próprios livros, leitores, empréstimos e reservas e **apaga-os no fim**, por isso não deixa registos na base. Tem 40 verificações, em quatro grupos:
- **Livros, leitores, empréstimo e devolução:** inclui o *rollback* de um empréstimo a meio.
- **Prazos, atrasos e multas:** recua as datas na base para simular 6 dias de atraso; verifica a multa de 60 MT e as regras RN14 e RN15.
- **Reservas:** RN16 e RN17.
- **Concorrência:** duas threads a emprestar o mesmo livro ao mesmo tempo; só uma consegue (RN03).

Deve terminar com `Todos os testes passaram.`. Qualquer `[FALHA]` indica um problema nos DAOs ou na base. Se aparecer um erro de ligação, ver "Problemas comuns" em [04 — Instalação](04_INSTALACAO.md).

## 3. Testes manuais na interface
Abrir a aplicação com `mvn exec:java`. Os dados de exemplo (`sql/dados_teste.sql`) já trazem um empréstimo em atraso (Ualalapi, com o Carlos), uma multa por pagar (Rui) e uma reserva (Clean Code, para a Ana).

| Teste | Passos | Esperado |
|---|---|---|
| T01 | Entrar com `admin` / password errada | "Utilizador ou password errados." e a janela continua aberta |
| T02 | Entrar com `admin` / `admin123` | Abre o Início com os números e o atraso do Carlos a vermelho |
| T03 | Livros: preencher título e autor, escolher uma imagem, **Registar** | "Livro registado..." e o livro aparece com a capa |
| T04 | Livros: deixar o título vazio, **Registar** | "O título do livro é obrigatório." e nada é gravado |
| T05 | Livros: ISBN `123`, **Registar** | Mensagem sobre os 10 ou 13 dígitos |
| T06 | Leitores: preencher nome, **Registar**; depois clicar na Ana | O leitor aparece; à direita vê-se o histórico da Ana |
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
| T20 | Num terminal novo: `$env:BIBLIOTECA_DB_PASSWORD="errada"` e depois `mvn exec:java` | A janela de entrada mostra "Não foi possível ligar à base de dados." sem fechar |
