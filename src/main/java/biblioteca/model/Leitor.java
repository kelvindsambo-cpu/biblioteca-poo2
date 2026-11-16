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

    /** Opcional: vazio fica null. Máximo 20 caracteres (tamanho da coluna). */
    public void setTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            this.telefone = null;
            return;
        }
        String limpo = telefone.trim();
        if (limpo.length() > 20) {
            throw new IllegalArgumentException("O telefone pode ter no máximo 20 caracteres.");
        }
        this.telefone = limpo;
    }

    /**
     * Verificação extra feita só no formulário (não ao ler da base, para não rejeitar dados antigos):
     * dígitos, espaços e hífens, com "+" opcional no início, e pelo menos 6 dígitos.
     */
    public static void validarFormatoTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return;
        }
        String limpo = telefone.trim();
        if (!limpo.matches("\\+?[\\d -]+") || limpo.replaceAll("\\D", "").length() < 6) {
            throw new IllegalArgumentException("Telefone inválido: use só dígitos (mínimo 6), espaços ou hífens, com \"+\" opcional.");
        }
    }

    @Override
    public String toString() { return nome; }
}
