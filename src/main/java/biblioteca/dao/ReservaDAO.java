package biblioteca.dao;

import biblioteca.database.ConnectionFactory;
import biblioteca.model.EstadoReserva;
import biblioteca.model.RegraNegocioException;
import biblioteca.model.Reserva;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO implements ReservaRepository {

    private static final String SELECT_BASE =
            "SELECT s.id, s.data_reserva, s.estado, "
            + "l.id AS livro_id, l.titulo, l.autor, l.disponivel, l.isbn, l.ano, l.capa, "
            + "r.id AS leitor_id, r.nome, r.telefone "
            + "FROM reserva s "
            + "JOIN livro l ON l.id = s.livro_id "
            + "JOIN leitor r ON r.id = s.leitor_id ";

    /**
     * Transacção: bloqueia o livro e verifica as regras de reserva (RN16, RN17):
     * só livros emprestados, nunca pelo próprio leitor, e no máximo uma reserva activa por livro.
     */
    @Override
    public int reservar(int livroId, int leitorId) throws SQLException, RegraNegocioException {
        try (Connection con = ConnectionFactory.abrir()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT disponivel FROM livro WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, livroId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new RegraNegocioException("Livro não encontrado (ID " + livroId + ").");
                        }
                        if (rs.getBoolean("disponivel")) {
                            throw new RegraNegocioException("O livro está disponível: pode ser emprestado já, sem reserva.");
                        }
                    }
                }

                if (LivroDAO.contar(con, "SELECT COUNT(*) FROM leitor WHERE id = ?", leitorId) == 0) {
                    throw new RegraNegocioException("Leitor não encontrado (ID " + leitorId + ").");
                }

                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM emprestimo "
                        + "WHERE livro_id = ? AND leitor_id = ? AND data_devolucao IS NULL")) {
                    ps.setInt(1, livroId);
                    ps.setInt(2, leitorId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            throw new RegraNegocioException("Este leitor já tem o livro emprestado.");
                        }
                    }
                }

                if (LivroDAO.contar(con, "SELECT COUNT(*) FROM reserva WHERE livro_id = ? AND estado = 'ACTIVA'", livroId) > 0) {
                    throw new RegraNegocioException("O livro já tem uma reserva activa.");
                }

                int id;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO reserva (livro_id, leitor_id, data_reserva, estado) VALUES (?, ?, ?, 'ACTIVA')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, livroId);
                    ps.setInt(2, leitorId);
                    ps.setDate(3, Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Não foi possível obter o ID da reserva.");
                        }
                        id = rs.getInt(1);
                    }
                }

                con.commit();
                return id;
            } catch (SQLException | RegraNegocioException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public void cancelar(int reservaId) throws SQLException, RegraNegocioException {
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE reserva SET estado = 'CANCELADA' WHERE id = ? AND estado = 'ACTIVA'")) {
            ps.setInt(1, reservaId);
            if (ps.executeUpdate() == 0) {
                throw new RegraNegocioException("Só uma reserva activa pode ser cancelada.");
            }
        }
    }

    @Override
    public Reserva procurarActivaPorLivro(int livroId) throws SQLException {
        List<Reserva> reservas = listar(SELECT_BASE + "WHERE s.livro_id = ? AND s.estado = 'ACTIVA'", livroId);
        return reservas.isEmpty() ? null : reservas.get(0);
    }

    @Override
    public List<Reserva> listarActivas() throws SQLException {
        return listar(SELECT_BASE + "WHERE s.estado = 'ACTIVA' ORDER BY s.data_reserva, s.id");
    }

    @Override
    public List<Reserva> listarTodas() throws SQLException {
        return listar(SELECT_BASE + "ORDER BY s.data_reserva DESC, s.id DESC");
    }

    private List<Reserva> listar(String sql, Object... parametros) throws SQLException {
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Reserva> reservas = new ArrayList<>();
                while (rs.next()) {
                    reservas.add(new Reserva(
                            rs.getInt("id"),
                            EmprestimoDAO.livroDoJoin(rs),
                            EmprestimoDAO.leitorDoJoin(rs),
                            rs.getDate("data_reserva").toLocalDate(),
                            EstadoReserva.valueOf(rs.getString("estado"))));
                }
                return reservas;
            }
        }
    }
}
