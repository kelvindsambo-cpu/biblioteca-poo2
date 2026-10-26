package biblioteca.view;

import biblioteca.model.Leitor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
 
/**
 * View dos Leitores. 
 * O Controller liga as acções aos botões através dos métodos aoXxx().
 */
public class LeitorPanel extends JPanel {
 
    private final JTextField txtNome = new JTextField(20);
    private final JTextField txtTelefone = new JTextField(15);
    private final JButton btnRegistar = new JButton("Registar");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnRemover = new JButton("Remover");
    private final JButton btnLimpar = new JButton("Limpar");
 
    private final DefaultTableModel modelo =
            new DefaultTableModel(new String[]{"ID", "Nome", "Telefone"}, 0) {
                @Override
                public boolean isCellEditable(int linha, int coluna) {
                    return false;
                }
            };
    private final JTable tabela = new JTable(modelo);
 
    public LeitorPanel() {
        setLayout(new BorderLayout(8, 8));
 
        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.add(new JLabel("Nome:"));
        formulario.add(txtNome);
        formulario.add(new JLabel("Telefone (opcional):"));
        formulario.add(txtTelefone);
 
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(btnRegistar);
        botoes.add(btnEditar);
        botoes.add(btnRemover);
        botoes.add(btnLimpar);
 
        JPanel topo = new JPanel(new BorderLayout());
        topo.add(formulario, BorderLayout.NORTH);
        topo.add(botoes, BorderLayout.SOUTH);
 
        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
 
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabela.getSelectedRow() >= 0) {
                int linha = tabela.getSelectedRow();
                txtNome.setText(String.valueOf(modelo.getValueAt(linha, 1)));
                Object telefone = modelo.getValueAt(linha, 2);
                txtTelefone.setText(telefone == null ? "" : telefone.toString());
            }
        });
    }
 
    // ---- leitura dos campos ----
    public String getNome() {
        return txtNome.getText().trim();
    }
 
    public String getTelefone() {
        return txtTelefone.getText().trim();
    }
 
    /** Devolve o id do leitor seleccionado, ou 0 se não houver selecção. */
    public int getIdSeleccionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            return 0;
        }
        return (int) modelo.getValueAt(linha, 0);
    }
 
    // ---- actualização da tabela e dos campos ----
    public void mostrarLeitores(List<Leitor> leitores) {
        modelo.setRowCount(0);
        for (Leitor l : leitores) {
            modelo.addRow(new Object[]{l.getId(), l.getNome(), l.getTelefone()});
        }
    }
 
    public void limparCampos() {
        txtNome.setText("");
        txtTelefone.setText("");
        tabela.clearSelection();
    }
 
    // ---- mensagens amigáveis ----
    public void mostrarMensagem(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Informação",
                JOptionPane.INFORMATION_MESSAGE);
    }
 
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro",
                JOptionPane.ERROR_MESSAGE);
    }
 
    public boolean confirmar(String pergunta) {
        return JOptionPane.showConfirmDialog(this, pergunta, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
 
    // ---- ligação dos botões  ----
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
}