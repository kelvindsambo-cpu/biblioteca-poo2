package biblioteca.dao;

import biblioteca.database.ConnectionFactory;
import biblioteca.model.Emprestimo;
import biblioteca.model.Leitor;
import biblioteca.model.Livro;
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
    private static int falhas = 0;

    // Registos criados pelo teste, apagados no fim (mesmo que algo falhe)
    private static final List<Integer> livrosCriados = new ArrayList<>();
    private static final List<Integer> leitoresCriados = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        try {
            testarCrudEEmprestimos();
            testarEmprestimoConcorrente();
        } finally {
            limpar();
        }

        System.out.println(falhas == 0 ? "\nTodos os testes passaram." : "\n" + falhas + " teste(s) falharam.");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    private static void testarCrudEEmprestimos() throws SQLException {
        LivroRepository livros = new LivroDAO();
        LeitorRepository leitores = new LeitorDAO();
        EmprestimoRepository emprestimos = new EmprestimoDAO();

        Livro livro = new Livro(0, "Livro de teste", "Autor de teste", true);
        int livroId = livros.inserir(livro);
        livrosCriados.add(livroId);
        verificar("inserir livro devolve ID", livroId > 0);
        verificar("livro aparece na pesquisa",
                livros.pesquisar("de teste").stream().anyMatch(l -> l.getId() == livroId));

        livro.setTitulo("Livro de teste (editado)");
        livros.actualizar(livro);
        verificar("actualizar livro", livros.procurarPorId(livroId).getTitulo().endsWith("(editado)"));

        int leitorId = leitores.inserir(new Leitor(0, "Leitor de teste", "  "));
        leitoresCriados.add(leitorId);
        verificar("telefone vazio fica NULL", leitores.procurarPorId(leitorId).getTelefone() == null);

        int emprestimoId = emprestimos.emprestar(livroId, leitorId);
        verificar("T04 emprestar marca indisponível", !livros.procurarPorId(livroId).isDisponivel());
        verificar("empréstimo aparece nos activos",
                emprestimos.listarActivos().stream().anyMatch(e -> e.getId() == emprestimoId));

        verificarFalha("T05 emprestar livro indisponível", () -> emprestimos.emprestar(livroId, leitorId));
        verificarFalha("T08 remover livro com histórico", () -> livros.remover(livroId));
        verificarFalha("RN09 remover leitor com histórico", () -> leitores.remover(leitorId));

        emprestimos.devolver(emprestimoId);
        verificar("T06 devolver marca disponível", livros.procurarPorId(livroId).isDisponivel());
        Emprestimo e = emprestimos.listarTodos().stream()
                .filter(x -> x.getId() == emprestimoId).findFirst().orElseThrow();
        verificar("devolução tem data", e.getDataDevolucao() != null);

        verificarFalha("T07 devolver duas vezes", () -> emprestimos.devolver(emprestimoId));
        verificarFalha("emprestar livro inexistente", () -> emprestimos.emprestar(-1, leitorId));

        // Rollback: o livro existe mas o leitor não, por isso nada pode ficar gravado
        int antes = emprestimos.listarTodos().size();
        verificarFalha("emprestar a leitor inexistente", () -> emprestimos.emprestar(livroId, -1));
        verificar("rollback: nenhum empréstimo a mais e livro continua disponível",
                emprestimos.listarTodos().size() == antes && livros.procurarPorId(livroId).isDisponivel());

        int livroSemHistorico = livros.inserir(new Livro(0, "Para remover", "X", true));
        livros.remover(livroSemHistorico);
        verificar("remover livro sem histórico", livros.procurarPorId(livroSemHistorico) == null);
    }

    /**
     * RN03 com duas threads: as duas tentam emprestar o mesmo livro ao mesmo tempo.
     * O SELECT ... FOR UPDATE faz a segunda esperar pelo commit da primeira,
     * e aí já vê o livro indisponível. Só um empréstimo pode ser criado.
     */
    private static void testarEmprestimoConcorrente() throws Exception {
        LivroRepository livros = new LivroDAO();
        LeitorRepository leitores = new LeitorDAO();
        EmprestimoRepository emprestimos = new EmprestimoDAO();

        int livroId = livros.inserir(new Livro(0, "Livro concorrência", "Autor de teste", true));
        livrosCriados.add(livroId);
        int leitorA = leitores.inserir(new Leitor(0, "Leitor A", null));
        leitoresCriados.add(leitorA);
        int leitorB = leitores.inserir(new Leitor(0, "Leitor B", null));
        leitoresCriados.add(leitorB);

        CountDownLatch partida = new CountDownLatch(1);
        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger recusados = new AtomicInteger();

        List<Thread> threads = new ArrayList<>();
        for (int leitorId : new int[]{leitorA, leitorB}) {
            Thread t = new Thread(() -> {
                try {
                    partida.await(); // as duas arrancam ao mesmo tempo
                    emprestimos.emprestar(livroId, leitorId);
                    sucessos.incrementAndGet();
                } catch (SQLException ex) {
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

    /** Apaga directamente na base o que o teste criou (os DAOs não deixam: RN08/RN09). */
    private static void limpar() {
        if (livrosCriados.isEmpty() && leitoresCriados.isEmpty()) {
            return;
        }
        try (Connection con = ConnectionFactory.abrir()) {
            for (int id : livrosCriados) {
                apagar(con, "DELETE FROM emprestimo WHERE livro_id = ?", id);
                apagar(con, "DELETE FROM livro WHERE id = ?", id);
            }
            for (int id : leitoresCriados) {
                apagar(con, "DELETE FROM emprestimo WHERE leitor_id = ?", id);
                apagar(con, "DELETE FROM leitor WHERE id = ?", id);
            }
        } catch (SQLException ex) {
            System.out.println("Aviso: não foi possível apagar os registos de teste: " + ex.getMessage());
        }
    }

    private static void apagar(Connection con, String sql, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private interface Operacao {
        void executar() throws SQLException;
    }

    private static void verificar(String nome, boolean ok) {
        System.out.println((ok ? "[OK]    " : "[FALHA] ") + nome);
        if (!ok) {
            falhas++;
        }
    }

    private static void verificarFalha(String nome, Operacao operacao) {
        try {
            operacao.executar();
            verificar(nome + " (devia ter sido recusado)", false);
        } catch (SQLException ex) {
            verificar(nome + " -> \"" + ex.getMessage() + "\"", true);
        }
    }
}
