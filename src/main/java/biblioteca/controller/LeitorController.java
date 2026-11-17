package biblioteca.controller;

import biblioteca.dao.EmprestimoRepository;
import biblioteca.dao.LeitorRepository;
import biblioteca.model.Leitor;
import biblioteca.view.LeitorPanel;

/** Liga o LeitorPanel aos repositórios de leitores e de empréstimos (para o histórico). */
public class LeitorController {

    private final LeitorRepository dao;
    private final EmprestimoRepository emprestimos;
    private final LeitorPanel view;

    public LeitorController(LeitorRepository dao, EmprestimoRepository emprestimos, LeitorPanel view) {
        this.dao = dao;
        this.emprestimos = emprestimos;
        this.view = view;

        view.aoRegistar(this::registar);
        view.aoEditar(this::editar);
        view.aoRemover(this::remover);
        view.aoLimpar(view::limparCampos);
        view.aoSeleccionarLeitor(this::carregarHistorico);
    }

    public void listar() {
        Tarefa.emFundo(dao::listarTodos, view::mostrarLeitores,
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível carregar os leitores.", erro)));
    }

    private void carregarHistorico() {
        Leitor leitor = view.getLeitorSeleccionado();
        if (leitor == null) {
            view.limparHistorico();
            return;
        }
        int id = leitor.getId();
        Tarefa.emFundo(() -> emprestimos.listarPorLeitor(id),
                lista -> {
                    // Se entretanto o funcionário escolheu outro leitor, esta resposta já não interessa
                    if (view.getIdSeleccionado() == id) {
                        view.mostrarHistorico(leitor.getNome(), lista);
                    }
                },
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível carregar o histórico.", erro)));
    }

    private void registar() {
        Leitor leitor = lerFormulario(0);
        if (leitor == null) {
            return;
        }
        Tarefa.emFundo(() -> dao.inserir(leitor),
                id -> {
                    view.mostrarMensagem("Leitor registado com sucesso.");
                    view.limparCampos();
                    listar();
                },
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível registar o leitor.", erro)));
    }

    private void editar() {
        int id = view.getIdSeleccionado();
        if (id == 0) {
            view.mostrarErro("Seleccione um leitor na tabela para editar.");
            return;
        }
        Leitor leitor = lerFormulario(id);
        if (leitor == null) {
            return;
        }
        Tarefa.emFundo(() -> {
                    dao.actualizar(leitor);
                    return null;
                },
                nada -> {
                    view.mostrarMensagem("Dados actualizados.");
                    view.limparCampos();
                    listar();
                },
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível actualizar o leitor.", erro)));
    }

    private void remover() {
        int id = view.getIdSeleccionado();
        if (id == 0) {
            view.mostrarErro("Seleccione um leitor na tabela para remover.");
            return;
        }
        if (!view.confirmar("Deseja mesmo remover este leitor?")) {
            return;
        }
        Tarefa.emFundo(() -> {
                    dao.remover(id); // RN09 é verificada no DAO
                    return null;
                },
                nada -> {
                    view.mostrarMensagem("Leitor removido.");
                    view.limparCampos();
                    listar();
                },
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível remover o leitor.", erro)));
    }

    /** Validação no modelo (RN02); devolve null e mostra a mensagem se algo estiver errado. */
    private Leitor lerFormulario(int id) {
        try {
            Leitor.validarFormatoTelefone(view.getTelefone());
            return new Leitor(id, view.getNome(), view.getTelefone());
        } catch (IllegalArgumentException ex) {
            view.mostrarErro(ex.getMessage());
            return null;
        }
    }
}
