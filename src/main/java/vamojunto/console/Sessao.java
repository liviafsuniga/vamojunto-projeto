package vamojunto.console;

import vamojunto.modelo.Motorista;
import vamojunto.modelo.Usuario;
import vamojunto.util.VamoJuntoException;

/**
 * Guarda quem esta logado. ATRIBUTO ESTATICO: so existe um usuario logado
 * por vez no console, e qualquer tela consegue consultar.
 */
public final class Sessao {

    private static Usuario usuario;

    private Sessao() {
    }

    public static void entrar(Usuario u) {
        usuario = u;
    }

    public static void sair() {
        usuario = null;
    }

    public static boolean estaLogado() {
        return usuario != null;
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static int getCodigo() {
        return usuario == null ? 0 : usuario.getCodigo();
    }

    /** Devolve o motorista logado ou explica o que falta (RF02). */
    public static Motorista exigirMotorista() throws VamoJuntoException {
        if (usuario instanceof Motorista) {
            Motorista m = (Motorista) usuario;
            if (!m.isMotoristaAtivo()) {
                throw new VamoJuntoException("Seu cadastro de motorista esta inativo.");
            }
            return m;
        }
        throw new VamoJuntoException("Voce ainda nao e motorista. Use a opcao 'Quero ser motorista'.");
    }
}
