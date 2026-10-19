package vamojunto.consulta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import vamojunto.conexao.Conexao;
import vamojunto.console.OpcaoMenu;
import vamojunto.console.TabelaConsole;
import vamojunto.console.Terminal;

/**
 * Base de toda consulta do menu: pede os parametros, executa no banco e
 * imprime o resultado.
 *
 * POO: CLASSE ABSTRATA (nao pode ser instanciada), ENCAPSULAMENTO e
 * SOBRECARGA DE CONSTRUTOR. Padrao de projeto TEMPLATE METHOD: executar()
 * define o roteiro fixo e as subclasses so preenchem preparar() e,
 * se quiserem, mostrarResultados().
 */
public abstract class Consulta implements OpcaoMenu {

    private final String titulo;
    private final List<Parametro> parametros = new ArrayList<>();

    protected Consulta(String titulo) {
        this.titulo = titulo;
    }

    protected Consulta(String titulo, Parametro... parametros) {
        this(titulo);
        this.parametros.addAll(Arrays.asList(parametros));
    }

    @Override
    public String getTitulo() {
        return titulo;
    }

    protected List<Parametro> getParametros() {
        return parametros;
    }

    /** Roteiro fixo (template method). final: as subclasses nao podem mudar. */
    @Override
    public final void executar() throws SQLException {
        Terminal.titulo(titulo);
        List<Object> valores = new ArrayList<>();
        for (Parametro p : parametros) {
            valores.add(p.ler(Terminal.entrada()));
        }
        if (!confirmar(valores)) {
            Terminal.info("Operacao cancelada.");
            return;
        }
        try (Connection con = Conexao.obter();
             PreparedStatement st = preparar(con)) {
            definirValores(st, valores);
            boolean temResultado = st.execute();
            mostrarResultados(st, temResultado);
        }
    }

    /** Cada subclasse monta o comando SQL do seu jeito. */
    protected abstract PreparedStatement preparar(Connection con) throws SQLException;

    /** Por padrao imprime a tabela de resultado. OperacaoProcedure sobrescreve. */
    protected void mostrarResultados(PreparedStatement st, boolean temResultado) throws SQLException {
        if (!temResultado) {
            Terminal.info("Nenhum resultado retornado.");
            return;
        }
        try (ResultSet rs = st.getResultSet()) {
            TabelaConsole.imprimir(rs);
        }
    }

    /** Gancho opcional antes de executar (operacoes pedem confirmacao). */
    protected boolean confirmar(List<Object> valores) {
        return true;
    }

    /** Liga cada valor ao seu "?" (setObject) ou manda NULL com o tipo certo. */
    protected void definirValores(PreparedStatement st, List<Object> valores) throws SQLException {
        for (int i = 0; i < valores.size(); i++) {
            Object valor = valores.get(i);
            if (valor == null) {
                st.setNull(i + 1, parametros.get(i).getTipo().getTipoSql());
            } else {
                st.setObject(i + 1, valor);
            }
        }
    }

    /** "?, ?, ?" com a quantidade de parametros. */
    protected String marcadores() {
        return String.join(", ", java.util.Collections.nCopies(parametros.size(), "?"));
    }
}
