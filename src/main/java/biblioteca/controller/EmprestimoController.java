package biblioteca.controller;

import biblioteca.dao.EmprestimoRepository;
import biblioteca.dao.LeitorRepository;
import biblioteca.dao.LivroRepository;
import biblioteca.model.Emprestimo;
import biblioteca.model.Leitor;
import biblioteca.model.Livro;
import biblioteca.view.EmprestimoPanel;

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

    /** Carrega combos e tabela. Chamar uma vez depois de criar o controller. */
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

    private void actualizarCombos() {
        try {
            // O combo de livros só mostra os disponíveis (RN03)
            List<Livro> disponiveis = new ArrayList<>();
            for (Livro livro : livroDao.listarTodos()) {
                if (livro.isDisponivel()) {
                    disponiveis.add(livro);
                }
            }
            view.mostrarLivros(disponiveis);
            view.mostrarLeitores(leitorDao.listarTodos());
        } catch (SQLException ex) {
            view.mostrarErro("Erro ao carregar livros e leitores: " + ex.getMessage());
        }
    }

    private void actualizarTabela() {
        try {
            if (view.isApenasActivos()) {
                emprestimosNaTabela = dao.listarActivos();
            } else {
                emprestimosNaTabela = dao.listarTodos();
            }
            view.mostrarEmprestimos(emprestimosNaTabela);
        } catch (SQLException ex) {
            view.mostrarErro("Erro ao carregar empréstimos: " + ex.getMessage());
        }
    }
}
