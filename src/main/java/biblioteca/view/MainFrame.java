package biblioteca.view;

import biblioteca.controller.EmprestimoController;
import biblioteca.controller.InicioController;
import biblioteca.controller.LeitorController;
import biblioteca.controller.LivroController;
import biblioteca.controller.ReservaController;
import biblioteca.dao.EmprestimoDAO;
import biblioteca.dao.EstatisticaDAO;
import biblioteca.dao.LeitorDAO;
import biblioteca.dao.LivroDAO;
import biblioteca.dao.ReservaDAO;
import biblioteca.model.Funcionario;

import javax.swing.*;
import java.awt.*;

/**
 * Janela principal: menu lateral à esquerda e, à direita, o separador escolhido
 * (CardLayout mostra um painel de cada vez) sobre o fundo desenhado.
 */
public class MainFrame extends JFrame {

    private final CardLayout cartas = new CardLayout();
    private final FundoPanel conteudo = new FundoPanel(cartas);
    private final ButtonGroup grupoMenu = new ButtonGroup();
    private final JPanel menu = new JPanel();

    private final Botao btnPassword = new Botao("Alterar password", Tema.AZUL, Tema.AZUL.brighter(), Color.WHITE);
    private final Botao btnSair = new Botao("Sair", new Color(0x3A2F2A), new Color(0x5A4038), Color.WHITE);

    public MainFrame(Funcionario funcionario) {
        super("Biblioteca UEM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ---- Painéis e controllers (cada controller recebe os repositórios de que precisa) ----
        LivroDAO livroDao = new LivroDAO();
        LeitorDAO leitorDao = new LeitorDAO();
        EmprestimoDAO emprestimoDao = new EmprestimoDAO();
        ReservaDAO reservaDao = new ReservaDAO();

        InicioPanel inicioPanel = new InicioPanel(funcionario.getNome());
        InicioController inicio = new InicioController(new EstatisticaDAO(), emprestimoDao, inicioPanel);

        LivroPanel livroPanel = new LivroPanel();
        LivroController livros = new LivroController(livroDao, livroPanel);

        LeitorPanel leitorPanel = new LeitorPanel();
        LeitorController leitores = new LeitorController(leitorDao, emprestimoDao, leitorPanel);

        EmprestimoPanel emprestimoPanel = new EmprestimoPanel();
        EmprestimoController emprestimos = new EmprestimoController(
                emprestimoDao, livroDao, leitorDao, reservaDao, emprestimoPanel);

        ReservaPanel reservaPanel = new ReservaPanel();
        ReservaController reservas = new ReservaController(reservaDao, livroDao, leitorDao, reservaPanel);

        // ---- Menu lateral ----
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(Tema.AZUL_ESCURO);
        menu.setBorder(BorderFactory.createEmptyBorder(22, 14, 18, 14));
        menu.setPreferredSize(new Dimension(220, 0));

        JLabel marca = new JLabel("Biblioteca");
        marca.setFont(new Font("Serif", Font.BOLD, 26));
        marca.setForeground(Color.WHITE);
        JLabel subMarca = new JLabel("POO II");
        subMarca.setFont(Tema.FONTE_NEGRITO);
        subMarca.setForeground(Tema.DOURADO);
        menu.add(marca);
        menu.add(subMarca);
        menu.add(Box.createVerticalStrut(28));

        // Ao abrir um separador, os dados são recarregados da base
        BotaoMenu primeiro = adicionarSeparador("Início", inicioPanel, inicio::carregarDados);
        adicionarSeparador("Livros", livroPanel, livros::listar);
        adicionarSeparador("Leitores", leitorPanel, leitores::listar);
        adicionarSeparador("Empréstimos", emprestimoPanel, emprestimos::carregarDados);
        adicionarSeparador("Reservas", reservaPanel, reservas::carregarDados);

        menu.add(Box.createVerticalGlue());
        JLabel quem = new JLabel("<html><span style='color:#B9C2D6'>Sessão de</span><br><b>"
                + funcionario.getNome().replace("&", "&amp;").replace("<", "&lt;") + "</b></html>");
        quem.setForeground(Color.WHITE);
        quem.setFont(Tema.FONTE);
        menu.add(quem);
        menu.add(Box.createVerticalStrut(10));
        for (Botao b : new Botao[]{btnPassword, btnSair}) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            menu.add(b);
            menu.add(Box.createVerticalStrut(6));
        }

        add(menu, BorderLayout.WEST);
        add(conteudo, BorderLayout.CENTER);

        primeiro.doClick(); // começa no Início

        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
    }

    private BotaoMenu adicionarSeparador(String nome, JPanel painel, Runnable aoAbrir) {
        conteudo.add(painel, nome);
        BotaoMenu botao = new BotaoMenu(nome);
        botao.addActionListener(e -> {
            cartas.show(conteudo, nome);
            aoAbrir.run();
        });
        grupoMenu.add(botao);
        menu.add(botao);
        menu.add(Box.createVerticalStrut(4));
        return botao;
    }

    // ---------- Sessão (ligada pelo LoginController) ----------

    public void aoSair(Runnable acao) { btnSair.addActionListener(e -> acao.run()); }
    public void aoAlterarPassword(Runnable acao) { btnPassword.addActionListener(e -> acao.run()); }

    /** Pede a password actual e a nova duas vezes; devolve {actual, nova, confirmação} ou null se cancelar. */
    public String[] pedirPasswords() {
        JPasswordField actual = new JPasswordField(16);
        JPasswordField nova = new JPasswordField(16);
        JPasswordField confirmacao = new JPasswordField(16);
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel("Password actual"));
        p.add(actual);
        p.add(new JLabel("Nova password (mínimo 6 caracteres)"));
        p.add(nova);
        p.add(new JLabel("Repetir a nova password"));
        p.add(confirmacao);
        int resposta = JOptionPane.showConfirmDialog(this, p, "Alterar password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resposta != JOptionPane.OK_OPTION) {
            return null;
        }
        return new String[]{new String(actual.getPassword()), new String(nova.getPassword()),
                new String(confirmacao.getPassword())};
    }

    public boolean confirmar(String pergunta) {
        return JOptionPane.showConfirmDialog(this, pergunta, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public void mostrarMensagem(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarErro(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    /** Botão do menu lateral: fica dourado quando é o separador aberto. */
    private static class BotaoMenu extends JToggleButton {
        BotaoMenu(String texto) {
            super(texto);
            setFont(new Font(Tema.LETRA, Font.BOLD, 14));
            setForeground(new Color(0xDCE2EE));
            setHorizontalAlignment(LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 10));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            addItemListener(e -> setForeground(isSelected() ? Tema.AZUL_ESCURO : new Color(0xDCE2EE)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected()) {
                g2.setColor(Tema.DOURADO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
