package biblioteca.controller;

import biblioteca.dao.LeitorRepository;
import biblioteca.model.Leitor;
import biblioteca.view.LeitorPanel;
 
import javax.swing.SwingWorker;
import java.sql.SQLException;
import java.util.List;
 
public class LeitorController {
 
    private final LeitorRepository dao;
    private final LeitorPanel view;
 
    public LeitorController(LeitorRepository dao, LeitorPanel view) {
        this.dao = dao;
        this.view = view;
 
        view.aoRegistar(this::registar);
        view.aoEditar(this::editar);
        view.aoRemover(this::remover);
        view.aoLimpar(view::limparCampos);
 
        listar();
    }
 
    /** A consulta corre em background e a View é actualizada na EDT. */
    public void listar() {
        new SwingWorker<List<Leitor>, Void>() {
            @Override
            protected List<Leitor> doInBackground() throws SQLException {
                return dao.listarTodos();
            }
 
            @Override
            protected void done() {
                try {
                    view.mostrarLeitores(get());
                } catch (Exception ex) {
                    view.mostrarErro("Não foi possível carregar os leitores.");
                }
            }
        }.execute();
    }
 
    public void registar() {
        if (!nomeValido()) {
            return;
        }
        try {
            dao.inserir(new Leitor(0, view.getNome(), telefoneOuNull()));
            view.mostrarMensagem("Leitor registado com sucesso.");
            view.limparCampos();
            listar();
        } catch (SQLException ex) {
            view.mostrarErro("Não foi possível registar o leitor.");
        }
    }
 
    public void editar() {
        int id = view.getIdSeleccionado();
        if (id == 0) {
            view.mostrarErro("Seleccione um leitor na tabela para editar.");
            return;
        }
        if (!nomeValido()) {
            return;
        }
        try {
            dao.actualizar(new Leitor(id, view.getNome(), telefoneOuNull()));
            view.mostrarMensagem("Dados actualizados.");
            view.limparCampos();
            listar();
        } catch (SQLException ex) {
            view.mostrarErro("Não foi possível actualizar o leitor.");
        }
    }
 
    public void remover() {
        int id = view.getIdSeleccionado();
        if (id == 0) {
            view.mostrarErro("Seleccione um leitor na tabela para remover.");
            return;
        }
        if (!view.confirmar("Deseja mesmo remover este leitor?")) {
            return;
        }
        try {
            dao.remover(id);
            view.mostrarMensagem("Leitor removido.");
            view.limparCampos();
            listar();
        } catch (SQLException ex) {
            String msg = ex.getMessage();
            boolean temHistorico = ex.getErrorCode() == 1451
                    || (msg != null && msg.startsWith("Não é possível remover"));
            if (temHistorico) {
                // Leitor com histórico não pode ser eliminado
                view.mostrarErro("Este leitor tem histórico de empréstimos e não pode ser removido.");
            } else {
                view.mostrarErro("Não foi possível remover o leitor.");
            }
        }
    }
 
    private boolean nomeValido() {
        if (view.getNome().isEmpty()) {
            view.mostrarErro("O nome do leitor é obrigatório.");
            return false;
        }
        return true;
    }
 
    private String telefoneOuNull() {
        String telefone = view.getTelefone();
        return telefone.isEmpty() ? null : telefone;
    }
}