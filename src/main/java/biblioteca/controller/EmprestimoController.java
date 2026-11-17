package biblioteca.controller;

import biblioteca.dao.EmprestimoRepository;
import biblioteca.dao.LeitorRepository;
import biblioteca.dao.LivroRepository;
import biblioteca.dao.ReservaRepository;
import biblioteca.model.Emprestimo;
import biblioteca.model.Leitor;
import biblioteca.model.Livro;
import biblioteca.model.Reserva;
import biblioteca.view.EmprestimoPanel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Liga o EmprestimoPanel aos repositórios. Não escreve SQL: tudo passa pelas interfaces Repository.
 * As regras (RN03 a RN16) são garantidas pelo DAO dentro de transacções; aqui só se mostram os resultados.
 */
public class EmprestimoController {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EmprestimoRepository dao;
    private final LivroRepository livroDao;
    private final LeitorRepository leitorDao;
    private final ReservaRepository reservaDao;
    private final EmprestimoPanel view;

    public EmprestimoController(EmprestimoRepository dao, LivroRepository livroDao, LeitorRepository leitorDao,
                                ReservaRepository reservaDao, EmprestimoPanel view) {
        this.dao = dao;
        this.livroDao = livroDao;
        this.leitorDao = leitorDao;
        this.reservaDao = reservaDao;
        this.view = view;

        view.aoEmprestar(this::emprestar);
        view.aoDevolver(this::devolver);
        view.aoPagarMulta(this::pagarMulta);
        view.aoPesquisar(this::actualizarTabela);
        view.aoMudarFiltro(this::actualizarTabela);
    }

    /** Carrega listas e tabela sem bloquear a janela. */
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
        int prazo = view.getPrazo();
        Tarefa.emFundo(() -> dao.emprestar(livro.getId(), leitor.getId(), prazo),
                id -> {
                    view.mostrarMensagem("Empréstimo registado: \"" + livro.getTitulo() + "\" para "
                            + leitor.getNome() + ".\nDevolver até " + LocalDate.now().plusDays(prazo).format(DATA) + ".");
                    carregarDados(); // o livro deixa de estar disponível
                },
                erro -> {
                    view.mostrarErro(Tarefa.mensagem("Não foi possível emprestar.", erro));
                    carregarDados(); // o estado pode ter mudado (ex.: outro posto emprestou o livro)
                });
    }

    private void devolver() {
        Emprestimo emprestimo = view.getEmprestimoSeleccionado();
        if (emprestimo == null) {
            view.mostrarErro("Seleccione um empréstimo na tabela.");
            return;
        }
        if (!emprestimo.isActivo()) { // RN07
            view.mostrarErro("Este empréstimo já foi devolvido.");
            return;
        }
        int livroId = emprestimo.getLivro().getId();
        // Em fundo: devolve, e vê se alguém está à espera deste livro. Devolve o texto a mostrar.
        Tarefa.emFundo(() -> {
                    int multa = dao.devolver(emprestimo.getId());
                    Reserva reserva = reservaDao.procurarActivaPorLivro(livroId);
                    String texto = "Devolução registada.";
                    if (multa > 0) {
                        texto += "\nMulta por atraso: " + multa + " " + Emprestimo.MOEDA
                                + ". Use \"Registar pagamento de multa\" quando o leitor pagar.";
                    }
                    if (reserva != null) {
                        texto += "\nAtenção: este livro está reservado para " + reserva.getLeitor().getNome()
                                + ". Guarde-o para esse leitor.";
                    }
                    return texto;
                },
                texto -> {
                    view.mostrarMensagem(texto);
                    carregarDados(); // o livro volta a estar disponível
                },
                erro -> {
                    view.mostrarErro(Tarefa.mensagem("Não foi possível devolver.", erro));
                    carregarDados();
                });
    }

    private void pagarMulta() {
        Emprestimo emprestimo = view.getEmprestimoSeleccionado();
        if (emprestimo == null) {
            view.mostrarErro("Seleccione um empréstimo na tabela.");
            return;
        }
        if (emprestimo.isActivo()) {
            view.mostrarErro("O livro ainda não foi devolvido: a multa só fica definida na devolução.");
            return;
        }
        if (!emprestimo.temMultaPorPagar()) {
            view.mostrarErro("Este empréstimo não tem multa por pagar.");
            return;
        }
        if (!view.confirmar("Confirmar o pagamento de " + emprestimo.getMulta() + " " + Emprestimo.MOEDA
                + " por " + emprestimo.getLeitor().getNome() + "?")) {
            return;
        }
        Tarefa.emFundo(() -> {
                    dao.pagarMulta(emprestimo.getId());
                    return null;
                },
                nada -> {
                    view.mostrarMensagem("Pagamento registado.");
                    actualizarTabela();
                },
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível registar o pagamento.", erro)));
    }

    private void actualizarCombos() {
        Tarefa.emFundo(() -> {
                    // A lista de livros só mostra os disponíveis (RN03)
                    List<Livro> disponiveis = new ArrayList<>();
                    for (Livro livro : livroDao.listarTodos()) {
                        if (livro.isDisponivel()) {
                            disponiveis.add(livro);
                        }
                    }
                    return disponiveis;
                },
                view::mostrarLivros,
                erro -> view.mostrarErro(Tarefa.mensagem("Erro ao carregar os livros.", erro)));

        Tarefa.emFundo(leitorDao::listarTodos, view::mostrarLeitores,
                erro -> view.mostrarErro(Tarefa.mensagem("Erro ao carregar os leitores.", erro)));
    }

    private void actualizarTabela() {
        // Ler a View ainda na EDT, antes de passar para a thread de fundo
        boolean apenasActivos = view.isApenasActivos();
        String texto = view.getTextoPesquisa();
        Tarefa.emFundo(() -> {
                    if (!texto.isEmpty()) {
                        return dao.pesquisar(texto, apenasActivos);
                    }
                    return apenasActivos ? dao.listarActivos() : dao.listarTodos();
                },
                view::mostrarEmprestimos,
                erro -> view.mostrarErro(Tarefa.mensagem("Erro ao carregar empréstimos.", erro)));
    }
}
