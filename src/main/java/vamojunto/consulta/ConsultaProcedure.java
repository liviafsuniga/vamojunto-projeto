package vamojunto.consulta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Consulta que chama uma stored procedure: {CALL sp_nome(?, ?)}
 * POO: HERANCA (extends Consulta) e SOBRESCRITA de preparar().
 */
public class ConsultaProcedure extends Consulta {

    private final String procedure;

    public ConsultaProcedure(String titulo, String procedure, Parametro... parametros) {
        super(titulo, parametros);
        this.procedure = procedure;
    }

    public String getProcedure() {
        return procedure;
    }

    @Override
    protected PreparedStatement preparar(Connection con) throws SQLException {
        // CallableStatement e um PreparedStatement especializado para procedures
        return con.prepareCall("{CALL " + procedure + "(" + marcadores() + ")}");
    }
}
