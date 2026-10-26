package biblioteca.dao;

import biblioteca.model.Livro;
import java.sql.SQLException;
import java.util.List;

public interface LivroRepository {
    int inserir(Livro livro) throws SQLException;
    void actualizar(Livro livro) throws SQLException;
    void remover(int id) throws SQLException;
    Livro procurarPorId(int id) throws SQLException; // null se não existir
    List<Livro> listarTodos() throws SQLException;
    List<Livro> pesquisar(String texto) throws SQLException;
}
