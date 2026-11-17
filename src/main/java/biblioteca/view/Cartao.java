package biblioteca.view;

import javax.swing.*;
import java.awt.*;

/**
 * Caixa branca de cantos arredondados, com sombra suave e título opcional.
 * Usa BorderLayout: o título fica em NORTH e o conteúdo vai para CENTER (ou outra zona).
 */
class Cartao extends JPanel {

    private static final int RAIO = 18;
    private static final int SOMBRA = 4;

    Cartao(String titulo) {
        super(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 16 + SOMBRA, 16 + SOMBRA));
        if (titulo != null) {
            JLabel l = new JLabel(titulo);
            l.setFont(Tema.FONTE_SUBTITULO);
            l.setForeground(Tema.AZUL_ESCURO);
            add(l, BorderLayout.NORTH);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth() - SOMBRA;
        int h = getHeight() - SOMBRA;
        // sombra: o mesmo rectângulo, deslocado e quase transparente
        g2.setColor(new Color(60, 40, 10, 28));
        g2.fillRoundRect(SOMBRA, SOMBRA, w, h, RAIO, RAIO);
        g2.setColor(new Color(255, 255, 255, 242));
        g2.fillRoundRect(0, 0, w, h, RAIO, RAIO);
        g2.setColor(Tema.BORDA);
        g2.drawRoundRect(0, 0, w - 1, h - 1, RAIO, RAIO);
        g2.dispose();
        super.paintComponent(g);
    }
}
