package biblioteca.dao;

import biblioteca.database.ConnectionFactory;
import biblioteca.model.Livro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LivroDAO implements LivroRepository {

    @Override
    public int inserir(Livro livro) throws SQLException {
        String sql = "INSERT INTO livro (titulo, autor, disponivel) VALUES (?, ?, TRUE)";
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
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

    // Só altera título e autor: a disponibilidade é gerida pelo EmprestimoDAO (RN05, RN06).
    @Override
    public void actualizar(Livro livro) throws SQLException {
        String sql = "UPDATE livro SET titulo = ?, autor = ? WHERE id = ?";
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
            ps.setInt(3, livro.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Livro não encontrado (ID " + livro.getId() + ").");
            }
        }
    }

    @Override
    public void remover(int id) throws SQLException {
        try (Connection con = ConnectionFactory.abrir()) {
            // RN08: livro com histórico de empréstimos não pode ser eliminado.
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM emprestimo WHERE livro_id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        throw new SQLException("Não é possível remover: o livro tem empréstimos registados.");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM livro WHERE id = ?")) {
                ps.setInt(1, id);
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("Livro não encontrado (ID " + id + ").");
                }
            }
        }
    }

    @Override
    public Livro procurarPorId(int id) throws SQLException {
        String sql = "SELECT id, titulo, autor, disponivel FROM livro WHERE id = ?";
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Livro> listarTodos() throws SQLException {
        String sql = "SELECT id, titulo, autor, disponivel FROM livro ORDER BY titulo";
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Livro> livros = new ArrayList<>();
            while (rs.next()) {
                livros.add(mapear(rs));
            }
            return livros;
        }
    }

    // Pesquisa por parte do título ou do autor.
    @Override
    public List<Livro> pesquisar(String texto) throws SQLException {
        String sql = "SELECT id, titulo, autor, disponivel FROM livro "
                + "WHERE titulo LIKE ? OR autor LIKE ? ORDER BY titulo";
        String padrao = "%" + (texto == null ? "" : texto.trim()) + "%";
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, padrao);
            ps.setString(2, padrao);
            try (ResultSet rs = ps.executeQuery()) {
                List<Livro> livros = new ArrayList<>();
                while (rs.next()) {
                    livros.add(mapear(rs));
                }
                return livros;
            }
        }
    }

    private Livro mapear(ResultSet rs) throws SQLException {
        return new Livro(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getBoolean("disponivel"));
    }
}
