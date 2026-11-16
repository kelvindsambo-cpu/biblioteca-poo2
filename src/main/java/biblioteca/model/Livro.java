package biblioteca.model;

import java.time.Year;

/**
 * Um exemplar físico de um livro.
 * Os setters validam os dados (RN01): um Livro com título ou autor vazio nunca chega a existir.
 */
public class Livro {
    private int id;
    private String titulo;
    private String autor;
    private boolean disponivel;
    private String isbn;   // opcional
    private Integer ano;   // opcional (null = desconhecido)
    private String capa;   // nome do ficheiro da imagem; opcional

    public Livro() {
        this.disponivel = true;
    }

    public Livro(int id, String titulo, String autor, boolean disponivel) {
        this.id = id;
        setTitulo(titulo);
        setAutor(autor);
        this.disponivel = disponivel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) {
        this.titulo = Validacao.obrigatorio(titulo, 150, "O título do livro");
    }

    public String getAutor() { return autor; }
    public void setAutor(String autor) {
        this.autor = Validacao.obrigatorio(autor, 100, "O autor do livro");
    }

    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    public String getIsbn() { return isbn; }

    /** Aceita ISBN-10 ou ISBN-13, com ou sem hífens e espaços. Vazio fica null. */
    public void setIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            this.isbn = null;
            return;
        }
        String limpo = isbn.trim();
        String soDigitos = limpo.replace("-", "").replace(" ", "");
        boolean valido = soDigitos.matches("\\d{13}") || soDigitos.matches("\\d{9}[\\dXx]");
        if (!valido) {
            throw new IllegalArgumentException("O ISBN deve ter 10 ou 13 dígitos (hífens são opcionais).");
        }
        this.isbn = limpo;
    }

    public Integer getAno() { return ano; }

    public void setAno(Integer ano) {
        int maximo = Year.now().getValue() + 1;
        if (ano != null && (ano < 1000 || ano > maximo)) {
            throw new IllegalArgumentException("O ano deve estar entre 1000 e " + maximo + ".");
        }
        this.ano = ano;
    }

    public String getCapa() { return capa; }
    public void setCapa(String capa) {
        this.capa = (capa == null || capa.isBlank()) ? null : capa.trim();
    }

    @Override
    public String toString() { return titulo; }
}
