package biblioteca.model;

/** Leitor da biblioteca. Os setters validam os dados (RN02). */
public class Leitor {
    private int id;
    private String nome;
    private String telefone;

    public Leitor() { }

    public Leitor(int id, String nome, String telefone) {
        this.id = id;
        setNome(nome);
        setTelefone(telefone);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) {
        this.nome = Validacao.obrigatorio(nome, 100, "O nome do leitor");
    }

    public String getTelefone() { return telefone; }

    /** Opcional: vazio fica null. Só dígitos, espaços e um "+" no início. */
    public void setTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            this.telefone = null;
            return;
        }
        String limpo = telefone.trim();
        if (!limpo.matches("\\+?[\\d ]{6,19}")) {
            throw new IllegalArgumentException("O telefone só pode ter dígitos e espaços (mínimo 6), com \"+\" opcional no início.");
        }
        this.telefone = limpo;
    }

    @Override
    public String toString() { return nome; }
}
