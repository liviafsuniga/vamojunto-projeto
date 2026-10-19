package vamojunto.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ValidadorTest {

    @Test
    void placaAntigaEMercosul() {
        assertTrue(Validador.placaValida("DKS1234"));
        assertTrue(Validador.placaValida("FTR4A21"));
        assertFalse(Validador.placaValida("AB12345"));
        assertFalse(Validador.placaValida("ftr4a21"));
    }

    @Test
    void emailTelefoneCepEUf() {
        assertTrue(Validador.emailValido("elisa.rocha@outlook.com"));
        assertFalse(Validador.emailValido("sem-arroba.com"));
        assertTrue(Validador.telefoneValido("1532221005"));
        assertFalse(Validador.telefoneValido("123"));
        assertTrue(Validador.cepValido(null));
        assertFalse(Validador.cepValido("1801"));
        assertTrue(Validador.ufValida("sp"));
        assertFalse(Validador.ufValida("XX"));
    }

    @Test
    void hashIgualAoSha2DoMySQL() {
        // SELECT SHA2('vamo123', 256);
        assertEquals("d4a72d8897517acbbf6863d7cf363e1c36f77e248066cffc00f549aed8578753",
                Seguranca.gerarHash("vamo123"));
    }

    @Test
    void moedaNoPadraoBrasileiro() {
        assertEquals("R$ 1.234,50", Formatador.moeda(new BigDecimal("1234.5")));
        assertEquals("R$ 0,00", Formatador.moeda(BigDecimal.ZERO));
        assertEquals("*******7890", Formatador.mascararDocumento("01234567890"));
    }
}
