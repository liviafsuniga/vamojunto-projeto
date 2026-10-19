package vamojunto.console;

/** Lancada quando a entrada padrao termina (Ctrl+D / fim de arquivo): o programa fecha sem erro. */
public class EntradaEncerradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntradaEncerradaException() {
        super("Entrada encerrada.");
    }
}
