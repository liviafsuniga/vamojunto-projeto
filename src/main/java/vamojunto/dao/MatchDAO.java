package vamojunto.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import vamojunto.conexao.Conexao;
import vamojunto.match.ResultadoMatch;

/** RF06: busca trajetos compativeis com a procedure sp_match_rotas. */
public class MatchDAO {

    public List<ResultadoMatch> buscar(String origem, String destino, LocalDate data, Integer codPassageiro)
            throws SQLException {
        List<ResultadoMatch> lista = new ArrayList<>();
        try (Connection con = Conexao.obter();
             CallableStatement cs = con.prepareCall("{CALL sp_match_rotas(?, ?, ?, ?)}")) {
            cs.setString(1, origem);
            cs.setString(2, destino);
            if (data == null) {
                cs.setNull(3, Types.DATE);
            } else {
                cs.setDate(3, java.sql.Date.valueOf(data));
            }
            if (codPassageiro == null) {
                cs.setNull(4, Types.INTEGER);
            } else {
                cs.setInt(4, codPassageiro);
            }
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    ResultadoMatch r = new ResultadoMatch();
                    r.setCodViagem(rs.getInt("cod_viagem"));
                    r.setSaida(rs.getString("saida"));
                    r.setMotorista(rs.getString("motorista"));
                    r.setNotaMotorista(rs.getBigDecimal("nota_motorista"));
                    r.setCodPontoEmbarque(rs.getInt("cod_ponto_embarque"));
                    r.setTipoEmbarque(rs.getString("tipo_embarque"));
                    r.setEmbarque(rs.getString("embarque"));
                    r.setDestino(rs.getString("destino"));
                    r.setTrechoKm(rs.getBigDecimal("trecho_km"));
                    r.setRateioEstimado(rs.getBigDecimal("rateio_estimado"));
                    r.setVagasRestantes(rs.getInt("vagas_restantes"));
                    r.setSimilaridade(rs.getInt("similaridade"));
                    lista.add(r);
                }
            }
        }
        return lista;
    }
}
