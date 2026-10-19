package vamojunto.tela;

import java.util.ArrayList;
import java.util.List;
import vamojunto.console.Entrada;
import vamojunto.console.Sessao;
import vamojunto.console.TabelaConsole;
import vamojunto.console.Terminal;
import vamojunto.dao.AvaliacaoDAO;
import vamojunto.dao.ConsultaDAO;
import vamojunto.dao.UsuarioDAO;
import vamojunto.modelo.Avaliacao;
import vamojunto.modelo.Motorista;
import vamojunto.modelo.Usuario;
import vamojunto.util.Formatador;
import vamojunto.util.VamoJuntoException;

/** Funcoes comuns da conta: RF10 (historico), RF11 (avaliacao), perfil e dados. */
public class TelaConta {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();

    /** RF11 - passageiro avalia motorista e motorista avalia passageiro. */
    public void avaliar() throws Exception {
        Entrada e = Terminal.entrada();
        List<Avaliacao> pendentes = avaliacaoDAO.listarPendentes(Sessao.getCodigo());
        if (pendentes.isEmpty()) {
            Terminal.info("Nenhuma carona para avaliar. As avaliacoes abrem quando a viagem e concluida.");
            return;
        }
        List<String[]> linhas = new ArrayList<>();
        for (int i = 0; i < pendentes.size(); i++) {
            Avaliacao a = pendentes.get(i);
            String papel = a.getAutor().name().equals("PASSAGEIRO") ? "passageiro(a)" : "motorista";
            linhas.add(new String[]{String.valueOf(i + 1), String.valueOf(a.getCodViagem()), a.getDataViagem(),
                papel, a.getNomeAvaliado()});
        }
        TabelaConsole.imprimir(new String[]{"#", "viagem", "data", "voce foi", "avaliar"}, linhas);

        int escolha = e.lerInteiro("Qual deseja avaliar (0 = voltar)", 0, pendentes.size());
        if (escolha == 0) {
            return;
        }
        Avaliacao a = pendentes.get(escolha - 1);
        Terminal.tentar(() -> a.setNota(e.lerInteiro("Nota para " + a.getNomeAvaliado(), 1, 5)));
        Terminal.tentar(() -> a.setComentario(e.lerTexto("Comentario", null)));
        avaliacaoDAO.inserir(a);
        Terminal.sucesso("Avaliacao registrada: " + a.getEstrelas() + " (" + a.getNota() + "/5). Obrigado!");
    }

    /** RF10 - historico como motorista e como passageiro (UNION ALL no banco). */
    public void historico() throws Exception {
        ConsultaDAO.imprimirProcedure("sp_historico_usuario", Sessao.getCodigo());
    }

    public void detalhesViagem() throws Exception {
        int codigo = Terminal.entrada().lerInteiro("Codigo da viagem");
        ConsultaDAO.imprimirProcedure("sp_detalhes_viagem", codigo);
        Terminal.info("Rota:");
        ConsultaDAO.imprimirProcedure("sp_rota_viagem", codigo);
    }

    public void perfil() throws Exception {
        Usuario u = usuarioDAO.buscarPorCodigo(Sessao.getCodigo());
        Sessao.entrar(u);  // atualiza a sessao com os dados mais recentes
        UsuarioDAO.Indicadores ind = usuarioDAO.buscarIndicadores(u.getCodigo());

        Terminal.campo("Nome", u.getNome());
        Terminal.campo("E-mail", u.getEmail());
        Terminal.campo("CPF", u.getCpfMascarado());
        Terminal.campo("Telefone", u.getTelefone());
        Terminal.campo("Idade", u.getIdade() + " anos");
        Terminal.campo("Instituicao", u.getInstituicao() == null ? "nenhuma" : u.getInstituicao());
        Terminal.campo("Conta verificada", u.isVerificado() ? "Sim" : "Nao (aguardando administrador)");
        Terminal.campo("Perfil", u.getPerfil());   // polimorfismo: Usuario ou Motorista
        Terminal.campo("Membro desde", Formatador.data(u.getDataCadastro()));
        Terminal.campo("Nota como passageiro", Formatador.nota(ind.notaComoPassageiro));
        Terminal.campo("Caronas concluidas", ind.caronasConcluidas);
        Terminal.campo("Total pago em caronas", Formatador.moeda(ind.totalGasto));
        if (u instanceof Motorista) {
            Motorista m = (Motorista) u;
            Terminal.campo("CNH", Formatador.mascararDocumento(m.getCnh()) + " - categoria " + m.getCategoriaCnh());
            Terminal.campo("Validade da CNH", Formatador.data(m.getValidadeCnh()));
            Terminal.campo("Nota como motorista", Formatador.nota(ind.notaComoMotorista));
            Terminal.campo("Viagens concluidas", ind.viagensConcluidas);
        }
    }

    public void alterarDados() throws Exception {
        Entrada e = Terminal.entrada();
        Usuario u = usuarioDAO.buscarPorCodigo(Sessao.getCodigo());
        String opcao = e.lerOpcao("O que deseja alterar", "telefone", "senha");
        if (opcao.equals("telefone")) {
            Terminal.tentar(() -> u.setTelefone(e.lerTexto("Novo telefone com DDD")));
            usuarioDAO.alterarTelefone(u.getCodigo(), u.getTelefone());
            Terminal.sucesso("Telefone atualizado.");
            return;
        }
        if (!u.autenticar(e.lerSenha("Senha atual"))) {
            throw new VamoJuntoException("Senha atual incorreta.");
        }
        Terminal.tentar(() -> {
            String nova = e.lerSenha("Nova senha (minimo 6 caracteres)");
            if (!nova.equals(e.lerSenha("Repita a nova senha"))) {
                throw new IllegalArgumentException("As senhas nao conferem.");
            }
            u.definirSenha(nova);
        });
        usuarioDAO.alterarSenha(u.getCodigo(), u.getSenhaHash());
        Terminal.sucesso("Senha alterada.");
    }
}
