package biblioteca;

import biblioteca.controller.LoginController;
import biblioteca.dao.FuncionarioDAO;
import biblioteca.view.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Caixas de diálogo e campos com o aspecto do sistema operativo
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // se falhar, fica o aspecto padrão do Java
            }
            new LoginController(new FuncionarioDAO(), new LoginFrame()).iniciar();
        });
    }
}
