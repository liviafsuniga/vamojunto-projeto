package vamojunto.console;

/**
 * Item de menu que executa um metodo de uma tela. Ex.:
 *   new AcaoMenu("Criar conta", () -> cadastrarUsuario())
 */
public class AcaoMenu implements OpcaoMenu {

    /** O que a opcao faz (pode lancar erro de banco ou de regra). */
    @FunctionalInterface
    public interface Acao {
        void executar() throws Exception;
    }

    private final String titulo;
    private final Acao acao;

    public AcaoMenu(String titulo, Acao acao) {
        this.titulo = titulo;
        this.acao = acao;
    }

    @Override
    public String getTitulo() {
        return titulo;
    }

    @Override
    public void executar() throws Exception {
        Terminal.titulo(titulo);
        acao.executar();
    }
}
