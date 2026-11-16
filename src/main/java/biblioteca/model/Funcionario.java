package biblioteca.model;

/** Funcionário que entrou na aplicação. A password nunca fica guardada neste objecto. */
public class Funcionario {
    private final int id;
    private final String utilizador;
    private final String nome;

    public Funcionario(int id, String utilizador, String nome) {
        this.id = id;
        this.utilizador = utilizador;
        this.nome = nome;
    }

    public int getId() { return id; }
    public String getUtilizador() { return utilizador; }
    public String getNome() { return nome; }
}
