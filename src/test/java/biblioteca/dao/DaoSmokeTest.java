package biblioteca.dao;

import biblioteca.database.ConnectionFactory;
import biblioteca.model.Emprestimo;
import biblioteca.model.EstadoReserva;
import biblioteca.model.Leitor;
import biblioteca.model.Livro;
import biblioteca.model.RegraNegocioException;
import biblioteca.model.Reserva;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Teste manual dos DAOs contra a base de dados real (variáveis BIBLIOTECA_DB_*).
 * Cria os seus próprios registos e apaga-os no fim, por isso pode ser corrido
 * na base usada na apresentação. Executar com: mvn test-compile exec:java@smoke
 */
public class DaoSmokeTest {
    private static final int PRAZO = Emprestimo.PRAZO_PADRAO_DIAS;
    private static int falhas = 0;

    // Registos criados pelo teste, apagados no fim (mesmo que algo falhe)
    private static final List<Integer> livrosCriados = new ArrayList<>();
    private static final List<Integer> leitoresCriados = new ArrayList<>();

    private static final LivroRepository livros = new LivroDAO();
    private static final LeitorRepository leitores = new LeitorDAO();
    private static final EmprestimoRepository emprestimos = new EmprestimoDAO();
    private static final ReservaRepository reservas = new ReservaDAO();

    public static void main(String[] args) throws Exception {
        try {
            testarCrudEEmprestimos();
            testarMultasEAtrasos();
            testarReservas();
            testarEmprestimoConcorrente();
        } finally {
            limpar();
        }

        System.out.println(falhas == 0 ? "\nTodos os testes passaram." : "\n" + falhas + " teste(s) falharam.");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    private static void testarCrudEEmprestimos() throws Exception {
        System.out.println("--- Livros, leitores, empréstimo e devolução");
        Livro livro = new Livro(0, "Livro de teste", "Autor de teste", true);
        livro.setIsbn("978-0132350884");
        livro.setAno(2008);
        livro.setCapa("clean-code.png");
        int livroId = novoLivro(livro);
        verificar("inserir livro devolve ID", livroId > 0);
        verificar("livro aparece na pesquisa",
                livros.pesquisar("de teste").stream().anyMatch(l -> l.getId() == livroId));
        Livro lido = livros.procurarPorId(livroId);
        verificar("ISBN, ano e capa guardados",
                "978-0132350884".equals(lido.getIsbn()) && lido.getAno() == 2008 && "clean-code.png".equals(lido.getCapa()));

        livro.setTitulo("Livro de teste (editado)");
        livro.setAno(null);
        livros.actualizar(livro);
        lido = livros.procurarPorId(livroId);
        verificar("actualizar livro", lido.getTitulo().endsWith("(editado)") && lido.getAno() == null);

        int leitorId = novoLeitor("Leitor de teste", "  ");
        verificar("telefone vazio fica NULL", leitores.procurarPorId(leitorId).getTelefone() == null);

        int emprestimoId = emprestimos.emprestar(livroId, leitorId, PRAZO);
        verificar("T04 emprestar marca indisponível", !livros.procurarPorId(livroId).isDisponivel());
        Emprestimo criado = procurar(emprestimoId);
        verificar("prazo de " + PRAZO + " dias gravado",
                criado.getDataPrevista().equals(criado.getDataEmprestimo().plusDays(PRAZO)));
        verificar("empréstimo aparece nos activos",
                emprestimos.listarActivos().stream().anyMatch(e -> e.getId() == emprestimoId));
        verificar("pesquisa de empréstimos pelo nome do leitor",
                emprestimos.pesquisar("Leitor de teste", true).stream().anyMatch(e -> e.getId() == emprestimoId));
        verificar("histórico do leitor", emprestimos.listarPorLeitor(leitorId).size() == 1);

        verificarFalha("T05 emprestar livro indisponível", () -> emprestimos.emprestar(livroId, leitorId, PRAZO));
        verificarFalha("T08 remover livro com histórico", () -> livros.remover(livroId));
        verificarFalha("RN09 remover leitor com histórico", () -> leitores.remover(leitorId));

        int multa = emprestimos.devolver(emprestimoId);
        verificar("T06 devolver marca disponível e sem multa (a tempo)",
                livros.procurarPorId(livroId).isDisponivel() && multa == 0);
        verificar("devolução tem data", procurar(emprestimoId).getDataDevolucao() != null);

        verificarFalha("T07 devolver duas vezes", () -> emprestimos.devolver(emprestimoId));
        verificarFalha("emprestar livro inexistente", () -> emprestimos.emprestar(-1, leitorId, PRAZO));
        verificarFalha("RN11 prazo de 0 dias", () -> emprestimos.emprestar(livroId, leitorId, 0));

        // Rollback: o livro existe mas o leitor não, por isso nada pode ficar gravado
        int antes = emprestimos.listarTodos().size();
        verificarFalha("emprestar a leitor inexistente", () -> emprestimos.emprestar(livroId, -1, PRAZO));
        verificar("rollback: nenhum empréstimo a mais e livro continua disponível",
                emprestimos.listarTodos().size() == antes && livros.procurarPorId(livroId).isDisponivel());

        int livroSemHistorico = livros.inserir(new Livro(0, "Para remover", "X", true));
        livros.remover(livroSemHistorico);
        verificar("remover livro sem histórico", livros.procurarPorId(livroSemHistorico) == null);
    }

    /** Recua as datas de um empréstimo directamente na base, para simular um atraso sem esperar dias. */
    private static void testarMultasEAtrasos() throws Exception {
        System.out.println("--- Prazos, atrasos e multas");
        int livroA = novoLivro(new Livro(0, "Livro atraso A", "Autor de teste", true));
        int livroB = novoLivro(new Livro(0, "Livro atraso B", "Autor de teste", true));
        int leitor = novoLeitor("Leitor atrasado", null);

        int emprestimoId = emprestimos.emprestar(livroA, leitor, PRAZO);
        executar("UPDATE emprestimo SET data_emprestimo = CURDATE() - INTERVAL 20 DAY, "
                + "data_prevista = CURDATE() - INTERVAL 6 DAY WHERE id = ?", emprestimoId);

        verificar("aparece nos atrasados",
                emprestimos.listarAtrasados().stream().anyMatch(e -> e.getId() == emprestimoId));
        verificarFalha("RN15 leitor com livro em atraso não leva outro", () -> emprestimos.emprestar(livroB, leitor, PRAZO));

        int multa = emprestimos.devolver(emprestimoId);
        verificar("RN12 devolução 6 dias atrasada: multa de 60 MT", multa == 60);
        verificar("multa fica por pagar", procurar(emprestimoId).temMultaPorPagar());
        verificarFalha("RN14 leitor com multa por pagar não leva livros", () -> emprestimos.emprestar(livroB, leitor, PRAZO));

        emprestimos.pagarMulta(emprestimoId);
        verificar("pagamento registado", !procurar(emprestimoId).temMultaPorPagar());
        verificarFalha("pagar a mesma multa duas vezes", () -> emprestimos.pagarMulta(emprestimoId));

        int novo = emprestimos.emprestar(livroB, leitor, PRAZO);
        verificar("depois de pagar, o leitor já pode levar livros", novo > 0);
        emprestimos.devolver(novo);
    }

    private static void testarReservas() throws Exception {
        System.out.println("--- Reservas");
        int livro = novoLivro(new Livro(0, "Livro reservas", "Autor de teste", true));
        int dono = novoLeitor("Leitor com o livro", null);
        int quemReserva = novoLeitor("Leitor que reserva", null);
        int outro = novoLeitor("Outro leitor", null);

        verificarFalha("RN17 reservar livro disponível", () -> reservas.reservar(livro, quemReserva));
        int emprestimoId = emprestimos.emprestar(livro, dono, PRAZO);
        verificarFalha("RN17 reservar livro que o próprio leitor tem", () -> reservas.reservar(livro, dono));

        int reservaId = reservas.reservar(livro, quemReserva);
        verificar("reservar livro emprestado", reservaId > 0);
        verificarFalha("RN17 segunda reserva activa para o mesmo livro", () -> reservas.reservar(livro, outro));
        Reserva activa = reservas.procurarActivaPorLivro(livro);
        verificar("reserva activa encontrada", activa != null && activa.getLeitor().getId() == quemReserva);

        emprestimos.devolver(emprestimoId);
        verificarFalha("RN16 livro reservado não vai para outro leitor", () -> emprestimos.emprestar(livro, outro, PRAZO));

        int levantado = emprestimos.emprestar(livro, quemReserva, PRAZO);
        boolean atendida = reservas.listarTodas().stream()
                .anyMatch(r -> r.getId() == reservaId && r.getEstado() == EstadoReserva.ATENDIDA);
        verificar("RN16 quem reservou leva o livro e a reserva fica atendida", levantado > 0 && atendida);
        verificarFalha("cancelar reserva já atendida", () -> reservas.cancelar(reservaId));

        int segunda = reservas.reservar(livro, outro);
        reservas.cancelar(segunda);
        verificar("cancelar reserva activa", reservas.procurarActivaPorLivro(livro) == null);
        verificarFalha("RN08 remover livro com histórico e reservas", () -> livros.remover(livro));
        emprestimos.devolver(levantado);
    }

    /**
     * RN03 com duas threads: as duas tentam emprestar o mesmo livro ao mesmo tempo.
     * O SELECT ... FOR UPDATE faz a segunda esperar pelo commit da primeira,
     * e aí já vê o livro indisponível. Só um empréstimo pode ser criado.
     */
    private static void testarEmprestimoConcorrente() throws Exception {
        System.out.println("--- Concorrência");
        int livroId = novoLivro(new Livro(0, "Livro concorrência", "Autor de teste", true));
        int leitorA = novoLeitor("Leitor A", null);
        int leitorB = novoLeitor("Leitor B", null);

        CountDownLatch partida = new CountDownLatch(1);
        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger recusados = new AtomicInteger();

        List<Thread> threads = new ArrayList<>();
        for (int leitorId : new int[]{leitorA, leitorB}) {
            Thread t = new Thread(() -> {
                try {
                    partida.await(); // as duas arrancam ao mesmo tempo
                    emprestimos.emprestar(livroId, leitorId, PRAZO);
                    sucessos.incrementAndGet();
                } catch (SQLException | RegraNegocioException ex) {
                    recusados.incrementAndGet();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(t);
            t.start();
        }
        partida.countDown();
        for (Thread t : threads) {
            t.join();
        }

        long activos = emprestimos.listarActivos().stream()
                .filter(e -> e.getLivro().getId() == livroId).count();
        verificar("RN03 concorrência: 1 empréstimo aceite e 1 recusado",
                sucessos.get() == 1 && recusados.get() == 1 && activos == 1);
    }

    // ---------- Apoio ----------

    private static int novoLivro(Livro livro) throws SQLException {
        int id = livros.inserir(livro);
        livrosCriados.add(id);
        return id;
    }

    private static int novoLeitor(String nome, String telefone) throws SQLException {
        int id = leitores.inserir(new Leitor(0, nome, telefone));
        leitoresCriados.add(id);
        return id;
    }

    private static Emprestimo procurar(int emprestimoId) throws SQLException {
        return emprestimos.listarTodos().stream().filter(e -> e.getId() == emprestimoId).findFirst().orElseThrow();
    }

    private static void executar(String sql, int id) throws SQLException {
        try (Connection con = ConnectionFactory.abrir()) {
            sql(con, sql, id);
        }
    }

    /** Apaga directamente na base o que o teste criou (os DAOs não deixam: RN08/RN09). */
    private static void limpar() {
        if (livrosCriados.isEmpty() && leitoresCriados.isEmpty()) {
            return;
        }
        try (Connection con = ConnectionFactory.abrir()) {
            for (int id : livrosCriados) {
                sql(con, "DELETE FROM reserva WHERE livro_id = ?", id);
                sql(con, "DELETE FROM emprestimo WHERE livro_id = ?", id);
                sql(con, "DELETE FROM livro WHERE id = ?", id);
            }
            for (int id : leitoresCriados) {
                sql(con, "DELETE FROM reserva WHERE leitor_id = ?", id);
                sql(con, "DELETE FROM emprestimo WHERE leitor_id = ?", id);
                sql(con, "DELETE FROM leitor WHERE id = ?", id);
            }
        } catch (SQLException ex) {
            System.out.println("Aviso: não foi possível apagar os registos de teste: " + ex.getMessage());
        }
    }

    private static void sql(Connection con, String sql, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private interface Operacao {
        void executar() throws Exception;
    }

    private static void verificar(String nome, boolean ok) {
        System.out.println((ok ? "[OK]    " : "[FALHA] ") + nome);
        if (!ok) {
            falhas++;
        }
    }

    /** A operação tem de ser recusada com uma regra de negócio (ou erro SQL), nunca aceite. */
    private static void verificarFalha(String nome, Operacao operacao) throws Exception {
        try {
            operacao.executar();
            verificar(nome + " (devia ter sido recusado)", false);
        } catch (RegraNegocioException | SQLException ex) {
            verificar(nome + " -> \"" + ex.getMessage() + "\"", true);
        }
    }
}
