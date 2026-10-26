# Requisitos e regras de negócio

## Actor
Funcionário da biblioteca (sem autenticação nesta versão).

## Casos de uso
| ID | Caso de uso | Resultado esperado |
|---|---|---|
| UC01 | Registar livro | Livro guardado e visível |
| UC02 | Listar/pesquisar livros | Tabela filtrada |
| UC03 | Editar livro | Título/autor actualizados |
| UC04 | Remover livro | Só se não houver empréstimos associados |
| UC05 | Registar leitor | Leitor guardado |
| UC06 | Listar leitores | Tabela actualizada |
| UC07 | Editar leitor | Dados actualizados |
| UC08 | Remover leitor | Só se não houver empréstimos associados |
| UC09 | Emprestar livro | Empréstimo criado e livro indisponível |
| UC10 | Devolver livro | Data registada e livro disponível |
| UC11 | Consultar empréstimos | Activos e histórico apresentados |

## Regras
- RN01: título e autor obrigatórios.
- RN02: nome do leitor obrigatório; telefone opcional.
- RN03: um livro só pode ter um empréstimo activo.
- RN04: empréstimo referencia livro e leitor existentes.
- RN05: emprestar marca livro como indisponível.
- RN06: devolver marca livro como disponível.
- RN07: devolução duplicada é proibida.
- RN08: livro com histórico não pode ser eliminado.
- RN09: leitor com histórico não pode ser eliminado.
- RN10: data de devolução não pode preceder a data do empréstimo.

## Decisões simplificadoras
Um registo `livro` representa **um exemplar físico**. Se existirem dois exemplares do mesmo título, são dois registos. `data_devolucao = NULL` significa empréstimo activo. Datas são registadas pelo sistema.

## Consistência
Emprestar e devolver são operações atómicas numa transacção JDBC. Na operação de empréstimo, bloquear a linha do livro (`SELECT ... FOR UPDATE`), verificar disponibilidade, inserir empréstimo e actualizar livro. Na devolução, bloquear o empréstimo e o livro, verificar que ainda está activo, actualizar a devolução e disponibilidade. Fazer `commit` ou `rollback`.
