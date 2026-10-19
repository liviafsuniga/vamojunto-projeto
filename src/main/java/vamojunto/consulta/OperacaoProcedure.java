package vamojunto.consulta;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import vamojunto.console.Terminal;

/**
 * Procedure que ALTERA dados (com transacao dentro do banco), por exemplo
 * sp_responder_solicitacao e sp_cancelar_solicitacao.
 *
 * POO: HERANCA EM DOIS NIVEIS (Consulta -> ConsultaProcedure -> OperacaoProcedure)
 * e SOBRESCRITA de mostrarResultados() e confirmar().
 */
public class OperacaoProcedure extends ConsultaProcedure {

    private final String mensagemSucesso;

    public OperacaoProcedure(String titulo, String procedure, String mensagemSucesso, Parametro... parametros) {
        super(titulo, procedure, parametros);
        this.mensagemSucesso = mensagemSucesso;
    }

    @Override
    protected boolean confirmar(List<Object> valores) {
        return Terminal.entrada().confirmar("Confirma a operacao?");
    }

    @Override
    protected void mostrarResultados(PreparedStatement st, boolean temResultado) throws SQLException {
        // se a procedure der erro (SIGNAL), o banco faz ROLLBACK e nem chegamos aqui
        Terminal.sucesso(mensagemSucesso);
    }
}
