package vamojunto.console;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vamojunto.util.ErroBanco;
import vamojunto.util.VamoJuntoException;

/**
 * Menu do console. Um Menu tambem e uma OpcaoMenu, entao um menu pode
 * conter outros menus (padrao de projeto Composite).
 *
 * POO: LISTA DE OBJETOS (ArrayList&lt;OpcaoMenu&gt;), POLIMORFISMO na chamada
 * opcao.executar(), estrutura de REPETICAO (while) e CONDICAO (if).
 * Todo erro e tratado aqui: o programa mostra a mensagem e continua.
 */
public class Menu implements OpcaoMenu {

    private final String titulo;
    private final String textoSair;
    private final List<OpcaoMenu> opcoes = new ArrayList<>();

    public Menu(String titulo) {
        this(titulo, "Voltar");
    }

    public Menu(String titulo, String textoSair) {
        this.titulo = titulo;
        this.textoSair = textoSair;
    }

    /** Adiciona e devolve o proprio menu, para encadear: menu.adicionar(a).adicionar(b) */
    public Menu adicionar(OpcaoMenu opcao) {
        opcoes.add(opcao);
        return this;
    }

    /** Sobrecarga: cria a AcaoMenu na hora. */
    public Menu adicionar(String titulo, AcaoMenu.Acao acao) {
        return adicionar(new AcaoMenu(titulo, acao));
    }

    @Override
    public String getTitulo() {
        return titulo;
    }

    public List<OpcaoMenu> getOpcoes() {
        return opcoes;
    }

    @Override
    public void executar() {
        while (true) {
            Terminal.cabecalho(titulo);
            for (int i = 0; i < opcoes.size(); i++) {
                System.out.printf("   %2d - %s%n", i + 1, opcoes.get(i).getTitulo());
            }
            System.out.printf("   %2d - %s%n", 0, textoSair);
            int escolha = Terminal.entrada().lerInteiro("Opcao", 0, opcoes.size());
            if (escolha == 0) {
                return;
            }
            OpcaoMenu opcao = opcoes.get(escolha - 1);
            executarComTratamento(opcao);
            if (!(opcao instanceof Menu)) {
                Terminal.pausar();
            }
        }
    }

    /** Executa a opcao sem deixar nenhum erro derrubar o programa. */
    public static void executarComTratamento(OpcaoMenu opcao) {
        try {
            opcao.executar();
        } catch (EntradaEncerradaException e) {
            throw e;
        } catch (VamoJuntoException | IllegalArgumentException e) {
            Terminal.erro(e.getMessage());
        } catch (SQLException e) {
            Terminal.erro(ErroBanco.traduzir(e));
        } catch (Exception e) {
            Terminal.erro("Erro inesperado: " + e);
        }
    }
}
