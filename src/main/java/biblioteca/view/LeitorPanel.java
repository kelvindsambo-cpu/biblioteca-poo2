package biblioteca.view;

import biblioteca.model.Emprestimo;
import biblioteca.model.Leitor;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Separador Leitores: formulário, lista de leitores e, por baixo,
 * o histórico de empréstimos do leitor seleccionado.
 */
public class LeitorPanel extends PainelBase {

    private final JTextField txtNome = Tema.campo(24);
    private final JTextField txtTelefone = Tema.campo(14);
    private final Botao btnRegistar = Tema.botaoPrincipal("Registar");
    private final Botao btnEditar = Tema.botaoSecundario("Guardar alterações");
    private final Botao btnRemover = Tema.botaoPerigo("Remover");
    private final Botao btnLimpar = Tema.botaoNeutro("Limpar");
    private final Botao btnImprimir = Tema.botaoNeutro("Imprimir / PDF");
    private final Botao btnImprimirHistorico = Tema.botaoNeutro("Imprimir histórico");

    private final ModeloTabela modelo = new ModeloTabela(
            new String[]{"ID", "Nome", "Telefone"},
            new Class<?>[]{Integer.class, String.class, String.class});
    private final JTable tabela = new JTable(modelo);
    private List<Leitor> leitores = new ArrayList<>();

    private final ModeloTabela modeloHistorico = new ModeloTabela(
            new String[]{"Livro", "Emprestado", "Prazo", "Devolvido", "Estado", "Multa"},
            new Class<?>[]{String.class, LocalDate.class, LocalDate.class, LocalDate.class, String.class, String.class});
    private final JTable tabelaHistorico = new JTable(modeloHistorico);
    private List<Emprestimo> historico = new ArrayList<>();
    private final JLabel lblHistorico = new JLabel();

    public LeitorPanel() {
        super("Leitores", "Registe leitores e consulte o histórico de cada um. Linhas a vermelho: atraso ou multa por pagar.");

        Cartao cartaoForm = new Cartao("Dados do leitor");
        JPanel form = Tema.transparente(new BorderLayout());
        form.add(Tema.linha(Tema.rotulo("Nome *"), txtNome, Tema.rotulo("Telefone"), txtTelefone), BorderLayout.NORTH);
        form.add(Tema.linha(btnRegistar, btnEditar, btnRemover, btnLimpar), BorderLayout.SOUTH);
        cartaoForm.add(form, BorderLayout.CENTER);

        JScrollPane scroll = Tema.estilizarTabela(tabela, 30);
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        Cartao cartaoLista = new Cartao("Leitores registados");
        cartaoLista.add(scroll, BorderLayout.CENTER);
        cartaoLista.add(Tema.linha(btnImprimir), BorderLayout.SOUTH);

        JScrollPane scrollHistorico = Tema.estilizarTabela(tabelaHistorico, 28);
        int[] larguras = {130, 82, 82, 82, 140, 110};
        for (int i = 0; i < larguras.length; i++) {
            tabelaHistorico.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        Tema.renderizador(tabelaHistorico).setLinhaEmAlerta(linha ->
                historico.get(linha).isAtrasado(LocalDate.now()) || historico.get(linha).temMultaPorPagar());
        lblHistorico.setFont(Tema.FONTE_SUBTITULO);
        lblHistorico.setForeground(Tema.AZUL_ESCURO);
        Cartao cartaoHistorico = new Cartao(null);
        cartaoHistorico.add(lblHistorico, BorderLayout.NORTH);
        cartaoHistorico.add(scrollHistorico, BorderLayout.CENTER);
        cartaoHistorico.add(Tema.linha(btnImprimirHistorico), BorderLayout.SOUTH);

        // A lista de leitores é estreita; o histórico tem mais colunas e fica com o resto da largura
        cartaoLista.setPreferredSize(new Dimension(330, 0));
        JPanel listas = Tema.transparente(new BorderLayout(14, 0));
        listas.add(cartaoLista, BorderLayout.WEST);
        listas.add(cartaoHistorico, BorderLayout.CENTER);

        JPanel centro = Tema.transparente(new BorderLayout(0, 14));
        centro.add(cartaoForm, BorderLayout.NORTH);
        centro.add(listas, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormulario();
            }
        });
        btnImprimir.addActionListener(e -> imprimir(tabela, "Relatório de leitores"));
        btnImprimirHistorico.addActionListener(e -> imprimir(tabelaHistorico, lblHistorico.getText()));
        limparHistorico();
    }

    // ---------- Ligação ao controller ----------

    public void aoRegistar(Runnable acao) { btnRegistar.addActionListener(e -> acao.run()); }
    public void aoEditar(Runnable acao) { btnEditar.addActionListener(e -> acao.run()); }
    public void aoRemover(Runnable acao) { btnRemover.addActionListener(e -> acao.run()); }
    public void aoLimpar(Runnable acao) { btnLimpar.addActionListener(e -> acao.run()); }

    /** Chamado quando a selecção da tabela de leitores muda (para carregar o histórico). */
    public void aoSeleccionarLeitor(Runnable acao) {
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                acao.run();
            }
        });
    }

    // ---------- Leitura ----------

    public String getNome() { return txtNome.getText().trim(); }
    public String getTelefone() { return txtTelefone.getText().trim(); }

    /** ID do leitor seleccionado, ou 0 se não houver selecção. */
    public int getIdSeleccionado() {
        Leitor leitor = getLeitorSeleccionado();
        return leitor == null ? 0 : leitor.getId();
    }

    public Leitor getLeitorSeleccionado() {
        int linha = Tema.linhaSeleccionada(tabela);
        return linha < 0 ? null : leitores.get(linha);
    }

    // ---------- Actualização ----------

    public void mostrarLeitores(List<Leitor> leitores) {
        this.leitores = leitores;
        modelo.setRowCount(0);
        for (Leitor l : leitores) {
            modelo.addRow(new Object[]{l.getId(), l.getNome(), l.getTelefone()});
        }
        limparHistorico();
    }

    public void mostrarHistorico(String nome, List<Emprestimo> emprestimos) {
        this.historico = emprestimos;
        LocalDate hoje = LocalDate.now();
        modeloHistorico.setRowCount(0);
        for (Emprestimo e : emprestimos) {
            modeloHistorico.addRow(new Object[]{
                    e.getLivro().getTitulo(),
                    e.getDataEmprestimo(),
                    e.getDataPrevista(),
                    e.getDataDevolucao(),
                    e.getEstado(hoje),
                    e.getDescricaoMulta(hoje)
            });
        }
        lblHistorico.setText("Histórico de " + nome + " (" + emprestimos.size() + ")");
    }

    public void limparHistorico() {
        historico = new ArrayList<>();
        modeloHistorico.setRowCount(0);
        lblHistorico.setText("Histórico: seleccione um leitor");
    }

    public void limparCampos() {
        txtNome.setText("");
        txtTelefone.setText("");
        tabela.clearSelection();
        txtNome.requestFocusInWindow();
    }

    private void preencherFormulario() {
        Leitor leitor = getLeitorSeleccionado();
        if (leitor != null) {
            txtNome.setText(leitor.getNome());
            txtTelefone.setText(leitor.getTelefone() == null ? "" : leitor.getTelefone());
        }
    }
}
