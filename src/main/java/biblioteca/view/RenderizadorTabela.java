package biblioteca.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.IntPredicate;

/**
 * Desenha as células de todas as tabelas:
 * linhas alternadas, datas em dd/MM/aaaa, números ao centro, imagens (capas)
 * e linhas destacadas a vermelho ou verde quando o painel o pede.
 */
class RenderizadorTabela extends DefaultTableCellRenderer {

    static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Recebem o índice da linha no MODELO (não muda quando se ordena a tabela)
    private IntPredicate linhaEmAlerta = linha -> false;
    private IntPredicate linhaPositiva = linha -> false;

    void setLinhaEmAlerta(IntPredicate condicao) { this.linhaEmAlerta = condicao; }
    void setLinhaPositiva(IntPredicate condicao) { this.linhaPositiva = condicao; }

    @Override
    public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean seleccionado,
                                                   boolean foco, int linha, int coluna) {
        super.getTableCellRendererComponent(tabela, valor, seleccionado, false, linha, coluna);
        setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        setIcon(null);
        setHorizontalAlignment(LEFT);

        if (valor instanceof Icon) {
            setIcon((Icon) valor);
            setText("");
            setHorizontalAlignment(CENTER);
        } else if (valor instanceof LocalDate) {
            setText(((LocalDate) valor).format(DATA));
        } else if (valor instanceof Number) {
            setHorizontalAlignment(CENTER);
        } else if (valor == null) {
            setText("—"); // travessão: "sem valor"
        }

        if (!seleccionado) {
            int linhaModelo = tabela.convertRowIndexToModel(linha);
            setForeground(Tema.TEXTO);
            if (linhaEmAlerta.test(linhaModelo)) {
                setBackground(Tema.VERMELHO_CLARO);
                setForeground(Tema.VERMELHO.darker());
            } else if (linhaPositiva.test(linhaModelo)) {
                setBackground(Tema.VERDE_CLARO);
            } else {
                setBackground(linha % 2 == 0 ? Color.WHITE : Tema.LINHA_ALTERNADA);
            }
        }
        return this;
    }
}
