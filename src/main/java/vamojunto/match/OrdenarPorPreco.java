package vamojunto.match;

import java.math.BigDecimal;
import java.util.Comparator;

/** Carona mais barata primeiro. */
public class OrdenarPorPreco implements CriterioOrdenacao {

    @Override
    public String getNome() {
        return "Menor preco";
    }

    @Override
    public Comparator<ResultadoMatch> comparador() {
        return Comparator.comparing(
                (ResultadoMatch r) -> r.getRateioEstimado() == null ? BigDecimal.valueOf(Long.MAX_VALUE) : r.getRateioEstimado());
    }
}
