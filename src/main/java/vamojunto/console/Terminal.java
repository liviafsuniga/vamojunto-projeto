package vamojunto.console;

/**
 * Ponto unico de entrada e saida do console.
 *
 * POO: ATRIBUTO ESTATICO (a Entrada escolhida vale para o programa todo) e
 * METODOS ESTATICOS de exibicao, usados por todas as telas.
 */
public final class Terminal {

    private static Entrada entrada = new EntradaConsole();

    /** Um passo de formulario que pode falhar na validacao (lambda). */
    @FunctionalInterface
    public interface Passo {
        void executar() throws Exception;
    }

    private Terminal() {
    }

    public static Entrada entrada() {
        return entrada;
    }

    public static void usar(Entrada novaEntrada) {
        entrada = novaEntrada;
    }

    public static void cabecalho(String titulo) {
        String linha = "=".repeat(Math.max(40, titulo.length() + 6));
        System.out.println();
        System.out.println(linha);
        System.out.println("   " + titulo);
        System.out.println(linha);
    }

    public static void titulo(String titulo) {
        System.out.println();
        System.out.println(">> " + titulo);
    }

    public static void info(String mensagem) {
        System.out.println("   " + mensagem);
    }

    public static void sucesso(String mensagem) {
        System.out.println("   OK: " + mensagem);
    }

    public static void erro(String mensagem) {
        System.out.println("   ERRO: " + mensagem);
    }

    /** Linha "rotulo ......: valor" para fichas e resumos. */
    public static void campo(String rotulo, Object valor) {
        String r = rotulo.length() >= 24 ? rotulo : rotulo + " " + ".".repeat(23 - rotulo.length());
        System.out.println("   " + r + ": " + (valor == null ? "-" : valor));
    }

    public static void pausar() {
        entrada.lerLinha("\nPressione Enter para continuar...");
    }

    /**
     * Repete o passo ate os dados serem validos: se um setter lancar
     * IllegalArgumentException, mostra a mensagem e pergunta de novo
     * (melhor usabilidade do que abortar o formulario inteiro).
     */
    public static void tentar(Passo passo) throws Exception {
        while (true) {
            try {
                passo.executar();
                return;
            } catch (IllegalArgumentException e) {
                entrada.avisar(e.getMessage());
            }
        }
    }
}
