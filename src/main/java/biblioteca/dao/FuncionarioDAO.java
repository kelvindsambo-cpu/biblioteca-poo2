package biblioteca.dao;

import biblioteca.database.ConnectionFactory;
import biblioteca.model.Funcionario;
import biblioteca.model.RegraNegocioException;
import biblioteca.util.Senha;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FuncionarioDAO implements FuncionarioRepository {

    @Override
    public Funcionario autenticar(String utilizador, String password) throws SQLException {
        String sql = "SELECT id, utilizador, nome, sal, password_hash FROM funcionario WHERE utilizador = ?";
        try (Connection con = ConnectionFactory.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, utilizador);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null; // não dizemos se o erro foi no utilizador ou na password
                }
                if (!Senha.confere(rs.getString("sal"), rs.getString("password_hash"), password)) {
                    return null;
                }
                return new Funcionario(rs.getInt("id"), rs.getString("utilizador"), rs.getString("nome"));
            }
        }
    }

    @Override
    public void alterarPassword(int funcionarioId, String actual, String nova)
            throws SQLException, RegraNegocioException {
        if (nova == null || nova.length() < Senha.TAMANHO_MINIMO) {
            throw new RegraNegocioException("A nova password deve ter pelo menos " + Senha.TAMANHO_MINIMO + " caracteres.");
        }
        try (Connection con = ConnectionFactory.abrir()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT sal, password_hash FROM funcionario WHERE id = ?")) {
                ps.setInt(1, funcionarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !Senha.confere(rs.getString("sal"), rs.getString("password_hash"), actual)) {
                        throw new RegraNegocioException("A password actual está errada.");
                    }
                }
            }
            // Sal novo a cada alteração
            String sal = Senha.gerarSal();
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE funcionario SET sal = ?, password_hash = ? WHERE id = ?")) {
                ps.setString(1, sal);
                ps.setString(2, Senha.calcularHash(sal, nova));
                ps.setInt(3, funcionarioId);
                ps.executeUpdate();
            }
        }
    }
}
