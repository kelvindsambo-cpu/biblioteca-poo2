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

        // Os controllers de Livros e Leitores já carregam a lista ao serem criados
        LivroPanel livroPanel = new LivroPanel();
        LivroController livroController = new LivroController(new LivroDAO(), livroPanel);
        tabs.addTab("Livros", livroPanel);

        LeitorPanel leitorPanel = new LeitorPanel();
        LeitorController leitorController = new LeitorController(new LeitorDAO(), leitorPanel);
        tabs.addTab("Leitores", leitorPanel);

        EmprestimoPanel emprestimoPanel = new EmprestimoPanel();
        EmprestimoController emprestimoController = new EmprestimoController(
                new EmprestimoDAO(), new LivroDAO(), new LeitorDAO(), emprestimoPanel);
        tabs.addTab("Empréstimos", emprestimoPanel);
        emprestimoController.carregarDados();

        // Ao mudar de separador, recarrega para mostrar alterações feitas nos outros
        tabs.addChangeListener(e -> {
            switch (tabs.getSelectedIndex()) {
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
