package vamojunto.console;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import vamojunto.util.Formatador;

/**
 * Desenha qualquer resultado como tabela de texto, no estilo do terminal do MySQL.
 *
 * POO: METODOS ESTATICOS e SOBRECARGA (imprimir de ResultSet ou de lista).
 * JDBC: ResultSetMetaData descobre sozinho o nome e a quantidade de colunas,
 * por isso a mesma classe serve para todas as consultas.
 */
public final class TabelaConsole {

    private static final int LARGURA_MAXIMA = 45;
    private static final int COLUNAS_PARA_FICHA = 8;

    private TabelaConsole() {
    }

    /** Le todo o ResultSet e imprime. Devolve a quantidade de linhas. */
    public static int imprimir(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colunas = meta.getColumnCount();
        String[] cabecalho = new String[colunas];
        for (int i = 1; i <= colunas; i++) {
            cabecalho[i - 1] = meta.getColumnLabel(i);
        }
        List<String[]> linhas = new ArrayList<>();
        while (rs.next()) {
            String[] linha = new String[colunas];
            for (int i = 1; i <= colunas; i++) {
                linha[i - 1] = formatar(rs.getObject(i));
            }
            linhas.add(linha);
        }
        // uma linha com muitas colunas fica melhor em formato de ficha
        if (linhas.size() == 1 && colunas >= COLUNAS_PARA_FICHA) {
            imprimirFicha(cabecalho, linhas.get(0));
            return 1;
        }
        imprimir(cabecalho, linhas);
        return linhas.size();
    }

    /** Sobrecarga: imprime dados montados pelo programa (lista de objetos ja convertida). */
    public static void imprimir(String[] cabecalho, List<String[]> linhas) {
        if (linhas.isEmpty()) {
            System.out.println("   Nenhum registro encontrado.");
            return;
        }
        int[] larguras = new int[cabecalho.length];
        for (int c = 0; c < cabecalho.length; c++) {
            larguras[c] = cabecalho[c].length();
            for (String[] linha : linhas) {
                larguras[c] = Math.max(larguras[c], cortar(linha[c]).length());
            }
        }
        String separador = separador(larguras);
        System.out.println(separador);
        System.out.println(linha(cabecalho, larguras));
        System.out.println(separador);
        for (String[] l : linhas) {
            System.out.println(linha(l, larguras));
        }
        System.out.println(separador);
        System.out.println(linhas.size() + " registro(s).");
    }

    /** Uma linha mostrada na vertical: "coluna ....: valor". */
    public static void imprimirFicha(String[] cabecalho, String[] valores) {
        int largura = 0;
        for (String c : cabecalho) {
            largura = Math.max(largura, c.length());
        }
        System.out.println("+" + "-".repeat(largura + 50));
        for (int i = 0; i < cabecalho.length; i++) {
            System.out.printf("| %-" + largura + "s : %s%n", cabecalho[i], valores[i]);
        }
        System.out.println("+" + "-".repeat(largura + 50));
    }

    /** Converte qualquer valor vindo do banco para texto amigavel. */
    public static String formatar(Object valor) {
        if (valor == null) {
            return "-";
        }
        if (valor instanceof BigDecimal) {
            // padrao brasileiro: virgula como separador decimal (40,00)
            return ((BigDecimal) valor).toPlainString().replace('.', ',');
        }
        if (valor instanceof java.sql.Timestamp) {
            return Formatador.data(((java.sql.Timestamp) valor).toLocalDateTime());
        }
        if (valor instanceof LocalDateTime) {
            return Formatador.data((LocalDateTime) valor);
        }
        if (valor instanceof java.sql.Date) {
            return Formatador.data(((java.sql.Date) valor).toLocalDate());
        }
        if (valor instanceof LocalDate) {
            return Formatador.data((LocalDate) valor);
        }
        if (valor instanceof Boolean) {
            return ((Boolean) valor) ? "Sim" : "Nao";
        }
        return valor.toString();
    }

    private static String cortar(String texto) {
        String t = texto == null ? "-" : texto;
        return t.length() > LARGURA_MAXIMA ? t.substring(0, LARGURA_MAXIMA - 3) + "..." : t;
    }

    private static String separador(int[] larguras) {
        StringBuilder sb = new StringBuilder("+");
        for (int l : larguras) {
            sb.append("-".repeat(l + 2)).append("+");
        }
        return sb.toString();
    }

    private static String linha(String[] valores, int[] larguras) {
        StringBuilder sb = new StringBuilder("|");
        for (int c = 0; c < larguras.length; c++) {
            sb.append(" ").append(String.format("%-" + larguras[c] + "s", cortar(valores[c]))).append(" |");
        }
        return sb.toString();
    }
}
