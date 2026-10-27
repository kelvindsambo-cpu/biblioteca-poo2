package biblioteca.view;

import biblioteca.model.Emprestimo;
import biblioteca.model.Leitor;
import biblioteca.model.Livro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Painel do módulo Empréstimos.
 * Só desenha componentes e expõe métodos simples; não tem SQL nem lógica de negócio.
 */
public class EmprestimoPanel extends JPanel {

    // Componentes para emprestar
    private final JComboBox<Livro> cmbLivros = new JComboBox<>();
    private final JComboBox<Leitor> cmbLeitores = new JComboBox<>();
    private final JButton btnEmprestar = new JButton("Emprestar");

    // Componentes para consultar e devolver
    private final JCheckBox chkApenasActivos = new JCheckBox("Mostrar só activos", true);
    private final JButton btnDevolver = new JButton("Devolver seleccionado");

    // Modelo da tabela: guarda os dados; isCellEditable devolve false para não editar células
    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new String[]{"ID", "Livro", "Leitor", "Data empréstimo", "Data devolução"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modeloTabela);

    public EmprestimoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Os combos mostram texto legível em vez do toString() do objecto
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
        cmbLeitores.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor,
                    int indice, boolean seleccionado, boolean comFoco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, comFoco);
                if (valor instanceof Leitor) {
                    setText(((Leitor) valor).getNome());
                }
                return this;
            }
        });

        // Só uma linha pode ser seleccionada de cada vez
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Topo: formulário de empréstimo
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Livro:"));
        topo.add(cmbLivros);
        topo.add(new JLabel("Leitor:"));
        topo.add(cmbLeitores);
        topo.add(btnEmprestar);

        // Fundo: filtro e botão de devolução
        JPanel fundo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fundo.add(chkApenasActivos);
        fundo.add(btnDevolver);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(fundo, BorderLayout.SOUTH);
    }

    // ---------- Leitura do que o utilizador escolheu ----------

    public Livro getLivroSeleccionado() {
        return (Livro) cmbLivros.getSelectedItem(); // null se o combo estiver vazio
    }

    public Leitor getLeitorSeleccionado() {
        return (Leitor) cmbLeitores.getSelectedItem();
    }

    /** Devolve o índice da linha seleccionada na tabela, ou -1 se não houver. */
    public int getLinhaSeleccionada() {
        return tabela.getSelectedRow();
    }

    public boolean isApenasActivos() {
        return chkApenasActivos.isSelected();
    }

    // ---------- Actualização do ecrã ----------

    public void mostrarLivros(List<Livro> livros) {
        cmbLivros.removeAllItems();
        for (Livro livro : livros) {
            cmbLivros.addItem(livro);
        }
    }

    public void mostrarLeitores(List<Leitor> leitores) {
        cmbLeitores.removeAllItems();
        for (Leitor leitor : leitores) {
            cmbLeitores.addItem(leitor);
        }
    }

    public void mostrarEmprestimos(List<Emprestimo> emprestimos) {
        modeloTabela.setRowCount(0); // limpa a tabela
        for (Emprestimo e : emprestimos) {
            String devolucao = (e.getDataDevolucao() == null)
                    ? "Por devolver" : e.getDataDevolucao().toString();
            modeloTabela.addRow(new Object[]{
                    e.getId(),
                    e.getLivro().getTitulo(),
                    e.getLeitor().getNome(),
                    e.getDataEmprestimo(),
                    devolucao
            });
        }
    }

    public void mostrarMensagem(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarErro(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // ---------- Ligação dos botões ao controller ----------

    public void addEmprestarListener(ActionListener ouvinte) {
        btnEmprestar.addActionListener(ouvinte);
    }

    public void addDevolverListener(ActionListener ouvinte) {
        btnDevolver.addActionListener(ouvinte);
    }

    public void addFiltroListener(ActionListener ouvinte) {
        chkApenasActivos.addActionListener(ouvinte);
    }
}
