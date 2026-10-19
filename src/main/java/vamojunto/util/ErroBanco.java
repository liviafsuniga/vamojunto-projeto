package vamojunto.util;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduz os erros do MySQL/MariaDB para mensagens claras no console.
 *
 * - Erros das triggers/procedures (SIGNAL SQLSTATE '45000') ja vem com
 *   mensagem pronta: so tiramos prefixos tecnicos como "(conn=860)".
 * - Erros de UNIQUE e CHECK vem com o nome da restricao: trocamos pelo texto
 *   que o usuario entende.
 *
 * Boas praticas (Eng. de Software / UX): o programa nunca "quebra" por
 * causa de um erro do banco; ele explica o que aconteceu.
 */
public final class ErroBanco {

    private static final Map<String, String> RESTRICOES = new LinkedHashMap<>();

    static {
        // UNIQUE
        RESTRICOES.put("uq_usuario_cpf", "Ja existe um usuario cadastrado com este CPF.");
        RESTRICOES.put("uq_usuario_email", "Ja existe um usuario cadastrado com este e-mail.");
        RESTRICOES.put("uq_motorista_cnh", "Esta CNH ja esta cadastrada para outro motorista.");
        RESTRICOES.put("uq_veiculo_placa", "Ja existe um veiculo com esta placa.");
        RESTRICOES.put("uq_veiculo_renavam", "Ja existe um veiculo com este RENAVAM.");
        RESTRICOES.put("uq_solicitacao_viagem_passageiro", "Voce ja pediu carona nesta viagem.");
        RESTRICOES.put("uq_avaliacao_solicitacao_autor", "Esta carona ja foi avaliada por voce.");
        RESTRICOES.put("uq_instituicao_nome_cidade", "Esta instituicao ja esta cadastrada nesta cidade.");
        RESTRICOES.put("uq_ponto_viagem_ordem", "Ja existe um ponto nesta posicao da rota.");
        RESTRICOES.put("uq_combustivel_tipo", "Este tipo de combustivel ja esta cadastrado.");
        // CHECK
        RESTRICOES.put("ck_usuario_cpf", "CPF deve ter 11 digitos.");
        RESTRICOES.put("ck_usuario_email", "E-mail invalido.");
        RESTRICOES.put("ck_usuario_telefone", "Telefone deve ter 10 ou 11 digitos (com DDD).");
        RESTRICOES.put("ck_motorista_categoria", "Categoria de CNH nao permitida para carro.");
        RESTRICOES.put("ck_motorista_cnh", "CNH deve ter 11 digitos.");
        RESTRICOES.put("ck_veiculo_placa", "Placa invalida. Use ABC1234 ou ABC1D23.");
        RESTRICOES.put("ck_veiculo_lugares", "O veiculo deve ter de 2 a 9 lugares.");
        RESTRICOES.put("ck_veiculo_ano", "Ano do veiculo invalido.");
        RESTRICOES.put("ck_veiculo_renavam", "RENAVAM deve ter 11 digitos.");
        RESTRICOES.put("ck_combustivel_preco", "O preco do litro deve ser maior que zero.");
        RESTRICOES.put("ck_viagem_particip", "Vagas nao podem ficar abaixo do numero de passageiros aceitos.");
        RESTRICOES.put("ck_viagem_distancia", "A distancia deve ser maior que zero.");
        RESTRICOES.put("ck_viagem_consumo", "O consumo deve ser maior que zero.");
        RESTRICOES.put("ck_viagem_datas", "A data de chegada deve ser depois da saida.");
        RESTRICOES.put("ck_ponto_cep", "CEP deve ter 8 digitos.");
        RESTRICOES.put("ck_ponto_lat", "Latitude deve estar entre -90 e 90.");
        RESTRICOES.put("ck_ponto_lng", "Longitude deve estar entre -180 e 180.");
        RESTRICOES.put("ck_ponto_uf", "UF invalida.");
        RESTRICOES.put("ck_instituicao_uf", "UF invalida.");
        RESTRICOES.put("ck_avaliacao_nota", "A nota deve ser de 1 a 5.");
        RESTRICOES.put("ck_solicitacao_trecho", "O trecho deve ser maior que zero.");
    }

    private ErroBanco() {
    }

    public static String traduzir(SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        // drivers diferentes colocam prefixos como "(conn=860) "
        msg = msg.replaceAll("^\\(conn=\\d+\\)\\s*", "").trim();

        // 45000 = regra de negocio sinalizada por trigger/procedure
        if ("45000".equals(e.getSQLState())) {
            return msg;
        }
        for (Map.Entry<String, String> r : RESTRICOES.entrySet()) {
            if (msg.contains(r.getKey())) {
                return r.getValue();
            }
        }
        String baixo = msg.toLowerCase();
        if (baixo.contains("no suitable driver")) {
            return "Driver JDBC nao encontrado. Adicione o mysql-connector-j ao projeto (veja o README).";
        }
        if (baixo.contains("unknown database")) {
            return "O banco 'vamojunto' nao existe. Rode o script banco/vamojunto.sql primeiro.";
        }
        if (baixo.contains("foreign key")) {
            return "Codigo informado nao existe ou o registro esta em uso (chave estrangeira).";
        }
        if (baixo.contains("denied")) {
            return "O usuario do banco nao tem permissao para esta operacao (menor privilegio).";
        }
        if (baixo.contains("communications link") || baixo.contains("connection refused")
                || baixo.contains("could not connect") || baixo.contains("socket")) {
            return "Nao foi possivel falar com o banco. O MySQL esta ligado?";
        }
        return "Erro no banco: " + msg;
    }
}
