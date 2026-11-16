package biblioteca.controller;

import biblioteca.dao.FuncionarioRepository;
import biblioteca.model.Funcionario;
import biblioteca.view.LoginFrame;
import biblioteca.view.MainFrame;

/** Entrada na aplicação, saída (terminar sessão) e alteração da password. */
public class LoginController {

    private final FuncionarioRepository dao;
    private final LoginFrame view;

    public LoginController(FuncionarioRepository dao, LoginFrame view) {
        this.dao = dao;
        this.view = view;
        view.aoEntrar(this::entrar);
    }

    public void iniciar() {
        view.prepararNovaEntrada();
        view.setVisible(true);
    }

    private void entrar() {
        String utilizador = view.getUtilizador();
        String password = view.getPassword();
        if (utilizador.isEmpty() || password.isEmpty()) {
            view.mostrarErro("Indique o utilizador e a password.");
            return;
        }
        view.setOcupado(true);
        Tarefa.emFundo(() -> dao.autenticar(utilizador, password),
                funcionario -> {
                    view.setOcupado(false);
                    if (funcionario == null) {
                        view.prepararNovaEntrada();
                        view.mostrarErro("Utilizador ou password errados.");
                    } else {
                        view.setVisible(false);
                        abrirJanela(funcionario);
                    }
                },
                erro -> {
                    view.setOcupado(false);
                    view.mostrarErro(Tarefa.mensagem("Não foi possível ligar à base de dados.", erro));
                });
    }

    private void abrirJanela(Funcionario funcionario) {
        MainFrame janela = new MainFrame(funcionario);
        janela.aoSair(() -> {
            if (janela.confirmar("Terminar a sessão?")) {
                janela.dispose();
                iniciar();
            }
        });
        janela.aoAlterarPassword(() -> alterarPassword(janela, funcionario));
        janela.setVisible(true);
    }

    private void alterarPassword(MainFrame janela, Funcionario funcionario) {
        String[] p = janela.pedirPasswords();
        if (p == null) {
            return;
        }
        if (!p[1].equals(p[2])) {
            janela.mostrarErro("A nova password e a confirmação não são iguais.");
            return;
        }
        Tarefa.emFundo(() -> {
                    dao.alterarPassword(funcionario.getId(), p[0], p[1]);
                    return null;
                },
                nada -> janela.mostrarMensagem("Password alterada com sucesso."),
                erro -> janela.mostrarErro(Tarefa.mensagem("Não foi possível alterar a password.", erro)));
    }
}
