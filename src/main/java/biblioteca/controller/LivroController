package biblioteca.controller;
import biblioteca.dao.LivroRepository;
import biblioteca.model.Livro;
import biblioteca.view.LivroPanel;
import javax.swing.SwingWorker;
import java.sql.SQLException;
import java.util.List;

public class LivroController {

    private final LivroRepository dao;
    private final LivroPanel view;

    public LivroController(LivroRepository dao, LivroPanel view) {
        this.dao = dao;
        this.view = view;

        view.aoRegistar(this::registar);
        view.aoEditar(this::editar);
        view.aoRemover(this::remover);
        view.aoLimpar(view::limparCampos);
        view.aoPesquisar(() -> pesquisar(view.getTextoPesquisa()));

        listar();
    }

    // LISTAR TODOS OS LIVROS

    public void listar() {
        new SwingWorker<List<Livro>, Void>() {

            @Override
            protected List<Livro> doInBackground() throws Exception {
                return dao.listarTodos();
            }

            @Override
            protected void done() {
                try {
                    view.mostrarLivros(get());
                } catch (Exception ex) {
                    view.mostrarErro("Não foi possível carregar os livros.");
                }
            }

        }.execute();
    }

    // REGISTAR LIVRO

    public void registar() {
        if (!dadosValidos()) {
            return;
        }

        Livro livro = new Livro(
                0,
                view.getTitulo(),
                view.getAutor(),
                true
        );

        try {
            dao.inserir(livro);

            view.mostrarMensagem("Livro registado com sucesso.");
            view.limparCampos();

            listar();

        } catch (SQLException ex) { view.mostrarErro("Não foi possível registar o livro." );
        }
    }

    // EDITAR LIVRO

    public void editar() {
        int id = view.getIdSeleccionado();

        if (id == 0) {
            view.mostrarErro(
                    "Seleccione um livro na tabela para editar.");
            return;
        }

        if (!dadosValidos()) {
            return;
        }

        Livro livro = new Livro(
                id,
                view.getTitulo(),
                view.getAutor(),
                true
        );

        try {
            dao.actualizar(livro);

            view.mostrarMensagem(
                    "Dados do livro actualizados com sucesso.");

            view.limparCampos();
            listar();

        } catch (SQLException ex) {
            view.mostrarErro(
                    "Não foi possível actualizar o livro." );
        }
    }

    // REMOVER LIVRO

    public void remover() {
        int id = view.getIdSeleccionado();

        if (id == 0) {
            view.mostrarErro(
                    "Seleccione um livro na tabela para remover.");
            return;
        }

        if (!view.confirmar(
                "Deseja realmente remover este livro?")) {
            return;
        }

        try {
            dao.remover(id);

            view.mostrarMensagem(
                    "Livro removido com sucesso.");

            view.limparCampos();
            listar();

        } catch (SQLException ex) {
            String mensagem = ex.getMessage();

            boolean temHistorico =
                    ex.getErrorCode() == 1451
                    || (mensagem != null
                    && mensagem.startsWith("Não é possível remover"));

            if (temHistorico) {
                view.mostrarErro(
                        "Este livro possui histórico de empréstimos " + "e não pode ser removido." );
            } else {
        view.mostrarErro( "Não foi possível remover o livro." );
            }
        }
    }

    // PESQUISAR LIVROS

    public void pesquisar(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            listar();
            return;
        }

        new SwingWorker<List<Livro>, Void>() {

            @Override
            protected List<Livro> doInBackground() throws Exception {
                return dao.pesquisar(texto.trim());
            }

            @Override
            protected void done() {
                try {
                    view.mostrarLivros(get());
                } catch (Exception ex) {
                    view.mostrarErro( "Não foi possível pesquisar os livros."  );
                }
            }

        }.execute();
    }

    // VALIDAR OS DADOS DO FORMULÁRIO

    private boolean dadosValidos() {
        if (view.getTitulo().isEmpty()) {
            view.mostrarErro("O título do livro é obrigatório.");
            return false;
        }

        if (view.getAutor().isEmpty()) {
            view.mostrarErro( "O autor do livro é obrigatório.");
            return false;
        }

        return true;
    }
}



