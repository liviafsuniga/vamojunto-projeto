package vamojunto.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pedido de carona: o passageiro escolhe em qual ponto embarca e desce no
 * destino final. POO: associacao com Viagem, Usuario (passageiro) e Ponto.
 */
public class Solicitacao {

    private int codigo;
    private Viagem viagem;
    private Usuario passageiro;
    private Ponto pontoEmbarque;
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataResposta;
    private StatusSolicitacao status = StatusSolicitacao.PENDENTE;
    private BigDecimal distanciaTrechoKm;
    private BigDecimal valorRateio;

    public Solicitacao() {
    }

    public Solicitacao(Viagem viagem, Usuario passageiro, Ponto pontoEmbarque, BigDecimal distanciaTrechoKm) {
        this.viagem = viagem;
        this.passageiro = passageiro;
        this.pontoEmbarque = pontoEmbarque;
        this.distanciaTrechoKm = distanciaTrechoKm;
    }

    /** O passageiro pode desistir de um pedido pendente ou aceito. */
    public boolean podeCancelar() {
        return status.podeMudarPara(StatusSolicitacao.CANCELADA);
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Viagem getViagem() {
        return viagem;
    }

    public void setViagem(Viagem viagem) {
        this.viagem = viagem;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Ponto getPontoEmbarque() {
        return pontoEmbarque;
    }

    public void setPontoEmbarque(Ponto pontoEmbarque) {
        this.pontoEmbarque = pontoEmbarque;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(LocalDateTime dataResposta) {
        this.dataResposta = dataResposta;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }

    public BigDecimal getDistanciaTrechoKm() {
        return distanciaTrechoKm;
    }

    public void setDistanciaTrechoKm(BigDecimal distanciaTrechoKm) {
        this.distanciaTrechoKm = distanciaTrechoKm;
    }

    public BigDecimal getValorRateio() {
        return valorRateio;
    }

    public void setValorRateio(BigDecimal valorRateio) {
        this.valorRateio = valorRateio;
    }

    @Override
    public String toString() {
        return "Pedido " + codigo + " (" + status + ")";
    }
}
