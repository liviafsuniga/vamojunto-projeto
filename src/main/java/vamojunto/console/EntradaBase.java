package vamojunto.console;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Implementa toda a validacao de digitacao em cima de um unico metodo
 * abstrato, lerLinha(). Cada subclasse so precisa dizer DE ONDE vem a linha.
 *
 * POO: CLASSE ABSTRATA que implementa uma INTERFACE; as subclasses usam
 * HERANCA e SOBRESCRITA. As datas usam ResolverStyle.STRICT para recusar
 * datas impossiveis como 31/02/2026 (correcao registrada na documentacao).
 */
public abstract class EntradaBase implements Entrada {

    // "uuuu" (e nao "yyyy") e obrigatorio com ResolverStyle.STRICT
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    @Override
    public abstract String lerLinha(String rotulo);

    @Override
    public String lerTexto(String rotulo) {
        while (true) {
            String valor = lerLinha(rotulo + ": ").trim();
            if (!valor.isEmpty()) {
                return valor;
            }
            avisar("Campo obrigatorio.");
        }
    }

    @Override
    public String lerTexto(String rotulo, String padrao) {
        String dica = padrao == null ? "[Enter = pular]" : "[Enter = " + padrao + "]";
        String valor = lerLinha(rotulo + " " + dica + ": ").trim();
        return valor.isEmpty() ? padrao : valor;
    }

    @Override
    public String lerSenha(String rotulo) {
        return lerTexto(rotulo);
    }

    @Override
    public int lerInteiro(String rotulo) {
        while (true) {
            String valor = lerLinha(rotulo + ": ").trim();
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                avisar("Digite um numero inteiro.");
            }
        }
    }

    @Override
    public int lerInteiro(String rotulo, int minimo, int maximo) {
        while (true) {
            int valor = lerInteiro(rotulo + " (" + minimo + " a " + maximo + ")");
            if (valor >= minimo && valor <= maximo) {
                return valor;
            }
            avisar("Digite um numero entre " + minimo + " e " + maximo + ".");
        }
    }

    @Override
    public Integer lerInteiroOpcional(String rotulo) {
        return lerInteiroOpcional(rotulo, "todos");
    }

    @Override
    public Integer lerInteiroOpcional(String rotulo, String dicaEnter) {
        while (true) {
            String valor = lerLinha(rotulo + " [Enter = " + dicaEnter + "]: ").trim();
            if (valor.isEmpty()) {
                return null;
            }
            try {
                return Integer.valueOf(valor);
            } catch (NumberFormatException e) {
                avisar("Digite um numero inteiro ou apenas Enter.");
            }
        }
    }

    @Override
    public BigDecimal lerDecimal(String rotulo) {
        while (true) {
            BigDecimal valor = converterDecimal(lerLinha(rotulo + ": "));
            if (valor != null) {
                return valor;
            }
            avisar("Digite um numero. Exemplo: 12,5");
        }
    }

    @Override
    public BigDecimal lerDecimalOpcional(String rotulo) {
        while (true) {
            String texto = lerLinha(rotulo + " [Enter = pular]: ").trim();
            if (texto.isEmpty()) {
                return null;
            }
            BigDecimal valor = converterDecimal(texto);
            if (valor != null) {
                return valor;
            }
            avisar("Digite um numero ou apenas Enter.");
        }
    }

    /** Aceita virgula ou ponto como separador decimal. */
    protected static BigDecimal converterDecimal(String texto) {
        try {
            return new BigDecimal(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public LocalDate lerData(String rotulo) {
        while (true) {
            String texto = lerLinha(rotulo + " (dd/mm/aaaa): ").trim();
            try {
                return LocalDate.parse(texto, FORMATO_DATA);
            } catch (DateTimeParseException e) {
                avisar("Data invalida. Use dd/mm/aaaa, por exemplo 25/12/2026.");
            }
        }
    }

    @Override
    public LocalDate lerDataOpcional(String rotulo) {
        while (true) {
            String texto = lerLinha(rotulo + " (dd/mm/aaaa) [Enter = todos]: ").trim();
            if (texto.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(texto, FORMATO_DATA);
            } catch (DateTimeParseException e) {
                avisar("Data invalida. Use dd/mm/aaaa ou apenas Enter.");
            }
        }
    }

    @Override
    public LocalDateTime lerDataHora(String rotulo, LocalDateTime padrao) {
        String dica = padrao == null ? "" : " [Enter = agora]";
        while (true) {
            String texto = lerLinha(rotulo + " (dd/mm/aaaa hh:mm)" + dica + ": ").trim();
            if (texto.isEmpty() && padrao != null) {
                return padrao;
            }
            try {
                return LocalDateTime.parse(texto, FORMATO_DATA_HORA);
            } catch (DateTimeParseException e) {
                avisar("Data/hora invalida. Exemplo: 20/10/2026 07:30");
            }
        }
    }

    @Override
    public boolean confirmar(String pergunta) {
        while (true) {
            String valor = lerLinha(pergunta + " (s/n): ").trim().toLowerCase();
            if (valor.equals("s") || valor.equals("sim")) {
                return true;
            }
            if (valor.equals("n") || valor.equals("nao") || valor.equals("não")) {
                return false;
            }
            avisar("Responda s ou n.");
        }
    }

    @Override
    public String lerOpcao(String rotulo, String... opcoes) {
        String lista = String.join("/", opcoes);
        while (true) {
            String valor = lerLinha(rotulo + " (" + lista + "): ").trim();
            for (String opcao : opcoes) {
                if (opcao.equalsIgnoreCase(valor)) {
                    return opcao;
                }
            }
            avisar("Opcao invalida. Use: " + lista);
        }
    }
}
