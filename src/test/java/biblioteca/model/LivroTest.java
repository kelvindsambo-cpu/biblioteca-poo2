package biblioteca.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LivroTest {

    @Test
    void tituloEAutorSaoObrigatorios() { // RN01
        assertThrows(IllegalArgumentException.class, () -> new Livro(0, "  ", "Mia Couto", true));
        assertThrows(IllegalArgumentException.class, () -> new Livro(0, "Ualalapi", null, true));
    }

    @Test
    void espacosNasPontasSaoRetirados() {
        Livro livro = new Livro(0, "  Terra Sonâmbula ", " Mia Couto  ", true);
        assertEquals("Terra Sonâmbula", livro.getTitulo());
        assertEquals("Mia Couto", livro.getAutor());
    }

    @Test
    void tituloDemasiadoLongoERecusado() {
        String longo = "x".repeat(151);
        assertThrows(IllegalArgumentException.class, () -> new Livro(0, longo, "Autor", true));
    }

    @Test
    void isbnAceitaDezOuTrezeDigitosComHifens() {
        Livro livro = new Livro(0, "Clean Code", "Robert C. Martin", true);
        livro.setIsbn("978-0132350884");
        assertEquals("978-0132350884", livro.getIsbn());
        livro.setIsbn("0-306-40615-X");
        assertEquals("0-306-40615-X", livro.getIsbn());
        livro.setIsbn("   ");
        assertNull(livro.getIsbn(), "ISBN vazio fica null");
    }

    @Test
    void isbnInvalidoERecusado() {
        Livro livro = new Livro(0, "Clean Code", "Robert C. Martin", true);
        assertThrows(IllegalArgumentException.class, () -> livro.setIsbn("12345"));
        assertThrows(IllegalArgumentException.class, () -> livro.setIsbn("978-01323508AB"));
    }

    @Test
    void anoForaDoIntervaloERecusado() {
        Livro livro = new Livro(0, "Os Lusíadas", "Camões", true);
        livro.setAno(1572);
        assertEquals(1572, livro.getAno());
        livro.setAno(null);
        assertNull(livro.getAno());
        assertThrows(IllegalArgumentException.class, () -> livro.setAno(999));
        assertThrows(IllegalArgumentException.class, () -> livro.setAno(3000));
    }
}
