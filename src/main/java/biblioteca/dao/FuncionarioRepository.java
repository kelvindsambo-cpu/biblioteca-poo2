package biblioteca.dao;

import biblioteca.model.Funcionario;
import biblioteca.model.RegraNegocioException;
import java.sql.SQLException;

public interface FuncionarioRepository {
    /** Devolve o funcionário se o utilizador e a password estiverem certos; caso contrário null. */
    Funcionario autenticar(String utilizador, String password) throws SQLException;

    void alterarPassword(int funcionarioId, String actual, String nova) throws SQLException, RegraNegocioException;
}
