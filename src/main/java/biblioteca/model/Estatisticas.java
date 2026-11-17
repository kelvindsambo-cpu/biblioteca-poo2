package biblioteca.model;

import java.util.List;

/** Números mostrados no separador Início. Só leitura: é preenchido pelo EstatisticaDAO. */
public class Estatisticas {
    private final int totalLivros;
    private final int livrosDisponiveis;
    private final int totalLeitores;
    private final int emprestimosActivos;
    private final int emprestimosAtrasados;
    private final int reservasActivas;
    private final int multasPorPagar; // soma em meticais
    private final List<LivroPopular> maisEmprestados;

    public Estatisticas(int totalLivros, int livrosDisponiveis, int totalLeitores,
                        int emprestimosActivos, int emprestimosAtrasados, int reservasActivas,
                        int multasPorPagar, List<LivroPopular> maisEmprestados) {
        this.totalLivros = totalLivros;
        this.livrosDisponiveis = livrosDisponiveis;
        this.totalLeitores = totalLeitores;
        this.emprestimosActivos = emprestimosActivos;
        this.emprestimosAtrasados = emprestimosAtrasados;
        this.reservasActivas = reservasActivas;
        this.multasPorPagar = multasPorPagar;
        this.maisEmprestados = maisEmprestados;
    }

    public int getTotalLivros() { return totalLivros; }
    public int getLivrosDisponiveis() { return livrosDisponiveis; }
    public int getTotalLeitores() { return totalLeitores; }
    public int getEmprestimosActivos() { return emprestimosActivos; }
    public int getEmprestimosAtrasados() { return emprestimosAtrasados; }
    public int getReservasActivas() { return reservasActivas; }
    public int getMultasPorPagar() { return multasPorPagar; }
    public List<LivroPopular> getMaisEmprestados() { return maisEmprestados; }

    /** Um título e quantas vezes foi emprestado. */
    public static class LivroPopular {
        private final String titulo;
        private final int emprestimos;

        public LivroPopular(String titulo, int emprestimos) {
            this.titulo = titulo;
            this.emprestimos = emprestimos;
        }

        public String getTitulo() { return titulo; }
        public int getEmprestimos() { return emprestimos; }
    }
}
