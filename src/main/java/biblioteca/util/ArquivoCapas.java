package biblioteca.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Ficheiros das capas dos livros. Na base fica só o nome do ficheiro (coluna livro.capa).
 * - As capas dos livros de exemplo vêm dentro da aplicação (src/main/resources/capas).
 * - As capas escolhidas pelo funcionário são copiadas para a pasta "capas" na raiz do projecto.
 */
public final class ArquivoCapas {

    public static final String PASTA = "capas";

    private ArquivoCapas() { }

    /** Lê a imagem pelo nome; devolve null se não existir ou não for uma imagem. */
    public static BufferedImage ler(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        try (InputStream recurso = ArquivoCapas.class.getResourceAsStream("/capas/" + nome)) {
            if (recurso != null) {
                return ImageIO.read(recurso);
            }
            File ficheiro = new File(PASTA, nome);
            return ficheiro.isFile() ? ImageIO.read(ficheiro) : null;
        } catch (IOException e) {
            return null;
        }
    }

    /** Copia a imagem escolhida para a pasta de capas e devolve o nome a guardar na base. */
    public static String guardar(File origem) throws IOException {
        if (ImageIO.read(origem) == null) {
            throw new IOException("O ficheiro escolhido não é uma imagem (use PNG, JPG ou GIF).");
        }
        // O prefixo com a hora evita apagar uma capa antiga com o mesmo nome
        String nome = System.currentTimeMillis() + "_" + origem.getName().replaceAll("[^A-Za-z0-9._-]", "_");
        File pasta = new File(PASTA);
        if (!pasta.isDirectory() && !pasta.mkdirs()) {
            throw new IOException("Não foi possível criar a pasta " + pasta.getAbsolutePath());
        }
        Files.copy(origem.toPath(), new File(pasta, nome).toPath(), StandardCopyOption.REPLACE_EXISTING);
        return nome;
    }
}
