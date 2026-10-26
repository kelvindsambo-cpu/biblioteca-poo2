package biblioteca.view;

import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        super("Biblioteca POO II");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Livros", new JLabel("Módulo de livros por implementar", SwingConstants.CENTER));
        tabs.addTab("Leitores", new JLabel("Módulo de leitores por implementar", SwingConstants.CENTER));
        tabs.addTab("Empréstimos", new JLabel("Módulo de empréstimos por implementar", SwingConstants.CENTER));
        add(tabs);
        setSize(780, 480);
        setLocationRelativeTo(null

                              EmprestimoPanel emprestimoPanel = new EmprestimoPanel();
EmprestimoController emprestimoController = new EmprestimoController(
        new EmprestimoDAO(), new LivroDAO(), new LeitorDAO(), emprestimoPanel);
emprestimoController.carregarDados();
tabs.addTab("Empréstimos", emprestimoPanel);
    }
}
