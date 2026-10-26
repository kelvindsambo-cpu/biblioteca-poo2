package biblioteca.dao;

import biblioteca.database.ConnectionFactory;
import biblioteca.model.Emprestimo;
import biblioteca.model.Leitor;
import biblioteca.model.Livro;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmprestimoDAO implements EmprestimoRepository {

    private static final String SELECT_BASE =
            "SELECT e.id, e.data_emprestimo, e.data_devolucao, "
            + "l.id AS livro_id, l.titulo, l.autor, l.disponivel, "
            + "r.id AS leitor_id, r.nome, r.telefone "
            + "FROM emprestimo e "
            + "JOIN livro l ON l.id = e.livro_id "
            + "JOIN leitor r ON r.id = e.leitor_id ";

    /**
     * Transacção: bloqueia o livro (FOR UPDATE), verifica disponibilidade,
     * insere o empréstimo e marca o livro como indisponível (RN03, RN04, RN05).
     */
    @Override
    public int emprestar(int livroId, int leitorId) throws SQLException {
        try (Connection con = ConnectionFactory.abrir()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT disponivel FROM livro WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, livroId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Livro não encontrado (ID " + livroId + ").");
                        }
                        if (!rs.getBoolean("disponivel")) {
                            throw new SQLException("O livro já está emprestado.");
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement("SELECT id FROM leitor WHERE id = ?")) {
                    ps.setInt(1, leitorId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Leitor não encontrado (ID " + leitorId + ").");
                        }
                    }
                }

                int emprestimoId;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO emprestimo (livro_id, leitor_id, data_emprestimo) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, livroId);
                    ps.setInt(2, leitorId);
                    ps.setDate(3, Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Não foi possível obter o ID do empréstimo.");
                        }
                        emprestimoId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE livro SET disponivel = FALSE WHERE id = ?")) {
                    ps.setInt(1, livroId);
                    ps.executeUpdate();
                }

                con.commit();
                return emprestimoId;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Transacção: bloqueia o empréstimo e o livro, verifica que o empréstimo
     * ainda está activo, regista a devolução e marca o livro como disponível
     * (RN06, RN07, RN10).
     */
    @Override
    public void devolver(int emprestimoId) throws SQLException {
        try (Connection con = ConnectionFactory.abrir()) {
            con.setAutoCommit(false);
            try {
                int livroId;
                LocalDate dataEmprestimo;
                // O FOR UPDATE com JOIN bloqueia a linha do empréstimo e a do livro.
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT e.livro_id, e.data_emprestimo, e.data_devolucao "
                        + "FROM emprestimo e JOIN livro l ON l.id = e.livro_id "
                        + "WHERE e.id = ? FOR UPDATE")) {
                    ps.setInt(1, emprestimoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Empréstimo não encontrado (ID " + emprestimoId + ").");
                        }
                        if (rs.getDate("data_devolucao") != null) {
                            throw new SQLException("Este empréstimo já foi devolvido.");
                        }
                        livroId = rs.getInt("livro_id");
                        dataEmprestimo = rs.getDate("data_emprestimo").toLocalDate();
                    }
                }

                LocalDate hoje = LocalDate.now();
                if (hoje.isBefore(dataEmprestimo)) {
                    throw new SQLException("A data de devolução não pode ser anterior à data do empréstimo.");
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE emprestimo SET data_devolucao = ? WHERE id = ?")) {
                    ps.setDate(1, Date.valueOf(hoje));
                    ps.setInt(2, emprestimoId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE livro SET disponivel = TRUE WHERE id = ?")) {
                    ps.setInt(1, livroId);
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<Emprestimo> listarTodos() throws SQLException {
        return listar(SELECT_BASE + "ORDER BY e.data_emprestimo DESC, e.id DESC");
    }

    @Override
    public List<Emprestimo> listarActivos() throws SQLException {
        return listar(SELECT_BASE + "WHERE e.data_devolucao IS NULL "
                + "ORDER BY e.data_emprestimo DESC, e.id DESC");
    }

    private List<Emprestimo> listar(String sql) throws SQLException {
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Emprestimo> emprestimos = new ArrayList<>();
            while (rs.next()) {
                emprestimos.add(mapear(rs));
            }
            return emprestimos;
        }
    }

    private Emprestimo mapear(ResultSet rs) throws SQLException {
        Livro livro = new Livro(
                rs.getInt("livro_id"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getBoolean("disponivel"));
        Leitor leitor = new Leitor(
                rs.getInt("leitor_id"),
                rs.getString("nome"),
                rs.getString("telefone"));
        Date devolucao = rs.getDate("data_devolucao");
        return new Emprestimo(
                rs.getInt("id"),
                livro,
                leitor,
                rs.getDate("data_emprestimo").toLocalDate(),
                devolucao == null ? null : devolucao.toLocalDate());
    }
}
