package biblioteca.view;

import javax.swing.*;
import java.awt.*;

/**
 * Botão com cantos arredondados e cor própria.
 * Desenhamos o fundo nós mesmos em paintComponent, para ficar igual em qualquer sistema.
 */
class Botao extends JButton {

    private final Color cor;
    private final Color corPorCima;

    Botao(String texto, Color cor, Color corPorCima, Color corTexto) {
        super(texto);
        this.cor = cor;
        this.corPorCima = corPorCima;
        setForeground(corTexto);
        setFont(Tema.FONTE_NEGRITO);
        setContentAreaFilled(false); // o fundo é desenhado abaixo
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fundo = getModel().isRollover() || getModel().isPressed() ? corPorCima : cor;
        if (!isEnabled()) {
            fundo = new Color(0xCFC8BC);
        }
        g2.setColor(fundo);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g); // desenha o texto por cima
    }
}
