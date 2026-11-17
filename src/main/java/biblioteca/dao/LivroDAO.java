package biblioteca.dao;

import biblioteca.database.ConnectionFactory;
import biblioteca.model.Livro;
import biblioteca.model.RegraNegocioException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class LivroDAO implements LivroRepository {

    private static final String COLUNAS = "SELECT id AS livro_id, titulo, autor, disponivel, isbn, ano, capa FROM livro ";

    @Override
    public int inserir(Livro livro) throws SQLException {
        String sql = "INSERT INTO livro (titulo, autor, isbn, ano, capa, disponivel) VALUES (?, ?, ?, ?, ?, TRUE)";
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, livro);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Não foi possível obter o ID do livro inserido.");
                }
                int id = rs.getInt(1);
                livro.setId(id);
                livro.setDisponivel(true);
                return id;
            }
        }
    }

    // Não altera a disponibilidade: essa é gerida pelo EmprestimoDAO (RN05, RN06).
    @Override
    public void actualizar(Livro livro) throws SQLException {
        String sql = "UPDATE livro SET titulo = ?, autor = ?, isbn = ?, ano = ?, capa = ? WHERE id = ?";
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            preencher(ps, livro);
            ps.setInt(6, livro.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Livro não encontrado (ID " + livro.getId() + ").");
            }
        }
    }

    @Override
    public void remover(int id) throws SQLException, RegraNegocioException {
        try (Connection con = ConnectionFactory.abrir()) {
            // RN08: livro com histórico de empréstimos ou reservas não pode ser eliminado.
            if (contar(con, "SELECT COUNT(*) FROM emprestimo WHERE livro_id = ?", id) > 0) {
                throw new RegraNegocioException("Este livro tem histórico de empréstimos e não pode ser removido.");
            }
            if (contar(con, "SELECT COUNT(*) FROM reserva WHERE livro_id = ?", id) > 0) {
                throw new RegraNegocioException("Este livro tem reservas registadas e não pode ser removido.");
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM livro WHERE id = ?")) {
                ps.setInt(1, id);
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("Livro não encontrado (ID " + id + ").");
                }
            } catch (SQLException e) {
                // 1451 = chave estrangeira: alguém registou um empréstimo entretanto
                if (e.getErrorCode() == 1451) {
                    throw new RegraNegocioException("Este livro tem histórico e não pode ser removido.");
                }
                throw e;
            }
        }
    }

    @Override
    public Livro procurarPorId(int id) throws SQLException {
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(COLUNAS + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Livro> listarTodos() throws SQLException {
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(COLUNAS + "ORDER BY titulo");
             ResultSet rs = ps.executeQuery()) {
            List<Livro> livros = new ArrayList<>();
            while (rs.next()) {
                livros.add(mapear(rs));
            }
            return livros;
        }
    }

    // Pesquisa por parte do título, do autor ou do ISBN.
    @Override
    public List<Livro> pesquisar(String texto) throws SQLException {
        String sql = COLUNAS + "WHERE titulo LIKE ? OR autor LIKE ? OR isbn LIKE ? ORDER BY titulo";
        String padrao = "%" + (texto == null ? "" : texto.trim()) + "%";
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, padrao);
            ps.setString(2, padrao);
            ps.setString(3, padrao);
            try (ResultSet rs = ps.executeQuery()) {
                List<Livro> livros = new ArrayList<>();
                while (rs.next()) {
                    livros.add(mapear(rs));
                }
                return livros;
            }
        }
    }

    /** Parâmetros 1 a 5: título, autor, ISBN, ano e capa (os três últimos podem ser NULL). */
    private void preencher(PreparedStatement ps, Livro livro) throws SQLException {
        ps.setString(1, livro.getTitulo());
        ps.setString(2, livro.getAutor());
        ps.setString(3, livro.getIsbn());
        if (livro.getAno() == null) {
            ps.setNull(4, Types.SMALLINT);
        } else {
            ps.setInt(4, livro.getAno());
        }
        ps.setString(5, livro.getCapa());
    }

    static int contar(Connection con, String sql, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    static Livro mapear(ResultSet rs) throws SQLException {
        return EmprestimoDAO.livroDoJoin(rs); // as colunas têm os mesmos nomes
    }
}
