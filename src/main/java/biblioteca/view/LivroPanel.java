package biblioteca.view;

import biblioteca.model.Livro;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Separador Livros: formulário com capa, pesquisa e tabela com miniaturas.
 * Não tem SQL nem regras de negócio; o LivroController liga os botões através dos métodos aoXxx().
 */
public class LivroPanel extends PainelBase {

    private static final int CAPA_L = 110, CAPA_A = 165;  // pré-visualização
    private static final int MINI_L = 36, MINI_A = 54;    // miniatura na tabela

    private final JTextField txtTitulo = Tema.campo(26);
    private final JTextField txtAutor = Tema.campo(26);
    private final JTextField txtIsbn = Tema.campo(14);
    private final JTextField txtAno = Tema.campo(6);
    private final JTextField txtPesquisa = Tema.campo(24);

    private final Botao btnRegistar = Tema.botaoPrincipal("Registar");
    private final Botao btnEditar = Tema.botaoSecundario("Guardar alterações");
    private final Botao btnRemover = Tema.botaoPerigo("Remover");
    private final Botao btnLimpar = Tema.botaoNeutro("Limpar");
    private final Botao btnPesquisar = Tema.botaoSecundario("Pesquisar");
    private final Botao btnImprimir = Tema.botaoNeutro("Imprimir / PDF");
    private final Botao btnEscolherCapa = Tema.botaoNeutro("Escolher imagem...");
    private final Botao btnSemCapa = Tema.botaoNeutro("Sem capa");

    private final JLabel lblCapa = new JLabel();

    private final ModeloTabela modelo = new ModeloTabela(
            new String[]{"Capa", "ID", "Título", "Autor", "Ano", "ISBN", "Estado"},
            new Class<?>[]{Icon.class, Integer.class, String.class, String.class, Integer.class, String.class, String.class});
    private final JTable tabela = new JTable(modelo);

    // Livros na tabela, pela ordem do modelo: a linha i do modelo é livros.get(i)
    private List<Livro> livros = new ArrayList<>();

    // Capa no formulário: a do livro seleccionado, ou um ficheiro novo ainda por guardar
    private String capaActual;
    private File ficheiroCapaNovo;

    public LivroPanel() {
        super("Livros", "Registe exemplares, escolha uma capa e consulte o acervo. Clique num cabeçalho para ordenar.");

        // ---- Formulário ----
        JPanel campos = Tema.transparente(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        adicionar(campos, c, 0, "Título *", txtTitulo);
        adicionar(campos, c, 1, "Autor *", txtAutor);
        // ISBN e ano na mesma linha; o FlowLayout de Tema.linha teria margens a mais aqui
        JPanel isbnAno = Tema.transparente(new BorderLayout(8, 0));
        isbnAno.add(txtIsbn, BorderLayout.CENTER);
        JPanel ano = Tema.transparente(new BorderLayout(8, 0));
        ano.add(Tema.rotulo("Ano"), BorderLayout.WEST);
        ano.add(txtAno, BorderLayout.CENTER);
        isbnAno.add(ano, BorderLayout.EAST);
        adicionar(campos, c, 2, "ISBN", isbnAno);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        campos.add(Tema.nota("* obrigatório. O ISBN tem 10 ou 13 dígitos; os hífens são opcionais."), c);
        c.gridy = 4;
        campos.add(Tema.linha(btnRegistar, btnEditar, btnRemover, btnLimpar), c);

        // Capa: imagem à esquerda, botões à direita
        lblCapa.setHorizontalAlignment(SwingConstants.CENTER);
        lblCapa.setPreferredSize(new Dimension(CAPA_L, CAPA_A));
        JPanel botoesCapa = Tema.transparente(new GridLayout(2, 1, 0, 6));
        botoesCapa.add(btnEscolherCapa);
        botoesCapa.add(btnSemCapa);
        JPanel colunaBotoes = Tema.transparente(new BorderLayout());
        colunaBotoes.add(Tema.rotulo("Capa"), BorderLayout.NORTH);
        colunaBotoes.add(botoesCapa, BorderLayout.SOUTH);
        JPanel capa = Tema.transparente(new BorderLayout(10, 0));
        capa.add(lblCapa, BorderLayout.WEST);
        capa.add(colunaBotoes, BorderLayout.CENTER);
        capa.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));

        Cartao cartaoForm = new Cartao("Dados do livro");
        cartaoForm.add(campos, BorderLayout.CENTER);
        cartaoForm.add(capa, BorderLayout.EAST);

        // ---- Tabela ----
        JScrollPane scroll = Tema.estilizarTabela(tabela, MINI_A + 8);
        tabela.getColumnModel().getColumn(0).setMaxWidth(MINI_L + 24);
        tabela.getColumnModel().getColumn(1).setMaxWidth(60);
        tabela.getColumnModel().getColumn(4).setMaxWidth(70);
        ((javax.swing.table.TableRowSorter<?>) tabela.getRowSorter()).setSortable(0, false);

        Cartao cartaoTabela = new Cartao("Acervo");
        JPanel barra = Tema.transparente(new BorderLayout());
        barra.add(Tema.linha(Tema.rotulo("Pesquisar (título, autor ou ISBN)"), txtPesquisa, btnPesquisar), BorderLayout.WEST);
        barra.add(Tema.linha(btnImprimir), BorderLayout.EAST);
        JPanel centroTabela = Tema.transparente(new BorderLayout(0, 8));
        centroTabela.add(barra, BorderLayout.NORTH);
        centroTabela.add(scroll, BorderLayout.CENTER);
        cartaoTabela.add(centroTabela, BorderLayout.CENTER);

        JPanel centro = Tema.transparente(new BorderLayout(0, 14));
        centro.add(cartaoForm, BorderLayout.NORTH);
        centro.add(cartaoTabela, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        // ---- Eventos que só dizem respeito ao ecrã ----
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormulario();
            }
        });
        btnEscolherCapa.addActionListener(e -> escolherCapa());
        btnSemCapa.addActionListener(e -> {
            capaActual = null;
            ficheiroCapaNovo = null;
            mostrarCapa();
        });
        btnImprimir.addActionListener(e -> imprimir(tabela, "Relatório de livros"));
        mostrarCapa();
    }

    private void adicionar(JPanel p, GridBagConstraints c, int linha, String rotulo, Component campo) {
        c.gridwidth = 1;
        c.gridy = linha;
        c.gridx = 0;
        c.weightx = 0;
        p.add(Tema.rotulo(rotulo), c);
        c.gridx = 1;
        c.weightx = 1;
        p.add(campo, c);
    }

    // ---------- Ligação dos botões ao controller ----------

    public void aoRegistar(Runnable acao) { btnRegistar.addActionListener(e -> acao.run()); }
    public void aoEditar(Runnable acao) { btnEditar.addActionListener(e -> acao.run()); }
    public void aoRemover(Runnable acao) { btnRemover.addActionListener(e -> acao.run()); }
    public void aoLimpar(Runnable acao) { btnLimpar.addActionListener(e -> acao.run()); }

    public void aoPesquisar(Runnable acao) {
        btnPesquisar.addActionListener(e -> acao.run());
        txtPesquisa.addActionListener(e -> acao.run()); // Enter também pesquisa
    }

    // ---------- Leitura do formulário ----------

    public String getTitulo() { return txtTitulo.getText().trim(); }
    public String getAutor() { return txtAutor.getText().trim(); }
    public String getIsbn() { return txtIsbn.getText().trim(); }
    public String getAno() { return txtAno.getText().trim(); }
    public String getTextoPesquisa() { return txtPesquisa.getText().trim(); }

    /** Nome da capa que o livro deve manter (null = sem capa ou substituída por um ficheiro novo). */
    public String getCapaActual() { return ficheiroCapaNovo == null ? capaActual : null; }

    /** Imagem escolhida agora pelo funcionário, ainda por copiar; null se não escolheu nenhuma. */
    public File getFicheiroCapaNovo() { return ficheiroCapaNovo; }

    /** ID do livro seleccionado, ou 0 se não houver selecção. */
    public int getIdSeleccionado() {
        int linha = Tema.linhaSeleccionada(tabela);
        return linha < 0 ? 0 : livros.get(linha).getId();
    }

    // ---------- Actualização do ecrã ----------

    public void mostrarLivros(List<Livro> livros) {
        this.livros = livros;
        modelo.setRowCount(0);
        for (Livro livro : livros) {
            modelo.addRow(new Object[]{
                    Capas.icone(livro, MINI_L, MINI_A),
                    livro.getId(),
                    livro.getTitulo(),
                    livro.getAutor(),
                    livro.getAno(),
                    livro.getIsbn(),
                    livro.isDisponivel() ? "Disponível" : "Emprestado"
            });
        }
    }

    public void limparCampos() {
        tabela.clearSelection();
        txtTitulo.setText("");
        txtAutor.setText("");
        txtIsbn.setText("");
        txtAno.setText("");
        capaActual = null;
        ficheiroCapaNovo = null;
        mostrarCapa();
        txtTitulo.requestFocusInWindow();
    }

    private void preencherFormulario() {
        int linha = Tema.linhaSeleccionada(tabela);
        if (linha < 0) {
            return;
        }
        Livro livro = livros.get(linha);
        txtTitulo.setText(livro.getTitulo());
        txtAutor.setText(livro.getAutor());
        txtIsbn.setText(livro.getIsbn() == null ? "" : livro.getIsbn());
        txtAno.setText(livro.getAno() == null ? "" : livro.getAno().toString());
        capaActual = livro.getCapa();
        ficheiroCapaNovo = null;
        mostrarCapa();
    }

    private void escolherCapa() {
        JFileChooser escolha = new JFileChooser();
        escolha.setDialogTitle("Escolher a imagem da capa");
        escolha.setFileFilter(new FileNameExtensionFilter("Imagens (PNG, JPG, GIF)", "png", "jpg", "jpeg", "gif"));
        if (escolha.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            ficheiroCapaNovo = escolha.getSelectedFile();
            mostrarCapa();
        }
    }

    /** Mostra a capa nova, a actual, ou uma capa desenhada a partir do título e do autor. */
    private void mostrarCapa() {
        if (ficheiroCapaNovo != null) {
            lblCapa.setIcon(Capas.deFicheiro(ficheiroCapaNovo, CAPA_L, CAPA_A));
            return;
        }
        Livro provisorio = new Livro();
        provisorio.setCapa(capaActual);
        try {
            provisorio.setTitulo(getTitulo().isEmpty() ? "Sem capa" : getTitulo());
            if (!getAutor().isEmpty()) {
                provisorio.setAutor(getAutor());
            }
        } catch (IllegalArgumentException textoInvalido) {
            // texto demasiado longo: a pré-visualização fica só com a cor
        }
        lblCapa.setIcon(Capas.icone(provisorio, CAPA_L, CAPA_A));
    }
}
