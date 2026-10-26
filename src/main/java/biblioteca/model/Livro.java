package biblioteca.model;
public class Livro{
    private int id;
    private String titulo;
    private String autor;
    public boolean disponivel;
    
    public Livro(int id, String titulo, String autor) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
    }
    public int getId() {
        return id;
    }
    
    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }
   public boolean estaDisponivel(){
    return disponivel;
   }
   
}