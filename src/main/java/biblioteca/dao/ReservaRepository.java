package biblioteca.dao;

import biblioteca.model.RegraNegocioException;
import biblioteca.model.Reserva;
import java.sql.SQLException;
import java.util.List;

public interface ReservaRepository {
    int reservar(int livroId, int leitorId) throws SQLException, RegraNegocioException;
    void cancelar(int reservaId) throws SQLException, RegraNegocioException;
    Reserva procurarActivaPorLivro(int livroId) throws SQLException; // null se não houver
    List<Reserva> listarActivas() throws SQLException;
    List<Reserva> listarTodas() throws SQLException;
}
