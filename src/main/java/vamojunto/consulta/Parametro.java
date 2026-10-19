package vamojunto.consulta;

import java.sql.Types;
import vamojunto.console.Entrada;

/**
 * Um parametro de consulta: sabe como pedir o valor ao usuario e qual o
 * tipo SQL dele. Parametro opcional + Enter = NULL ("sem filtro" nas procedures).
 *
 * POO: ENCAPSULAMENTO, ENUM interno e SOBRECARGA DE CONSTRUTOR.
 */
public class Parametro {

    /** Tipos de dado aceitos, cada um com o seu tipo JDBC (java.sql.Types). */
    public enum Tipo {
        TEXTO(Types.VARCHAR),
        INTEIRO(Types.INTEGER),
        DECIMAL(Types.DECIMAL),
        DATA(Types.DATE),
        OPCAO(Types.VARCHAR);

        private final int tipoSql;

        Tipo(int tipoSql) {
            this.tipoSql = tipoSql;
        }

        public int getTipoSql() {
            return tipoSql;
        }
    }

    private final String rotulo;
    private final Tipo tipo;
    private final boolean obrigatorio;
    private final String[] opcoes;

    /** Parametro opcional (Enter = sem filtro). */
    public Parametro(String rotulo, Tipo tipo) {
        this(rotulo, tipo, false);
    }

    public Parametro(String rotulo, Tipo tipo, boolean obrigatorio) {
        this.rotulo = rotulo;
        this.tipo = tipo;
        this.obrigatorio = obrigatorio;
        this.opcoes = new String[0];
    }

    /** Sobrecarga: parametro de escolha entre opcoes fixas (sempre obrigatorio). */
    public Parametro(String rotulo, String... opcoes) {
        this.rotulo = rotulo;
        this.tipo = Tipo.OPCAO;
        this.obrigatorio = true;
        this.opcoes = opcoes;
    }

    /** Pede o valor ao usuario de acordo com o tipo. Pode devolver null (sem filtro). */
    public Object ler(Entrada entrada) {
        switch (tipo) {
            case INTEIRO:
                return obrigatorio ? Integer.valueOf(entrada.lerInteiro(rotulo)) : entrada.lerInteiroOpcional(rotulo);
            case DECIMAL:
                return obrigatorio ? entrada.lerDecimal(rotulo) : entrada.lerDecimalOpcional(rotulo);
            case DATA:
                return obrigatorio
                        ? java.sql.Date.valueOf(entrada.lerData(rotulo))
                        : converterData(entrada.lerDataOpcional(rotulo));
            case OPCAO:
                return entrada.lerOpcao(rotulo, opcoes);
            case TEXTO:
            default:
                if (obrigatorio) {
                    return entrada.lerTexto(rotulo);
                }
                String texto = entrada.lerLinha(rotulo + " [Enter = todos]: ").trim();
                return texto.isEmpty() ? null : texto;
        }
    }

    private static java.sql.Date converterData(java.time.LocalDate data) {
        return data == null ? null : java.sql.Date.valueOf(data);
    }

    public String getRotulo() {
        return rotulo;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public boolean isObrigatorio() {
        return obrigatorio;
    }
}
