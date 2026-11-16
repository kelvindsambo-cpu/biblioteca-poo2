package biblioteca.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Passwords dos funcionários. Nunca se guarda a password em claro:
 * guarda-se um "sal" aleatório e o SHA-256 de (sal + password).
 * O mesmo cálculo pode ser feito no MySQL com SHA2(CONCAT(sal, password), 256),
 * que é o que o script SQL usa para criar o utilizador admin.
 */
public final class Senha {

    public static final int TAMANHO_MINIMO = 6;

    private Senha() { }

    /** 16 bytes aleatórios em hexadecimal (32 caracteres). */
    public static String gerarSal() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return hex(bytes);
    }

    /** SHA-256 de (sal + password), em hexadecimal minúsculo (64 caracteres). */
    public static String calcularHash(String sal, String password) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return hex(sha256.digest((sal + password).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não está disponível nesta JVM.", e);
        }
    }

    /** Compara em tempo constante para não dar pistas pelo tempo de resposta. */
    public static boolean confere(String sal, String hashGuardado, String password) {
        byte[] calculado = calcularHash(sal, password).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(calculado, hashGuardado.getBytes(StandardCharsets.UTF_8));
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
