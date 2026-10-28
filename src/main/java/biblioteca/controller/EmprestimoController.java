package biblioteca.controller;

import biblioteca.dao.EmprestimoRepository;
import biblioteca.dao.LeitorRepository;
import biblioteca.dao.LivroRepository;
import biblioteca.model.Emprestimo;
import biblioteca.model.Leitor;
import biblioteca.model.Livro;
import biblioteca.view.EmprestimoPanel;

import javax.swing.SwingWorker;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Liga o EmprestimoPanel aos repositórios.
 * Não escreve SQL: tudo passa pelas interfaces Repository.
 */
public class EmprestimoController {

    private final EmprestimoRepository dao;
    private final LivroRepository livroDao;
    private final LeitorRepository leitorDao;
    private final EmprestimoPanel view;

    // Lista que está agora na tabela; o índice da linha seleccionada corresponde ao índice aqui
    private List<Emprestimo> emprestimosNaTabela = new ArrayList<>();

    public EmprestimoController(EmprestimoRepository dao, LivroRepository livroDao,
                                LeitorRepository leitorDao, EmprestimoPanel view) {
        this.dao = dao;
        this.livroDao = livroDao;
        this.leitorDao = leitorDao;
        this.view = view;

        // Liga cada botão ao método correspondente
        view.addEmprestarListener(e -> emprestar());
        view.addDevolverListener(e -> devolver());
        view.addFiltroListener(e -> actualizarTabela());
    }

    /** Carrega combos e tabela sem bloquear a janela. */
    public void carregarDados() {
        actualizarCombos();
        actualizarTabela();
    }

    private void emprestar() {
        Livro livro = view.getLivroSeleccionado();
        Leitor leitor = view.getLeitorSeleccionado();

        if (livro == null) {
            view.mostrarErro("Seleccione um livro disponível.");
            return;
        }
        if (leitor == null) {
            view.mostrarErro("Seleccione um leitor.");
            return;
        }

        try {
            dao.emprestar(livro.getId(), leitor.getId()); // RN03, RN04, RN05 ficam no DAO
            view.mostrarMensagem("Empréstimo registado.");
            carregarDados(); // o livro deixa de estar disponível
        } catch (SQLException ex) {
            view.mostrarErro("Não foi possível emprestar: " + ex.getMessage());
            carregarDados(); // o estado pode ter mudado (ex.: outro posto emprestou o livro)
        }
    }

    private void devolver() {
        int linha = view.getLinhaSeleccionada();
        if (linha < 0) {
            view.mostrarErro("Seleccione um empréstimo na tabela.");
            return;
        }

        Emprestimo emprestimo = emprestimosNaTabela.get(linha);
        if (emprestimo.getDataDevolucao() != null) { // RN07
            view.mostrarErro("Este empréstimo já foi devolvido.");
            return;
        }

        try {
            dao.devolver(emprestimo.getId()); // RN06 e RN10 ficam no DAO
            view.mostrarMensagem("Devolução registada.");
            carregarDados(); // o livro volta a estar disponível
        } catch (SQLException ex) {
            view.mostrarErro("Não foi possível devolver: " + ex.getMessage());
            carregarDados();
        }
    }

    /** As consultas correm em background e a View é actualizada na EDT. */
    private void actualizarCombos() {
        new SwingWorker<Void, Void>() {
            private final List<Livro> disponiveis = new ArrayList<>();
            private List<Leitor> leitores;

            @Override
            protected Void doInBackground() throws SQLException {
                // O combo de livros só mostra os disponíveis (RN03)
                for (Livro livro : livroDao.listarTodos()) {
                    if (livro.isDisponivel()) {
                        disponiveis.add(livro);
                    }
                }
                leitores = leitorDao.listarTodos();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    view.mostrarLivros(disponiveis);
                    view.mostrarLeitores(leitores);
                } catch (Exception ex) {
                    view.mostrarErro("Erro ao carregar livros e leitores: " + causa(ex));
                }
            }
        }.execute();
    }

    private void actualizarTabela() {
        boolean apenasActivos = view.isApenasActivos(); // ler a View ainda na EDT
        new SwingWorker<List<Emprestimo>, Void>() {
            @Override
            protected List<Emprestimo> doInBackground() throws SQLException {
                return apenasActivos ? dao.listarActivos() : dao.listarTodos();
            }

            @Override
            protected void done() {
                try {
                    // Lista e tabela mudam juntas, para os índices continuarem a corresponder
                    emprestimosNaTabela = get();
                    view.mostrarEmprestimos(emprestimosNaTabela);
                } catch (Exception ex) {
                    view.mostrarErro("Erro ao carregar empréstimos: " + causa(ex));
                }
            }
        }.execute();
    }

    // O SwingWorker embrulha a SQLException numa ExecutionException
    private static String causa(Exception ex) {
        Throwable t = ex.getCause() != null ? ex.getCause() : ex;
        return t.getMessage();
    }
}
