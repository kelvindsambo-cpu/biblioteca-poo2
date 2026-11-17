package biblioteca.controller;

import biblioteca.dao.LeitorRepository;
import biblioteca.dao.LivroRepository;
import biblioteca.dao.ReservaRepository;
import biblioteca.model.Leitor;
import biblioteca.model.Livro;
import biblioteca.model.Reserva;
import biblioteca.view.ReservaPanel;

import java.util.ArrayList;
import java.util.List;

/** Liga o ReservaPanel aos repositórios. As regras de reserva (RN16, RN17) estão no ReservaDAO. */
public class ReservaController {

    private final ReservaRepository dao;
    private final LivroRepository livroDao;
    private final LeitorRepository leitorDao;
    private final ReservaPanel view;

    public ReservaController(ReservaRepository dao, LivroRepository livroDao,
                             LeitorRepository leitorDao, ReservaPanel view) {
        this.dao = dao;
        this.livroDao = livroDao;
        this.leitorDao = leitorDao;
        this.view = view;

        view.aoReservar(this::reservar);
        view.aoCancelar(this::cancelar);
        view.aoMudarFiltro(this::actualizarTabela);
    }

    public void carregarDados() {
        Tarefa.emFundo(() -> {
                    // Só faz sentido reservar livros que estão emprestados
                    List<Livro> emprestados = new ArrayList<>();
                    for (Livro livro : livroDao.listarTodos()) {
                        if (!livro.isDisponivel()) {
                            emprestados.add(livro);
                        }
                    }
                    return emprestados;
                },
                view::mostrarLivros,
                erro -> view.mostrarErro(Tarefa.mensagem("Erro ao carregar os livros.", erro)));

        Tarefa.emFundo(leitorDao::listarTodos, view::mostrarLeitores,
                erro -> view.mostrarErro(Tarefa.mensagem("Erro ao carregar os leitores.", erro)));

        actualizarTabela();
    }

    private void reservar() {
        Livro livro = view.getLivroSeleccionado();
        Leitor leitor = view.getLeitorSeleccionado();
        if (livro == null) {
            view.mostrarErro("Não há livros emprestados para reservar.");
            return;
        }
        if (leitor == null) {
            view.mostrarErro("Seleccione um leitor.");
            return;
        }
        Tarefa.emFundo(() -> dao.reservar(livro.getId(), leitor.getId()),
                id -> {
                    view.mostrarMensagem("Reserva registada: \"" + livro.getTitulo() + "\" para " + leitor.getNome() + ".");
                    carregarDados();
                },
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível reservar.", erro)));
    }

    private void cancelar() {
        Reserva reserva = view.getReservaSeleccionada();
        if (reserva == null) {
            view.mostrarErro("Seleccione uma reserva na tabela.");
            return;
        }
        if (!reserva.isActiva()) {
            view.mostrarErro("Esta reserva já não está activa.");
            return;
        }
        if (!view.confirmar("Cancelar a reserva de \"" + reserva.getLivro().getTitulo() + "\" para "
                + reserva.getLeitor().getNome() + "?")) {
            return;
        }
        Tarefa.emFundo(() -> {
                    dao.cancelar(reserva.getId());
                    return null;
                },
                nada -> {
                    view.mostrarMensagem("Reserva cancelada.");
                    actualizarTabela();
                },
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível cancelar a reserva.", erro)));
    }

    private void actualizarTabela() {
        boolean apenasActivas = view.isApenasActivas();
        Tarefa.emFundo(() -> apenasActivas ? dao.listarActivas() : dao.listarTodas(),
                view::mostrarReservas,
                erro -> view.mostrarErro(Tarefa.mensagem("Erro ao carregar as reservas.", erro)));
    }
}
