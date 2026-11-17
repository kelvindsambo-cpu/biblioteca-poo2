package biblioteca.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SenhaTest {

    @Test
    void hashIgualAoSha2DoMySQL() {
        // Valor obtido fora do Java: SHA-256 de "abc123" + "admin123".
        // Garante que o admin criado em biblioteca.sql com SHA2(CONCAT(sal, password), 256) consegue entrar.
        assertEquals("48250b108313b04cd3761a733d4c44ca83c7a6a74cdb698b3f82799ef399e288",
                Senha.calcularHash("abc123", "admin123"));
    }

    @Test
    void confereSoComAPasswordCerta() {
        String sal = Senha.gerarSal();
        String hash = Senha.calcularHash(sal, "segredo1");
        assertTrue(Senha.confere(sal, hash, "segredo1"));
        assertFalse(Senha.confere(sal, hash, "segredo2"));
    }

    @Test
    void cadaSalEDiferente() {
        String a = Senha.gerarSal();
        assertEquals(32, a.length());
        assertNotEquals(a, Senha.gerarSal());
        assertNotEquals(Senha.calcularHash(a, "x"), Senha.calcularHash(Senha.gerarSal(), "x"),
                "a mesma password com sais diferentes dá hashes diferentes");
    }
}
