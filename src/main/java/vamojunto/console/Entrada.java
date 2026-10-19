package vamojunto.console;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Contrato de leitura de dados do usuario.
 * Implementacoes: EntradaConsole (terminal, padrao) e EntradaDialogo (JOptionPane).
 *
 * POO: INTERFACE + POLIMORFISMO - as telas usam apenas "Entrada" e nao sabem
 * se os dados vem do teclado no terminal ou de uma caixa de dialogo.
 * Os varios "ler..." com o mesmo nome e parametros diferentes sao SOBRECARGA.
 */
public interface Entrada {

    /** Le uma linha "crua" (sem validacao). */
    String lerLinha(String rotulo);

    /** Texto obrigatorio. */
    String lerTexto(String rotulo);

    /** Texto opcional: Enter devolve o valor padrao (pode ser null). */
    String lerTexto(String rotulo, String padrao);

    String lerSenha(String rotulo);

    int lerInteiro(String rotulo);

    int lerInteiro(String rotulo, int minimo, int maximo);

    /** Enter devolve null ("[Enter = todos]"). */
    Integer lerInteiroOpcional(String rotulo);

    /** Sobrecarga: Enter devolve null, com a dica escolhida ("[Enter = nenhuma]"). */
    Integer lerInteiroOpcional(String rotulo, String dicaEnter);

    BigDecimal lerDecimal(String rotulo);

    /** Enter devolve null. */
    BigDecimal lerDecimalOpcional(String rotulo);

    LocalDate lerData(String rotulo);

    /** Enter devolve null. */
    LocalDate lerDataOpcional(String rotulo);

    /** Enter devolve o padrao. Formato dd/mm/aaaa hh:mm */
    LocalDateTime lerDataHora(String rotulo, LocalDateTime padrao);

    boolean confirmar(String pergunta);

    /** Escolhe um texto dentre as opcoes (sem diferenciar maiusculas). */
    String lerOpcao(String rotulo, String... opcoes);

    /** Mostra uma mensagem de erro de digitacao. */
    void avisar(String mensagem);
}
