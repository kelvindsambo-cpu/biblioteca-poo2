# Plano de testes e demonstração

Antes de testar, a aplicação tem de estar configurada (ver [05](05_AMBIENTE_E_GITHUB.md)).

## 1. Teste automático dos DAOs
O [`DaoSmokeTest`](../src/test/java/biblioteca/dao/DaoSmokeTest.java) testa a persistência directamente contra o MySQL, sem interface. Na raiz do projecto:

```powershell
mvn test-compile exec:java@smoke
```

Cria os seus próprios livros, leitores e empréstimos e **apaga-os no fim**: pode ser corrido na base da apresentação sem deixar lixo. Resultado esperado (17 verificações):

```
[OK]    inserir livro devolve ID
...
[OK]    T05 emprestar livro indisponível -> "O livro já está emprestado."
[OK]    T07 devolver duas vezes -> "Este empréstimo já foi devolvido."
[OK]    rollback: nenhum empréstimo a mais e livro continua disponível
[OK]    RN03 concorrência: 1 empréstimo aceite e 1 recusado

Todos os testes passaram.
```

Qualquer `[FALHA]` indica um problema nos DAOs ou na base. Se aparecer um erro de ligação, ver "Problemas comuns" no doc 05.

Nota: `mvn test` **não** corre este teste. Não é JUnit; é um programa com `main` porque precisa do MySQL real.

## 2. Testes manuais na interface
Abrir a aplicação com `mvn exec:java`. Usar dados com nomes fáceis de reconhecer (ex.: "Livro T01") e apagá-los no fim, quando possível.

| Teste | Passos | Esperado |
|---|---|---|
| T01 | Livros: preencher título e autor, **Registar** | "Livro registado com sucesso." e o livro aparece na tabela |
| T02 | Livros: deixar o título vazio, **Registar** | "O título do livro é obrigatório." e nada é gravado |
| T03 | Leitores: preencher nome, **Registar** | "Leitor registado com sucesso." e aparece na tabela |
| T04 | Empréstimos: escolher livro e leitor, **Emprestar** | "Empréstimo registado."; aparece na tabela; no separador Livros fica indisponível |
| T05 | Empréstimos: abrir a lista **Livro** depois do T04 | O livro emprestado já não aparece. A recusa ao nível da base é verificada pelo teste automático |
| T06 | Empréstimos: seleccionar o empréstimo do T04, **Devolver seleccionado** | "Devolução registada."; o livro volta a aparecer na lista e fica disponível |
| T07 | Desmarcar **Mostrar só activos**, seleccionar o empréstimo devolvido, **Devolver seleccionado** | "Este empréstimo já foi devolvido." |
| T08 | Livros: seleccionar o livro do T04, **Remover** e confirmar | "Este livro possui histórico de empréstimos e não pode ser removido." |
| T09 | Fechar a aplicação e voltar a abrir | Os dados continuam lá |
| T10 | Mudar de separador várias vezes | A janela nunca congela. As consultas correm em `SwingWorker`; com poucos dados é instantâneo, por isso mostrar no código |
| T11 | Num terminal novo: `$env:BIBLIOTECA_DB_PASSWORD="errada"` e depois `mvn exec:java` | A janela abre e mostra mensagens de erro, sem fechar de repente. Fechar esse terminal no fim |

## 3. Roteiro de apresentação (aprox. 5–8 minutos)
1. Explicar problema e tecnologias.
2. Mostrar as três tabelas e estrutura MVC.
3. Adicionar livro e leitor.
4. Emprestar e tentar emprestar o mesmo livro novamente.
5. Devolver e consultar histórico.
6. Mostrar `PreparedStatement`, transacção e `SwingWorker` no código (secção 4).
7. Concluir com os conceitos de POO II demonstrados.

Antes de apresentar: correr o teste automático, confirmar que a base tem os dados de exemplo e fazer o roteiro uma vez completo.

## 4. Persistência e transacções (membro 5)
Cerca de 1–2 minutos, no passo 6 do roteiro.

1. **Camadas.** A View e o Controller só conhecem as interfaces `LivroRepository`, `LeitorRepository` e `EmprestimoRepository`; todo o SQL está nos DAOs. Trocar o MySQL só obrigaria a mudar os DAOs.
2. **`PreparedStatement`.** Todos os valores entram por `?`, nunca concatenados no SQL: protege contra SQL injection. Mostrar `LivroDAO.inserir`.
3. **Transacção no empréstimo.** Abrir [`EmprestimoDAO.emprestar`](../src/main/java/biblioteca/dao/EmprestimoDAO.java#L32):
   - `setAutoCommit(false)`: o que vem a seguir é uma só operação;
   - `SELECT ... FOR UPDATE` bloqueia a linha do livro até ao fim da transacção;
   - verifica se o livro está disponível e se o leitor existe, insere o empréstimo e marca o livro indisponível;
   - `commit()` grava tudo; qualquer erro faz `rollback()` e nada fica a meio.
   A devolução segue o mesmo padrão.
4. **Demonstração.** Correr `mvn test-compile exec:java@smoke` e comentar as últimas linhas:
   - *rollback*: emprestar a um leitor que não existe falha depois de o livro já estar bloqueado, e mesmo assim não fica nenhum empréstimo gravado;
   - *concorrência*: [duas threads](../src/test/java/biblioteca/dao/DaoSmokeTest.java#L95) tentam emprestar o mesmo livro ao mesmo tempo. Com o `FOR UPDATE`, a segunda espera pelo commit da primeira, vê o livro indisponível e é recusada. Sem o bloqueio, as duas podiam passar e o livro ficava com dois empréstimos activos (RN03).
5. **Regras na base.** As chaves estrangeiras e o `CHECK` em [`sql/biblioteca.sql`](../sql/biblioteca.sql) garantem a consistência mesmo que alguém altere a base fora da aplicação.

Perguntas prováveis:
- *Porque não validar a disponibilidade só na interface?* Porque dois postos podem ver o livro disponível ao mesmo tempo; só o bloqueio na base garante RN03.
- *Porque abrir uma ligação por operação?* É simples e seguro para uma aplicação pequena; com muitos utilizadores usava-se um pool de ligações.

## Definição de pronto
Todas as operações funcionam no MySQL; nenhuma operação parcial deixa livro/empréstimo incoerente; interfaces não executam SQL; testes mínimos passam; projecto compila a partir do repositório.
