package biblioteca.view;

import javax.swing.table.DefaultTableModel;

/**
 * Modelo de tabela só de leitura que sabe o tipo de cada coluna.
 * Com o tipo certo, a ordenação compara números como números (2 antes de 10)
 * e datas como datas, em vez de comparar texto.
 */
class ModeloTabela extends DefaultTableModel {

    private final Class<?>[] tipos;

    ModeloTabela(String[] colunas, Class<?>[] tipos) {
        super(colunas, 0);
        this.tipos = tipos;
    }

    @Override
    public boolean isCellEditable(int linha, int coluna) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int coluna) {
        return tipos[coluna];
    }
}
