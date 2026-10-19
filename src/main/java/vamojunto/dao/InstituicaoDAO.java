package vamojunto.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import vamojunto.conexao.Conexao;
import vamojunto.modelo.Instituicao;

public class InstituicaoDAO implements Dao<Instituicao> {

    @Override
    public Instituicao buscarPorCodigo(int codigo) throws SQLException {
        String sql = "SELECT * FROM Instituicao WHERE cod_instituicao = ?";
        try (Connection con = Conexao.obter(); PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, codigo);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    @Override
    public List<Instituicao> listar() throws SQLException {
        List<Instituicao> lista = new ArrayList<>();
        try (Connection con = Conexao.obter();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Instituicao ORDER BY sigla, nome")) {
            while (rs.next()) {
                lista.add(montar(rs));
            }
        }
        return lista;
    }

    @Override
    public int inserir(Instituicao i) throws SQLException {
        String sql = "INSERT INTO Instituicao (nome, sigla, cidade, estado) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexao.obter();
             PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, i.getNome());
            st.setString(2, i.getSigla());
            st.setString(3, i.getCidade());
            st.setString(4, i.getEstado());
            st.executeUpdate();
            try (ResultSet chave = st.getGeneratedKeys()) {
                chave.next();
                i.setCodigo(chave.getInt(1));
            }
        }
        return i.getCodigo();
    }

    private Instituicao montar(ResultSet rs) throws SQLException {
        return new Instituicao(rs.getInt("cod_instituicao"), rs.getString("nome"), rs.getString("sigla"),
                rs.getString("cidade"), rs.getString("estado"));
    }
}
