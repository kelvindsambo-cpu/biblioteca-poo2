package biblioteca.view;

import biblioteca.model.Emprestimo;
import biblioteca.model.Estatisticas;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Separador Início (dashboard): números principais, livros mais procurados e atrasos. */
public class InicioPanel extends PainelBase {

    private final JLabel numLivros = numero();
    private final JLabel numDisponiveis = numero();
    private final JLabel numActivos = numero();
    private final JLabel numAtrasados = numero();
    private final JLabel numLeitores = numero();
    private final JLabel numReservas = numero();
    private final JLabel numMultas = numero();
    private final JLabel numOcupacao = numero();

    private final GraficoBarras grafico = new GraficoBarras();
    private final Botao btnActualizar = Tema.botaoSecundario("Actualizar");
    private final Botao btnImprimir = Tema.botaoNeutro("Imprimir / PDF");

    private final ModeloTabela modelo = new ModeloTabela(
            new String[]{"Livro", "Leitor", "Telefone", "Prazo", "Atraso", "Multa"},
            new Class<?>[]{String.class, String.class, String.class, LocalDate.class, Integer.class, String.class});
    private final JTable tabela = new JTable(modelo);

    public InicioPanel(String nomeFuncionario) {
        super("Olá, " + nomeFuncionario, "Resumo da biblioteca em "
                + LocalDate.now().format(RenderizadorTabela.DATA) + ".");

        JPanel numeros = Tema.transparente(new GridLayout(2, 4, 14, 14));
        numeros.add(cartaoNumero("Livros no acervo", numLivros, Tema.AZUL));
        numeros.add(cartaoNumero("Disponíveis", numDisponiveis, Tema.VERDE));
        numeros.add(cartaoNumero("Empréstimos activos", numActivos, Tema.DOURADO));
        numeros.add(cartaoNumero("Em atraso", numAtrasados, Tema.VERMELHO));
        numeros.add(cartaoNumero("Leitores", numLeitores, Tema.AZUL));
        numeros.add(cartaoNumero("Reservas activas", numReservas, Tema.DOURADO));
        numeros.add(cartaoNumero("Multas por pagar", numMultas, Tema.VERMELHO));
        numeros.add(cartaoNumero("Livros fora da estante", numOcupacao, Tema.AZUL));

        Cartao cartaoGrafico = new Cartao("Livros mais emprestados");
        cartaoGrafico.add(grafico, BorderLayout.CENTER);

        JScrollPane scroll = Tema.estilizarTabela(tabela, 28);
        int[] larguras = {130, 125, 90, 85, 55, 65};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        Tema.renderizador(tabela).setLinhaEmAlerta(linha -> true); // todos estão em atraso
        Cartao cartaoAtrasos = new Cartao("Empréstimos em atraso: contactar os leitores");
        cartaoAtrasos.add(scroll, BorderLayout.CENTER);
        cartaoAtrasos.add(Tema.linha(btnImprimir), BorderLayout.SOUTH);

        JPanel baixo = Tema.transparente(new GridLayout(1, 2, 14, 0));
        baixo.add(cartaoGrafico);
        baixo.add(cartaoAtrasos);

        JPanel centro = Tema.transparente(new BorderLayout(0, 14));
        centro.add(numeros, BorderLayout.NORTH);
        centro.add(baixo, BorderLayout.CENTER);
        centro.add(Tema.linha(btnActualizar), BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        btnImprimir.addActionListener(e -> imprimir(tabela, "Empréstimos em atraso"));
    }

    private static JLabel numero() {
        JLabel l = new JLabel("-");
        l.setFont(new Font(Tema.LETRA, Font.BOLD, 30));
        l.setForeground(Tema.AZUL_ESCURO);
        return l;
    }

    /** Cartão com uma barra de cor à esquerda, o número grande e a legenda por baixo. */
    private static Cartao cartaoNumero(String legenda, JLabel valor, Color cor) {
        Cartao cartao = new Cartao(null);
        JPanel barra = new JPanel();
        barra.setBackground(cor);
        barra.setPreferredSize(new Dimension(5, 0));
        JPanel textos = Tema.transparente(new BorderLayout());
        textos.add(valor, BorderLayout.CENTER);
        textos.add(Tema.nota(legenda), BorderLayout.SOUTH);
        cartao.add(barra, BorderLayout.WEST);
        cartao.add(textos, BorderLayout.CENTER);
        return cartao;
    }

    // ---------- Actualização ----------

    public void mostrarEstatisticas(Estatisticas e) {
        numLivros.setText(String.valueOf(e.getTotalLivros()));
        numDisponiveis.setText(String.valueOf(e.getLivrosDisponiveis()));
        numActivos.setText(String.valueOf(e.getEmprestimosActivos()));
        numAtrasados.setText(String.valueOf(e.getEmprestimosAtrasados()));
        numLeitores.setText(String.valueOf(e.getTotalLeitores()));
        numReservas.setText(String.valueOf(e.getReservasActivas()));
        numMultas.setText(e.getMultasPorPagar() + " " + Emprestimo.MOEDA);
        int percentagem = e.getTotalLivros() == 0 ? 0
                : 100 * (e.getTotalLivros() - e.getLivrosDisponiveis()) / e.getTotalLivros();
        numOcupacao.setText(percentagem + "%");
        grafico.setDados(e.getMaisEmprestados());
    }

    public void mostrarAtrasados(List<Emprestimo> atrasados) {
        LocalDate hoje = LocalDate.now();
        modelo.setRowCount(0);
        for (Emprestimo e : atrasados) {
            modelo.addRow(new Object[]{
                    e.getLivro().getTitulo(),
                    e.getLeitor().getNome(),
                    e.getLeitor().getTelefone(),
                    e.getDataPrevista(),
                    e.getDiasAtraso(hoje),
                    e.getMultaActual(hoje) + " " + Emprestimo.MOEDA
            });
        }
    }

    public void limpar() {
        mostrarAtrasados(new ArrayList<>());
        grafico.setDados(new ArrayList<>());
    }

    public void aoActualizar(Runnable acao) { btnActualizar.addActionListener(e -> acao.run()); }
}
