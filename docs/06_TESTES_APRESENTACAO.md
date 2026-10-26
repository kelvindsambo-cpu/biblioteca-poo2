# Plano de testes e demonstração

## Testes mínimos
| Teste | Acção | Esperado |
|---|---|---|
| T01 | Registar livro válido | Aparece na lista após recarregar |
| T02 | Registar livro sem título | Mensagem de validação |
| T03 | Registar leitor válido | Aparece na lista |
| T04 | Emprestar livro disponível | Cria empréstimo e marca indisponível |
| T05 | Emprestar livro indisponível | Operação recusada |
| T06 | Devolver empréstimo activo | Marca devolução e disponibilidade |
| T07 | Devolver duas vezes | Segunda operação recusada |
| T08 | Eliminar livro com histórico | Operação recusada |
| T09 | Reiniciar aplicação | Dados persistem |
| T10 | Carregar livros em segundo plano | Interface continua responsiva |
| T11 | Falha de ligação MySQL | Erro tratado sem encerrar abruptamente |

## Roteiro de apresentação (aprox. 5–8 minutos)
1. Explicar problema e tecnologias.
2. Mostrar as três tabelas e estrutura MVC.
3. Adicionar livro e leitor.
4. Emprestar e tentar emprestar o mesmo livro novamente.
5. Devolver e consultar histórico.
6. Mostrar `PreparedStatement`, transacção e `SwingWorker` no código.
7. Concluir com os conceitos de POO II demonstrados.

## Definição de pronto
Todas as operações funcionam no MySQL; nenhuma operação parcial deixa livro/empréstimo incoerente; interfaces não executam SQL; testes mínimos passam; projecto compila a partir do repositório.
