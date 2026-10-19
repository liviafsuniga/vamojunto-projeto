package vamojunto.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import vamojunto.conexao.Conexao;
import vamojunto.modelo.Combustivel;
import vamojunto.modelo.TipoCombustivel;

public class CombustivelDAO implements Dao<Combustivel> {

    @Override
    public Combustivel buscarPorCodigo(int codigo) throws SQLException {
        String sql = "SELECT * FROM Combustivel WHERE cod_combustivel = ?";
        try (Connection con = Conexao.obter(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, codigo);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    @Override
    public List<Combustivel> listar() throws SQLException {
        List<Combustivel> lista = new ArrayList<>();
        try (Connection con = Conexao.obter();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Combustivel ORDER BY cod_combustivel")) {
            while (rs.next()) {
                lista.add(montar(rs));
            }
        }
        return lista;
    }

    @Override
    public int inserir(Combustivel c) throws SQLException {
        String sql = "INSERT INTO Combustivel (tipo, preco_litro) VALUES (?, ?)";
        try (Connection con = Conexao.obter();
             PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, c.getTipo().name());
            st.setBigDecimal(2, c.getPrecoLitro());
            st.executeUpdate();
            try (ResultSet chave = st.getGeneratedKeys()) {
                chave.next();
                c.setCodigo(chave.getInt(1));
            }
        }
        return c.getCodigo();
    }

    /** DML UPDATE. Viagens ja criadas mantem o preco "congelado". */
    public boolean atualizarPreco(int codigo, BigDecimal novoPreco) throws SQLException {
        String sql = "UPDATE Combustivel SET preco_litro = ? WHERE cod_combustivel = ?";
        try (Connection con = Conexao.obter(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setBigDecimal(1, novoPreco);
            st.setInt(2, codigo);
            return st.executeUpdate() == 1;
        }
    }

    private Combustivel montar(ResultSet rs) throws SQLException {
        Combustivel c = new Combustivel(rs.getInt("cod_combustivel"),
                TipoCombustivel.valueOf(rs.getString("tipo")), rs.getBigDecimal("preco_litro"));
        c.setDataAtualizacao(rs.getObject("dt_atualizacao", LocalDateTime.class));
        return c;
    }
}
