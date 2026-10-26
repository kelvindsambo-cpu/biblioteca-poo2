# Divisão de tarefas — 6 elementos

Os nomes serão preenchidos após a distribuição. Não é obrigatório equilibrar a dificuldade; o membro com tarefas mais fáceis ajuda nos testes.

| Membro | Responsabilidade | Entregáveis | Dificuldade |
|---|---|---|---|
| 1 | Model e validações | `Livro`, `Leitor`, `Emprestimo`; validações básicas e testes | Fácil |
| 2 | Módulo Livros | `LivroPanel`, `LivroController`, JTable, formulário e pesquisa | Média |
| 3 | Módulo Leitores | `LeitorPanel`, `LeitorController`, JTable e formulário | Média |
| 4 | Módulo Empréstimos | `EmprestimoPanel`, `EmprestimoController`, selecção e devolução | Difícil |
| 5 | Persistência | 3 interfaces repository, 3 DAOs, JDBC, SQL e transacções | Média/alta |
| 6 | Aplicação e integração | `MainFrame`, `Main`, `SwingWorker` com membro 2, integração e testes | Média |

## Sequência de trabalho
1. Todos: confirmar requisitos, contratos e JDK.
2. Membros 1 e 5: estabilizar Models, SQL e interfaces repository.
3. Membro 6: disponibilizar janela principal e separadores.
4. Membros 2, 3 e 4: criar painéis e controllers com dados de teste.
5. Membro 5: completar DAOs e operações transaccionais.
6. Todos: integrar, testar, corrigir e ensaiar apresentação.

## Checklist por membro
- [ ] Código compila (`mvn clean compile`).
- [ ] Não alterei contratos sem combinar com o grupo.
- [ ] Não coloquei SQL na View.
- [ ] Não guardei credenciais no repositório.
- [ ] Testei pelo menos um caso de sucesso e um caso de erro.
- [ ] Registei o que fiz no Pull Request.

## Política de colaboração
Branches `feature/model`, `feature/livros`, `feature/leitores`, `feature/emprestimos`, `feature/jdbc`, `feature/integracao`. Pull Requests pequenos para `main`, revistos por outro membro. Não enviar código que não compila. A integração deve começar cedo, não apenas na véspera da entrega.
