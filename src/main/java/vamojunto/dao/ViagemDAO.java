package vamojunto.dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import vamojunto.conexao.Conexao;
import vamojunto.modelo.Combustivel;
import vamojunto.modelo.Motorista;
import vamojunto.modelo.Ponto;
import vamojunto.modelo.StatusViagem;
import vamojunto.modelo.TipoCombustivel;
import vamojunto.modelo.TipoPonto;
import vamojunto.modelo.Veiculo;
import vamojunto.modelo.Viagem;

/**
 * Viagem + rota (Pontos).
 * - inserir(): TRANSACAO JDBC (setAutoCommit(false) / commit / rollback):
 *   a viagem e todos os pontos sao gravados juntos ou nada e gravado.
 * - iniciar/concluir/cancelar: CallableStatement das procedures com transacao.
 */
public class ViagemDAO {

    private static final String SELECT_BASE =
            "SELECT v.*, u.nome AS motorista_nome, "
            + "       ve.marca, ve.modelo, ve.cor, ve.placa, ve.ano, ve.renavam, ve.qtd_lugares, "
            + "       c.tipo AS combustivel_tipo, c.preco_litro "
            + "  FROM Viagem v "
            + "  JOIN Usuario u     ON u.cod_usuario      = v.cod_motorista "
            + "  JOIN Veiculo ve    ON ve.cod_veiculo     = v.cod_veiculo "
            + "  JOIN Combustivel c ON c.cod_combustivel  = v.cod_combustivel ";

    /** RF04: grava a viagem com a rota completa (origem, paradas e destino). */
    public int inserir(Viagem v) throws SQLException {
        String sqlViagem = "INSERT INTO Viagem (cod_motorista, cod_veiculo, cod_combustivel, dt_inicio, "
                + "vagas, distancia_km, consumo_aplicado_km_l) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexao.obter()) {
            con.setAutoCommit(false);  // inicio da transacao
            try {
                int codigo;
                try (PreparedStatement st = con.prepareStatement(sqlViagem, Statement.RETURN_GENERATED_KEYS)) {
                    st.setInt(1, v.getMotorista().getCodigo());
                    st.setInt(2, v.getVeiculo().getCodigo());
                    st.setInt(3, v.getCombustivel().getCodigo());
                    st.setTimestamp(4, Timestamp.valueOf(v.getDataInicio()));
                    st.setInt(5, v.getVagas());
                    st.setBigDecimal(6, v.getDistanciaKm());
                    st.setBigDecimal(7, v.getConsumoKmL());
                    st.executeUpdate();
                    try (ResultSet chave = st.getGeneratedKeys()) {
                        chave.next();
                        codigo = chave.getInt(1);
                    }
                }
                for (Ponto p : v.getPontos()) {
                    inserirPonto(con, codigo, p);
                }
                con.commit();         // deu tudo certo: grava
                v.setCodigo(codigo);
                return codigo;
            } catch (SQLException e) {
                con.rollback();       // algum erro: desfaz a viagem e os pontos
                throw e;
            }
        }
    }

    /**
     * Inclui uma parada antes do DESTINO numa viagem agendada:
     * o destino "anda" uma posicao e a parada entra no lugar dele (transacao).
     */
    public void adicionarParada(int codViagem, Ponto parada) throws SQLException {
        try (Connection con = Conexao.obter()) {
            con.setAutoCommit(false);
            try {
                int codDestino;
                int ordemDestino;
                String sql = "SELECT cod_ponto, ordem FROM Ponto WHERE cod_viagem = ? AND tipo = 'DESTINO' FOR UPDATE";
                try (PreparedStatement st = con.prepareStatement(sql)) {
                    st.setInt(1, codViagem);
                    try (ResultSet rs = st.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Viagem sem DESTINO cadastrado.", "45000");
                        }
                        codDestino = rs.getInt("cod_ponto");
                        ordemDestino = rs.getInt("ordem");
                    }
                }
                try (PreparedStatement st = con.prepareStatement("UPDATE Ponto SET ordem = ordem + 1 WHERE cod_ponto = ?")) {
                    st.setInt(1, codDestino);
                    st.executeUpdate();
                }
                parada.setOrdem(ordemDestino);
                inserirPonto(con, codViagem, parada);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private void inserirPonto(Connection con, int codViagem, Ponto p) throws SQLException {
        String sql = "INSERT INTO Ponto (cod_viagem, ordem, tipo, logradouro, numero, bairro, cep, cidade, "
                + "estado, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setInt(1, codViagem);
            st.setInt(2, p.getOrdem());
            st.setString(3, p.getTipo().name());
            st.setString(4, p.getLogradouro());
            st.setString(5, p.getNumero());
            st.setString(6, p.getBairro());
            st.setString(7, p.getCep());
            st.setString(8, p.getCidade());
            st.setString(9, p.getEstado());
            setDecimalOuNulo(st, 10, p.getLatitude());
            setDecimalOuNulo(st, 11, p.getLongitude());
            st.executeUpdate();
            try (ResultSet chave = st.getGeneratedKeys()) {
                chave.next();
                p.setCodigo(chave.getInt(1));
                p.setCodViagem(codViagem);
            }
        }
    }

    private static void setDecimalOuNulo(PreparedStatement st, int indice, BigDecimal valor) throws SQLException {
        if (valor == null) {
            st.setNull(indice, Types.DECIMAL);
        } else {
            st.setBigDecimal(indice, valor);
        }
    }

    public Viagem buscarPorCodigo(int codigo) throws SQLException {
        try (Connection con = Conexao.obter();
             PreparedStatement st = con.prepareStatement(SELECT_BASE + "WHERE v.cod_viagem = ?")) {
            st.setInt(1, codigo);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Viagem v = montar(rs);
                carregarPontos(con, v);
                return v;
            }
        }
    }

    /** Viagens do motorista, mais recentes primeiro (lista de objetos). */
    public List<Viagem> listarDoMotorista(int codMotorista) throws SQLException {
        List<Viagem> lista = new ArrayList<>();
        try (Connection con = Conexao.obter();
             PreparedStatement st = con.prepareStatement(SELECT_BASE + "WHERE v.cod_motorista = ? ORDER BY v.dt_inicio DESC")) {
            st.setInt(1, codMotorista);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
            for (Viagem v : lista) {
                carregarPontos(con, v);
            }
        }
        return lista;
    }

    /** fn_trecho_estimado: km do ponto de embarque ate o destino. */
    public BigDecimal trechoEstimado(int codPonto) throws SQLException {
        Object valor = ConsultaDAO.valor("SELECT fn_trecho_estimado(?)", codPonto);
        return valor == null ? null : new BigDecimal(valor.toString());
    }

    public void iniciar(int codViagem, int codMotorista) throws SQLException {
        chamarOperacao("sp_iniciar_viagem", codViagem, codMotorista);
    }

    public void concluir(int codViagem, int codMotorista) throws SQLException {
        chamarOperacao("sp_concluir_viagem", codViagem, codMotorista);
    }

    public void cancelar(int codViagem, int codMotorista) throws SQLException {
        chamarOperacao("sp_cancelar_viagem", codViagem, codMotorista);
    }

    private void chamarOperacao(String procedure, int codViagem, int codMotorista) throws SQLException {
        try (Connection con = Conexao.obter();
             CallableStatement cs = con.prepareCall("{CALL " + procedure + "(?, ?)}")) {
            cs.setInt(1, codViagem);
            cs.setInt(2, codMotorista);
            cs.execute();
        }
    }

    // ------------------------------------------------------------------

    private void carregarPontos(Connection con, Viagem v) throws SQLException {
        try (PreparedStatement st = con.prepareStatement("SELECT * FROM Ponto WHERE cod_viagem = ? ORDER BY ordem")) {
            st.setInt(1, v.getCodigo());
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Ponto p = new Ponto(TipoPonto.valueOf(rs.getString("tipo")), rs.getString("logradouro"),
                            rs.getString("numero"), rs.getString("bairro"), rs.getString("cidade"),
                            rs.getString("estado"));
                    p.setCodigo(rs.getInt("cod_ponto"));
                    p.setCodViagem(v.getCodigo());
                    p.setOrdem(rs.getInt("ordem"));
                    p.setCep(rs.getString("cep"));
                    p.setLatitude(rs.getBigDecimal("latitude"));
                    p.setLongitude(rs.getBigDecimal("longitude"));
                    v.carregarPonto(p);
                }
            }
        }
    }

    private Viagem montar(ResultSet rs) throws SQLException {
        Motorista m = new Motorista();
        m.setCodigo(rs.getInt("cod_motorista"));
        m.setNome(rs.getString("motorista_nome"));

        Veiculo ve = new Veiculo(rs.getInt("cod_veiculo"), rs.getString("marca"), rs.getString("modelo"),
                rs.getString("cor"), rs.getString("placa"), rs.getInt("ano"), rs.getString("renavam"),
                rs.getInt("qtd_lugares"));
        Combustivel c = new Combustivel(rs.getInt("cod_combustivel"),
                TipoCombustivel.valueOf(rs.getString("combustivel_tipo")), rs.getBigDecimal("preco_litro"));

        Viagem v = new Viagem();
        v.setCodigo(rs.getInt("cod_viagem"));
        v.setMotorista(m);
        v.setVeiculo(ve);
        v.setCombustivel(c);
        v.setDataInicio(rs.getObject("dt_inicio", LocalDateTime.class));
        v.setDataFim(rs.getObject("dt_fim", LocalDateTime.class));
        v.setStatus(StatusViagem.valueOf(rs.getString("status")));
        v.setVagas(rs.getInt("vagas"));
        v.setDistanciaKm(rs.getBigDecimal("distancia_km"));
        v.setConsumoKmL(rs.getBigDecimal("consumo_aplicado_km_l"));
        v.setPrecoLitroAplicado(rs.getBigDecimal("preco_litro_aplicado"));
        v.setCustoTotal(rs.getBigDecimal("custo_total"));
        v.setQtdParticipantes(rs.getInt("qtd_participantes"));
        return v;
    }
}
