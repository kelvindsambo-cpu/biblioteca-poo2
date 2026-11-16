package biblioteca.model;

/** Estados possíveis de uma reserva. O nome (ACTIVA, ...) é o que fica guardado na base. */
public enum EstadoReserva {
    ACTIVA("Activa"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada");

    private final String descricao;

    EstadoReserva(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
}
