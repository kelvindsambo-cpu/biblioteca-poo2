package biblioteca.model;

public class Leitor {
    private int id;
    private String nome;
    private String telefone;

    public Leitor(int id, String nome, String telefone) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @Override
    public String toString() {
        return "Leitor [id=" + id + ", nome=" + nome + ", telefone=" + telefone + "]";
    }

    



    
}
