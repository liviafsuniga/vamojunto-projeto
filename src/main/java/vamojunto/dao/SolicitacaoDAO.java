package vamojunto.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import vamojunto.conexao.Conexao;
import vamojunto.modelo.Motorista;
import vamojunto.modelo.Ponto;
import vamojunto.modelo.Solicitacao;
import vamojunto.modelo.StatusSolicitacao;
import vamojunto.modelo.StatusViagem;
import vamojunto.modelo.TipoPonto;
import vamojunto.modelo.Usuario;
import vamojunto.modelo.Viagem;

/** Pedidos de carona (RF07, RF08). */
public class SolicitacaoDAO {

    private static final String SELECT_BASE =
            "SELECT s.*, v.cod_motorista, v.status AS status_viagem, v.dt_inicio, v.distancia_km, v.custo_total, "
            + "       up.nome AS passageiro_nome, um.nome AS motorista_nome, "
            + "       p.tipo AS ponto_tipo, p.ordem AS ponto_ordem, p.logradouro, p.numero, p.bairro, "
            + "       p.cidade, p.estado, "
            + "       pd.logradouro AS dest_logradouro, pd.numero AS dest_numero, pd.bairro AS dest_bairro, "
            + "       pd.cidade AS dest_cidade, pd.estado AS dest_estado "
            + "  FROM Solicitacao s "
            + "  JOIN Viagem v   ON v.cod_viagem   = s.cod_viagem "
            + "  JOIN Usuario up ON up.cod_usuario = s.cod_passageiro "
            + "  JOIN Usuario um ON um.cod_usuario = v.cod_motorista "
            + "  JOIN Ponto p    ON p.cod_ponto    = s.cod_ponto "
            + "  JOIN Ponto pd   ON pd.cod_viagem  = s.cod_viagem AND pd.tipo = 'DESTINO' ";

    /** RF07: a trigger trg_solicitacao_bi garante que o pedido entra PENDENTE. */
    public int inserir(Solicitacao s) throws SQLException {
        String sql = "INSERT INTO Solicitacao (cod_viagem, cod_passageiro, cod_ponto, distancia_trecho_km) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection con = Conexao.obter();
             PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setInt(1, s.getViagem().getCodigo());
            st.setInt(2, s.getPassageiro().getCodigo());
            st.setInt(3, s.getPontoEmbarque().getCodigo());
            st.setBigDecimal(4, s.getDistanciaTrechoKm());
            st.executeUpdate();
            try (ResultSet chave = st.getGeneratedKeys()) {
                chave.next();
                s.setCodigo(chave.getInt(1));
            }
        }
        return s.getCodigo();
    }

    public Solicitacao buscarPorCodigo(int codigo) throws SQLException {
        List<Solicitacao> lista = listar("WHERE s.cod_solicitacao = ?", codigo);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** "Minhas solicitacoes": todos os pedidos do passageiro. */
    public List<Solicitacao> listarDoPassageiro(int codPassageiro) throws SQLException {
        return listar("WHERE s.cod_passageiro = ? ORDER BY v.dt_inicio DESC", codPassageiro);
    }

    /** Pedidos pendentes nas viagens agendadas de um motorista. */
    public List<Solicitacao> listarPendentesDoMotorista(int codMotorista) throws SQLException {
        return listar("WHERE v.cod_motorista = ? AND s.status = 'PENDENTE' AND v.status = 'AGENDADA' "
                + "ORDER BY v.dt_inicio, s.dt_solicitacao", codMotorista);
    }

    /** RF08: procedure com transacao e FOR UPDATE (evita dois aceites na mesma vaga). */
    public void responder(int codSolicitacao, StatusSolicitacao resposta) throws SQLException {
        try (Connection con = Conexao.obter();
             CallableStatement cs = con.prepareCall("{CALL sp_responder_solicitacao(?, ?)}")) {
            cs.setInt(1, codSolicitacao);
            cs.setString(2, resposta.name());
            cs.execute();
        }
    }

    /** Passageiro desiste: a vaga e liberada e o rateio e redividido. */
    public void cancelar(int codSolicitacao) throws SQLException {
        try (Connection con = Conexao.obter();
             CallableStatement cs = con.prepareCall("{CALL sp_cancelar_solicitacao(?)}")) {
            cs.setInt(1, codSolicitacao);
            cs.execute();
        }
    }

    // ------------------------------------------------------------------

    private List<Solicitacao> listar(String filtro, int parametro) throws SQLException {
        List<Solicitacao> lista = new ArrayList<>();
        try (Connection con = Conexao.obter(); PreparedStatement st = con.prepareStatement(SELECT_BASE + filtro)) {
            st.setInt(1, parametro);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
        }
        return lista;
    }

    private Solicitacao montar(ResultSet rs) throws SQLException {
        Motorista motorista = new Motorista();
        motorista.setCodigo(rs.getInt("cod_motorista"));
        motorista.setNome(rs.getString("motorista_nome"));

        Viagem v = new Viagem();
        v.setCodigo(rs.getInt("cod_viagem"));
        v.setMotorista(motorista);
        v.setStatus(StatusViagem.valueOf(rs.getString("status_viagem")));
        v.setDataInicio(rs.getObject("dt_inicio", LocalDateTime.class));
        v.setDistanciaKm(rs.getBigDecimal("distancia_km"));
        v.setCustoTotal(rs.getBigDecimal("custo_total"));
        // o destino da viagem (onde todo passageiro desce)
        v.carregarPonto(new Ponto(TipoPonto.DESTINO, rs.getString("dest_logradouro"), rs.getString("dest_numero"),
                rs.getString("dest_bairro"), rs.getString("dest_cidade"), rs.getString("dest_estado")));

        Usuario passageiro = new Usuario();
        passageiro.setCodigo(rs.getInt("cod_passageiro"));
        passageiro.setNome(rs.getString("passageiro_nome"));

        Ponto embarque = new Ponto(TipoPonto.valueOf(rs.getString("ponto_tipo")), rs.getString("logradouro"),
                rs.getString("numero"), rs.getString("bairro"), rs.getString("cidade"), rs.getString("estado"));
        embarque.setCodigo(rs.getInt("cod_ponto"));
        embarque.setOrdem(rs.getInt("ponto_ordem"));

        Solicitacao s = new Solicitacao(v, passageiro, embarque,
                rs.getBigDecimal("distancia_trecho_km"));
        s.setCodigo(rs.getInt("cod_solicitacao"));
        s.setStatus(StatusSolicitacao.valueOf(rs.getString("status")));
        s.setDataSolicitacao(rs.getObject("dt_solicitacao", LocalDateTime.class));
        s.setDataResposta(rs.getObject("dt_resposta", LocalDateTime.class));
        s.setValorRateio(rs.getBigDecimal("valor_rateio"));
        return s;
    }
}
