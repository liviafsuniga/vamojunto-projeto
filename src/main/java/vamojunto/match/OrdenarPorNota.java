package vamojunto.match;

import java.math.BigDecimal;
import java.util.Comparator;

/** Motorista mais bem avaliado primeiro. */
public class OrdenarPorNota implements CriterioOrdenacao {

    @Override
    public String getNome() {
        return "Melhor nota do motorista";
    }

    @Override
    public Comparator<ResultadoMatch> comparador() {
        Comparator<ResultadoMatch> porNota = Comparator.comparing(
                (ResultadoMatch r) -> r.getNotaMotorista() == null ? BigDecimal.ZERO : r.getNotaMotorista());
        return porNota.reversed().thenComparing(ResultadoMatch::getSaidaOrdenavel);
    }
}
