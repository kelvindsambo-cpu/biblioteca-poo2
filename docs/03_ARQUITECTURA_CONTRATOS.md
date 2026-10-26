# Arquitectura e contratos de integração — v1

## Fluxo
`View (Swing) -> Controller -> DAO -> MySQL`. O Controller utiliza os Models. A View recebe resultados e mensagens; **nunca escreve SQL**. Os DAOs não conhecem componentes Swing.

## Pacotes
`biblioteca.model`, `biblioteca.view`, `biblioteca.controller`, `biblioteca.dao`, `biblioteca.database`.

## Modelos (atributos privados)
- `Livro(int id, String titulo, String autor, boolean disponivel)`; getters e setters.
- `Leitor(int id, String nome, String telefone)`; getters e setters.
- `Emprestimo(int id, Livro livro, Leitor leitor, LocalDate dataEmprestimo, LocalDate dataDevolucao)`; getters e setters.
- `id=0` indica objecto ainda não persistido.

## Assinaturas propostas (interfaces contratuais)
Todos os métodos de DAO que consultam ou modificam a base de dados declaram `throws SQLException`. `inserir` devolve o ID gerado. Operações sem resultado devolvem `void` e lançam excepção se falharem. Não alterar assinaturas unilateralmente.

```java
public interface LivroRepository {
    int inserir(Livro livro) throws SQLException;
    void actualizar(Livro livro) throws SQLException;
    void remover(int id) throws SQLException;
    Livro procurarPorId(int id) throws SQLException; // null se não existir
    List<Livro> listarTodos() throws SQLException;
    List<Livro> pesquisar(String texto) throws SQLException;
}

public interface LeitorRepository {
    int inserir(Leitor leitor) throws SQLException;
    void actualizar(Leitor leitor) throws SQLException;
    void remover(int id) throws SQLException;
    Leitor procurarPorId(int id) throws SQLException;
    List<Leitor> listarTodos() throws SQLException;
}

public interface EmprestimoRepository {
    int emprestar(int livroId, int leitorId) throws SQLException;
    void devolver(int emprestimoId) throws SQLException;
    List<Emprestimo> listarTodos() throws SQLException;
    List<Emprestimo> listarActivos() throws SQLException;
}
```

As classes `LivroDAO`, `LeitorDAO` e `EmprestimoDAO` implementam essas interfaces. Os ficheiros das interfaces ficam em `dao/` e pertencem ao membro 5, com revisão do grupo.

## Controllers
- `LivroController(LivroRepository dao, LivroPanel view)` coordena listar, pesquisar, inserir, editar e remover.
- `LeitorController(LeitorRepository dao, LeitorPanel view)` coordena operações dos leitores.
- `EmprestimoController(EmprestimoRepository dao, EmprestimoPanel view)` coordena empréstimos e devoluções.
- As views expõem métodos simples de leitura dos campos e actualização de tabelas. A assinatura concreta desses métodos será acordada pelo respectivo par Controller/View antes da implementação.
- `MainFrame` cria um `JTabbedPane` e adiciona os três painéis.

## Threads
Consultas longas podem ser executadas com `SwingWorker<List<Livro>, Void>`: `doInBackground()` consulta DAO; `done()` actualiza Swing na EDT. Nunca actualizar componentes Swing dentro de `doInBackground()`. Erros SQL são apresentados ao utilizador sem mostrar senhas.

## Validação
Campos obrigatórios não vazios; mensagens amigáveis na View. Usar `PreparedStatement` para valores SQL. Em caso de remoção de registo referenciado, apresentar erro compreensível.

## Dependências entre membros
Model e contratos de DAO devem estar acordados primeiro. As views podem começar com dados fictícios, sem depender da base de dados. A integração final liga controllers aos DAOs reais.
