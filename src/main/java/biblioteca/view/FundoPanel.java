package biblioteca.view;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Fundo da aplicação: degradé cor de papel, dois círculos dourados suaves
 * e uma fila de lombadas de livros desenhada no fundo da janela.
 * Tudo é desenhado com Graphics2D, por isso acompanha o tamanho da janela.
 */
class FundoPanel extends JPanel {

    // Lombadas: larguras e alturas geradas uma vez, sempre iguais (semente fixa)
    private static final int[] LARGURAS = new int[60];
    private static final int[] ALTURAS = new int[60];
    private static final Color[] CORES = {
            new Color(0x2C3E66), new Color(0x8C3B2E), new Color(0xD9982B), new Color(0x2F6B5E), new Color(0x5B4A7A)
    };

    static {
        Random r = new Random(7);
        for (int i = 0; i < LARGURAS.length; i++) {
            LARGURAS[i] = 14 + r.nextInt(16);
            ALTURAS[i] = 55 + r.nextInt(70);
        }
    }

    FundoPanel(LayoutManager layout) {
        super(layout);
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        g2.setPaint(new GradientPaint(0, 0, Tema.CREME, w, h, Tema.BEGE));
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(217, 152, 43, 30));
        g2.fillOval(w - 260, -140, 420, 420);
        g2.setColor(new Color(44, 62, 102, 18));
        g2.fillOval(-160, h / 3, 360, 360);

        // Prateleira com lombadas, muito transparente para não atrapalhar a leitura
        int x = 0;
        int base = h - 10;
        for (int i = 0; x < w; i = (i + 1) % LARGURAS.length) {
            Color c = CORES[i % CORES.length];
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 26));
            g2.fillRoundRect(x, base - ALTURAS[i], LARGURAS[i] - 3, ALTURAS[i], 4, 4);
            x += LARGURAS[i];
        }
        g2.setColor(new Color(90, 60, 30, 40));
        g2.fillRect(0, base, w, 10);
        g2.dispose();
    }
}
