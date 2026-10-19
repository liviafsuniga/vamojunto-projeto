package vamojunto.util;

/**
 * Erro de regra de negocio detectado pelo proprio Java (antes de ir ao banco).
 * Ex.: "Voce ainda nao e motorista", "Esta viagem nao e sua".
 *
 * POO: heranca (estende Exception) e sobrecarga de construtor.
 */
public class VamoJuntoException extends Exception {

    private static final long serialVersionUID = 1L;

    public VamoJuntoException(String mensagem) {
        super(mensagem);
    }

    public VamoJuntoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
