package biblioteca.model;

public class Emprestimo {

    private int id;
    private Livro livro;
    private Leitor leitor;
    private String dataEmprestimo;
    private String dataPrevistaDevolucao;
    private String dataDevolucao;


    public Emprestimo(int id, Livro livro, Leitor leitor, String dataEmprestimo, String dataPrevistaDevolucao,
            String dataDevolucao) {
        this.id = id;
        this.livro = livro;
        this.leitor = leitor;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
        this.dataDevolucao = dataDevolucao;
    }

    public int getId() {
        return id;
    }

    public Livro getLivro() {
        return livro;
    }

    public Leitor getLeitor() {
        return leitor;
    }

    public String getDataEmprestimo() {
        return dataEmprestimo;
    }

    public String getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public String getDataDevolucao() {
        return dataDevolucao;
    }

    public boolean estaActivo() {
        return dataDevolucao == null;
    }

    public void devolver(String dataDevolucao)
            throws EmprestimoException {

        if (!estaActivo()) {
            throw new EmprestimoException("O livro já foi devolvido.");
        }

        if (dataDevolucao == null ||
            dataDevolucao.trim().isEmpty()) {

            throw new EmprestimoException("A data de devolução é obrigatória.");
        }

    }
    public void emprestar() throws EmprestimoException {

    if (livro == null || leitor == null) {
        throw new EmprestimoException(
            "É necessário indicar o livro e o leitor."
        );
    }

    if (!livro.estaDisponivel()) {
        throw new EmprestimoException(
            "O livro não está disponível para empréstimo."
        );
    }

    emprestar();
}
}


