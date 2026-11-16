package biblioteca.view;

import biblioteca.model.Emprestimo;
import biblioteca.model.Leitor;
import biblioteca.model.Livro;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Separador Empréstimos: emprestar com prazo, pesquisar, devolver e registar o pagamento de multas.
 * Só desenha componentes e expõe métodos simples; não tem SQL nem regras de negócio.
 */
public class EmprestimoPanel extends PainelBase {

    private final JComboBox<Livro> cmbLivros = new JComboBox<>();
    private final JComboBox<Leitor> cmbLeitores = new JComboBox<>();
    private final JSpinner spnPrazo = new JSpinner(
            new SpinnerNumberModel(Emprestimo.PRAZO_PADRAO_DIAS, 1, Emprestimo.PRAZO_MAXIMO_DIAS, 1));
    private final Botao btnEmprestar = Tema.botaoPrincipal("Emprestar");

    private final JTextField txtPesquisa = Tema.campo(20);
    private final Botao btnPesquisar = Tema.botaoSecundario("Pesquisar");
    private final JCheckBox chkApenasActivos = new JCheckBox("Mostrar só activos", true);
    private final Botao btnDevolver = Tema.botaoSecundario("Devolver seleccionado");
    private final Botao btnPagarMulta = Tema.botaoPrincipal("Registar pagamento de multa");
    private final Botao btnImprimir = Tema.botaoNeutro("Imprimir / PDF");

    private final ModeloTabela modelo = new ModeloTabela(
            new String[]{"ID", "Livro", "Leitor", "Emprestado", "Prazo", "Devolvido", "Dias", "Estado", "Multa"},
            new Class<?>[]{Integer.class, String.class, String.class, LocalDate.class, LocalDate.class,
                    LocalDate.class, Integer.class, String.class, String.class});
    private final JTable tabela = new JTable(modelo);
    private List<Emprestimo> emprestimos = new ArrayList<>();

    public EmprestimoPanel() {
        super("Empréstimos", "Multa de " + Emprestimo.MULTA_POR_DIA + " " + Emprestimo.MOEDA
                + " por dia de atraso. Leitores com atrasos ou multas por pagar não podem levar livros.");

        cmbLivros.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor,
                    int indice, boolean seleccionado, boolean comFoco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, comFoco);
                if (valor instanceof Livro) {
                    Livro livro = (Livro) valor;
                    setText(livro.getTitulo() + " - " + livro.getAutor());
                }
                return this;
            }
        });
        cmbLivros.setPreferredSize(new Dimension(300, 30));
        cmbLeitores.setPreferredSize(new Dimension(200, 30));
        chkApenasActivos.setOpaque(false);
        chkApenasActivos.setFont(Tema.FONTE);

        Cartao cartaoNovo = new Cartao("Novo empréstimo");
        cartaoNovo.add(Tema.linha(
                Tema.rotulo("Livro"), cmbLivros,
                Tema.rotulo("Leitor"), cmbLeitores,
                Tema.rotulo("Prazo (dias)"), spnPrazo,
                btnEmprestar), BorderLayout.CENTER);
        cartaoNovo.add(Tema.nota("A lista mostra só os livros disponíveis. Um livro reservado só pode ser levado por quem o reservou."),
                BorderLayout.SOUTH);

        JScrollPane scroll = Tema.estilizarTabela(tabela, 30);
        int[] larguras = {45, 150, 125, 100, 90, 95, 50, 160, 160};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        Tema.renderizador(tabela).setLinhaEmAlerta(linha ->
                emprestimos.get(linha).isAtrasado(LocalDate.now()) || emprestimos.get(linha).temMultaPorPagar());

        JPanel barra = Tema.transparente(new BorderLayout());
        barra.add(Tema.linha(Tema.rotulo("Livro ou leitor"), txtPesquisa, btnPesquisar, chkApenasActivos), BorderLayout.WEST);
        barra.add(Tema.linha(btnImprimir), BorderLayout.EAST);

        Cartao cartaoLista = new Cartao("Empréstimos");
        JPanel conteudo = Tema.transparente(new BorderLayout(0, 8));
        conteudo.add(barra, BorderLayout.NORTH);
        conteudo.add(scroll, BorderLayout.CENTER);
        conteudo.add(Tema.linha(btnDevolver, btnPagarMulta,
                Tema.nota("  Linhas a vermelho: em atraso ou com multa por pagar.")), BorderLayout.SOUTH);
        cartaoLista.add(conteudo, BorderLayout.CENTER);

        JPanel centro = Tema.transparente(new BorderLayout(0, 14));
        centro.add(cartaoNovo, BorderLayout.NORTH);
        centro.add(cartaoLista, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        btnImprimir.addActionListener(e -> imprimir(tabela,
                isApenasActivos() ? "Empréstimos activos" : "Histórico de empréstimos"));
    }

    // ---------- Leitura do que o utilizador escolheu ----------

    public Livro getLivroSeleccionado() { return (Livro) cmbLivros.getSelectedItem(); } // null se vazio
    public Leitor getLeitorSeleccionado() { return (Leitor) cmbLeitores.getSelectedItem(); }
    public int getPrazo() { return (Integer) spnPrazo.getValue(); }
    public boolean isApenasActivos() { return chkApenasActivos.isSelected(); }
    public String getTextoPesquisa() { return txtPesquisa.getText().trim(); }

    /** O empréstimo da linha seleccionada (mesmo com a tabela ordenada), ou null. */
    public Emprestimo getEmprestimoSeleccionado() {
        int linha = Tema.linhaSeleccionada(tabela);
        return linha < 0 ? null : emprestimos.get(linha);
    }

    // ---------- Actualização do ecrã ----------

    public void mostrarLivros(List<Livro> livros) {
        cmbLivros.removeAllItems();
        for (Livro livro : livros) {
            cmbLivros.addItem(livro);
        }
    }

    public void mostrarLeitores(List<Leitor> leitores) {
        Leitor anterior = getLeitorSeleccionado();
        cmbLeitores.removeAllItems();
        for (Leitor leitor : leitores) {
            cmbLeitores.addItem(leitor);
            if (anterior != null && leitor.getId() == anterior.getId()) {
                cmbLeitores.setSelectedItem(leitor); // manter o leitor escolhido depois de recarregar
            }
        }
    }

    public void mostrarEmprestimos(List<Emprestimo> emprestimos) {
        this.emprestimos = emprestimos;
        LocalDate hoje = LocalDate.now();
        modelo.setRowCount(0);
        for (Emprestimo e : emprestimos) {
            modelo.addRow(new Object[]{
                    e.getId(),
                    e.getLivro().getTitulo(),
                    e.getLeitor().getNome(),
                    e.getDataEmprestimo(),
                    e.getDataPrevista(),
                    e.getDataDevolucao(),
                    e.getDiasEmprestado(hoje),
                    e.getEstado(hoje),
                    e.getDescricaoMulta(hoje)
            });
        }
    }

    // ---------- Ligação dos botões ao controller ----------

    public void aoEmprestar(Runnable acao) { btnEmprestar.addActionListener(e -> acao.run()); }
    public void aoDevolver(Runnable acao) { btnDevolver.addActionListener(e -> acao.run()); }
    public void aoPagarMulta(Runnable acao) { btnPagarMulta.addActionListener(e -> acao.run()); }
    public void aoMudarFiltro(Runnable acao) { chkApenasActivos.addActionListener(e -> acao.run()); }

    public void aoPesquisar(Runnable acao) {
        btnPesquisar.addActionListener(e -> acao.run());
        txtPesquisa.addActionListener(e -> acao.run());
    }
}
