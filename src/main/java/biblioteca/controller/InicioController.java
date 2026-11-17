package biblioteca.controller;

import biblioteca.dao.EmprestimoRepository;
import biblioteca.dao.EstatisticaRepository;
import biblioteca.view.InicioPanel;

/** Separador Início: carrega os números e a lista de atrasos. */
public class InicioController {

    private static final int TOP_LIVROS = 5;

    private final EstatisticaRepository estatisticas;
    private final EmprestimoRepository emprestimos;
    private final InicioPanel view;

    public InicioController(EstatisticaRepository estatisticas, EmprestimoRepository emprestimos, InicioPanel view) {
        this.estatisticas = estatisticas;
        this.emprestimos = emprestimos;
        this.view = view;
        view.aoActualizar(this::carregarDados);
    }

    public void carregarDados() {
        Tarefa.emFundo(() -> estatisticas.obter(TOP_LIVROS), view::mostrarEstatisticas,
                erro -> view.mostrarErro(Tarefa.mensagem("Não foi possível carregar o resumo.", erro)));
        Tarefa.emFundo(emprestimos::listarAtrasados, view::mostrarAtrasados,
                erro -> view.limpar()); // o erro já foi mostrado pela tarefa anterior
    }
}
