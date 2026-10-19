package vamojunto.match;

import java.util.Comparator;

/** Trajeto mais parecido primeiro (desempate pelo horario). Criterio padrao. */
public class OrdenarPorSimilaridade implements CriterioOrdenacao {

    @Override
    public String getNome() {
        return "Trajeto mais parecido";
    }

    @Override
    public Comparator<ResultadoMatch> comparador() {
        return Comparator.comparingInt(ResultadoMatch::getSimilaridade).reversed()
                .thenComparing(ResultadoMatch::getSaidaOrdenavel);
    }
}
