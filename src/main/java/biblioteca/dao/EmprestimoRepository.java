package biblioteca.dao;

import biblioteca.model.Emprestimo;
import biblioteca.model.EmprestimoException;
import java.sql.SQLException;
import java.util.List;

public interface EmprestimoRepository {
    /** Cria o empréstimo com prazo de devolução em dias e devolve o ID. */
    int emprestar(int livroId, int leitorId, int prazoDias) throws SQLException, EmprestimoException;

    /** Regista a devolução e devolve a multa aplicada (0 se foi a tempo). */
    int devolver(int emprestimoId) throws SQLException, EmprestimoException;

    void pagarMulta(int emprestimoId) throws SQLException, EmprestimoException;

    List<Emprestimo> listarTodos() throws SQLException;
    List<Emprestimo> listarActivos() throws SQLException;
    List<Emprestimo> listarAtrasados() throws SQLException;
    List<Emprestimo> listarPorLeitor(int leitorId) throws SQLException;

    /** Pesquisa por parte do título do livro ou do nome do leitor. */
    List<Emprestimo> pesquisar(String texto, boolean apenasActivos) throws SQLException;
}
