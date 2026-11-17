package biblioteca.model;

/**
 * Regra de negócio violada ao emprestar, devolver ou pagar uma multa
 * (ex.: livro já emprestado, devolução duplicada, leitor com multa por pagar).
 */
public class EmprestimoException extends RegraNegocioException {

    public EmprestimoException(String mensagem) {
        super(mensagem);
    }
}
