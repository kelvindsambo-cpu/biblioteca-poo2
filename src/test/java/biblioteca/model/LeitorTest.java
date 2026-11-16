package biblioteca.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeitorTest {

    @Test
    void nomeEObrigatorio() { // RN02
        assertThrows(IllegalArgumentException.class, () -> new Leitor(0, "", "841234567"));
    }

    @Test
    void telefoneEOpcional() { // RN02
        assertNull(new Leitor(0, "Ana Machava", "   ").getTelefone());
        assertNull(new Leitor(0, "Ana Machava", null).getTelefone());
        assertEquals("841234567", new Leitor(0, "Ana Machava", " 841234567 ").getTelefone());
    }

    @Test
    void formatoDoTelefoneNoFormulario() {
        assertDoesNotThrow(() -> Leitor.validarFormatoTelefone("+258 84 123 4567"));
        assertDoesNotThrow(() -> Leitor.validarFormatoTelefone("84-123-4567"));
        assertDoesNotThrow(() -> Leitor.validarFormatoTelefone(""));
        assertThrows(IllegalArgumentException.class, () -> Leitor.validarFormatoTelefone("abc"));
        assertThrows(IllegalArgumentException.class, () -> Leitor.validarFormatoTelefone("123"));
    }
}
