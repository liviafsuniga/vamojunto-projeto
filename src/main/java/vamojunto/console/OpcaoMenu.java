package vamojunto.console;

/**
 * Contrato de todo item de menu: consultas, operacoes, acoes e submenus.
 *
 * POO: INTERFACE. O Menu guarda uma List&lt;OpcaoMenu&gt; e chama executar()
 * sem saber qual classe concreta esta ali - isso e POLIMORFISMO.
 * Padrao de projeto: Command (cada opcao e um objeto que sabe se executar).
 */
public interface OpcaoMenu {

    String getTitulo();

    void executar() throws Exception;
}
