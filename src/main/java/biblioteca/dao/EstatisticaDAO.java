package biblioteca.dao;

import biblioteca.database.ConnectionFactory;
import biblioteca.model.Estatisticas;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EstatisticaDAO implements EstatisticaRepository {

    @Override
    public Estatisticas obter(int limite) throws SQLException {
        try (Connection con = ConnectionFactory.abrir()) {
            int totalLivros = numero(con, "SELECT COUNT(*) FROM livro");
            int disponiveis = numero(con, "SELECT COUNT(*) FROM livro WHERE disponivel = TRUE");
            int leitores = numero(con, "SELECT COUNT(*) FROM leitor");
            int activos = numero(con, "SELECT COUNT(*) FROM emprestimo WHERE data_devolucao IS NULL");
            int atrasados = numero(con, "SELECT COUNT(*) FROM emprestimo "
                    + "WHERE data_devolucao IS NULL AND data_prevista < CURDATE()");
            int reservas = numero(con, "SELECT COUNT(*) FROM reserva WHERE estado = 'ACTIVA'");
            int multas = numero(con, "SELECT COALESCE(SUM(multa), 0) FROM emprestimo WHERE multa_paga = FALSE");

            List<Estatisticas.LivroPopular> populares = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT l.titulo, COUNT(*) AS total FROM emprestimo e JOIN livro l ON l.id = e.livro_id "
                    + "GROUP BY l.id, l.titulo ORDER BY total DESC, l.titulo LIMIT ?")) {
                ps.setInt(1, limite);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        populares.add(new Estatisticas.LivroPopular(rs.getString("titulo"), rs.getInt("total")));
                    }
                }
            }

            return new Estatisticas(totalLivros, disponiveis, leitores, activos, atrasados,
                    reservas, multas, populares);
        }
    }

    private int numero(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
