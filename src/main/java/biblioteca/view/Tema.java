package biblioteca.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Cores, letras e pequenas "fábricas" de componentes usadas por todos os painéis.
 * Mudar uma cor aqui muda-a na aplicação inteira.
 */
final class Tema {

    // Paleta: azul-marinho (menu e títulos), dourado (destaques) e papel creme (fundo)
    static final Color AZUL_ESCURO = new Color(0x1B2540);
    static final Color AZUL = new Color(0x2C3E66);
    static final Color DOURADO = new Color(0xD9982B);
    static final Color DOURADO_CLARO = new Color(0xF6E3BC);
    static final Color CREME = new Color(0xF7F1E6);
    static final Color BEGE = new Color(0xE8D9BF);
    static final Color TEXTO = new Color(0x2A2A33);
    static final Color TEXTO_SUAVE = new Color(0x6E6A63);
    static final Color VERMELHO = new Color(0xB83A2E);
    static final Color VERMELHO_CLARO = new Color(0xFBE4E0);
    static final Color VERDE = new Color(0x2F7D4F);
    static final Color VERDE_CLARO = new Color(0xE2F3E7);
    static final Color LINHA_ALTERNADA = new Color(0xFAF6EF);
    static final Color BORDA = new Color(0xE2D8C8);

    static final String LETRA = "Segoe UI";
    static final Font FONTE = new Font(LETRA, Font.PLAIN, 13);
    static final Font FONTE_NEGRITO = new Font(LETRA, Font.BOLD, 13);
    static final Font FONTE_TITULO = new Font(LETRA, Font.BOLD, 24);
    static final Font FONTE_SUBTITULO = new Font(LETRA, Font.BOLD, 15);

    private Tema() { }

    // ---------- Textos ----------

    static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE_TITULO);
        l.setForeground(AZUL_ESCURO);
        return l;
    }

    static JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE_NEGRITO);
        l.setForeground(TEXTO);
        return l;
    }

    static JLabel nota(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE.deriveFont(12f));
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

    // ---------- Botões ----------

    static Botao botaoPrincipal(String texto) {
        return new Botao(texto, DOURADO, DOURADO.darker(), Color.WHITE);
    }

    static Botao botaoSecundario(String texto) {
        return new Botao(texto, AZUL, AZUL_ESCURO, Color.WHITE);
    }

    static Botao botaoNeutro(String texto) {
        return new Botao(texto, new Color(0xE9E2D6), new Color(0xDCD2C2), TEXTO);
    }

    static Botao botaoPerigo(String texto) {
        return new Botao(texto, VERMELHO, VERMELHO.darker(), Color.WHITE);
    }

    // ---------- Campos ----------

    static JTextField campo(int colunas) {
        JTextField campo = new JTextField(colunas);
        estilizarCampo(campo);
        return campo;
    }

    static void estilizarCampo(JComponent campo) {
        campo.setFont(FONTE);
        Border linha = BorderFactory.createLineBorder(BORDA);
        campo.setBorder(BorderFactory.createCompoundBorder(linha, BorderFactory.createEmptyBorder(5, 8, 5, 8)));
    }

    /** Painel sem fundo próprio, para deixar ver o cartão ou o fundo por trás. */
    static JPanel transparente(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    /** Linha de componentes alinhados à esquerda. */
    static JPanel linha(Component... componentes) {
        JPanel p = transparente(new FlowLayout(FlowLayout.LEFT, 8, 4));
        for (Component c : componentes) {
            p.add(c);
        }
        return p;
    }

    // ---------- Tabelas ----------

    /** Aplica o estilo comum e devolve a tabela dentro de um JScrollPane. */
    static JScrollPane estilizarTabela(JTable tabela, int alturaLinha) {
        tabela.setFont(FONTE);
        tabela.setRowHeight(alturaLinha);
        tabela.setShowVerticalLines(false);
        tabela.setIntercellSpacing(new Dimension(0, 1)); // sem riscas brancas entre colunas
        tabela.setGridColor(new Color(0xEFE7DA));
        tabela.setSelectionBackground(DOURADO_CLARO);
        tabela.setSelectionForeground(TEXTO);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.setAutoCreateRowSorter(true); // clicar no cabeçalho ordena
        tabela.getTableHeader().setReorderingAllowed(false);

        RenderizadorTabela renderizador = new RenderizadorTabela();
        tabela.setDefaultRenderer(Object.class, renderizador);
        tabela.setDefaultRenderer(Number.class, renderizador);
        tabela.setDefaultRenderer(Icon.class, renderizador);

        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setDefaultRenderer(new RenderizadorCabecalho());
        cabecalho.setPreferredSize(new Dimension(0, 34));

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(BORDA));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    /** O renderizador instalado por estilizarTabela, para o painel definir que linhas destacar. */
    static RenderizadorTabela renderizador(JTable tabela) {
        return (RenderizadorTabela) tabela.getDefaultRenderer(Object.class);
    }

    /** Índice no modelo da linha seleccionada (diferente do da vista quando a tabela está ordenada), ou -1. */
    static int linhaSeleccionada(JTable tabela) {
        int linha = tabela.getSelectedRow();
        return linha < 0 ? -1 : tabela.convertRowIndexToModel(linha);
    }

    /** Cabeçalho azul-marinho com a seta da ordenação actual. */
    private static class RenderizadorCabecalho extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean seleccionado,
                                                       boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, false, false, linha, coluna);
            String texto = valor == null ? "" : valor.toString();
            RowSorter<?> sorter = tabela.getRowSorter();
            if (sorter != null) {
                List<? extends RowSorter.SortKey> chaves = sorter.getSortKeys();
                if (!chaves.isEmpty() && chaves.get(0).getColumn() == tabela.convertColumnIndexToModel(coluna)) {
                    texto += chaves.get(0).getSortOrder() == SortOrder.ASCENDING ? "  ▲" : "  ▼";
                }
            }
            setText(texto);
            setFont(FONTE_NEGRITO);
            setBackground(AZUL);
            setForeground(Color.WHITE);
            setHorizontalAlignment(LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 6));
            return this;
        }
    }
}
