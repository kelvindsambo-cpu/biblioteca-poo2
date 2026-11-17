package biblioteca.controller;

import biblioteca.dao.LivroRepository;
import biblioteca.model.Livro;
import biblioteca.util.ArquivoCapas;
import biblioteca.view.LivroPanel;

import java.io.File;

/**
 * Liga o LivroPanel ao LivroRepository.
 * Todas as operações na base correm em fundo (Tarefa/SwingWorker): a janela nunca congela.
 */
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
        view.aoPesquisar(this::pesquisar);
    }

    public void listar() {
        Tarefa.emFundo(dao::listarTodos, view::mostrarLivros,
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível carregar os livros.", erro)));
    }

    private void pesquisar() {
        String texto = view.getTextoPesquisa();
        if (texto.isEmpty()) {
            listar();
            return;
        }
        Tarefa.emFundo(() -> dao.pesquisar(texto), view::mostrarLivros,
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível pesquisar os livros.", erro)));
    }

    private void registar() {
        Livro livro = lerFormulario(0);
        if (livro == null) {
            return;
        }
        File capaNova = view.getFicheiroCapaNovo();
        Tarefa.emFundo(() -> {
                    guardarCapa(livro, capaNova);
                    return dao.inserir(livro);
                },
                id -> {
                    view.mostrarMensagem("Livro registado com sucesso (ID " + id + ").");
                    view.limparCampos();
                    listar();
                },
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível registar o livro.", erro)));
    }

    private void editar() {
        int id = view.getIdSeleccionado();
        if (id == 0) {
            view.mostrarErro("Seleccione um livro na tabela para editar.");
            return;
        }
        Livro livro = lerFormulario(id);
        if (livro == null) {
            return;
        }
        File capaNova = view.getFicheiroCapaNovo();
        Tarefa.emFundo(() -> {
                    guardarCapa(livro, capaNova);
                    dao.actualizar(livro);
                    return null;
                },
                nada -> {
                    view.mostrarMensagem("Dados do livro actualizados com sucesso.");
                    view.limparCampos();
                    listar();
                },
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível actualizar o livro.", erro)));
    }

    private void remover() {
        int id = view.getIdSeleccionado();
        if (id == 0) {
            view.mostrarErro("Seleccione um livro na tabela para remover.");
            return;
        }
        if (!view.confirmar("Deseja realmente remover este livro?")) {
            return;
        }
        Tarefa.emFundo(() -> {
                    dao.remover(id); // RN08 é verificada no DAO
                    return null;
                },
                nada -> {
                    view.mostrarMensagem("Livro removido com sucesso.");
                    view.limparCampos();
                    listar();
                },
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível remover o livro.", erro)));
    }

    /**
     * Cria o Livro a partir do formulário. As regras (RN01, ISBN, ano) estão nos setters do modelo;
     * se algum valor for inválido mostra a mensagem e devolve null.
     */
    private Livro lerFormulario(int id) {
        try {
            Livro livro = new Livro(id, view.getTitulo(), view.getAutor(), true);
            livro.setIsbn(view.getIsbn());
            livro.setAno(converterAno(view.getAno()));
            livro.setCapa(view.getCapaActual());
            return livro;
        } catch (IllegalArgumentException ex) {
            view.mostrarErro(ex.getMessage());
            return null;
        }
    }

    private static Integer converterAno(String texto) {
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("O ano deve ser um número, por exemplo 1992.");
        }
    }

    /** Corre em fundo: copiar o ficheiro é uma operação de disco. */
    private static void guardarCapa(Livro livro, File capaNova) throws java.io.IOException {
        if (capaNova != null) {
            livro.setCapa(ArquivoCapas.guardar(capaNova));
        }
    }
}
