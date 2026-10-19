package vamojunto.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import vamojunto.conexao.Conexao;
import vamojunto.modelo.AutorAvaliacao;
import vamojunto.modelo.Avaliacao;

/** Avaliacoes entre passageiro e motorista (RF11). */
public class AvaliacaoDAO {

    /** trg_avaliacao_bi garante: so carona ACEITA de viagem CONCLUIDA. */
    public int inserir(Avaliacao a) throws SQLException {
        String sql = "INSERT INTO Avaliacao (cod_solicitacao, autor, nota, comentario) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexao.obter();
             PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setInt(1, a.getCodSolicitacao());
            st.setString(2, a.getAutor().name());
            st.setInt(3, a.getNota());
            st.setString(4, a.getComentario());
            st.executeUpdate();
            try (ResultSet chave = st.getGeneratedKeys()) {
                chave.next();
                a.setCodigo(chave.getInt(1));
            }
        }
        return a.getCodigo();
    }

    /** sp_avaliacoes_pendentes: o que o usuario ainda precisa avaliar. */
    public List<Avaliacao> listarPendentes(int codUsuario) throws SQLException {
        List<Avaliacao> lista = new ArrayList<>();
        try (Connection con = Conexao.obter();
             CallableStatement cs = con.prepareCall("{CALL sp_avaliacoes_pendentes(?)}")) {
            cs.setInt(1, codUsuario);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Avaliacao a = new Avaliacao();
                    a.setCodSolicitacao(rs.getInt("cod_solicitacao"));
                    a.setAutor(AutorAvaliacao.valueOf(rs.getString("meu_papel")));
                    a.setNomeAvaliado(rs.getString("avaliar"));
                    a.setCodViagem(rs.getInt("cod_viagem"));
                    a.setDataViagem(rs.getString("data_viagem"));
                    lista.add(a);
                }
            }
        }
        return lista;
    }
}
