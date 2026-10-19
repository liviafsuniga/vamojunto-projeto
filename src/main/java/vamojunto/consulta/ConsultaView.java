package vamojunto.consulta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Consulta uma view: SELECT * FROM vw_nome [WHERE coluna = ?] [ORDER BY ...]
 * POO: HERANCA, SOBRESCRITA de preparar() e SOBRECARGA DE CONSTRUTOR
 * (com ou sem ordenacao, com ou sem filtro).
 */
public class ConsultaView extends Consulta {

    private final String view;
    private final String ordenacao;
    private final String colunaFiltro;

    public ConsultaView(String titulo, String view) {
        this(titulo, view, null);
    }

    public ConsultaView(String titulo, String view, String ordenacao) {
        super(titulo);
        this.view = view;
        this.ordenacao = ordenacao;
        this.colunaFiltro = null;
    }

    /** Sobrecarga: filtra uma coluna por igualdade com o valor digitado. */
    public ConsultaView(String titulo, String view, String ordenacao, String colunaFiltro, Parametro filtro) {
        super(titulo, filtro);
        this.view = view;
        this.ordenacao = ordenacao;
        this.colunaFiltro = colunaFiltro;
    }

    @Override
    protected PreparedStatement preparar(Connection con) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(view);
        if (colunaFiltro != null) {
            // coluna = ? OU parametro vazio (NULL) = sem filtro
            sql.append(" WHERE (? IS NULL OR ").append(colunaFiltro).append(" = ?)");
        }
        if (ordenacao != null) {
            sql.append(" ORDER BY ").append(ordenacao);
        }
        return con.prepareStatement(sql.toString());
    }

    /** O filtro usa o mesmo valor em dois "?", entao sobrescrevemos a ligacao. */
    @Override
    protected void definirValores(PreparedStatement st, java.util.List<Object> valores) throws SQLException {
        if (colunaFiltro == null) {
            return;
        }
        Object valor = valores.get(0);
        int tipoSql = getParametros().get(0).getTipo().getTipoSql();
        for (int i = 1; i <= 2; i++) {
            if (valor == null) {
                st.setNull(i, tipoSql);
            } else {
                st.setObject(i, valor);
            }
        }
    }
}
