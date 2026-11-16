package biblioteca.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Regras de prazo, atraso e multa (RN11 a RN13), com datas fixas para o resultado não depender do dia. */
class EmprestimoTest {

    private static final LocalDate DIA_1 = LocalDate.of(2026, 3, 1);
    private static final LocalDate PRAZO = DIA_1.plusDays(14); // 15/03

    private Emprestimo activo() {
        return new Emprestimo(1, new Livro(1, "Ualalapi", "Khosa", false), new Leitor(1, "Carlos", null),
                DIA_1, PRAZO, null, 0, false);
    }

    @Test
    void semMultaQuandoDevolvidoATempo() {
        assertEquals(0, Emprestimo.calcularMulta(PRAZO, PRAZO));
        assertEquals(0, Emprestimo.calcularMulta(PRAZO, PRAZO.minusDays(5)));
    }

    @Test
    void multaDeDezMeticaisPorDiaDeAtraso() { // RN12
        assertEquals(10, Emprestimo.calcularMulta(PRAZO, PRAZO.plusDays(1)));
        assertEquals(60, Emprestimo.calcularMulta(PRAZO, PRAZO.plusDays(6)));
    }

    @Test
    void prazoTemDeEstarEntreUmESessentaDias() { // RN11
        assertDoesNotThrow(() -> Emprestimo.validarPrazo(1));
        assertDoesNotThrow(() -> Emprestimo.validarPrazo(60));
        assertThrows(IllegalArgumentException.class, () -> Emprestimo.validarPrazo(0));
        assertThrows(IllegalArgumentException.class, () -> Emprestimo.validarPrazo(61));
    }

    @Test
    void activoSoFicaAtrasadoDepoisDoPrazo() {
        Emprestimo e = activo();
        assertFalse(e.isAtrasado(PRAZO), "no último dia ainda está a tempo");
        assertTrue(e.isAtrasado(PRAZO.plusDays(1)));
        assertEquals(3, e.getDiasAtraso(PRAZO.plusDays(3)));
        assertEquals(0, e.getDiasAtraso(PRAZO.minusDays(2)));
        assertEquals(30, e.getMultaActual(PRAZO.plusDays(3)), "multa que pagaria se devolvesse hoje");
        assertEquals("Em atraso (3 d)", e.getEstado(PRAZO.plusDays(3)));
        assertEquals("Activo", e.getEstado(PRAZO));
    }

    @Test
    void devolvidoUsaADataDeDevolucaoENaoHoje() {
        Emprestimo e = activo();
        e.setDataDevolucao(PRAZO.plusDays(2));
        e.setMulta(20);
        LocalDate muitoDepois = PRAZO.plusDays(100);
        assertFalse(e.isAtrasado(muitoDepois), "um livro devolvido nunca está em atraso");
        assertEquals(2, e.getDiasAtraso(muitoDepois));
        assertEquals(16, e.getDiasEmprestado(muitoDepois));
        assertEquals(20, e.getMultaActual(muitoDepois));
        assertEquals("Devolvido com atraso", e.getEstado(muitoDepois));
    }

    @Test
    void multaPorPagarAteSerRegistadoOPagamento() {
        Emprestimo e = activo();
        e.setDataDevolucao(PRAZO.plusDays(3));
        e.setMulta(30);
        assertTrue(e.temMultaPorPagar());
        assertEquals("30 MT (por pagar)", e.getDescricaoMulta(PRAZO.plusDays(10)));
        e.setMultaPaga(true);
        assertFalse(e.temMultaPorPagar());
        assertEquals("30 MT (paga)", e.getDescricaoMulta(PRAZO.plusDays(10)));
    }
}
