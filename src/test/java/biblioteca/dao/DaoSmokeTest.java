package biblioteca.dao;

import biblioteca.model.Emprestimo;
import biblioteca.model.Leitor;
import biblioteca.model.Livro;
import java.sql.SQLException;

/**
 * Teste manual dos DAOs contra a base de dados real (variáveis BIBLIOTECA_DB_*).
 * Cria os seus próprios registos; o livro e o leitor ficam na base porque têm
 * histórico (RN08/RN09).
 */
public class DaoSmokeTest {
    private static int falhas = 0;

    public static void main(String[] args) throws SQLException {
        LivroRepository livros = new LivroDAO();
        LeitorRepository leitores = new LeitorDAO();
        EmprestimoRepository emprestimos = new EmprestimoDAO();

        Livro livro = new Livro(0, "Livro de teste", "Autor de teste", true);
        int livroId = livros.inserir(livro);
        verificar("inserir livro devolve ID", livroId > 0);
        verificar("livro aparece na pesquisa",
                livros.pesquisar("de teste").stream().anyMatch(l -> l.getId() == livroId));

        livro.setTitulo("Livro de teste (editado)");
        livros.actualizar(livro);
        verificar("actualizar livro", livros.procurarPorId(livroId).getTitulo().endsWith("(editado)"));

        int leitorId = leitores.inserir(new Leitor(0, "Leitor de teste", "  "));
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

        int livroSemHistorico = livros.inserir(new Livro(0, "Para remover", "X", true));
        livros.remover(livroSemHistorico);
        verificar("remover livro sem histórico", livros.procurarPorId(livroSemHistorico) == null);

        System.out.println(falhas == 0 ? "\nTodos os testes passaram." : "\n" + falhas + " teste(s) falharam.");
        if (falhas > 0) {
            System.exit(1);
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
