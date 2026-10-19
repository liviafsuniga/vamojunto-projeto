package vamojunto.tela;

import vamojunto.console.Menu;
import vamojunto.consulta.ConsultaProcedure;
import vamojunto.consulta.ConsultaView;
import vamojunto.consulta.OperacaoProcedure;
import vamojunto.consulta.Parametro;
import vamojunto.consulta.Parametro.Tipo;

/**
 * As 16 opcoes de consulta documentadas (secao 8 do documento do banco):
 * 13 procedures de consulta + a view de caronas + 2 operacoes com transacao.
 * Cada opcao e um OBJETO de uma subclasse de Consulta, guardado na lista do Menu.
 *
 * Aulas de Banco de Dados cobertas: 7 (SELECT), 8 (JOIN/subconsulta),
 * 9 (GROUP BY/HAVING), 10 (views), 11 (procedures/functions) e 12 (transacoes).
 */
public class TelaRelatorios {

    public Menu criarMenu() {
        return new Menu("Consultas e relatorios do banco")
                // Aula 7 - SELECT, filtros, ordenacao e funcoes
                .adicionar(TelaPassageiro.consultaBuscarViagens("Buscar viagens disponiveis"))
                .adicionar(new ConsultaProcedure("Buscar usuarios (CPF mascarado)", "sp_buscar_usuarios",
                        new Parametro("Parte do nome ou e-mail", Tipo.TEXTO)))
                // Aula 8 - JOINs e subconsultas
                .adicionar(new ConsultaProcedure("Detalhes de uma viagem", "sp_detalhes_viagem",
                        new Parametro("Codigo da viagem", Tipo.INTEIRO, true)))
                .adicionar(new ConsultaProcedure("Rota de uma viagem", "sp_rota_viagem",
                        new Parametro("Codigo da viagem", Tipo.INTEIRO, true)))
                .adicionar(new ConsultaProcedure("Passageiros de uma viagem", "sp_passageiros_viagem",
                        new Parametro("Codigo da viagem", Tipo.INTEIRO, true)))
                .adicionar(new ConsultaProcedure("Historico de um usuario", "sp_historico_usuario",
                        new Parametro("Codigo do usuario", Tipo.INTEIRO, true)))
                .adicionar(new ConsultaProcedure("Usuarios que ainda nao pegaram carona", "sp_usuarios_sem_carona"))
                .adicionar(new ConsultaProcedure("Viagens com custo acima da media", "sp_viagens_acima_media"))
                // Aula 9 - agregacao, GROUP BY e HAVING
                .adicionar(new ConsultaProcedure("Ranking de motoristas", "sp_ranking_motoristas",
                        new Parametro("Minimo de avaliacoes", Tipo.INTEIRO)))
                .adicionar(new ConsultaProcedure("Relatorio mensal", "sp_relatorio_mensal",
                        new Parametro("Ano", Tipo.INTEIRO, true)))
                .adicionar(new ConsultaProcedure("Resumo por instituicao", "sp_resumo_por_instituicao"))
                .adicionar(new ConsultaProcedure("Passageiros que mais usam o app", "sp_resumo_passageiros",
                        new Parametro("Minimo de caronas", Tipo.INTEIRO)))
                .adicionar(new ConsultaProcedure("Estatisticas gerais", "sp_estatisticas_gerais"))
                // Aula 10 - view
                .adicionar(new ConsultaView("Todas as caronas (view vw_caronas)", "vw_caronas",
                        "dt_inicio DESC, cod_solicitacao", "status",
                        new Parametro("Status (PENDENTE, ACEITA, RECUSADA, CANCELADA)", Tipo.TEXTO)))
                // Aulas 11 e 12 - procedures de operacao com transacao
                .adicionar(new OperacaoProcedure("Responder pedido de carona", "sp_responder_solicitacao",
                        "Resposta gravada. O rateio dos passageiros foi recalculado.",
                        new Parametro("Codigo da solicitacao", Tipo.INTEIRO, true),
                        new Parametro("Resposta", "ACEITA", "RECUSADA")))
                .adicionar(new OperacaoProcedure("Cancelar pedido de carona", "sp_cancelar_solicitacao",
                        "Pedido cancelado. A vaga foi liberada.",
                        new Parametro("Codigo da solicitacao", Tipo.INTEIRO, true)));
    }
}
