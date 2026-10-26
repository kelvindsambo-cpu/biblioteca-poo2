package biblioteca.dao;

import biblioteca.database.ConnectionFactory;
import biblioteca.model.Leitor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class LeitorDAO implements LeitorRepository {

    @Override
    public int inserir(Leitor leitor) throws SQLException {
        String sql = "INSERT INTO leitor (nome, telefone) VALUES (?, ?)";
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, leitor.getNome());
            definirTelefone(ps, 2, leitor.getTelefone());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Não foi possível obter o ID do leitor inserido.");
                }
                int id = rs.getInt(1);
                leitor.setId(id);
                return id;
            }
        }
    }

    @Override
    public void actualizar(Leitor leitor) throws SQLException {
        String sql = "UPDATE leitor SET nome = ?, telefone = ? WHERE id = ?";
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, leitor.getNome());
            definirTelefone(ps, 2, leitor.getTelefone());
            ps.setInt(3, leitor.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Leitor não encontrado (ID " + leitor.getId() + ").");
            }
        }
    }

    @Override
    public void remover(int id) throws SQLException {
        try (Connection con = ConnectionFactory.abrir()) {
            // RN09: leitor com histórico de empréstimos não pode ser eliminado.
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM emprestimo WHERE leitor_id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        throw new SQLException("Não é possível remover: o leitor tem empréstimos registados.");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM leitor WHERE id = ?")) {
                ps.setInt(1, id);
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("Leitor não encontrado (ID " + id + ").");
                }
            }
        }
    }

    @Override
    public Leitor procurarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome, telefone FROM leitor WHERE id = ?";
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Leitor> listarTodos() throws SQLException {
        String sql = "SELECT id, nome, telefone FROM leitor ORDER BY nome";
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Leitor> leitores = new ArrayList<>();
            while (rs.next()) {
                leitores.add(mapear(rs));
            }
            return leitores;
        }
    }

    // RN02: telefone é opcional; texto vazio é guardado como NULL.
    private void definirTelefone(PreparedStatement ps, int indice, String telefone) throws SQLException {
        if (telefone == null || telefone.isBlank()) {
            ps.setNull(indice, Types.VARCHAR);
        } else {
            ps.setString(indice, telefone.trim());
        }
    }

    private Leitor mapear(ResultSet rs) throws SQLException {
        return new Leitor(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("telefone"));
    }
}
