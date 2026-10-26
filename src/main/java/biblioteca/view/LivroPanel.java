package biblioteca.view;
import biblioteca.model.Livro;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LivroPanel extends JPanel {

    private JTextField txtTitulo;
    private JTextField txtAutor;
    private JTextField txtPesquisa;

    private JButton btnRegistar;
    private JButton btnEditar;
    private JButton btnRemover;
    private JButton btnLimpar;
    private JButton btnPesquisar;

    private JTable tabelaLivros;
    private DefaultTableModel modeloTabela;

    public LivroPanel() {
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Título do painel
        JLabel lblTitulo = new JLabel("GESTÃO DE LIVROS");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        add(lblTitulo, BorderLayout.NORTH);

        // Painel principal
        JPanel painelCentral = new JPanel(new BorderLayout(10, 15));

        // Formulário
        JPanel painelFormulario = new JPanel(new GridBagLayout());
        painelFormulario.setBorder(
                BorderFactory.createTitledBorder("Dados do Livro"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtTitulo = new JTextField(25);
        txtAutor = new JTextField(25);

        gbc.gridx = 0;
        gbc.gridy = 0;
        painelFormulario.add(new JLabel("Título:"), gbc);

        gbc.gridx = 1;
        painelFormulario.add(txtTitulo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        painelFormulario.add(new JLabel("Autor:"), gbc);

        gbc.gridx = 1;
        painelFormulario.add(txtAutor, gbc);

        // Botões do formulário
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));

        btnRegistar = new JButton("Registar");
        btnEditar = new JButton("Editar");
        btnRemover = new JButton("Remover");
        btnLimpar = new JButton("Limpar");

        painelBotoes.add(btnRegistar);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnRemover);
        painelBotoes.add(btnLimpar);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        painelFormulario.add(painelBotoes, gbc);

        painelCentral.add(painelFormulario, BorderLayout.NORTH);

        // Área de pesquisa
        JPanel painelPesquisa = new JPanel(new BorderLayout(8, 0));
        painelPesquisa.setBorder(
         BorderFactory.createTitledBorder("Pesquisar Livros"));

        txtPesquisa = new JTextField();
        btnPesquisar = new JButton("Pesquisar");

        painelPesquisa.add(txtPesquisa, BorderLayout.CENTER);
        painelPesquisa.add(btnPesquisar, BorderLayout.EAST);

        // Tabela
        modeloTabela = new DefaultTableModel( new Object[]{"ID", "Título", "Autor", "Disponibilidade"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaLivros = new JTable(modeloTabela);
        tabelaLivros.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        tabelaLivros.setRowHeight(25);
        tabelaLivros.setAutoCreateRowSorter(true);

        JScrollPane scrollTabela = new JScrollPane(tabelaLivros);

        JPanel painelTabela = new JPanel(new BorderLayout(5, 10));
        painelTabela.add(painelPesquisa, BorderLayout.NORTH);
        painelTabela.add(scrollTabela, BorderLayout.CENTER);

        painelCentral.add(painelTabela, BorderLayout.CENTER);

        add(painelCentral, BorderLayout.CENTER);

        // Eventos da tabela
        tabelaLivros.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                preencherFormulario();
            }
        });
    }

    // Ligações dos botões ao Controller

    public void aoRegistar(Runnable acao) {
        btnRegistar.addActionListener(e -> acao.run());
    }

    public void aoEditar(Runnable acao) {
        btnEditar.addActionListener(e -> acao.run());
    }

    public void aoRemover(Runnable acao) {
        btnRemover.addActionListener(e -> acao.run());
    }

    public void aoLimpar(Runnable acao) {
        btnLimpar.addActionListener(e -> acao.run());
    }

    public void aoPesquisar(Runnable acao) {
        btnPesquisar.addActionListener(e -> acao.run());

        // Permite pesquisar pressionando Enter
        txtPesquisa.addActionListener(e -> acao.run());
    }

    // Dados introduzidos no formulário

    public String getTitulo() {
        return txtTitulo.getText().trim();
    }

    public String getAutor() {
        return txtAutor.getText().trim();
    }

    public String getTextoPesquisa() {
        return txtPesquisa.getText().trim();
    }

    // ID do livro seleccionado na tabela

    public int getIdSeleccionado() {
        int linha = tabelaLivros.getSelectedRow();

        if (linha == -1) {
            return 0;
        }

        int linhaModelo = tabelaLivros.convertRowIndexToModel(linha);

        return Integer.parseInt(
                modeloTabela.getValueAt(linhaModelo, 0).toString()
        );
    }

    // Mostra os livros na tabela

    public void mostrarLivros(List<Livro> livros) {
        modeloTabela.setRowCount(0);

        for (Livro livro : livros) {
            modeloTabela.addRow(new Object[]{
                    livro.getId(),
                    livro.getTitulo(),
                    livro.getAutor(),
                    livro.isDisponivel() ? "Disponível" : "Emprestado"
            });
        }
    }

    // Preenche o formulário ao seleccionar um livro

    private void preencherFormulario() {
        int linha = tabelaLivros.getSelectedRow();

        if (linha == -1) {
            return;
        }

        int linhaModelo = tabelaLivros.convertRowIndexToModel(linha);

        txtTitulo.setText(
                modeloTabela.getValueAt(linhaModelo, 1).toString()
        );

        txtAutor.setText(
                modeloTabela.getValueAt(linhaModelo, 2).toString()
        );
    }

    // Limpa os campos e a selecção

    public void limparCampos() {
        txtTitulo.setText("");
        txtAutor.setText("");
        txtPesquisa.setText("");
        tabelaLivros.clearSelection();
        txtTitulo.requestFocusInWindow();
    }

    // Mensagens apresentadas ao utilizador

    public void mostrarMensagem(String mensagem) {
        JOptionPane.showMessageDialog(
                this,
                mensagem,
                "Informação",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(
                this,
                mensagem,
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public boolean confirmar(String mensagem) {
        int resposta = JOptionPane.showConfirmDialog(
                this,
                mensagem,
                "Confirmar operação",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        return resposta == JOptionPane.YES_OPTION;
    }
}



