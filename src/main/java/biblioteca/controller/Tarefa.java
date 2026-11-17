package biblioteca.controller;

import biblioteca.model.RegraNegocioException;

import javax.swing.SwingWorker;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Corre trabalho da base de dados numa thread de fundo com SwingWorker,
 * para a janela nunca congelar, e volta à EDT para actualizar o ecrã.
 *
 * Uso nos controllers:
 *   Tarefa.emFundo(() -> dao.listarTodos(),       // doInBackground: thread de fundo
 *                  livros -> view.mostrar(livros),  // done: EDT, se correu bem
 *                  erro -> view.mostrarErro(...));  // done: EDT, se falhou
 */
final class Tarefa {

    private Tarefa() { }

    static <T> void emFundo(Callable<T> trabalho, Consumer<T> sucesso, Consumer<Exception> falha) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return trabalho.call(); // NUNCA mexer em componentes Swing aqui
            }

            @Override
            protected void done() {
                T resultado;
                try {
                    resultado = get();
                } catch (ExecutionException ex) {
                    // O SwingWorker embrulha a excepção original; desembrulhamos para a mensagem certa
                    Throwable causa = ex.getCause();
                    falha.accept(causa instanceof Exception ? (Exception) causa : ex);
                    return;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    falha.accept(ex);
                    return;
                }
                sucesso.accept(resultado);
            }
        }.execute();
    }

    /**
     * Texto amigável para mostrar ao funcionário.
     * Regras de negócio e validação já trazem uma mensagem pronta;
     * erros técnicos ficam com um prefixo que explica o contexto.
     */
    static String mensagem(String contexto, Exception erro) {
        if (erro instanceof RegraNegocioException || erro instanceof IllegalArgumentException) {
            return erro.getMessage();
        }
        if (erro instanceof SQLException) {
            return contexto + "\nErro na base de dados: " + erro.getMessage();
        }
        if (erro instanceof IOException) {
            return contexto + "\n" + erro.getMessage();
        }
        return contexto + "\n" + erro;
    }
}
