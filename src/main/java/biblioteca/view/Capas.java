package biblioteca.view;

import biblioteca.model.Livro;
import biblioteca.util.ArquivoCapas;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Imagens das capas, já no tamanho pedido. Guarda as que já calculou (cache)
 * para não ler o ficheiro outra vez a cada repintura da tabela.
 * Se um livro não tem capa, desenha uma com o título e o autor.
 */
final class Capas {

    private static final Map<String, ImageIcon> CACHE = new HashMap<>();

    private Capas() { }

    static ImageIcon icone(Livro livro, int largura, int altura) {
        String chave = (livro.getCapa() != null ? livro.getCapa() : "#" + livro.getTitulo() + "|" + livro.getAutor())
                + "@" + largura + "x" + altura;
        ImageIcon icone = CACHE.get(chave);
        if (icone == null) {
            BufferedImage original = ArquivoCapas.ler(livro.getCapa());
            Image imagem = original != null
                    ? original.getScaledInstance(largura, altura, Image.SCALE_SMOOTH)
                    : capaGerada(livro.getTitulo(), livro.getAutor(), largura, altura);
            icone = new ImageIcon(imagem);
            CACHE.put(chave, icone);
        }
        return icone;
    }

    /** Pré-visualização de um ficheiro escolhido pelo funcionário (ainda não guardado). */
    static ImageIcon deFicheiro(java.io.File ficheiro, int largura, int altura) {
        ImageIcon original = new ImageIcon(ficheiro.getPath());
        return new ImageIcon(original.getImage().getScaledInstance(largura, altura, Image.SCALE_SMOOTH));
    }

    /** Capa simples: cor escolhida a partir do título, lombada mais escura, título e autor. */
    private static Image capaGerada(String titulo, String autor, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        float tom = Math.abs((titulo == null ? 0 : titulo.hashCode()) % 360) / 360f;
        Color base = Color.getHSBColor(tom, 0.45f, 0.55f);
        g.setPaint(new GradientPaint(0, 0, base.brighter(), w, h, base.darker()));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(0, 0, 0, 60));
        g.fillRect(0, 0, Math.max(3, w / 14), h);          // lombada
        g.setColor(new Color(255, 255, 255, 120));
        g.drawRect(w / 8, h / 10, w - w / 4, h - h / 5);   // moldura fina

        if (w >= 60) { // nas miniaturas o texto não caberia
            g.setColor(Color.WHITE);
            g.setFont(new Font("Serif", Font.BOLD, Math.max(10, w / 9)));
            escreverCentrado(g, titulo == null ? "" : titulo, w, h / 3, w - w / 3);
            g.setFont(new Font(Tema.LETRA, Font.PLAIN, Math.max(8, w / 14)));
            escreverCentrado(g, autor == null ? "" : autor, w, h - h / 5, w - w / 3);
        }
        g.dispose();
        return img;
    }

    /** Parte o texto em linhas que caibam na largura e centra-as a partir de y. */
    private static void escreverCentrado(Graphics2D g, String texto, int w, int y, int larguraMax) {
        FontMetrics fm = g.getFontMetrics();
        StringBuilder linha = new StringBuilder();
        for (String palavra : texto.split(" ")) {
            String tentativa = linha.length() == 0 ? palavra : linha + " " + palavra;
            if (fm.stringWidth(tentativa) > larguraMax && linha.length() > 0) {
                g.drawString(linha.toString(), (w - fm.stringWidth(linha.toString())) / 2, y);
                y += fm.getHeight();
                linha = new StringBuilder(palavra);
            } else {
                linha = new StringBuilder(tentativa);
            }
        }
        g.drawString(linha.toString(), (w - fm.stringWidth(linha.toString())) / 2, y);
    }
}
