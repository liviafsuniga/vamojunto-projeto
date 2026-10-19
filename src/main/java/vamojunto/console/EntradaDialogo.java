package vamojunto.console;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/**
 * Leitura por caixas de dialogo (aulas "Praticando encapsulamento" -
 * JOptionPane - e "Java Swing"). Os resultados continuam no console.
 *
 * Para usar: rode o programa com  -Dentrada=dialogo
 *
 * POO: mesma interface (Entrada) com outra implementacao = POLIMORFISMO.
 */
public class EntradaDialogo extends EntradaBase {

    private static final String TITULO = "VamoJunto";

    @Override
    public String lerLinha(String rotulo) {
        String valor = JOptionPane.showInputDialog(null, rotulo, TITULO, JOptionPane.QUESTION_MESSAGE);
        if (valor == null) {           // usuario clicou em Cancelar / fechou
            return "";
        }
        System.out.println(rotulo + valor);
        return valor;
    }

    @Override
    public String lerSenha(String rotulo) {
        JPasswordField campo = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(null, campo, rotulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        return ok == JOptionPane.OK_OPTION ? new String(campo.getPassword()) : "";
    }

    @Override
    public void avisar(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, TITULO, JOptionPane.WARNING_MESSAGE);
    }
}
