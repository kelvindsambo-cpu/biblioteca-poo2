package biblioteca.view;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.time.LocalDate;

/**
 * Relatórios: usa a impressão que o próprio JTable já sabe fazer.
 * Para obter um PDF, escolhe-se "Microsoft Print to PDF" (Windows) ou "Guardar como PDF"
 * na janela de impressão. Não é preciso nenhuma biblioteca extra.
 */
final class Impressao {

    private Impressao() { }

    static void imprimir(Component pai, JTable tabela, String titulo) {
        if (tabela.getRowCount() == 0) {
            JOptionPane.showMessageDialog(pai, "A tabela está vazia: não há nada para imprimir.",
                    "Relatório", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // Em MessageFormat, a plica é especial e {0} é o número da página
        String cabecalho = (titulo + " - " + LocalDate.now().format(RenderizadorTabela.DATA)).replace("'", "''");
        try {
            tabela.print(JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat(cabecalho),
                    new MessageFormat("Biblioteca POO II  -  página {0}"));
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(pai, "Não foi possível imprimir: " + e.getMessage(),
                    "Relatório", JOptionPane.WARNING_MESSAGE);
        }
    }
}
