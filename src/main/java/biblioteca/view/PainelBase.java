package biblioteca.view;

import javax.swing.*;
import java.awt.*;

/**
 * O que todos os painéis (separadores) têm em comum: são transparentes para deixar ver
 * o fundo, têm um título no topo e mostram mensagens e confirmações da mesma maneira.
 */
abstract class PainelBase extends JPanel {

    PainelBase(String titulo, String descricao) {
        super(new BorderLayout(14, 14));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 26, 26));

        JPanel topo = Tema.transparente(new BorderLayout(0, 2));
        topo.add(Tema.titulo(titulo), BorderLayout.NORTH);
        topo.add(Tema.nota(descricao), BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);
    }

    public void mostrarMensagem(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarErro(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public boolean confirmar(String pergunta) {
        return JOptionPane.showConfirmDialog(this, pergunta, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Relatório: imprime a tabela; para PDF escolher a impressora "Microsoft Print to PDF". */
    void imprimir(JTable tabela, String tituloRelatorio) {
        Impressao.imprimir(this, tabela, tituloRelatorio);
    }
}
