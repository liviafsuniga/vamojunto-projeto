package vamojunto.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatacao para exibir no console (padrao brasileiro).
 * POO: metodos estaticos e sobrecarga (varios "moeda" e "data").
 */
public final class Formatador {

    public static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DecimalFormat DUAS_CASAS =
            new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-BR")));

    private Formatador() {
    }

    /** R$ 1.234,50 (mesmo resultado de fn_formatar_moeda no banco). */
    public static String moeda(BigDecimal valor) {
        return valor == null ? "-" : "R$ " + DUAS_CASAS.format(valor);
    }

    public static String moeda(double valor) {
        return moeda(BigDecimal.valueOf(valor));
    }

    public static String decimal(BigDecimal valor) {
        return valor == null ? "-" : DUAS_CASAS.format(valor);
    }

    public static String data(LocalDate data) {
        return data == null ? "-" : data.format(DATA);
    }

    public static String data(LocalDateTime dataHora) {
        return dataHora == null ? "-" : dataHora.format(DATA_HORA);
    }

    /** 123.***.***-01 (mesmo resultado de fn_mascarar_cpf no banco). */
    public static String mascararCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }

    /** Mostra so os 4 ultimos digitos: *******7890 */
    public static String mascararDocumento(String doc) {
        if (doc == null || doc.length() < 4) {
            return doc;
        }
        return "*".repeat(doc.length() - 4) + doc.substring(doc.length() - 4);
    }

    public static String nota(BigDecimal nota) {
        return nota == null ? "sem avaliacoes" : DUAS_CASAS.format(nota) + " / 5";
    }
}
