# Requisitos e regras de negócio

## Actor
Funcionário da biblioteca, que entra com utilizador e password. Todos os funcionários têm as mesmas permissões.

## Casos de uso
| ID | Caso de uso | Resultado esperado |
|---|---|---|
| UC01 | Registar livro | Livro guardado e visível (ISBN, ano e capa opcionais) |
| UC02 | Listar/pesquisar livros | Tabela filtrada por título, autor ou ISBN |
| UC03 | Editar livro | Dados e capa actualizados |
| UC04 | Remover livro | Só se não houver empréstimos nem reservas associados |
| UC05 | Registar leitor | Leitor guardado |
| UC06 | Listar leitores e ver histórico | Tabela actualizada; histórico do leitor seleccionado |
| UC07 | Editar leitor | Dados actualizados |
| UC08 | Remover leitor | Só se não houver empréstimos nem reservas associados |
| UC09 | Emprestar livro | Empréstimo criado com prazo; livro indisponível |
| UC10 | Devolver livro | Data e multa registadas; livro disponível; aviso se estiver reservado |
| UC11 | Consultar/pesquisar empréstimos | Activos e histórico, com dias, estado e multa |
| UC12 | Entrar / sair / alterar password | Só um funcionário autenticado usa a aplicação |
| UC13 | Registar pagamento de multa | Multa marcada como paga |
| UC14 | Reservar livro / cancelar reserva | Reserva activa ou cancelada |
| UC15 | Consultar o Início | Estatísticas, livros mais emprestados e atrasos |
| UC16 | Gerar relatório | Tabela impressa ou guardada em PDF |

## Regras
- RN01: título e autor obrigatórios; ISBN com 10 ou 13 dígitos; ano entre 1000 e o ano seguinte ao actual.
- RN02: nome do leitor obrigatório; telefone opcional.
- RN03: um livro só pode ter um empréstimo activo.
- RN04: o empréstimo referencia um livro e um leitor existentes.
- RN05: emprestar marca o livro como indisponível.
- RN06: devolver marca o livro como disponível.
- RN07: a devolução duplicada é proibida.
- RN08: um livro com histórico (empréstimos ou reservas) não pode ser eliminado.
- RN09: um leitor com histórico (empréstimos ou reservas) não pode ser eliminado.
- RN10: a data de devolução não pode preceder a data do empréstimo.
- RN11: cada empréstimo tem um prazo entre 1 e 60 dias (14 por defeito); a data prevista é a data do empréstimo mais o prazo.
- RN12: a devolução depois da data prevista gera uma multa de 10 MT por dia de atraso, fixada no momento da devolução.
- RN13: um empréstimo activo está em atraso quando hoje é depois da data prevista.
- RN14: um leitor com multas por pagar não pode levar livros.
- RN15: um leitor com livros em atraso não pode levar mais livros.
- RN16: um livro com reserva activa só pode ser emprestado a quem o reservou; nesse momento a reserva passa a "Atendida".
- RN17: só se reservam livros emprestados, nunca pelo próprio leitor que o tem, e no máximo com uma reserva activa por livro.
- RN18: as passwords nunca são guardadas em claro; guarda-se um sal aleatório e o SHA-256 de (sal + password). A password nova tem pelo menos 6 caracteres.

## Decisões simplificadoras
- Um registo `livro` representa **um exemplar físico**. Se existirem dois exemplares do mesmo título, são dois registos.
- `data_devolucao = NULL` significa empréstimo activo.
- As datas são registadas pelo sistema.
- A multa não é paga em parcelas: está paga ou por pagar.

## Consistência
Emprestar, devolver e reservar são operações atómicas numa transacção JDBC.
- **Emprestar:** bloqueia a linha do livro (`SELECT ... FOR UPDATE`) e verifica a disponibilidade, o leitor, as multas, os atrasos e a reserva. Depois insere o empréstimo, actualiza o livro e, se for o caso, a reserva.
- **Devolver:** bloqueia o empréstimo e o livro, verifica que o empréstimo ainda está activo, calcula a multa e regista a devolução e a disponibilidade.

Em todas: `commit` se tudo correr bem, `rollback` em qualquer erro ou regra violada.
