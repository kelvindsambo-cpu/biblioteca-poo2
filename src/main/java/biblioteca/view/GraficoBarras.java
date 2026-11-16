package biblioteca.view;

import biblioteca.model.Estatisticas.LivroPopular;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Gráfico de barras horizontais simples, desenhado à mão com Graphics2D. */
class GraficoBarras extends JPanel {

    private List<LivroPopular> dados = new ArrayList<>();

    GraficoBarras() {
        setOpaque(false);
        setPreferredSize(new Dimension(360, 220));
    }

    void setDados(List<LivroPopular> dados) {
        this.dados = dados;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(Tema.FONTE);

        if (dados.isEmpty()) {
            g2.setColor(Tema.TEXTO_SUAVE);
            g2.drawString("Ainda não há empréstimos registados.", 4, 24);
            g2.dispose();
            return;
        }

        int maximo = 1;
        for (LivroPopular p : dados) {
            maximo = Math.max(maximo, p.getEmprestimos());
        }

        int alturaLinha = Math.min(40, getHeight() / dados.size());
        int larguraTexto = Math.min(170, getWidth() / 2 - 10);
        int larguraBarras = getWidth() - larguraTexto - 40;
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < dados.size(); i++) {
            LivroPopular p = dados.get(i);
            int y = i * alturaLinha;
            int meio = y + alturaLinha / 2;

            g2.setColor(Tema.TEXTO);
            g2.drawString(cortar(p.getTitulo(), fm, larguraTexto), 0, meio + fm.getAscent() / 2 - 2);

            int largura = Math.max(6, larguraBarras * p.getEmprestimos() / maximo);
            g2.setColor(i == 0 ? Tema.DOURADO : Tema.AZUL);
            g2.fillRoundRect(larguraTexto + 8, meio - 9, largura, 18, 9, 9);

            g2.setColor(Tema.TEXTO_SUAVE);
            g2.drawString(String.valueOf(p.getEmprestimos()), larguraTexto + 14 + largura, meio + fm.getAscent() / 2 - 2);
        }
        g2.dispose();
    }

    /** Encurta o título com "..." se não couber. */
    private static String cortar(String texto, FontMetrics fm, int largura) {
        if (fm.stringWidth(texto) <= largura) {
            return texto;
        }
        while (texto.length() > 1 && fm.stringWidth(texto + "...") > largura) {
            texto = texto.substring(0, texto.length() - 1);
        }
        return texto + "...";
    }
}
