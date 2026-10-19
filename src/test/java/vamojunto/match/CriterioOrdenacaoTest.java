package vamojunto.match;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Testa o padrao Strategy: a mesma lista, quatro ordens diferentes. */
class CriterioOrdenacaoTest {

    private ResultadoMatch r(int viagem, String saida, String preco, String nota, int similaridade) {
        ResultadoMatch m = new ResultadoMatch();
        m.setCodViagem(viagem);
        m.setSaida(saida);
        m.setRateioEstimado(new BigDecimal(preco));
        m.setNotaMotorista(new BigDecimal(nota));
        m.setSimilaridade(similaridade);
        return m;
    }

    private List<ResultadoMatch> lista() {
        List<ResultadoMatch> l = new ArrayList<>();
        l.add(r(1, "10/12/2026 07:00", "9.91", "4.50", 2));
        l.add(r(2, "04/11/2026 18:30", "1.81", "5.00", 4));
        l.add(r(3, "05/11/2026 07:00", "5.00", "3.00", 6));
        return l;
    }

    @Test
    void cadaEstrategiaOrdenaDoSeuJeito() {
        List<ResultadoMatch> l = lista();
        new OrdenarPorHorario().ordenar(l);
        assertEquals(2, l.get(0).getCodViagem());

        new OrdenarPorPreco().ordenar(l);
        assertEquals(2, l.get(0).getCodViagem());
        assertEquals(1, l.get(2).getCodViagem());

        new OrdenarPorNota().ordenar(l);
        assertEquals(2, l.get(0).getCodViagem());
        assertEquals(3, l.get(2).getCodViagem());

        CriterioOrdenacao padrao = new OrdenarPorSimilaridade();  // polimorfismo
        padrao.ordenar(l);
        assertEquals(3, l.get(0).getCodViagem());
    }
}
