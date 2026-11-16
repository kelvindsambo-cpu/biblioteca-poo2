package biblioteca.model;

/** Verificações repetidas pelos modelos. Lança IllegalArgumentException com mensagem amigável. */
final class Validacao {

    private Validacao() { }

    /** Devolve o texto sem espaços nas pontas; recusa vazio ou mais longo do que a coluna da base. */
    static String obrigatorio(String texto, int maximo, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
        String limpo = texto.trim();
        if (limpo.length() > maximo) {
            throw new IllegalArgumentException(campo + " pode ter no máximo " + maximo + " caracteres.");
        }
        return limpo;
    }
}
