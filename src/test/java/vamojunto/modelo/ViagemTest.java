package vamojunto.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes de unidade das regras de custo e rota (Eng. de Software - Qualidade e Testes).
 * Os valores esperados sao os mesmos do exemplo da documentacao (viagem 1).
 */
class ViagemTest {

    private Motorista ana;
    private Veiculo onix;
    private Combustivel gasolina;

    @BeforeEach
    void preparar() {
        Usuario u = new Usuario("Ana Souza", "11122233344", "ana@uniso.br", "15991110001", LocalDate.of(2003, 4, 12));
        u.setCodigo(1);
        u.setVerificado(true);
        ana = new Motorista(u, "01234567890", CategoriaCnh.B, LocalDate.now().plusYears(3));
        onix = new Veiculo(1, "Chevrolet", "Onix", "Prata", "FTR4A21", 2021, "12345678901", 5);
        gasolina = new Combustivel(1, TipoCombustivel.GASOLINA, new BigDecimal("6.190"));
    }

    private Viagem viagemExemplo() {
        return ana.criarViagem(onix, gasolina, LocalDateTime.now().plusDays(1), 3,
                new BigDecimal("18.00"), new BigDecimal("12.00"));
    }

    @Test
    void custoDaViagemSegueAFormulaDaDocumentacao() {
        // 18 km / 12 km/l x R$ 6,19 = R$ 9,29
        assertEquals(new BigDecimal("9.29"), viagemExemplo().calcularCusto());
    }

    @Test
    void rateioProporcionalAoTrecho() {
        Viagem v = viagemExemplo();
        // 2 passageiros + motorista: viagem toda = 9,29 / 3 = 3,10
        assertEquals(new BigDecimal("3.10"), v.calcularRateio(new BigDecimal("18.00"), 2));
        // embarca na parada (12 km): 9,29 x 12/18 / 3 = 2,06
        assertEquals(new BigDecimal("2.06"), v.calcularRateio(new BigDecimal("12.00"), 2));
    }

    @Test
    void metodoEstaticoIgualAoDoObjeto() {
        assertEquals(Viagem.calcularRateio(new BigDecimal("9.29"), new BigDecimal("12"), new BigDecimal("18"), 2),
                viagemExemplo().calcularRateio(new BigDecimal("12"), 2));
    }

    @Test
    void vagasNaoPodemPassarDosLugaresMenosOMotorista() {
        assertThrows(IllegalArgumentException.class, () -> ana.criarViagem(onix, gasolina,
                LocalDateTime.now().plusDays(1), 5, BigDecimal.TEN, BigDecimal.TEN));
    }

    @Test
    void cnhVencidaNaDataDaViagemEBloqueada() {
        assertThrows(IllegalArgumentException.class, () -> ana.criarViagem(onix, gasolina,
                LocalDateTime.now().plusYears(10), 2, BigDecimal.TEN, BigDecimal.TEN));
    }

    @Test
    void rotaComecaNaOrigemETerminaNoDestino() {
        Viagem v = viagemExemplo();
        assertThrows(IllegalArgumentException.class,
                () -> v.adicionarPonto(TipoPonto.PARADA, "Rua A", "1", null, "Sorocaba", "SP"));
        v.adicionarPonto(TipoPonto.ORIGEM, "Rua XV de Novembro", "100", "Centro", "Sorocaba", "SP");
        v.adicionarPonto(TipoPonto.PARADA, "Av. General Carneiro", "850", "Lageado", "Sorocaba", "SP");
        v.adicionarPonto(TipoPonto.DESTINO, "Rod. Raposo Tavares", null, null, "Sorocaba", "SP");
        assertThrows(IllegalArgumentException.class,
                () -> v.adicionarPonto(TipoPonto.PARADA, "Depois", "1", null, "Itu", "SP"));
        assertTrue(v.rotaCompleta());
        assertEquals(3, v.getPontos().size());
        assertEquals(2, v.getPontosDeEmbarque().size());
        assertEquals(3, v.getDestino().getOrdem());
    }

    @Test
    void motoristaNaoPedeCaronaNaPropriaViagem() {
        Viagem v = viagemExemplo();
        v.adicionarPonto(TipoPonto.ORIGEM, "Rua XV", "100", null, "Sorocaba", "SP");
        v.adicionarPonto(TipoPonto.DESTINO, "Rua Y", "1", null, "Itu", "SP");
        assertThrows(IllegalArgumentException.class,
                () -> ana.solicitarCarona(v, v.getOrigem(), new BigDecimal("18")));
    }

    @Test
    void passageiroNaoEmbarcaNoDestino() {
        Viagem v = viagemExemplo();
        v.adicionarPonto(TipoPonto.ORIGEM, "Rua XV", "100", null, "Sorocaba", "SP");
        v.adicionarPonto(TipoPonto.DESTINO, "Rua Y", "1", null, "Itu", "SP");
        Usuario bruno = new Usuario("Bruno Lima", "22233344455", "bruno@gmail.com", "15991110002",
                LocalDate.of(2001, 9, 30));
        bruno.setCodigo(2);
        assertThrows(IllegalArgumentException.class,
                () -> bruno.solicitarCarona(v, v.getDestino(), new BigDecimal("5")));
        Solicitacao s = bruno.solicitarCarona(v, v.getOrigem(), new BigDecimal("18"));
        assertEquals(StatusSolicitacao.PENDENTE, s.getStatus());
    }
}
