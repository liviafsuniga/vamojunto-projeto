package vamojunto.tela;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import vamojunto.conexao.Conexao;
import vamojunto.console.Entrada;
import vamojunto.console.Menu;
import vamojunto.console.TabelaConsole;
import vamojunto.console.Terminal;
import vamojunto.consulta.ConsultaView;
import vamojunto.dao.CombustivelDAO;
import vamojunto.dao.ConsultaDAO;
import vamojunto.dao.InstituicaoDAO;
import vamojunto.dao.UsuarioDAO;
import vamojunto.dao.VeiculoDAO;
import vamojunto.modelo.Combustivel;
import vamojunto.modelo.Instituicao;
import vamojunto.modelo.Usuario;
import vamojunto.modelo.Veiculo;
import vamojunto.util.Formatador;
import vamojunto.util.VamoJuntoException;

/**
 * Ator "Administrador" do diagrama de casos de uso: verifica contas,
 * ativa/desativa usuarios, cadastra instituicoes e atualiza precos.
 * Senha padrao: admin2026 (troque com -Dadmin.senha=...).
 */
public class TelaAdministrador {

    private static final String SENHA_ADMIN = System.getProperty("admin.senha", "admin2026");

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final InstituicaoDAO instituicaoDAO = new InstituicaoDAO();
    private final CombustivelDAO combustivelDAO = new CombustivelDAO();
    private final VeiculoDAO veiculoDAO = new VeiculoDAO();

    public static boolean senhaCorreta(String senha) {
        return SENHA_ADMIN.equals(senha);
    }

    public Menu criarMenu() {
        return new Menu("Area do administrador")
                .adicionar(new ConsultaView("Listar usuarios (view sem senha e com CPF mascarado)",
                        "vw_usuario_publico", "nome"))
                .adicionar("Verificar conta de usuario", this::verificarConta)
                .adicionar("Ativar ou desativar usuario", this::ativarDesativar)
                .adicionar("Cadastrar instituicao de ensino", this::cadastrarInstituicao)
                .adicionar("Atualizar preco do combustivel", this::atualizarPreco)
                .adicionar("Listar veiculos", this::listarVeiculos)
                .adicionar(new ConsultaView("Reputacao dos motoristas (view)", "vw_reputacao_motorista",
                        "media_nota DESC, qtd_avaliacoes DESC"))
                .adicionar(new ConsultaView("Viagens com vaga (view)", "vw_viagens_disponiveis", "dt_inicio"))
                .adicionar("Informacoes da conexao", this::informacoesConexao);
    }

    private void verificarConta() throws Exception {
        int qtd = ConsultaDAO.imprimir("SELECT cod_usuario, nome, email, cpf_mascarado, instituicao, dt_cadastro "
                + "FROM vw_usuario_publico WHERE verificado = 0 AND ativo = 1 ORDER BY nome");
        if (qtd == 0) {
            return;
        }
        int codigo = Terminal.entrada().lerInteiro("Codigo do usuario a verificar (0 = voltar)");
        if (codigo == 0) {
            return;
        }
        if (!usuarioDAO.verificar(codigo)) {
            throw new VamoJuntoException("Usuario nao encontrado ou ja verificado.");
        }
        Terminal.sucesso("Conta verificada. O usuario ja pode se cadastrar como motorista.");
    }

    private void ativarDesativar() throws Exception {
        int codigo = Terminal.entrada().lerInteiro("Codigo do usuario");
        Usuario u = usuarioDAO.buscarPorCodigo(codigo);
        if (u == null) {
            throw new VamoJuntoException("Usuario nao encontrado.");
        }
        boolean novo = !u.isAtivo();
        String acao = novo ? "REATIVAR" : "DESATIVAR";
        if (!Terminal.entrada().confirmar(u.getNome() + " esta " + (u.isAtivo() ? "ativo" : "inativo") + ". " + acao + "?")) {
            return;
        }
        usuarioDAO.alterarAtivo(codigo, novo);
        Terminal.sucesso("Usuario " + (novo ? "reativado" : "desativado") + " (o historico foi preservado).");
    }

    private void cadastrarInstituicao() throws Exception {
        Entrada e = Terminal.entrada();
        TelaInicial.imprimirInstituicoes(instituicaoDAO.listar());
        Instituicao i = new Instituicao();
        Terminal.tentar(() -> i.setNome(e.lerTexto("Nome da instituicao")));
        i.setSigla(e.lerTexto("Sigla", null));
        Terminal.tentar(() -> i.setCidade(e.lerTexto("Cidade")));
        Terminal.tentar(() -> i.setEstado(e.lerTexto("UF", "SP")));
        instituicaoDAO.inserir(i);
        Terminal.sucesso("Instituicao " + i.getCodigo() + " cadastrada: " + i);
    }

    private void atualizarPreco() throws Exception {
        Entrada e = Terminal.entrada();
        List<Combustivel> lista = combustivelDAO.listar();
        for (Combustivel c : lista) {
            Terminal.info(c.getCodigo() + " - " + c + " (atualizado em " + Formatador.data(c.getDataAtualizacao()) + ")");
        }
        int codigo = e.lerInteiro("Codigo do combustivel");
        BigDecimal preco = e.lerDecimal("Novo preco do litro");
        if (preco.signum() <= 0) {
            throw new VamoJuntoException("O preco deve ser maior que zero.");
        }
        if (!combustivelDAO.atualizarPreco(codigo, preco)) {
            throw new VamoJuntoException("Combustivel nao encontrado.");
        }
        Terminal.sucesso("Preco atualizado para " + Formatador.moeda(preco) + ".");
        Terminal.info("Viagens ja criadas mantem o preco antigo (preco congelado na criacao).");
    }

    private void listarVeiculos() throws Exception {
        List<String[]> linhas = new ArrayList<>();
        for (Veiculo v : veiculoDAO.listar()) {
            linhas.add(new String[]{String.valueOf(v.getCodigo()), v.getMarca(), v.getModelo(), v.getCor(),
                v.getPlaca(), String.valueOf(v.getAno()), String.valueOf(v.getQtdLugares())});
        }
        TabelaConsole.imprimir(new String[]{"codigo", "marca", "modelo", "cor", "placa", "ano", "lugares"}, linhas);
    }

    private void informacoesConexao() throws Exception {
        Terminal.campo("Servidor", Conexao.testar());
        Terminal.campo("URL JDBC", Conexao.getUrl());
        Terminal.campo("Usuario do banco", Conexao.getUsuario() + " (menor privilegio, nunca root)");
        Terminal.campo("Conexoes abertas", Conexao.getConexoesAbertas() + " nesta execucao (atributo estatico)");
    }
}
