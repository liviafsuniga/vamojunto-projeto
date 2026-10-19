package vamojunto.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import vamojunto.util.Validador;

/**
 * Motorista e uma especializacao de Usuario (no banco: Motorista.cod_motorista
 * e PK e FK para Usuario). Acrescenta os dados da CNH.
 *
 * POO: HERANCA (extends Usuario), SOBRESCRITA (@Override de isMotorista,
 * getPerfil e toString) e metodos proprios criarViagem() e responderSolicitacao().
 */
public class Motorista extends Usuario {

    private String cnh;
    private CategoriaCnh categoriaCnh;
    private LocalDate validadeCnh;
    private boolean motoristaAtivo = true;

    public Motorista() {
        super();
    }

    /** Copia os dados do usuario; a CNH e preenchida depois pelos setters. */
    public Motorista(Usuario usuario) {
        super(usuario);
    }

    /** "Promove" um usuario ja existente a motorista (RF02). */
    public Motorista(Usuario usuario, String cnh, CategoriaCnh categoria, LocalDate validadeCnh) {
        super(usuario);  // chama o construtor de copia da superclasse
        setCnh(cnh);
        setCategoriaCnh(categoria);
        setValidadeCnh(validadeCnh);
    }

    // ------------------------------------------------------------------
    // Sobrescrita de metodos
    // ------------------------------------------------------------------

    @Override
    public boolean isMotorista() {
        return true;
    }

    @Override
    public String getPerfil() {
        return "MOTORISTA";
    }

    @Override
    public String toString() {
        return super.toString() + " | CNH " + categoriaCnh + " valida ate " + validadeCnh;
    }

    // ------------------------------------------------------------------
    // Metodos de negocio
    // ------------------------------------------------------------------

    public boolean cnhValidaEm(LocalDate data) {
        return validadeCnh != null && !validadeCnh.isBefore(data);
    }

    /**
     * RF04: cria a viagem que o motorista ja vai fazer. Confere as mesmas
     * regras do trg_viagem_bi (motorista ativo, CNH valida e vagas que cabem no carro).
     */
    public Viagem criarViagem(Veiculo veiculo, Combustivel combustivel, LocalDateTime saida,
                              int vagas, BigDecimal distanciaKm, BigDecimal consumoKmL) {
        Validador.exigir(motoristaAtivo && isAtivo(), "Motorista inativo nao pode criar viagem.");
        Validador.exigir(cnhValidaEm(saida.toLocalDate()), "Sua CNH estara vencida na data da viagem.");
        Validador.exigir(vagas >= 1 && vagas <= veiculo.getVagasMaximas(),
                "Vagas deve ser de 1 a " + veiculo.getVagasMaximas() + " (lugares do veiculo menos o motorista).");
        return new Viagem(this, veiculo, combustivel, saida, vagas, distanciaKm, consumoKmL);
    }

    /**
     * RF08: aceita ou recusa um pedido. Devolve o novo status, que o DAO grava
     * com a procedure sp_responder_solicitacao (transacao no banco).
     */
    public StatusSolicitacao responderSolicitacao(Solicitacao solicitacao, boolean aceitar) {
        Validador.exigir(solicitacao.getViagem() != null
                        && solicitacao.getViagem().getMotorista() != null
                        && solicitacao.getViagem().getMotorista().getCodigo() == getCodigo(),
                "Este pedido nao e de uma viagem sua.");
        StatusSolicitacao novo = aceitar ? StatusSolicitacao.ACEITA : StatusSolicitacao.RECUSADA;
        Validador.exigir(solicitacao.getStatus().podeMudarPara(novo),
                "Este pedido ja foi respondido (" + solicitacao.getStatus() + ").");
        return novo;
    }

    // ------------------------------------------------------------------
    // Getters e setters
    // ------------------------------------------------------------------

    public String getCnh() {
        return cnh;
    }

    public void setCnh(String cnh) {
        String digitos = Validador.apenasDigitos(cnh);
        Validador.exigir(Validador.onzeDigitos(digitos), "CNH deve ter 11 digitos.");
        this.cnh = digitos;
    }

    public CategoriaCnh getCategoriaCnh() {
        return categoriaCnh;
    }

    public void setCategoriaCnh(CategoriaCnh categoriaCnh) {
        Validador.exigir(categoriaCnh != null, "Categoria da CNH e obrigatoria.");
        this.categoriaCnh = categoriaCnh;
    }

    public LocalDate getValidadeCnh() {
        return validadeCnh;
    }

    public void setValidadeCnh(LocalDate validadeCnh) {
        Validador.exigir(validadeCnh != null, "Validade da CNH e obrigatoria.");
        this.validadeCnh = validadeCnh;
    }

    public boolean isMotoristaAtivo() {
        return motoristaAtivo;
    }

    public void setMotoristaAtivo(boolean motoristaAtivo) {
        this.motoristaAtivo = motoristaAtivo;
    }
}
