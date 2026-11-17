package biblioteca.model;

/**
 * Uma operação foi recusada por violar uma regra de negócio (RN).
 * A mensagem é sempre escrita para o funcionário e pode ser mostrada tal como está.
 * Separa estes casos dos erros técnicos (SQLException), que têm outra mensagem.
 */
public class RegraNegocioException extends Exception {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
