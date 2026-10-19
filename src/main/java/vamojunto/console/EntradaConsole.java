package vamojunto.console;

import java.io.Console;
import java.util.Scanner;

/**
 * Leitura pelo terminal (padrao do projeto nesta etapa).
 * POO: HERANCA de EntradaBase e SOBRESCRITA de lerLinha, lerSenha e avisar.
 */
public class EntradaConsole extends EntradaBase {

    private final Scanner scanner;

    public EntradaConsole() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public String lerLinha(String rotulo) {
        System.out.print(rotulo);
        System.out.flush();
        if (!scanner.hasNextLine()) {
            throw new EntradaEncerradaException();
        }
        return scanner.nextLine();
    }

    /** No terminal de verdade a senha nao aparece; no NetBeans aparece (nao ha System.console()). */
    @Override
    public String lerSenha(String rotulo) {
        Console console = System.console();
        if (console != null) {
            char[] senha = console.readPassword(rotulo + ": ");
            if (senha == null) {
                throw new EntradaEncerradaException();
            }
            return new String(senha);
        }
        return super.lerSenha(rotulo);
    }

    @Override
    public void avisar(String mensagem) {
        System.out.println("  ! " + mensagem);
    }
}
