package vamojunto.match;

import java.util.Comparator;
import java.util.List;

/**
 * Padrao de projeto STRATEGY: cada criterio e uma classe diferente que
 * ordena a lista de caronas do seu jeito. A tela escolhe a estrategia em
 * tempo de execucao e chama ordenar() sem saber qual e (POLIMORFISMO).
 */
public interface CriterioOrdenacao {

    String getNome();

    Comparator<ResultadoMatch> comparador();

    /** Metodo default da interface: igual para todas as estrategias. */
    default void ordenar(List<ResultadoMatch> resultados) {
        resultados.sort(comparador());
    }
}
