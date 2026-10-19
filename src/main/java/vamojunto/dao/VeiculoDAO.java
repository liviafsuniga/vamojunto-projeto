package vamojunto.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import vamojunto.conexao.Conexao;
import vamojunto.modelo.Veiculo;

/**
 * Observacao do modelo: no DER nao ha relacao Motorista-Veiculo, entao
 * qualquer motorista pode usar qualquer veiculo cadastrado (fiel ao DER).
 */
public class VeiculoDAO implements Dao<Veiculo> {

    @Override
    public Veiculo buscarPorCodigo(int codigo) throws SQLException {
        String sql = "SELECT * FROM Veiculo WHERE cod_veiculo = ?";
        try (Connection con = Conexao.obter(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, codigo);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    @Override
    public List<Veiculo> listar() throws SQLException {
        List<Veiculo> lista = new ArrayList<>();
        try (Connection con = Conexao.obter();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Veiculo ORDER BY marca, modelo")) {
            while (rs.next()) {
                lista.add(montar(rs));
            }
        }
        return lista;
    }

    @Override
    public int inserir(Veiculo v) throws SQLException {
        String sql = "INSERT INTO Veiculo (marca, modelo, cor, placa, ano, renavam, qtd_lugares) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexao.obter();
             PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, v.getMarca());
            st.setString(2, v.getModelo());
            st.setString(3, v.getCor());
            st.setString(4, v.getPlaca());
            st.setInt(5, v.getAno());
            st.setString(6, v.getRenavam());
            st.setInt(7, v.getQtdLugares());
            st.executeUpdate();
            try (ResultSet chave = st.getGeneratedKeys()) {
                chave.next();
                v.setCodigo(chave.getInt(1));
            }
        }
        return v.getCodigo();
    }

    private Veiculo montar(ResultSet rs) throws SQLException {
        return new Veiculo(rs.getInt("cod_veiculo"), rs.getString("marca"), rs.getString("modelo"),
                rs.getString("cor"), rs.getString("placa"), rs.getInt("ano"), rs.getString("renavam"),
                rs.getInt("qtd_lugares"));
    }
}
