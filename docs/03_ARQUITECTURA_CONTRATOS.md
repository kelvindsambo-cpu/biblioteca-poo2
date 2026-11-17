# Arquitectura e contratos de integração — v2

## Fluxo
`View (Swing) -> Controller -> Repository (DAO) -> MySQL`.
- O Controller usa os Models.
- A View recebe resultados e mensagens e **nunca escreve SQL**.
- Os DAOs não conhecem componentes Swing.

## Pacotes
| Pacote | Conteúdo |
|---|---|
| `biblioteca.model` | Entidades com validação: `Livro`, `Leitor`, `Emprestimo`, `Reserva`, `EstadoReserva`, `Funcionario`, `Estatisticas`; excepções `RegraNegocioException` e `EmprestimoException` |
| `biblioteca.dao` | Interfaces `*Repository` e as classes `*DAO` (todo o SQL) |
| `biblioteca.controller` | Um controller por ecrã, mais `Tarefa`, que corre o trabalho em fundo com `SwingWorker` |
| `biblioteca.view` | Janelas, painéis e componentes desenhados (`Tema`, `Botao`, `Cartao`, `FundoPanel`, `GraficoBarras`, `Capas`) |
| `biblioteca.database` | `ConnectionFactory` |
| `biblioteca.util` | `Senha` (hash das passwords) e `ArquivoCapas` (ficheiros das imagens) |

## Modelos (atributos privados)
- `Livro(int id, String titulo, String autor, boolean disponivel)`, com `isbn`, `ano` e `capa` opcionais. Os setters validam (RN01) e lançam `IllegalArgumentException` com uma mensagem amigável.
- `Leitor(int id, String nome, String telefone)`, que valida o nome (RN02). `Leitor.validarFormatoTelefone` é usado só no formulário.
- `Emprestimo(id, livro, leitor, dataEmprestimo, dataPrevista, dataDevolucao, multa, multaPaga)`. Tem as regras de prazo, atraso e multa: `calcularMulta`, `isAtrasado(hoje)`, `getDiasAtraso(hoje)`, `getEstado(hoje)`, etc. Os métodos recebem "hoje" como parâmetro para poderem ser testados com qualquer data.
- `Reserva(id, livro, leitor, dataReserva, EstadoReserva estado)`.
- `id=0` indica um objecto ainda não persistido.

## Excepções
- `RegraNegocioException`: a operação foi recusada por uma regra; a mensagem é para o funcionário.
- `EmprestimoException extends RegraNegocioException`: regras de empréstimo, devolução e multa.
- `SQLException`: erro técnico (ligação, base). O Controller mostra-o com outro texto (`Tarefa.mensagem`).

## Interfaces
```java
public interface LivroRepository {
    int inserir(Livro livro) throws SQLException;
    void actualizar(Livro livro) throws SQLException;
    void remover(int id) throws SQLException, RegraNegocioException;
    Livro procurarPorId(int id) throws SQLException; // null se não existir
    List<Livro> listarTodos() throws SQLException;
    List<Livro> pesquisar(String texto) throws SQLException;
}

public interface LeitorRepository {
    int inserir(Leitor leitor) throws SQLException;
    void actualizar(Leitor leitor) throws SQLException;
    void remover(int id) throws SQLException, RegraNegocioException;
    Leitor procurarPorId(int id) throws SQLException;
    List<Leitor> listarTodos() throws SQLException;
}

public interface EmprestimoRepository {
    int emprestar(int livroId, int leitorId, int prazoDias) throws SQLException, EmprestimoException;
    int devolver(int emprestimoId) throws SQLException, EmprestimoException; // devolve a multa
    void pagarMulta(int emprestimoId) throws SQLException, EmprestimoException;
    List<Emprestimo> listarTodos() throws SQLException;
    List<Emprestimo> listarActivos() throws SQLException;
    List<Emprestimo> listarAtrasados() throws SQLException;
    List<Emprestimo> listarPorLeitor(int leitorId) throws SQLException;
    List<Emprestimo> pesquisar(String texto, boolean apenasActivos) throws SQLException;
}

public interface ReservaRepository {
    int reservar(int livroId, int leitorId) throws SQLException, RegraNegocioException;
    void cancelar(int reservaId) throws SQLException, RegraNegocioException;
    Reserva procurarActivaPorLivro(int livroId) throws SQLException;
    List<Reserva> listarActivas() throws SQLException;
    List<Reserva> listarTodas() throws SQLException;
}

public interface FuncionarioRepository {
    Funcionario autenticar(String utilizador, String password) throws SQLException; // null se errado
    void alterarPassword(int funcionarioId, String actual, String nova) throws SQLException, RegraNegocioException;
}

public interface EstatisticaRepository {
    Estatisticas obter(int limite) throws SQLException;
}
```

## Controllers
| Controller | Ecrã | Repositórios |
|---|---|---|
| `LoginController` | `LoginFrame` e sessão (`MainFrame`) | `FuncionarioRepository` |
| `InicioController` | `InicioPanel` | `EstatisticaRepository`, `EmprestimoRepository` |
| `LivroController` | `LivroPanel` | `LivroRepository` |
| `LeitorController` | `LeitorPanel` | `LeitorRepository`, `EmprestimoRepository` (histórico) |
| `EmprestimoController` | `EmprestimoPanel` | `EmprestimoRepository`, `LivroRepository`, `LeitorRepository`, `ReservaRepository` |
| `ReservaController` | `ReservaPanel` | `ReservaRepository`, `LivroRepository`, `LeitorRepository` |

As views expõem métodos simples:
- leitura dos campos (`getTitulo()`, `getEmprestimoSeleccionado()`, ...);
- actualização (`mostrarLivros(lista)`, ...);
- ligação de acções (`aoRegistar(Runnable)`, ...).

O `MainFrame` tem o menu lateral e um `CardLayout` com os cinco painéis. Cada painel herda de `PainelBase` (título, mensagens, confirmação, impressão).

## Threads
Todas as operações na base, incluindo as escritas, passam por `Tarefa.emFundo(trabalho, sucesso, falha)`, que usa um `SwingWorker`:
- `doInBackground()` chama o DAO numa thread de fundo;
- `done()` volta à EDT e chama `sucesso` ou `falha`.

Nunca actualizar componentes Swing dentro do trabalho de fundo. Os valores da View são lidos **antes** de passar para o fundo.

## Relatórios e imagens
- **Relatórios:** `JTable.print()` com cabeçalho e rodapé. O PDF obtém-se com a impressora "Microsoft Print to PDF".
- **Capas:** a coluna `livro.capa` guarda só o nome do ficheiro.
  - As capas de exemplo estão em `src/main/resources/capas`.
  - As escolhidas pelo funcionário são copiadas para a pasta `capas/` na raiz, que está no `.gitignore`.
  - Um livro sem capa recebe uma capa desenhada com o título.

## Validação
- As regras simples (campos obrigatórios, formatos) estão nos setters dos modelos.
- As regras que dependem da base (disponibilidade, multas, reservas) estão nos DAOs, dentro de transacções.
- Usar sempre `PreparedStatement`.
