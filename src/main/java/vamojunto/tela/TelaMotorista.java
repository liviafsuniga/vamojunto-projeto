package vamojunto.tela;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import vamojunto.console.Entrada;
import vamojunto.console.Menu;
import vamojunto.console.Sessao;
import vamojunto.console.TabelaConsole;
import vamojunto.console.Terminal;
import vamojunto.dao.CombustivelDAO;
import vamojunto.dao.ConsultaDAO;
import vamojunto.dao.SolicitacaoDAO;
import vamojunto.dao.UsuarioDAO;
import vamojunto.dao.VeiculoDAO;
import vamojunto.dao.ViagemDAO;
import vamojunto.modelo.CategoriaCnh;
import vamojunto.modelo.Combustivel;
import vamojunto.modelo.Motorista;
import vamojunto.modelo.Ponto;
import vamojunto.modelo.Solicitacao;
import vamojunto.modelo.StatusSolicitacao;
import vamojunto.modelo.StatusViagem;
import vamojunto.modelo.TipoPonto;
import vamojunto.modelo.Usuario;
import vamojunto.modelo.Veiculo;
import vamojunto.modelo.Viagem;
import vamojunto.util.ErroBanco;
import vamojunto.util.Formatador;
import vamojunto.util.VamoJuntoException;

/**
 * Funcionalidades do motorista: RF02 (habilitar), RF03 (veiculo), RF04 (rota),
 * RF08 (aceitar/recusar), RF09 (custo) e RF10 (iniciar/concluir/cancelar).
 */
public class TelaMotorista {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final VeiculoDAO veiculoDAO = new VeiculoDAO();
    private final CombustivelDAO combustivelDAO = new CombustivelDAO();
    private final ViagemDAO viagemDAO = new ViagemDAO();
    private final SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();

    /** RF02 - somente usuario verificado e ativo, com CNH de carro valida. */
    public void queroSerMotorista() throws Exception {
        // recarrega do banco: o administrador pode ter verificado a conta depois do login
        Usuario u = usuarioDAO.buscarPorCodigo(Sessao.getCodigo());
        Sessao.entrar(u);
        if (u.isMotorista()) {
            Terminal.info("Voce ja e motorista. CNH valida ate " + Formatador.data(((Motorista) u).getValidadeCnh()) + ".");
            return;
        }
        if (!u.podeSerMotorista()) {
            throw new VamoJuntoException("Sua conta ainda nao foi verificada. Peca ao administrador "
                    + "(Area do administrador > Verificar conta) e tente de novo.");
        }
        Entrada e = Terminal.entrada();
        Motorista m = new Motorista(u);
        Terminal.tentar(() -> m.setCnh(e.lerTexto("Numero da CNH (11 digitos)")));
        Terminal.tentar(() -> m.setCategoriaCnh(CategoriaCnh.deTexto(e.lerTexto("Categoria (B, C, D, E, AB, AC, AD, AE)"))));
        Terminal.tentar(() -> {
            LocalDate validade = e.lerData("Validade da CNH");
            if (validade.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("CNH vencida. Renove a CNH para oferecer caronas.");
            }
            m.setValidadeCnh(validade);
        });
        usuarioDAO.inserirMotorista(m);
        Sessao.entrar(usuarioDAO.buscarPorCodigo(u.getCodigo()));
        Terminal.sucesso("Pronto! Agora voce tambem pode oferecer caronas.");
    }

    /** RF03 - cadastro de veiculo. */
    public void cadastrarVeiculo() throws Exception {
        Sessao.exigirMotorista();
        Veiculo v = lerVeiculo();
        Terminal.sucesso("Veiculo " + v.getCodigo() + " cadastrado: " + v);
    }

    private Veiculo lerVeiculo() throws Exception {
        Entrada e = Terminal.entrada();
        Veiculo v = new Veiculo();
        Terminal.tentar(() -> v.setMarca(e.lerTexto("Marca")));
        Terminal.tentar(() -> v.setModelo(e.lerTexto("Modelo")));
        Terminal.tentar(() -> v.setCor(e.lerTexto("Cor")));
        Terminal.tentar(() -> v.setPlaca(e.lerTexto("Placa (ABC1234 ou ABC1D23)")));
        Terminal.tentar(() -> v.setAno(e.lerInteiro("Ano")));
        Terminal.tentar(() -> v.setRenavam(e.lerTexto("RENAVAM (11 digitos)")));
        Terminal.tentar(() -> v.setQtdLugares(e.lerInteiro("Lugares no carro, contando o motorista", 2, 9)));
        veiculoDAO.inserir(v);
        return v;
    }

    /** RF04 + RF09 - oferece a viagem que o motorista ja vai fazer. */
    public void oferecerViagem() throws Exception {
        Motorista m = Sessao.exigirMotorista();
        Entrada e = Terminal.entrada();

        // 1) veiculo
        List<Veiculo> veiculos = veiculoDAO.listar();
        List<String[]> linhas = new ArrayList<>();
        for (Veiculo v : veiculos) {
            linhas.add(new String[]{String.valueOf(v.getCodigo()), v.getMarca() + " " + v.getModelo(), v.getCor(),
                v.getPlaca(), String.valueOf(v.getQtdLugares())});
        }
        TabelaConsole.imprimir(new String[]{"codigo", "veiculo", "cor", "placa", "lugares"}, linhas);
        Veiculo veiculo = null;
        while (veiculo == null) {
            int cod = e.lerInteiro("Codigo do veiculo (0 = cadastrar um novo)");
            if (cod == 0) {
                veiculo = lerVeiculo();
            } else {
                veiculo = procurarVeiculo(veiculos, cod);
                if (veiculo == null) {
                    e.avisar("Veiculo nao encontrado.");
                }
            }
        }

        // 2) combustivel
        List<Combustivel> combustiveis = combustivelDAO.listar();
        for (Combustivel c : combustiveis) {
            Terminal.info(c.getCodigo() + " - " + c);
        }
        Combustivel combustivel = null;
        while (combustivel == null) {
            int cod = e.lerInteiro("Codigo do combustivel");
            for (Combustivel c : combustiveis) {
                if (c.getCodigo() == cod) {
                    combustivel = c;
                }
            }
            if (combustivel == null) {
                e.avisar("Combustivel nao encontrado.");
            }
        }

        // 3) dados da viagem
        LocalDateTime agora = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime saida = e.lerDataHora("Saida", agora);
        int vagas = e.lerInteiro("Vagas para passageiros", 1, veiculo.getVagasMaximas());
        BigDecimal distancia = lerPositivo("Distancia total da rota em km");
        BigDecimal consumo = lerPositivo("Consumo do carro em km/l");
        Viagem viagem = m.criarViagem(veiculo, combustivel, saida, vagas, distancia, consumo);

        // 4) rota: origem, paradas e destino
        Terminal.info("");
        Terminal.info("Agora a rota: ORIGEM -> paradas (opcional) -> DESTINO");
        viagem.adicionarPonto(lerPonto(TipoPonto.ORIGEM));
        while (e.confirmar("Adicionar uma parada no caminho?")) {
            viagem.adicionarPonto(lerPonto(TipoPonto.PARADA));
        }
        viagem.adicionarPonto(lerPonto(TipoPonto.DESTINO));

        // 5) resumo com o calculo do custo (RF09)
        Terminal.titulo("Resumo da viagem");
        Terminal.campo("Veiculo", veiculo);
        Terminal.campo("Combustivel", combustivel);
        Terminal.campo("Saida", Formatador.data(saida));
        Terminal.campo("Vagas", vagas);
        for (Ponto p : viagem.getPontos()) {
            Terminal.campo("  " + p.getOrdem() + ". " + p.getTipo(), p.getEnderecoResumido());
        }
        Terminal.campo("Custo do combustivel", Formatador.moeda(viagem.calcularCusto())
                + " = " + distancia + " km / " + consumo + " km/l x " + Formatador.moeda(combustivel.getPrecoLitro()));
        Terminal.campo("Com o carro cheio", "cada passageiro da viagem toda paga "
                + Formatador.moeda(viagem.calcularRateio(distancia, vagas)) + " e voce tambem");
        if (!e.confirmar("Publicar a viagem?")) {
            Terminal.info("Viagem descartada.");
            return;
        }
        int codigo = viagemDAO.inserir(viagem);
        Viagem gravada = viagemDAO.buscarPorCodigo(codigo);
        Terminal.sucesso("Viagem " + codigo + " publicada! Custo calculado pelo banco: "
                + Formatador.moeda(gravada.getCustoTotal()));
    }

    public void minhasViagens() throws Exception {
        Motorista m = Sessao.exigirMotorista();
        imprimirViagens(viagemDAO.listarDoMotorista(m.getCodigo()));
    }

    /** RF08 - aceitar ou recusar pedidos (procedure com transacao). */
    public void pedidosRecebidos() throws Exception {
        Motorista m = Sessao.exigirMotorista();
        Entrada e = Terminal.entrada();
        while (true) {
            List<Solicitacao> pendentes = solicitacaoDAO.listarPendentesDoMotorista(m.getCodigo());
            if (pendentes.isEmpty()) {
                Terminal.info("Nenhum pedido aguardando resposta.");
                return;
            }
            List<String[]> linhas = new ArrayList<>();
            for (Solicitacao s : pendentes) {
                Object nota = ConsultaDAO.valor("SELECT fn_media_avaliacao_passageiro(?)", s.getPassageiro().getCodigo());
                linhas.add(new String[]{String.valueOf(s.getCodigo()), String.valueOf(s.getViagem().getCodigo()),
                    Formatador.data(s.getViagem().getDataInicio()), s.getPassageiro().getNome(),
                    nota == null ? "sem nota" : TabelaConsole.formatar(nota), s.getPontoEmbarque().getEnderecoResumido(),
                    Formatador.decimal(s.getDistanciaTrechoKm())});
            }
            TabelaConsole.imprimir(new String[]{"pedido", "viagem", "saida", "passageiro", "nota", "embarque", "km"}, linhas);

            int codigo = e.lerInteiro("Codigo do pedido para responder (0 = voltar)");
            if (codigo == 0) {
                return;
            }
            Solicitacao escolhida = null;
            for (Solicitacao s : pendentes) {
                if (s.getCodigo() == codigo) {
                    escolhida = s;
                }
            }
            if (escolhida == null) {
                e.avisar("Pedido " + codigo + " nao esta na lista.");
                continue;
            }
            boolean aceitar = e.lerOpcao("Aceitar (A) ou recusar (R)", "A", "R").equals("A");
            StatusSolicitacao resposta = m.responderSolicitacao(escolhida, aceitar);
            try {
                solicitacaoDAO.responder(codigo, resposta);
            } catch (SQLException erro) {
                // ex.: "Viagem sem vagas disponiveis." - o banco desfez tudo (ROLLBACK)
                Terminal.erro(ErroBanco.traduzir(erro));
                continue;
            }
            Terminal.sucesso("Pedido " + codigo + (aceitar ? " aceito." : " recusado."));
            if (aceitar) {
                Terminal.info("Rateio atualizado da viagem " + escolhida.getViagem().getCodigo() + ":");
                ConsultaDAO.imprimirProcedure("sp_passageiros_viagem", escolhida.getViagem().getCodigo());
            }
        }
    }

    /** RF10 - acoes sobre uma viagem do motorista (submenu). */
    public void gerenciarViagem() throws Exception {
        Motorista m = Sessao.exigirMotorista();
        List<Viagem> viagens = viagemDAO.listarDoMotorista(m.getCodigo());
        imprimirViagens(viagens);
        if (viagens.isEmpty()) {
            return;
        }
        int codigo = Terminal.entrada().lerInteiro("Codigo da viagem (0 = voltar)");
        if (codigo == 0) {
            return;
        }
        Viagem viagem = null;
        for (Viagem v : viagens) {
            if (v.getCodigo() == codigo) {
                viagem = v;
            }
        }
        if (viagem == null) {
            throw new VamoJuntoException("A viagem " + codigo + " nao e sua.");
        }
        final int cod = codigo;
        Terminal.info("Viagem " + cod + ": " + viagem.getStatus().getDescricao() + ", "
                + viagem.getQtdParticipantes() + " passageiro(s) aceito(s).");
        new Menu("Gerenciar viagem " + cod)
                .adicionar("Ver passageiros e rateio", () -> ConsultaDAO.imprimirProcedure("sp_passageiros_viagem", cod))
                .adicionar("Ver rota", () -> ConsultaDAO.imprimirProcedure("sp_rota_viagem", cod))
                .adicionar("Adicionar parada antes do destino", () -> adicionarParada(cod))
                .adicionar("Iniciar viagem (pedidos pendentes sao recusados)", () -> {
                    if (confirmar("Iniciar a viagem " + cod + " agora?")) {
                        viagemDAO.iniciar(cod, m.getCodigo());
                        Terminal.sucesso("Viagem em andamento. Boa viagem!");
                    }
                })
                .adicionar("Concluir viagem (libera as avaliacoes)", () -> {
                    if (confirmar("Concluir a viagem " + cod + "?")) {
                        viagemDAO.concluir(cod, m.getCodigo());
                        Terminal.sucesso("Viagem concluida. Voce e os passageiros ja podem se avaliar.");
                    }
                })
                .adicionar("Cancelar viagem (cancela todos os pedidos)", () -> {
                    if (confirmar("Cancelar a viagem " + cod + "? Essa acao nao pode ser desfeita.")) {
                        viagemDAO.cancelar(cod, m.getCodigo());
                        Terminal.sucesso("Viagem cancelada e passageiros liberados.");
                    }
                })
                .executar();
    }

    private void adicionarParada(int codViagem) throws Exception {
        Viagem v = viagemDAO.buscarPorCodigo(codViagem);
        if (v.getStatus() != StatusViagem.AGENDADA) {
            throw new VamoJuntoException("So e possivel mudar a rota de uma viagem agendada.");
        }
        Ponto parada = lerPonto(TipoPonto.PARADA);
        viagemDAO.adicionarParada(codViagem, parada);
        Terminal.sucesso("Parada incluida. Rota atualizada:");
        ConsultaDAO.imprimirProcedure("sp_rota_viagem", codViagem);
    }

    // ------------------------------------------------------------------

    private Ponto lerPonto(TipoPonto tipo) throws Exception {
        Entrada e = Terminal.entrada();
        Terminal.info("-- " + tipo + " --");
        Ponto p = new Ponto();
        p.setTipo(tipo);
        Terminal.tentar(() -> p.setLogradouro(e.lerTexto("Rua / avenida")));
        p.setNumero(e.lerTexto("Numero", null));
        p.setBairro(e.lerTexto("Bairro", null));
        Terminal.tentar(() -> p.setCidade(e.lerTexto("Cidade")));
        Terminal.tentar(() -> p.setEstado(e.lerTexto("UF", "SP")));
        Terminal.tentar(() -> p.setCep(e.lerTexto("CEP", null)));
        Terminal.tentar(() -> {
            BigDecimal lat = e.lerDecimalOpcional("Latitude (ex.: -23,5015)");
            p.setLatitude(lat);
            p.setLongitude(lat == null ? null : e.lerDecimal("Longitude (ex.: -47,4581)"));
        });
        return p;
    }

    private BigDecimal lerPositivo(String rotulo) {
        while (true) {
            BigDecimal valor = Terminal.entrada().lerDecimal(rotulo);
            if (valor.signum() > 0) {
                return valor;
            }
            Terminal.entrada().avisar("O valor deve ser maior que zero.");
        }
    }

    private boolean confirmar(String pergunta) {
        return Terminal.entrada().confirmar(pergunta);
    }

    private Veiculo procurarVeiculo(List<Veiculo> veiculos, int codigo) {
        for (Veiculo v : veiculos) {
            if (v.getCodigo() == codigo) {
                return v;
            }
        }
        return null;
    }

    private void imprimirViagens(List<Viagem> viagens) {
        List<String[]> linhas = new ArrayList<>();
        for (Viagem v : viagens) {
            linhas.add(new String[]{String.valueOf(v.getCodigo()), v.getStatus().getDescricao(),
                Formatador.data(v.getDataInicio()),
                v.getOrigem() == null ? "-" : v.getOrigem().getCidade(),
                v.getDestino() == null ? "-" : v.getDestino().getCidade(),
                String.valueOf(v.getParadas().size()), v.getQtdParticipantes() + "/" + v.getVagas(),
                Formatador.moeda(v.getCustoTotal())});
        }
        TabelaConsole.imprimir(new String[]{"viagem", "status", "saida", "origem", "destino", "paradas",
            "passageiros", "custo"}, linhas);
    }
}
