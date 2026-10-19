package vamojunto.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    private Usuario novo() {
        return new Usuario("Mariana Silva", "123.456.789-01", "Mariana@Facens.br", "(15) 99999-0000",
                LocalDate.now().minusYears(20));
    }

    @Test
    void normalizaCpfEmailETelefone() {
        Usuario u = novo();
        assertEquals("12345678901", u.getCpf());
        assertEquals("mariana@facens.br", u.getEmail());
        assertEquals("15999990000", u.getTelefone());
        assertEquals("123.***.***-01", u.getCpfMascarado());
    }

    @Test
    void menorDe18NaoPodeSeCadastrar() {
        assertThrows(IllegalArgumentException.class, () -> new Usuario("Joao Pedro", "12345678901",
                "joao@x.com", "15999990000", LocalDate.now().minusYears(17)));
    }

    @Test
    void senhaGuardadaSoComoHash() {
        Usuario u = novo();
        u.definirSenha("vamo123");
        assertNotEquals("vamo123", u.getSenhaHash());
        assertTrue(u.autenticar("vamo123"));
        assertFalse(u.autenticar("errada"));
        u.setAtivo(false);
        assertFalse(u.autenticar("vamo123"), "usuario inativo nao entra");
    }

    @Test
    void senhaCurtaERecusada() {
        assertThrows(IllegalArgumentException.class, () -> novo().definirSenha("123"));
    }

    @Test
    void soVerificadoPodeSerMotoristaEPolimorfismo() {
        Usuario u = novo();
        assertFalse(u.podeSerMotorista());
        u.setVerificado(true);
        assertTrue(u.podeSerMotorista());

        Usuario comoUsuario = new Motorista(u, "01234567890", CategoriaCnh.AB, LocalDate.now().plusYears(1));
        assertTrue(comoUsuario.isMotorista());
        assertEquals("MOTORISTA", comoUsuario.getPerfil());
        assertEquals("PASSAGEIRO", u.getPerfil());
        assertEquals(u.getEmail(), comoUsuario.getEmail(), "construtor de copia manteve os dados");
    }

    @Test
    void categoriaASoMotoNaoEAceita() {
        assertThrows(IllegalArgumentException.class, () -> CategoriaCnh.deTexto("A"));
        assertEquals(CategoriaCnh.AB, CategoriaCnh.deTexto(" ab "));
    }

    @Test
    void transicoesDoPedidoIguaisATrigger() {
        assertTrue(StatusSolicitacao.PENDENTE.podeMudarPara(StatusSolicitacao.ACEITA));
        assertTrue(StatusSolicitacao.ACEITA.podeMudarPara(StatusSolicitacao.CANCELADA));
        assertFalse(StatusSolicitacao.ACEITA.podeMudarPara(StatusSolicitacao.PENDENTE));
        assertFalse(StatusSolicitacao.CANCELADA.podeMudarPara(StatusSolicitacao.ACEITA));
    }

    @Test
    void notaDaAvaliacaoDe1a5() {
        assertThrows(IllegalArgumentException.class, () -> new Avaliacao(1, AutorAvaliacao.PASSAGEIRO, 6, null));
        assertEquals("****.", new Avaliacao(1, AutorAvaliacao.PASSAGEIRO, 4, "Boa").getEstrelas());
    }
}
