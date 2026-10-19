package vamojunto;

import java.sql.SQLException;
import vamojunto.conexao.Conexao;
import vamojunto.console.EntradaDialogo;
import vamojunto.console.EntradaEncerradaException;
import vamojunto.console.Terminal;
import vamojunto.tela.TelaInicial;
import vamojunto.util.ErroBanco;

/**
 * VamoJunto - aplicativo de caronas compartilhadas em tempo real.
 * UPX 2 - ADS Facens - Equipe Vision Hexa.
 *
 * Ponto de entrada do console. Fluxo: testa a conexao com o banco e abre o
 * menu inicial. Rodar no NetBeans: F6 (classe principal vamojunto.App).
 *
 * Opcoes (VM options / -D):
 *   -Ddb.url=...  -Ddb.usuario=...  -Ddb.senha=...   conexao com o banco
 *   -Dentrada=dialogo                                 entrada por JOptionPane
 *   -Dadmin.senha=...                                 senha da area do administrador
 */
public class App {

    public static void main(String[] args) {
        if ("dialogo".equalsIgnoreCase(System.getProperty("entrada"))) {
            Terminal.usar(new EntradaDialogo());
        }

        System.out.println();
        System.out.println("  __   __                     _              _        ");
        System.out.println("  \\ \\ / /_ _ _ __ ___   ___  | |_   _ _ __ | |_ ___  ");
        System.out.println("   \\ V / _` | '_ ` _ \\ / _ \\ | | | | | '_ \\| __/ _ \\ ");
        System.out.println("    | | (_| | | | | | | (_) || | |_| | | | | || (_) |");
        System.out.println("    |_|\\__,_|_| |_| |_|\\___/_/ |\\__,_|_| |_|\\__\\___/ ");
        System.out.println("                          |__/   Voce ja vai? Vamos juntos.");
        System.out.println();

        try {
            Terminal.info("Conectado ao banco: " + Conexao.testar());
        } catch (SQLException e) {
            Terminal.erro(ErroBanco.traduzir(e));
            Terminal.info("Confira: 1) o MySQL esta ligado; 2) o script banco/vamojunto.sql foi executado;");
            Terminal.info("3) a URL " + Conexao.getUrl());
            System.exit(1);
            return;
        }

        try {
            new TelaInicial().criarMenu().executar();
        } catch (EntradaEncerradaException e) {
            System.out.println();
        }
        System.out.println();
        Terminal.info("Obrigado por usar o VamoJunto. Ate a proxima!");
    }
}
