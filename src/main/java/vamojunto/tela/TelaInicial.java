package vamojunto.tela;

import java.util.ArrayList;
import java.util.List;
import vamojunto.console.Entrada;
import vamojunto.console.Menu;
import vamojunto.console.Sessao;
import vamojunto.console.TabelaConsole;
import vamojunto.console.Terminal;
import vamojunto.dao.InstituicaoDAO;
import vamojunto.dao.UsuarioDAO;
import vamojunto.modelo.Instituicao;
import vamojunto.modelo.Usuario;
import vamojunto.util.VamoJuntoException;

/**
 * Primeira tela: o que o VISITANTE pode fazer (caso de uso do Visitante):
 * entrar, criar conta, buscar viagens, ver consultas e acessar o administrador.
 */
public class TelaInicial {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final InstituicaoDAO instituicaoDAO = new InstituicaoDAO();

    public Menu criarMenu() {
        return new Menu("VAMOJUNTO - Voce ja vai? Vamos juntos.", "Sair do sistema")
                .adicionar("Entrar (login)", this::entrar)
                .adicionar("Criar conta", this::criarConta)
                .adicionar(TelaPassageiro.consultaBuscarViagens("Buscar viagens disponiveis (sem login)"))
                .adicionar(new TelaRelatorios().criarMenu())
                .adicionar("Area do administrador", this::areaAdministrador);
    }

    /** RF01 - login com e-mail e senha. */
    private void entrar() throws Exception {
        Entrada e = Terminal.entrada();
        String email = e.lerTexto("E-mail");
        String senha = e.lerSenha("Senha");
        Usuario u = usuarioDAO.autenticar(email, senha);
        if (u == null) {
            // mensagem generica: nao revela se o e-mail existe (seguranca)
            throw new VamoJuntoException("E-mail ou senha incorretos, ou conta desativada.");
        }
        Sessao.entrar(u);
        Terminal.sucesso("Bem-vindo(a), " + u.getNome() + "!");
        new TelaUsuario().criarMenu().executar();
        Sessao.sair();
        Terminal.info("Voce saiu da sua conta.");
    }

    /** RF01 - cadastro (18+ anos, CPF e e-mail unicos, senha guardada como hash). */
    private void criarConta() throws Exception {
        Entrada e = Terminal.entrada();
        Usuario u = new Usuario();

        Terminal.tentar(() -> u.setNome(e.lerTexto("Nome completo")));
        Terminal.tentar(() -> u.setCpf(e.lerTexto("CPF (so numeros ou com pontos)")));
        Terminal.tentar(() -> {
            u.setEmail(e.lerTexto("E-mail"));
            if (usuarioDAO.buscarPorEmail(u.getEmail()) != null) {
                throw new IllegalArgumentException("Ja existe uma conta com este e-mail.");
            }
        });
        Terminal.tentar(() -> u.setTelefone(e.lerTexto("Telefone com DDD")));
        Terminal.tentar(() -> u.setDataNascimento(e.lerData("Data de nascimento")));

        List<Instituicao> instituicoes = instituicaoDAO.listar();
        imprimirInstituicoes(instituicoes);
        Terminal.tentar(() -> {
            Integer cod = e.lerInteiroOpcional("Codigo da sua instituicao de ensino", "nenhuma");
            u.setInstituicao(cod == null ? null : procurar(instituicoes, cod));
        });

        Terminal.tentar(() -> {
            String senha = e.lerSenha("Crie uma senha (minimo 6 caracteres)");
            String repetir = e.lerSenha("Repita a senha");
            if (!senha.equals(repetir)) {
                throw new IllegalArgumentException("As senhas nao conferem.");
            }
            u.definirSenha(senha);
        });

        Terminal.info("");
        Terminal.campo("Nome", u.getNome());
        Terminal.campo("CPF", u.getCpfMascarado());
        Terminal.campo("E-mail", u.getEmail());
        Terminal.campo("Idade", u.getIdade() + " anos");
        Terminal.campo("Instituicao", u.getInstituicao() == null ? "nenhuma" : u.getInstituicao());
        if (!e.confirmar("Confirma o cadastro?")) {
            Terminal.info("Cadastro cancelado.");
            return;
        }
        int codigo = usuarioDAO.inserir(u);
        Terminal.sucesso("Conta criada! Seu codigo e " + codigo + ". Ja pode entrar com seu e-mail.");
        Terminal.info("Para oferecer caronas, sua conta precisa ser verificada pelo administrador.");
    }

    private void areaAdministrador() throws Exception {
        String senha = Terminal.entrada().lerSenha("Senha do administrador");
        if (!TelaAdministrador.senhaCorreta(senha)) {
            throw new VamoJuntoException("Senha do administrador incorreta.");
        }
        new TelaAdministrador().criarMenu().executar();
    }

    static void imprimirInstituicoes(List<Instituicao> instituicoes) {
        List<String[]> linhas = new ArrayList<>();
        for (Instituicao i : instituicoes) {
            linhas.add(new String[]{String.valueOf(i.getCodigo()), i.getSigla() == null ? "-" : i.getSigla(),
                i.getNome(), i.getCidade() + "/" + i.getEstado()});
        }
        TabelaConsole.imprimir(new String[]{"codigo", "sigla", "instituicao", "cidade"}, linhas);
    }

    static Instituicao procurar(List<Instituicao> lista, int codigo) {
        for (Instituicao i : lista) {
            if (i.getCodigo() == codigo) {
                return i;
            }
        }
        throw new IllegalArgumentException("Instituicao " + codigo + " nao existe. Deixe em branco se nao tiver.");
    }
}
