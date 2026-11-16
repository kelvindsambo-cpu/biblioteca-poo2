package biblioteca.model;

import java.time.LocalDate;

/** Um leitor pede para ser o próximo a levar um livro que está emprestado. */
public class Reserva {
    private int id;
    private Livro livro;
    private Leitor leitor;
    private LocalDate dataReserva;
    private EstadoReserva estado;

    public Reserva() { }

    public Reserva(int id, Livro livro, Leitor leitor, LocalDate dataReserva, EstadoReserva estado) {
        this.id = id;
        this.livro = livro;
        this.leitor = leitor;
        this.dataReserva = dataReserva;
        this.estado = estado;
    }

    public boolean isActiva() { return estado == EstadoReserva.ACTIVA; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }

    public Leitor getLeitor() { return leitor; }
    public void setLeitor(Leitor leitor) { this.leitor = leitor; }

    public LocalDate getDataReserva() { return dataReserva; }
    public void setDataReserva(LocalDate dataReserva) { this.dataReserva = dataReserva; }

    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
}
