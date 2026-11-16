package biblioteca.view;

import biblioteca.model.Leitor;
import biblioteca.model.Livro;
import biblioteca.model.Reserva;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Separador Reservas: reservar um livro emprestado e consultar ou cancelar reservas. */
public class ReservaPanel extends PainelBase {

    private final JComboBox<Livro> cmbLivros = new JComboBox<>();
    private final JComboBox<Leitor> cmbLeitores = new JComboBox<>();
    private final Botao btnReservar = Tema.botaoPrincipal("Reservar");
    private final JCheckBox chkApenasActivas = new JCheckBox("Mostrar só activas", true);
    private final Botao btnCancelar = Tema.botaoPerigo("Cancelar reserva");
    private final Botao btnImprimir = Tema.botaoNeutro("Imprimir / PDF");

    private final ModeloTabela modelo = new ModeloTabela(
            new String[]{"ID", "Livro", "Leitor", "Reservado em", "Estado", "Situação do livro"},
            new Class<?>[]{Integer.class, String.class, String.class, LocalDate.class, String.class, String.class});
    private final JTable tabela = new JTable(modelo);
    private List<Reserva> reservas = new ArrayList<>();

    public ReservaPanel() {
        super("Reservas", "Um leitor pode reservar um livro emprestado. Quando o livro voltar, só esse leitor o pode levar.");

        cmbLivros.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor,
                    int indice, boolean seleccionado, boolean comFoco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, comFoco);
                if (valor instanceof Livro) {
                    Livro livro = (Livro) valor;
                    setText(livro.getTitulo() + " - " + livro.getAutor());
                }
                return this;
            }
        });
        cmbLivros.setPreferredSize(new Dimension(300, 30));
        cmbLeitores.setPreferredSize(new Dimension(200, 30));
        chkApenasActivas.setOpaque(false);
        chkApenasActivas.setFont(Tema.FONTE);

        Cartao cartaoNova = new Cartao("Nova reserva");
        cartaoNova.add(Tema.linha(Tema.rotulo("Livro emprestado"), cmbLivros,
                Tema.rotulo("Leitor"), cmbLeitores, btnReservar), BorderLayout.CENTER);
        cartaoNova.add(Tema.nota("Cada livro aceita uma reserva activa de cada vez."), BorderLayout.SOUTH);

        JScrollPane scroll = Tema.estilizarTabela(tabela, 30);
        tabela.getColumnModel().getColumn(0).setMaxWidth(55);
        // Verde: a reserva está activa e o livro já voltou, pronto para o leitor o levantar
        Tema.renderizador(tabela).setLinhaPositiva(linha -> prontoALevantar(reservas.get(linha)));

        Cartao cartaoLista = new Cartao("Reservas");
        JPanel conteudo = Tema.transparente(new BorderLayout(0, 8));
        JPanel barra = Tema.transparente(new BorderLayout());
        barra.add(Tema.linha(chkApenasActivas), BorderLayout.WEST);
        barra.add(Tema.linha(btnImprimir), BorderLayout.EAST);
        conteudo.add(barra, BorderLayout.NORTH);
        conteudo.add(scroll, BorderLayout.CENTER);
        conteudo.add(Tema.linha(btnCancelar,
                Tema.nota("  Linhas a verde: o livro já foi devolvido e espera pelo leitor.")), BorderLayout.SOUTH);
        cartaoLista.add(conteudo, BorderLayout.CENTER);

        JPanel centro = Tema.transparente(new BorderLayout(0, 14));
        centro.add(cartaoNova, BorderLayout.NORTH);
        centro.add(cartaoLista, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        btnImprimir.addActionListener(e -> imprimir(tabela, "Relatório de reservas"));
    }

    private static boolean prontoALevantar(Reserva r) {
        return r.isActiva() && r.getLivro().isDisponivel();
    }

    // ---------- Leitura ----------

    public Livro getLivroSeleccionado() { return (Livro) cmbLivros.getSelectedItem(); }
    public Leitor getLeitorSeleccionado() { return (Leitor) cmbLeitores.getSelectedItem(); }
    public boolean isApenasActivas() { return chkApenasActivas.isSelected(); }

    public Reserva getReservaSeleccionada() {
        int linha = Tema.linhaSeleccionada(tabela);
        return linha < 0 ? null : reservas.get(linha);
    }

    // ---------- Actualização ----------

    public void mostrarLivros(List<Livro> livros) {
        cmbLivros.removeAllItems();
        for (Livro livro : livros) {
            cmbLivros.addItem(livro);
        }
    }

    public void mostrarLeitores(List<Leitor> leitores) {
        cmbLeitores.removeAllItems();
        for (Leitor leitor : leitores) {
            cmbLeitores.addItem(leitor);
        }
    }

    public void mostrarReservas(List<Reserva> reservas) {
        this.reservas = reservas;
        modelo.setRowCount(0);
        for (Reserva r : reservas) {
            String situacao;
            if (!r.isActiva()) {
                situacao = "-";
            } else if (prontoALevantar(r)) {
                situacao = "Pronto a levantar";
            } else {
                situacao = "Ainda emprestado";
            }
            modelo.addRow(new Object[]{
                    r.getId(),
                    r.getLivro().getTitulo(),
                    r.getLeitor().getNome(),
                    r.getDataReserva(),
                    r.getEstado().getDescricao(),
                    situacao
            });
        }
    }

    // ---------- Ligação ao controller ----------

    public void aoReservar(Runnable acao) { btnReservar.addActionListener(e -> acao.run()); }
    public void aoCancelar(Runnable acao) { btnCancelar.addActionListener(e -> acao.run()); }
    public void aoMudarFiltro(Runnable acao) { chkApenasActivas.addActionListener(e -> acao.run()); }
}
