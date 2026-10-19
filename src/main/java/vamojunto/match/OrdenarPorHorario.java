package vamojunto.match;

import java.util.Comparator;

/** Quem sai primeiro aparece primeiro. */
public class OrdenarPorHorario implements CriterioOrdenacao {

    @Override
    public String getNome() {
        return "Saida mais cedo";
    }

    @Override
    public Comparator<ResultadoMatch> comparador() {
        return Comparator.comparing(ResultadoMatch::getSaidaOrdenavel);
    }
}
