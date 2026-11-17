package biblioteca.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Empréstimo de um livro a um leitor.
 * As regras de prazo, atraso e multa (RN11 a RN13) estão aqui, num só sítio,
 * para serem usadas pelo DAO e pela View e testadas sem base de dados.
 * Os métodos recebem a data de "hoje" como parâmetro para poderem ser testados com qualquer data.
 */
public class Emprestimo {

    public static final int PRAZO_PADRAO_DIAS = 14;
    public static final int PRAZO_MAXIMO_DIAS = 60;
    public static final int MULTA_POR_DIA = 10; // meticais
    public static final String MOEDA = "MT";

    private int id;
    private Livro livro;
    private Leitor leitor;
    private LocalDate dataEmprestimo;
    private LocalDate dataPrevista;
    private LocalDate dataDevolucao; // null = ainda não devolvido
    private int multa;               // valor fixado na devolução
    private boolean multaPaga;

    public Emprestimo() { }

    public Emprestimo(int id, Livro livro, Leitor leitor, LocalDate dataEmprestimo,
                      LocalDate dataPrevista, LocalDate dataDevolucao, int multa, boolean multaPaga) {
        this.id = id;
        this.livro = livro;
        this.leitor = leitor;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevista = dataPrevista;
        this.dataDevolucao = dataDevolucao;
        this.multa = multa;
        this.multaPaga = multaPaga;
    }

    // ---------- Regras ----------

    /** RN12: 10 MT por cada dia depois da data prevista; 0 se devolvido a tempo. */
    public static int calcularMulta(LocalDate dataPrevista, LocalDate dataDevolucao) {
        long diasAtraso = ChronoUnit.DAYS.between(dataPrevista, dataDevolucao);
        return diasAtraso > 0 ? (int) diasAtraso * MULTA_POR_DIA : 0;
    }

    /** RN11: o prazo tem de estar entre 1 e 60 dias. */
    public static void validarPrazo(int prazoDias) {
        if (prazoDias < 1 || prazoDias > PRAZO_MAXIMO_DIAS) {
            throw new IllegalArgumentException("O prazo deve estar entre 1 e " + PRAZO_MAXIMO_DIAS + " dias.");
        }
    }

    public boolean isActivo() { return dataDevolucao == null; }

    /** Activo e já passou a data prevista. */
    public boolean isAtrasado(LocalDate hoje) {
        return isActivo() && hoje.isAfter(dataPrevista);
    }

    /** Dias de atraso até hoje (activo) ou até à devolução (devolvido). Nunca negativo. */
    public int getDiasAtraso(LocalDate hoje) {
        LocalDate fim = isActivo() ? hoje : dataDevolucao;
        long dias = ChronoUnit.DAYS.between(dataPrevista, fim);
        return dias > 0 ? (int) dias : 0;
    }

    /** Dias que o livro esteve (ou está) com o leitor. */
    public int getDiasEmprestado(LocalDate hoje) {
        LocalDate fim = isActivo() ? hoje : dataDevolucao;
        return (int) ChronoUnit.DAYS.between(dataEmprestimo, fim);
    }

    /** Devolvido: a multa registada. Activo: quanto pagaria se fosse devolvido hoje. */
    public int getMultaActual(LocalDate hoje) {
        return isActivo() ? calcularMulta(dataPrevista, hoje) : multa;
    }

    public boolean temMultaPorPagar() {
        return multa > 0 && !multaPaga;
    }

    /** Texto curto para a coluna "Estado" das tabelas. */
    public String getEstado(LocalDate hoje) {
        if (isActivo()) {
            return isAtrasado(hoje) ? "Em atraso (" + getDiasAtraso(hoje) + " d)" : "Activo";
        }
        return getDiasAtraso(hoje) > 0 ? "Devolvido com atraso" : "Devolvido";
    }

    /** Texto para a coluna "Multa" das tabelas. */
    public String getDescricaoMulta(LocalDate hoje) {
        int valor = getMultaActual(hoje);
        if (valor == 0) {
            return "-";
        }
        if (isActivo()) {
            return valor + " " + MOEDA + " (a acumular)";
        }
        return valor + " " + MOEDA + (multaPaga ? " (paga)" : " (por pagar)");
    }

    // ---------- Getters e setters ----------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }

    public Leitor getLeitor() { return leitor; }
    public void setLeitor(Leitor leitor) { this.leitor = leitor; }

    public LocalDate getDataEmprestimo() { return dataEmprestimo; }
    public void setDataEmprestimo(LocalDate dataEmprestimo) { this.dataEmprestimo = dataEmprestimo; }

    public LocalDate getDataPrevista() { return dataPrevista; }
    public void setDataPrevista(LocalDate dataPrevista) { this.dataPrevista = dataPrevista; }

    public LocalDate getDataDevolucao() { return dataDevolucao; }
    public void setDataDevolucao(LocalDate dataDevolucao) { this.dataDevolucao = dataDevolucao; }

    public int getMulta() { return multa; }
    public void setMulta(int multa) { this.multa = multa; }

    public boolean isMultaPaga() { return multaPaga; }
    public void setMultaPaga(boolean multaPaga) { this.multaPaga = multaPaga; }
}
