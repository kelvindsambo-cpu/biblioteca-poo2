package biblioteca.dao;

import biblioteca.model.Emprestimo;
import java.sql.SQLException;
import java.util.List;

public interface EmprestimoRepository {
    int emprestar(int livroId, int leitorId) throws SQLException;
    void devolver(int emprestimoId) throws SQLException;
    List<Emprestimo> listarTodos() throws SQLException;
    List<Emprestimo> listarActivos() throws SQLException;
}
