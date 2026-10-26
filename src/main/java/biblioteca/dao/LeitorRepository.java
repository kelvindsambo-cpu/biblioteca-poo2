package biblioteca.dao;

import biblioteca.model.Leitor;
import java.sql.SQLException;
import java.util.List;

public interface LeitorRepository {
    int inserir(Leitor leitor) throws SQLException;
    void actualizar(Leitor leitor) throws SQLException;
    void remover(int id) throws SQLException;
    Leitor procurarPorId(int id) throws SQLException; // null se não existir
    List<Leitor> listarTodos() throws SQLException;
}
