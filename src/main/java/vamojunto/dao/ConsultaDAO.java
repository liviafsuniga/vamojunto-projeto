package vamojunto.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import vamojunto.conexao.Conexao;
import vamojunto.console.TabelaConsole;

/**
 * Atalhos para consultas de exibicao: executa e ja imprime como tabela.
 * Usa PreparedStatement com "?" (nunca concatena o valor digitado no SQL,
 * o que evita SQL Injection - boa pratica de seguranca).
 */
public final class ConsultaDAO {

    private ConsultaDAO() {
    }

    /** SELECT com parametros (varargs). Devolve quantas linhas foram mostradas. */
    public static int imprimir(String sql, Object... parametros) throws SQLException {
        try (Connection con = Conexao.obter();
             PreparedStatement st = con.prepareStatement(sql)) {
            ligar(st, parametros);
            try (ResultSet rs = st.executeQuery()) {
                return TabelaConsole.imprimir(rs);
            }
        }
    }

    /** CALL de uma procedure de consulta. */
    public static int imprimirProcedure(String procedure, Object... parametros) throws SQLException {
        String marcadores = String.join(", ", Collections.nCopies(parametros.length, "?"));
        try (Connection con = Conexao.obter();
             CallableStatement cs = con.prepareCall("{CALL " + procedure + "(" + marcadores + ")}")) {
            ligar(cs, parametros);
            try (ResultSet rs = cs.executeQuery()) {
                return TabelaConsole.imprimir(rs);
            }
        }
    }

    /** Primeira coluna da primeira linha (ex.: SELECT fn_x(?)). */
    public static Object valor(String sql, Object... parametros) throws SQLException {
        try (Connection con = Conexao.obter();
             PreparedStatement st = con.prepareStatement(sql)) {
            ligar(st, parametros);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? rs.getObject(1) : null;
            }
        }
    }

    static void ligar(PreparedStatement st, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            if (parametros[i] == null) {
                st.setNull(i + 1, Types.VARCHAR);
            } else {
                st.setObject(i + 1, parametros[i]);
            }
        }
    }
}
