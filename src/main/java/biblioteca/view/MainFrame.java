package biblioteca.view;

import biblioteca.controller.EmprestimoController;
import biblioteca.controller.LeitorController;
import biblioteca.controller.LivroController;
import biblioteca.dao.EmprestimoDAO;
import biblioteca.dao.LeitorDAO;
import biblioteca.dao.LivroDAO;

import javax.swing.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("Biblioteca POO II");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        LivroPanel livroPainel = new LivroPanel();
        LivroController livroController = new LivroController(new LivroDAO(), livroPainel);
        tabs.addTab("Livros", livroPainel);
        //tabs.addTab("Livros", new JLabel("Módulo de livros por implementar", SwingConstants.CENTER));
        //tabs.addTab("Leitores", new JLabel("Módulo de leitores por implementar", SwingConstants.CENTER));
        LeitorPanel leitorPanel = new LeitorPanel();
        LeitorController leitorController = new LeitorController(new LeitorDAO(), leitorPanel);
        tabs.addTab("Leitores", leitorPanel);

        EmprestimoPanel emprestimoPanel = new EmprestimoPanel();
        EmprestimoController emprestimoController =  new EmprestimoController(new EmprestimoDAO(), new LivroDAO(), new LeitorDAO(), emprestimoPanel);
        tabs.addTab("Empréstimos", emprestimoPanel);

        // Módulo Empréstimos: View + DAOs + Controller
        /*EmprestimoPanel emprestimoPanel = new EmprestimoPanel();
        EmprestimoController emprestimoController = new EmprestimoController(
                new EmprestimoDAO(), new LivroDAO(), new LeitorDAO(), emprestimoPanel);
        emprestimoController.carregarDados();
        tabs.addTab("Empréstimos", emprestimoPanel);/* */

        livroController.listar();
        emprestimoController.carregarDados();

        tabs.addChangeListener(e -> {
            switch(tabs.getSelectedIndex()){
                case 0 -> livroController.listar();
                case 1 -> leitorController.listar();
                case 2 -> emprestimoController.carregarDados();
            }
        });
        add(tabs);
        setSize(780, 480);
        setLocationRelativeTo(null);
    }
}