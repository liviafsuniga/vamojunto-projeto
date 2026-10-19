package vamojunto.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import vamojunto.conexao.Conexao;
import vamojunto.modelo.CategoriaCnh;
import vamojunto.modelo.Instituicao;
import vamojunto.modelo.Motorista;
import vamojunto.modelo.Usuario;

/**
 * Acesso a Usuario e Motorista. Ao ler do banco, o DAO devolve um objeto
 * Motorista quando a pessoa tem CNH cadastrada, e Usuario caso contrario:
 * quem chama recebe sempre um "Usuario" (POLIMORFISMO).
 */
public class UsuarioDAO implements Dao<Usuario> {

    private static final String SELECT_BASE =
            "SELECT u.*, m.cnh, m.categoria_cnh, m.validade_cnh, m.ativo AS motorista_ativo, "
            + "       i.nome AS inst_nome, i.sigla AS inst_sigla, i.cidade AS inst_cidade, i.estado AS inst_estado "
            + "  FROM Usuario u "
            + "  LEFT JOIN Motorista m   ON m.cod_motorista   = u.cod_usuario "
            + "  LEFT JOIN Instituicao i ON i.cod_instituicao = u.cod_instituicao ";

    /** Numeros do perfil, calculados pelas functions do banco. */
    public static class Indicadores {
        public BigDecimal notaComoMotorista;
        public BigDecimal notaComoPassageiro;
        public BigDecimal totalGasto;
        public int caronasConcluidas;
        public int viagensConcluidas;
    }

    @Override
    public Usuario buscarPorCodigo(int codigo) throws SQLException {
        return buscarUm(SELECT_BASE + "WHERE u.cod_usuario = ?", codigo);
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        return buscarUm(SELECT_BASE + "WHERE u.email = ?", email.trim().toLowerCase());
    }

    @Override
    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = Conexao.obter();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE + "ORDER BY u.nome")) {
            while (rs.next()) {
                lista.add(montar(rs));
            }
        }
        return lista;
    }

    /** RF01 - login: busca pelo e-mail e confere o hash da senha no objeto. */
    public Usuario autenticar(String email, String senha) throws SQLException {
        Usuario u = buscarPorEmail(email);
        return (u != null && u.autenticar(senha)) ? u : null;
    }

    /** RF01 - cadastro. A trigger trg_usuario_bi confere de novo os 18 anos. */
    @Override
    public int inserir(Usuario u) throws SQLException {
        String sql = "INSERT INTO Usuario (cod_instituicao, nome, cpf, email, telefone, senha_hash, dt_nascimento) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexao.obter();
             PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (u.getInstituicao() == null) {
                st.setNull(1, Types.INTEGER);
            } else {
                st.setInt(1, u.getInstituicao().getCodigo());
            }
            st.setString(2, u.getNome());
            st.setString(3, u.getCpf());
            st.setString(4, u.getEmail());
            st.setString(5, u.getTelefone());
            st.setString(6, u.getSenhaHash());
            st.setDate(7, Date.valueOf(u.getDataNascimento()));
            st.executeUpdate();
            try (ResultSet chave = st.getGeneratedKeys()) {
                chave.next();
                u.setCodigo(chave.getInt(1));
            }
        }
        return u.getCodigo();
    }

    /** RF02 - torna o usuario motorista (trg_motorista_bi exige verificado e ativo). */
    public void inserirMotorista(Motorista m) throws SQLException {
        String sql = "INSERT INTO Motorista (cod_motorista, cnh, categoria_cnh, validade_cnh) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexao.obter(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, m.getCodigo());
            st.setString(2, m.getCnh());
            st.setString(3, m.getCategoriaCnh().name());
            st.setDate(4, Date.valueOf(m.getValidadeCnh()));
            st.executeUpdate();
        }
    }

    /** Administrador confirma os documentos do usuario. */
    public boolean verificar(int codUsuario) throws SQLException {
        String sql = "UPDATE Usuario SET verificado = 1, dt_verificacao = NOW() "
                + "WHERE cod_usuario = ? AND verificado = 0";
        try (Connection con = Conexao.obter(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, codUsuario);
            return st.executeUpdate() == 1;
        }
    }

    /**
     * Ativa/desativa a conta (no lugar de DELETE, para preservar o historico).
     * Dois UPDATEs na MESMA transacao JDBC: ou muda os dois, ou nenhum.
     */
    public void alterarAtivo(int codUsuario, boolean ativo) throws SQLException {
        try (Connection con = Conexao.obter()) {
            con.setAutoCommit(false);
            try (PreparedStatement u = con.prepareStatement("UPDATE Usuario SET ativo = ? WHERE cod_usuario = ?");
                 PreparedStatement m = con.prepareStatement("UPDATE Motorista SET ativo = ? WHERE cod_motorista = ?")) {
                u.setBoolean(1, ativo);
                u.setInt(2, codUsuario);
                if (u.executeUpdate() != 1) {
                    throw new SQLException("Usuario nao encontrado.", "45000");
                }
                m.setBoolean(1, ativo);
                m.setInt(2, codUsuario);
                m.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public void alterarTelefone(int codUsuario, String telefone) throws SQLException {
        executarUpdate("UPDATE Usuario SET telefone = ? WHERE cod_usuario = ?", telefone, codUsuario);
    }

    public void alterarSenha(int codUsuario, String senhaHash) throws SQLException {
        executarUpdate("UPDATE Usuario SET senha_hash = ? WHERE cod_usuario = ?", senhaHash, codUsuario);
    }

    /** Chama varias functions do banco numa unica consulta. */
    public Indicadores buscarIndicadores(int codUsuario) throws SQLException {
        String sql = "SELECT fn_media_avaliacao_motorista(?) AS nota_motorista, "
                + "       fn_media_avaliacao_passageiro(?) AS nota_passageiro, "
                + "       fn_total_gasto_passageiro(?)     AS total_gasto, "
                + "       (SELECT COUNT(*) FROM Solicitacao s JOIN Viagem v ON v.cod_viagem = s.cod_viagem "
                + "         WHERE s.cod_passageiro = ? AND s.status = 'ACEITA' AND v.status = 'CONCLUIDA') AS caronas, "
                + "       (SELECT COUNT(*) FROM Viagem WHERE cod_motorista = ? AND status = 'CONCLUIDA') AS viagens";
        try (Connection con = Conexao.obter(); PreparedStatement st = con.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                st.setInt(i, codUsuario);
            }
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                Indicadores ind = new Indicadores();
                ind.notaComoMotorista = rs.getBigDecimal("nota_motorista");
                ind.notaComoPassageiro = rs.getBigDecimal("nota_passageiro");
                ind.totalGasto = rs.getBigDecimal("total_gasto");
                ind.caronasConcluidas = rs.getInt("caronas");
                ind.viagensConcluidas = rs.getInt("viagens");
                return ind;
            }
        }
    }

    // ------------------------------------------------------------------

    private Usuario buscarUm(String sql, Object parametro) throws SQLException {
        try (Connection con = Conexao.obter(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setObject(1, parametro);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    private void executarUpdate(String sql, Object valor, int codigo) throws SQLException {
        try (Connection con = Conexao.obter(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setObject(1, valor);
            st.setInt(2, codigo);
            st.executeUpdate();
        }
    }

    /** Monta Usuario ou Motorista a partir da linha do ResultSet. */
    private Usuario montar(ResultSet rs) throws SQLException {
        Usuario u;
        if (rs.getString("cnh") != null) {
            Motorista m = new Motorista();
            m.setCnh(rs.getString("cnh"));
            m.setCategoriaCnh(CategoriaCnh.deTexto(rs.getString("categoria_cnh")));
            m.setValidadeCnh(rs.getObject("validade_cnh", LocalDate.class));
            m.setMotoristaAtivo(rs.getBoolean("motorista_ativo"));
            u = m;
        } else {
            u = new Usuario();
        }
        u.setCodigo(rs.getInt("cod_usuario"));
        u.setNome(rs.getString("nome"));
        u.setCpf(rs.getString("cpf"));
        u.setEmail(rs.getString("email"));
        u.setTelefone(rs.getString("telefone"));
        u.setSenhaHash(rs.getString("senha_hash"));
        u.carregarDataNascimento(rs.getObject("dt_nascimento", LocalDate.class));
        u.setVerificado(rs.getBoolean("verificado"));
        u.setDataVerificacao(rs.getObject("dt_verificacao", LocalDateTime.class));
        u.setDataCadastro(rs.getObject("dt_cadastro", LocalDateTime.class));
        u.setAtivo(rs.getBoolean("ativo"));
        int codInst = rs.getInt("cod_instituicao");
        if (!rs.wasNull()) {
            u.setInstituicao(new Instituicao(codInst, rs.getString("inst_nome"), rs.getString("inst_sigla"),
                    rs.getString("inst_cidade"), rs.getString("inst_estado")));
        }
        return u;
    }
}
