package vamojunto.tela;

import vamojunto.console.Menu;
import vamojunto.console.Sessao;
import vamojunto.modelo.Usuario;

/**
 * Menu do usuario logado. Com a mesma conta a pessoa pede carona (passageiro)
 * e, se tiver CNH, oferece carona (motorista) - como no prototipo do Figma.
 * Os submenus sao objetos Menu dentro de outro Menu (padrao Composite).
 */
public class TelaUsuario {

    public Menu criarMenu() {
        Usuario u = Sessao.getUsuario();
        TelaPassageiro passageiro = new TelaPassageiro();
        TelaMotorista motorista = new TelaMotorista();
        TelaConta conta = new TelaConta();

        Menu areaPassageiro = new Menu("Area do passageiro")
                .adicionar(TelaPassageiro.consultaBuscarViagens("Buscar viagens disponiveis"))
                .adicionar("Encontrar carona para o meu trajeto (match)", passageiro::encontrarCarona)
                .adicionar("Pedir carona pelo codigo da viagem", passageiro::pedirCaronaPorCodigo)
                .adicionar("Minhas solicitacoes de carona", passageiro::minhasSolicitacoes)
                .adicionar("Cancelar uma solicitacao", passageiro::cancelarSolicitacao);

        Menu areaMotorista = new Menu("Area do motorista")
                .adicionar("Quero ser motorista (cadastrar CNH)", motorista::queroSerMotorista)
                .adicionar("Cadastrar veiculo", motorista::cadastrarVeiculo)
                .adicionar("Oferecer viagem (rota com paradas)", motorista::oferecerViagem)
                .adicionar("Minhas viagens como motorista", motorista::minhasViagens)
                .adicionar("Pedidos de carona recebidos (aceitar/recusar)", motorista::pedidosRecebidos)
                .adicionar("Gerenciar uma viagem (passageiros, parada, iniciar, concluir, cancelar)",
                        motorista::gerenciarViagem);

        return new Menu("Ola, " + u.getNome() + "!", "Sair da conta")
                .adicionar(areaPassageiro)
                .adicionar(areaMotorista)
                .adicionar("Avaliar caronas", conta::avaliar)
                .adicionar("Meu historico de viagens", conta::historico)
                .adicionar("Detalhes e rota de uma viagem", conta::detalhesViagem)
                .adicionar("Meu perfil e reputacao", conta::perfil)
                .adicionar("Alterar telefone ou senha", conta::alterarDados);
    }
}
