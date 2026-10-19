package vamojunto.match;

import java.math.BigDecimal;

/** Uma opcao de carona encontrada pelo match (uma viagem + um ponto de embarque). */
public class ResultadoMatch {

    private int codViagem;
    private String saida;
    private String motorista;
    private BigDecimal notaMotorista;
    private int codPontoEmbarque;
    private String tipoEmbarque;
    private String embarque;
    private String destino;
    private BigDecimal trechoKm;
    private BigDecimal rateioEstimado;
    private int vagasRestantes;
    private int similaridade;

    public int getCodViagem() {
        return codViagem;
    }

    public void setCodViagem(int codViagem) {
        this.codViagem = codViagem;
    }

    public String getSaida() {
        return saida;
    }

    public void setSaida(String saida) {
        this.saida = saida;
    }

    /** "dd/MM/yyyy HH:mm" convertido para "yyyyMMddHHmm", para comparar datas como texto. */
    public String getSaidaOrdenavel() {
        if (saida == null || saida.length() < 16) {
            return "";
        }
        return saida.substring(6, 10) + saida.substring(3, 5) + saida.substring(0, 2)
                + saida.substring(11, 13) + saida.substring(14, 16);
    }

    public String getMotorista() {
        return motorista;
    }

    public void setMotorista(String motorista) {
        this.motorista = motorista;
    }

    public BigDecimal getNotaMotorista() {
        return notaMotorista;
    }

    public void setNotaMotorista(BigDecimal notaMotorista) {
        this.notaMotorista = notaMotorista;
    }

    public int getCodPontoEmbarque() {
        return codPontoEmbarque;
    }

    public void setCodPontoEmbarque(int codPontoEmbarque) {
        this.codPontoEmbarque = codPontoEmbarque;
    }

    public String getTipoEmbarque() {
        return tipoEmbarque;
    }

    public void setTipoEmbarque(String tipoEmbarque) {
        this.tipoEmbarque = tipoEmbarque;
    }

    public String getEmbarque() {
        return embarque;
    }

    public void setEmbarque(String embarque) {
        this.embarque = embarque;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public BigDecimal getTrechoKm() {
        return trechoKm;
    }

    public void setTrechoKm(BigDecimal trechoKm) {
        this.trechoKm = trechoKm;
    }

    public BigDecimal getRateioEstimado() {
        return rateioEstimado;
    }

    public void setRateioEstimado(BigDecimal rateioEstimado) {
        this.rateioEstimado = rateioEstimado;
    }

    public int getVagasRestantes() {
        return vagasRestantes;
    }

    public void setVagasRestantes(int vagasRestantes) {
        this.vagasRestantes = vagasRestantes;
    }

    public int getSimilaridade() {
        return similaridade;
    }

    public void setSimilaridade(int similaridade) {
        this.similaridade = similaridade;
    }
}
