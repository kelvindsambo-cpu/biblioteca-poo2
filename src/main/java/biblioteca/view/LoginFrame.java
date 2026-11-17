package biblioteca.view;

import javax.swing.*;
import java.awt.*;

/** Janela de entrada: utilizador e password do funcionário. */
public class LoginFrame extends JFrame {

    private final JTextField txtUtilizador = Tema.campo(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final Botao btnEntrar = Tema.botaoPrincipal("Entrar");
    private final JLabel lblErro = new JLabel(" ");

    public LoginFrame() {
        super("Biblioteca POO II - Entrar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Tema.estilizarCampo(txtPassword);

        JLabel titulo = new JLabel("Biblioteca POO II", SwingConstants.CENTER);
        titulo.setFont(new Font("Serif", Font.BOLD, 30));
        titulo.setForeground(Tema.AZUL_ESCURO);
        JLabel subtitulo = Tema.nota("Entre com a sua conta de funcionário");
        subtitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblErro.setForeground(Tema.VERMELHO);
        lblErro.setFont(Tema.FONTE);
        lblErro.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel form = Tema.transparente(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);
        c.gridy = 0; form.add(new IconeLivro(), c);
        c.gridy = 1; form.add(titulo, c);
        c.gridy = 2; c.insets = new Insets(0, 0, 18, 0); form.add(subtitulo, c);
        c.insets = new Insets(4, 0, 4, 0);
        c.gridy = 3; form.add(Tema.rotulo("Utilizador"), c);
        c.gridy = 4; form.add(txtUtilizador, c);
        c.gridy = 5; form.add(Tema.rotulo("Password"), c);
        c.gridy = 6; form.add(txtPassword, c);
        c.gridy = 7; c.insets = new Insets(16, 0, 4, 0); form.add(btnEntrar, c);
        c.gridy = 8; c.insets = new Insets(8, 0, 0, 0); form.add(lblErro, c);

        Cartao cartao = new Cartao(null);
        cartao.setBorder(BorderFactory.createEmptyBorder(26, 36, 30, 40));
        cartao.add(form, BorderLayout.CENTER);

        FundoPanel fundo = new FundoPanel(new GridBagLayout()); // GridBagLayout centra o cartão
        fundo.add(cartao);
        setContentPane(fundo);

        getRootPane().setDefaultButton(btnEntrar); // Enter = Entrar
        setSize(560, 600);
        setMinimumSize(new Dimension(460, 560));
        setLocationRelativeTo(null);
    }

    public String getUtilizador() { return txtUtilizador.getText().trim(); }
    public String getPassword() { return new String(txtPassword.getPassword()); }

    public void aoEntrar(Runnable acao) { btnEntrar.addActionListener(e -> acao.run()); }

    public void mostrarErro(String texto) {
        // HTML para a mensagem poder ocupar várias linhas
        lblErro.setText("<html><div style='text-align:center;width:260px'>"
                + texto.replace("&", "&amp;").replace("<", "&lt;").replace("\n", "<br>") + "</div></html>");
    }

    /** Enquanto se verifica a password, o botão fica desligado para não haver cliques repetidos. */
    public void setOcupado(boolean ocupado) {
        btnEntrar.setEnabled(!ocupado);
        btnEntrar.setText(ocupado ? "A entrar..." : "Entrar");
        if (ocupado) {
            lblErro.setText(" ");
        }
    }

    public void prepararNovaEntrada() {
        txtPassword.setText("");
        lblErro.setText(" ");
        (getUtilizador().isEmpty() ? txtUtilizador : txtPassword).requestFocusInWindow();
    }

    /** Pequeno desenho de um livro aberto, feito com Graphics2D. */
    private static class IconeLivro extends JComponent {
        IconeLivro() {
            setPreferredSize(new Dimension(80, 64));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2;
            g2.setColor(Tema.DOURADO);
            g2.fillOval(cx - 30, 2, 60, 60);
            g2.setColor(Color.WHITE);
            g2.fillPolygon(new int[]{cx - 20, cx - 1, cx - 1, cx - 20}, new int[]{20, 24, 46, 42}, 4);
            g2.fillPolygon(new int[]{cx + 1, cx + 20, cx + 20, cx + 1}, new int[]{24, 20, 42, 46}, 4);
            g2.setColor(Tema.AZUL_ESCURO);
            g2.setStroke(new BasicStroke(1.4f));
            for (int i = 0; i < 3; i++) {
                g2.drawLine(cx - 16, 28 + i * 5, cx - 5, 30 + i * 5);
                g2.drawLine(cx + 5, 30 + i * 5, cx + 16, 28 + i * 5);
            }
            g2.dispose();
        }
    }
}
