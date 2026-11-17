package biblioteca.dao;

import biblioteca.database.ConnectionFactory;
import biblioteca.model.Emprestimo;
import biblioteca.model.EmprestimoException;
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
            "SELECT e.id, e.data_emprestimo, e.data_prevista, e.data_devolucao, e.multa, e.multa_paga, "
            + "l.id AS livro_id, l.titulo, l.autor, l.disponivel, l.isbn, l.ano, l.capa, "
            + "r.id AS leitor_id, r.nome, r.telefone "
            + "FROM emprestimo e "
            + "JOIN livro l ON l.id = e.livro_id "
            + "JOIN leitor r ON r.id = e.leitor_id ";

    private static final String ORDEM = "ORDER BY e.data_emprestimo DESC, e.id DESC";

    /**
     * Transacção: bloqueia o livro (FOR UPDATE), verifica as regras, insere o empréstimo,
     * marca o livro como indisponível e, se era a reserva deste leitor, dá-a como atendida.
     * RN03, RN04, RN05, RN11, RN14, RN15, RN16.
     */
    @Override
    public int emprestar(int livroId, int leitorId, int prazoDias) throws SQLException, EmprestimoException {
        try {
            Emprestimo.validarPrazo(prazoDias);
        } catch (IllegalArgumentException e) {
            throw new EmprestimoException(e.getMessage());
        }

        try (Connection con = ConnectionFactory.abrir()) {
            con.setAutoCommit(false);
            try {
                // RN03: o FOR UPDATE faz outro posto esperar aqui até ao commit ou rollback
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT disponivel FROM livro WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, livroId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new EmprestimoException("Livro não encontrado (ID " + livroId + ").");
                        }
                        if (!rs.getBoolean("disponivel")) {
                            throw new EmprestimoException("O livro já está emprestado.");
                        }
                    }
                }

                String nomeLeitor;
                try (PreparedStatement ps = con.prepareStatement("SELECT nome FROM leitor WHERE id = ?")) {
                    ps.setInt(1, leitorId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new EmprestimoException("Leitor não encontrado (ID " + leitorId + ").");
                        }
                        nomeLeitor = rs.getString("nome");
                    }
                }

                // RN14: leitor com multas por pagar não leva livros
                int multas = somar(con, "SELECT COALESCE(SUM(multa), 0) FROM emprestimo "
                        + "WHERE leitor_id = ? AND multa > 0 AND multa_paga = FALSE", leitorId);
                if (multas > 0) {
                    throw new EmprestimoException(nomeLeitor + " tem " + multas + " " + Emprestimo.MOEDA
                            + " de multas por pagar. Registe o pagamento antes de emprestar.");
                }

                // RN15: leitor com livros em atraso não leva mais livros
                int atrasados = somar(con, "SELECT COUNT(*) FROM emprestimo WHERE leitor_id = ? "
                        + "AND data_devolucao IS NULL AND data_prevista < CURDATE()", leitorId);
                if (atrasados > 0) {
                    throw new EmprestimoException(nomeLeitor + " tem " + atrasados
                            + " livro(s) em atraso. Tem de os devolver primeiro.");
                }

                // RN16: um livro reservado só pode ser levado por quem o reservou
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT s.id, s.leitor_id, r.nome FROM reserva s JOIN leitor r ON r.id = s.leitor_id "
                        + "WHERE s.livro_id = ? AND s.estado = 'ACTIVA' FOR UPDATE")) {
                    ps.setInt(1, livroId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            if (rs.getInt("leitor_id") != leitorId) {
                                throw new EmprestimoException("Este livro está reservado para "
                                        + rs.getString("nome") + ". Só esse leitor o pode levar.");
                            }
                            actualizarReserva(con, rs.getInt("id"));
                        }
                    }
                }

                LocalDate hoje = LocalDate.now();
                int emprestimoId;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO emprestimo (livro_id, leitor_id, data_emprestimo, data_prevista) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, livroId);
                    ps.setInt(2, leitorId);
                    ps.setDate(3, Date.valueOf(hoje));
                    ps.setDate(4, Date.valueOf(hoje.plusDays(prazoDias)));
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
            } catch (SQLException | EmprestimoException e) {
                con.rollback(); // nada fica a meio
                throw e;
            }
        }
    }

    /**
     * Transacção: bloqueia o empréstimo e o livro, verifica que o empréstimo ainda está activo,
     * regista a devolução com a multa calculada e marca o livro como disponível.
     * RN06, RN07, RN10, RN12.
     */
    @Override
    public int devolver(int emprestimoId) throws SQLException, EmprestimoException {
        try (Connection con = ConnectionFactory.abrir()) {
            con.setAutoCommit(false);
            try {
                int livroId;
                LocalDate dataEmprestimo;
                LocalDate dataPrevista;
                // O FOR UPDATE com JOIN bloqueia a linha do empréstimo e a do livro.
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT e.livro_id, e.data_emprestimo, e.data_prevista, e.data_devolucao "
                        + "FROM emprestimo e JOIN livro l ON l.id = e.livro_id "
                        + "WHERE e.id = ? FOR UPDATE")) {
                    ps.setInt(1, emprestimoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new EmprestimoException("Empréstimo não encontrado (ID " + emprestimoId + ").");
                        }
                        if (rs.getDate("data_devolucao") != null) {
                            throw new EmprestimoException("Este empréstimo já foi devolvido.");
                        }
                        livroId = rs.getInt("livro_id");
                        dataEmprestimo = rs.getDate("data_emprestimo").toLocalDate();
                        dataPrevista = rs.getDate("data_prevista").toLocalDate();
                    }
                }

                LocalDate hoje = LocalDate.now();
                if (hoje.isBefore(dataEmprestimo)) {
                    throw new EmprestimoException("A data de devolução não pode ser anterior à data do empréstimo.");
                }
                int multa = Emprestimo.calcularMulta(dataPrevista, hoje);

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE emprestimo SET data_devolucao = ?, multa = ? WHERE id = ?")) {
                    ps.setDate(1, Date.valueOf(hoje));
                    ps.setInt(2, multa);
                    ps.setInt(3, emprestimoId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE livro SET disponivel = TRUE WHERE id = ?")) {
                    ps.setInt(1, livroId);
                    ps.executeUpdate();
                }

                con.commit();
                return multa;
            } catch (SQLException | EmprestimoException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public void pagarMulta(int emprestimoId) throws SQLException, EmprestimoException {
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE emprestimo SET multa_paga = TRUE WHERE id = ? AND multa > 0 AND multa_paga = FALSE")) {
            ps.setInt(1, emprestimoId);
            if (ps.executeUpdate() == 0) {
                throw new EmprestimoException("Este empréstimo não tem multa por pagar.");
            }
        }
    }

    @Override
    public List<Emprestimo> listarTodos() throws SQLException {
        return listar(SELECT_BASE + ORDEM);
    }

    @Override
    public List<Emprestimo> listarActivos() throws SQLException {
        return listar(SELECT_BASE + "WHERE e.data_devolucao IS NULL " + ORDEM);
    }

    @Override
    public List<Emprestimo> listarAtrasados() throws SQLException {
        return listar(SELECT_BASE + "WHERE e.data_devolucao IS NULL AND e.data_prevista < CURDATE() "
                + "ORDER BY e.data_prevista, e.id");
    }

    @Override
    public List<Emprestimo> listarPorLeitor(int leitorId) throws SQLException {
        return listar(SELECT_BASE + "WHERE e.leitor_id = ? " + ORDEM, leitorId);
    }

    @Override
    public List<Emprestimo> pesquisar(String texto, boolean apenasActivos) throws SQLException {
        String padrao = "%" + (texto == null ? "" : texto.trim()) + "%";
        String sql = SELECT_BASE + "WHERE (l.titulo LIKE ? OR r.nome LIKE ?) "
                + (apenasActivos ? "AND e.data_devolucao IS NULL " : "") + ORDEM;
        return listar(sql, padrao, padrao);
    }

    /** Executa um SELECT_BASE; os parâmetros substituem os "?" pela ordem. */
    private List<Emprestimo> listar(String sql, Object... parametros) throws SQLException {
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Emprestimo> emprestimos = new ArrayList<>();
                while (rs.next()) {
                    emprestimos.add(mapear(rs));
                }
                return emprestimos;
            }
        }
    }

    private static void actualizarReserva(Connection con, int reservaId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE reserva SET estado = 'ATENDIDA' WHERE id = ?")) {
            ps.setInt(1, reservaId);
            ps.executeUpdate();
        }
    }

    private static int somar(Connection con, String sql, int leitorId) throws SQLException {
        return LivroDAO.contar(con, sql, leitorId);
    }

    private Emprestimo mapear(ResultSet rs) throws SQLException {
        Date devolucao = rs.getDate("data_devolucao");
        return new Emprestimo(
                rs.getInt("id"),
                livroDoJoin(rs),
                leitorDoJoin(rs),
                rs.getDate("data_emprestimo").toLocalDate(),
                rs.getDate("data_prevista").toLocalDate(),
                devolucao == null ? null : devolucao.toLocalDate(),
                rs.getInt("multa"),
                rs.getBoolean("multa_paga"));
    }

    // ---- Usados também pelo ReservaDAO, que faz o mesmo JOIN com livro e leitor ----

    static Livro livroDoJoin(ResultSet rs) throws SQLException {
        Livro livro = new Livro(
                rs.getInt("livro_id"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getBoolean("disponivel"));
        livro.setIsbn(rs.getString("isbn"));
        int ano = rs.getInt("ano");
        livro.setAno(rs.wasNull() ? null : ano);
        livro.setCapa(rs.getString("capa"));
        return livro;
    }

    static Leitor leitorDoJoin(ResultSet rs) throws SQLException {
        return new Leitor(
                rs.getInt("leitor_id"),
                rs.getString("nome"),
                rs.getString("telefone"));
    }
}
