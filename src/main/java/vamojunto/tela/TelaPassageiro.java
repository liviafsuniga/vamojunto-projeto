package vamojunto.tela;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import vamojunto.console.Entrada;
import vamojunto.console.Sessao;
import vamojunto.console.TabelaConsole;
import vamojunto.console.Terminal;
import vamojunto.consulta.ConsultaProcedure;
import vamojunto.consulta.Parametro;
import vamojunto.dao.MatchDAO;
import vamojunto.dao.SolicitacaoDAO;
import vamojunto.dao.ViagemDAO;
import vamojunto.match.CriterioOrdenacao;
import vamojunto.match.OrdenarPorHorario;
import vamojunto.match.OrdenarPorNota;
import vamojunto.match.OrdenarPorPreco;
import vamojunto.match.OrdenarPorSimilaridade;
import vamojunto.match.ResultadoMatch;
import vamojunto.modelo.Ponto;
import vamojunto.modelo.Solicitacao;
import vamojunto.modelo.StatusViagem;
import vamojunto.modelo.Viagem;
import vamojunto.util.Formatador;
import vamojunto.util.VamoJuntoException;

/** Funcionalidades do passageiro: RF05 (busca), RF06 (match), RF07 (pedido) e cancelamento. */
public class TelaPassageiro {

    private final MatchDAO matchDAO = new MatchDAO();
    private final ViagemDAO viagemDAO = new ViagemDAO();
    private final SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();

    /** As estrategias de ordenacao disponiveis (lista de objetos de uma interface). */
    private final List<CriterioOrdenacao> criterios = Arrays.asList(
            new OrdenarPorSimilaridade(), new OrdenarPorHorario(), new OrdenarPorPreco(), new OrdenarPorNota());

    /** RF05 - mesma consulta usada pelo visitante e pelo usuario logado. */
    public static ConsultaProcedure consultaBuscarViagens(String titulo) {
        return new ConsultaProcedure(titulo, "sp_buscar_viagens",
                new Parametro("Cidade de origem", Parametro.Tipo.TEXTO),
                new Parametro("Cidade de destino", Parametro.Tipo.TEXTO),
                new Parametro("Data", Parametro.Tipo.DATA));
    }

    /** RF06 - match de trajetos semelhantes + RF07 pedido de carona. */
    public void encontrarCarona() throws Exception {
        Entrada e = Terminal.entrada();
        Terminal.info("Dica: pode digitar o bairro, a rua ou so a cidade.");
        String origem = e.lerTexto("Onde voce esta (embarque)");
        String destino = e.lerTexto("Para onde voce vai");
        LocalDate data = e.lerDataOpcional("Data");

        List<ResultadoMatch> resultados = matchDAO.buscar(origem, destino, data, Sessao.getCodigo());
        if (resultados.isEmpty()) {
            Terminal.info("Nenhuma carona compativel agora. Tente so o nome da cidade ou outra data.");
            return;
        }
        if (resultados.size() > 1) {
            Terminal.info(resultados.size() + " opcoes encontradas. Como deseja ordenar?");
            for (int i = 0; i < criterios.size(); i++) {
                Terminal.info((i + 1) + " - " + criterios.get(i).getNome());
            }
            CriterioOrdenacao criterio = criterios.get(e.lerInteiro("Ordenar por", 1, criterios.size()) - 1);
            criterio.ordenar(resultados);   // polimorfismo: cada estrategia ordena do seu jeito
        }

        List<String[]> linhas = new ArrayList<>();
        for (int i = 0; i < resultados.size(); i++) {
            ResultadoMatch r = resultados.get(i);
            linhas.add(new String[]{String.valueOf(i + 1), String.valueOf(r.getCodViagem()), r.getSaida(),
                r.getMotorista(), Formatador.decimal(r.getNotaMotorista()), r.getEmbarque(), r.getDestino(),
                Formatador.decimal(r.getTrechoKm()), Formatador.moeda(r.getRateioEstimado()),
                String.valueOf(r.getVagasRestantes()), String.valueOf(r.getSimilaridade())});
        }
        TabelaConsole.imprimir(new String[]{"#", "viagem", "saida", "motorista", "nota", "embarque", "destino",
            "km", "voce paga", "vagas", "similaridade"}, linhas);

        int escolha = e.lerInteiro("Numero da opcao para pedir carona (0 = nenhuma)", 0, resultados.size());
        if (escolha == 0) {
            return;
        }
        ResultadoMatch r = resultados.get(escolha - 1);
        pedirCarona(r.getCodViagem(), r.getCodPontoEmbarque(), r.getTrechoKm());
    }

    /** RF07 - o passageiro ja sabe o codigo da viagem (ex.: viu na busca). */
    public void pedirCaronaPorCodigo() throws Exception {
        int codigo = Terminal.entrada().lerInteiro("Codigo da viagem");
        pedirCarona(codigo, null, null);
    }

    private void pedirCarona(int codViagem, Integer codPonto, BigDecimal trechoSugerido) throws Exception {
        Entrada e = Terminal.entrada();
        Viagem viagem = viagemDAO.buscarPorCodigo(codViagem);
        if (viagem == null) {
            throw new VamoJuntoException("Viagem nao encontrada.");
        }
        Terminal.campo("Viagem", viagem.getCodigo() + " - " + viagem.getMotorista().getNome());
        Terminal.campo("Saida", Formatador.data(viagem.getDataInicio()));
        Terminal.campo("Veiculo", viagem.getVeiculo());
        Terminal.campo("Destino final", viagem.getDestino().getEnderecoResumido());
        Terminal.campo("Vagas restantes", viagem.getVagasRestantes());

        Ponto embarque = null;
        if (codPonto != null) {
            for (Ponto p : viagem.getPontos()) {
                if (p.getCodigo() == codPonto) {
                    embarque = p;
                }
            }
        }
        if (embarque == null) {
            List<Ponto> opcoes = viagem.getPontosDeEmbarque();
            Terminal.info("Onde voce vai embarcar?");
            for (int i = 0; i < opcoes.size(); i++) {
                Terminal.info((i + 1) + " - " + opcoes.get(i).getTipo() + ": " + opcoes.get(i).getEnderecoResumido());
            }
            embarque = opcoes.get(e.lerInteiro("Ponto de embarque", 1, opcoes.size()) - 1);
        }

        BigDecimal trecho = trechoSugerido != null ? trechoSugerido : viagemDAO.trechoEstimado(embarque.getCodigo());
        Terminal.campo("Embarque", embarque.getEnderecoResumido());
        Terminal.campo("Trecho estimado", Formatador.decimal(trecho) + " km de " + Formatador.decimal(viagem.getDistanciaKm()) + " km");
        if (!e.confirmar("Usar essa distancia?")) {
            trecho = e.lerDecimal("Quantos km voce vai percorrer");
        }

        // regras conferidas no objeto (e de novo no banco pela trigger)
        Solicitacao s = Sessao.getUsuario().solicitarCarona(viagem, embarque, trecho);
        Terminal.campo("Custo total da viagem", Formatador.moeda(viagem.getCustoTotal()));
        Terminal.campo("Voce pagaria", Formatador.moeda(viagem.calcularRateio(trecho))
                + " (diminui se entrarem mais passageiros)");
        if (!e.confirmar("Enviar o pedido de carona?")) {
            Terminal.info("Pedido nao enviado.");
            return;
        }
        int codigo = solicitacaoDAO.inserir(s);
        Terminal.sucesso("Pedido " + codigo + " enviado! Agora e so aguardar o motorista aceitar.");
    }

    public void minhasSolicitacoes() throws Exception {
        List<Solicitacao> lista = solicitacaoDAO.listarDoPassageiro(Sessao.getCodigo());
        imprimir(lista);
    }

    public void cancelarSolicitacao() throws Exception {
        List<Solicitacao> lista = new ArrayList<>();
        for (Solicitacao s : solicitacaoDAO.listarDoPassageiro(Sessao.getCodigo())) {
            if (s.podeCancelar() && s.getViagem().getStatus() == StatusViagem.AGENDADA) {
                lista.add(s);
            }
        }
        if (lista.isEmpty()) {
            Terminal.info("Voce nao tem pedidos que possam ser cancelados.");
            return;
        }
        imprimir(lista);
        int codigo = Terminal.entrada().lerInteiro("Codigo do pedido a cancelar (0 = voltar)");
        if (codigo == 0) {
            return;
        }
        Solicitacao escolhida = null;
        for (Solicitacao s : lista) {
            if (s.getCodigo() == codigo) {
                escolhida = s;
            }
        }
        if (escolhida == null) {
            throw new VamoJuntoException("Pedido " + codigo + " nao esta na sua lista.");
        }
        if (!Terminal.entrada().confirmar("Cancelar o pedido " + codigo + "?")) {
            return;
        }
        solicitacaoDAO.cancelar(codigo);
        Terminal.sucesso("Pedido cancelado. A vaga foi liberada e o rateio foi recalculado.");
    }

    private void imprimir(List<Solicitacao> lista) {
        List<String[]> linhas = new ArrayList<>();
        for (Solicitacao s : lista) {
            linhas.add(new String[]{String.valueOf(s.getCodigo()), String.valueOf(s.getViagem().getCodigo()),
                Formatador.data(s.getViagem().getDataInicio()), s.getViagem().getMotorista().getNome(),
                s.getPontoEmbarque().getEnderecoResumido(), s.getViagem().getDestino().getCidade(),
                Formatador.decimal(s.getDistanciaTrechoKm()), s.getStatus().name(),
                s.getViagem().getStatus().getDescricao(), Formatador.moeda(s.getValorRateio())});
        }
        TabelaConsole.imprimir(new String[]{"pedido", "viagem", "saida", "motorista", "embarque", "destino",
            "km", "status_pedido", "status_viagem", "valor"}, linhas);
    }
}
