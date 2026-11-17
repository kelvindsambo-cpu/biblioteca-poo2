package biblioteca.dao;

import biblioteca.model.Estatisticas;
import java.sql.SQLException;

public interface EstatisticaRepository {
    /** Contagens para o separador Início e os livros mais emprestados (no máximo "limite"). */
    Estatisticas obter(int limite) throws SQLException;
}
