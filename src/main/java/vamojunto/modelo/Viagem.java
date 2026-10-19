package vamojunto.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import vamojunto.util.Validador;

/**
 * Viagem oferecida pelo motorista.
 *
 * POO:
 * - ASSOCIACAO com Motorista, Veiculo e Combustivel;
 * - COMPOSICAO com Ponto (a rota e uma lista de pontos que so existe dentro da viagem);
 * - LISTA DE OBJETOS (ArrayList de Ponto);
 * - metodos ESTATICOS de calculo + SOBRECARGA (calcularCusto/calcularRateio).
 *
 * Formulas (iguais as functions fn_calcular_custo e fn_calcular_rateio):
 *   custo_total  = distancia / consumo x preco do litro
 *   valor_rateio = custo_total x (trecho / distancia) / (participantes + 1)
 */
public class Viagem {

    private static final int ESCALA_CALCULO = 10;

    private int codigo;
    private Motorista motorista;
    private Veiculo veiculo;
    private Combustivel combustivel;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private StatusViagem status = StatusViagem.AGENDADA;
    private int vagas;
    private BigDecimal distanciaKm;
    private BigDecimal consumoKmL;
    private BigDecimal precoLitroAplicado;
    private BigDecimal custoTotal;
    private int qtdParticipantes;
    private final List<Ponto> pontos = new ArrayList<>();

    public Viagem() {
    }

    public Viagem(Motorista motorista, Veiculo veiculo, Combustivel combustivel, LocalDateTime dataInicio,
                  int vagas, BigDecimal distanciaKm, BigDecimal consumoKmL) {
        this.motorista = motorista;
        this.veiculo = veiculo;
        this.combustivel = combustivel;
        setDataInicio(dataInicio);
        setVagas(vagas);
        setDistanciaKm(distanciaKm);
        setConsumoKmL(consumoKmL);
        if (combustivel != null) {
            this.precoLitroAplicado = combustivel.getPrecoLitro();  // "congela" o preco
        }
        this.custoTotal = calcularCusto();
    }

    // ------------------------------------------------------------------
    // Rota (composicao com Ponto)
    // ------------------------------------------------------------------

    /**
     * Adiciona o proximo ponto da rota. Regras (iguais as do banco):
     * o primeiro e a ORIGEM, depois paradas, e o DESTINO e sempre o ultimo.
     */
    public void adicionarPonto(Ponto ponto) {
        if (pontos.isEmpty()) {
            Validador.exigir(ponto.getTipo() == TipoPonto.ORIGEM, "O primeiro ponto da rota deve ser a ORIGEM.");
        } else {
            Validador.exigir(ponto.getTipo() != TipoPonto.ORIGEM, "A rota so pode ter uma ORIGEM.");
            Validador.exigir(getDestino() == null, "Nao pode haver ponto depois do DESTINO.");
        }
        ponto.setOrdem(pontos.size() + 1);
        pontos.add(ponto);
    }

    /** Sobrecarga: monta o ponto a partir dos dados basicos. */
    public void adicionarPonto(TipoPonto tipo, String logradouro, String numero, String bairro,
                               String cidade, String estado) {
        adicionarPonto(new Ponto(tipo, logradouro, numero, bairro, cidade, estado));
    }

    /** Usado pelo DAO ao carregar a rota ja gravada (mantem a ordem do banco). */
    public void carregarPonto(Ponto ponto) {
        pontos.add(ponto);
    }

    public List<Ponto> getPontos() {
        return Collections.unmodifiableList(pontos);
    }

    public Ponto getOrigem() {
        for (Ponto p : pontos) {
            if (p.getTipo() == TipoPonto.ORIGEM) {
                return p;
            }
        }
        return null;
    }

    public Ponto getDestino() {
        for (Ponto p : pontos) {
            if (p.getTipo() == TipoPonto.DESTINO) {
                return p;
            }
        }
        return null;
    }

    public List<Ponto> getParadas() {
        List<Ponto> paradas = new ArrayList<>();
        for (Ponto p : pontos) {
            if (p.getTipo() == TipoPonto.PARADA) {
                paradas.add(p);
            }
        }
        return paradas;
    }

    /** Pontos onde o passageiro pode embarcar (todos menos o DESTINO). */
    public List<Ponto> getPontosDeEmbarque() {
        List<Ponto> lista = new ArrayList<>();
        for (Ponto p : pontos) {
            if (p.getTipo() != TipoPonto.DESTINO) {
                lista.add(p);
            }
        }
        return lista;
    }

    public boolean rotaCompleta() {
        return getOrigem() != null && getDestino() != null;
    }

    public int getVagasRestantes() {
        return status == StatusViagem.AGENDADA ? vagas - qtdParticipantes : 0;
    }

    // ------------------------------------------------------------------
    // Calculos (RF09)
    // ------------------------------------------------------------------

    public static BigDecimal calcularCusto(BigDecimal distanciaKm, BigDecimal consumoKmL, BigDecimal precoLitro) {
        if (distanciaKm == null || consumoKmL == null || precoLitro == null || consumoKmL.signum() <= 0) {
            return null;
        }
        return distanciaKm.divide(consumoKmL, ESCALA_CALCULO, RoundingMode.HALF_UP)
                .multiply(precoLitro)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Sobrecarga: custo desta viagem. */
    public BigDecimal calcularCusto() {
        return calcularCusto(distanciaKm, consumoKmL, precoLitroAplicado);
    }

    public static BigDecimal calcularRateio(BigDecimal custoTotal, BigDecimal trechoKm, BigDecimal distanciaKm,
                                            int participantes) {
        if (custoTotal == null || trechoKm == null || distanciaKm == null || distanciaKm.signum() <= 0) {
            return null;
        }
        BigDecimal fracao = trechoKm.divide(distanciaKm, ESCALA_CALCULO, RoundingMode.HALF_UP);
        return custoTotal.multiply(fracao)
                .divide(BigDecimal.valueOf(participantes + 1L), ESCALA_CALCULO, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Sobrecarga: quanto um passageiro pagaria nesta viagem com N participantes. */
    public BigDecimal calcularRateio(BigDecimal trechoKm, int participantes) {
        return calcularRateio(custoTotal, trechoKm, distanciaKm, participantes);
    }

    /** Sobrecarga: rateio de um NOVO passageiro (os aceitos + ele). */
    public BigDecimal calcularRateio(BigDecimal trechoKm) {
        return calcularRateio(trechoKm, qtdParticipantes + 1);
    }

    // ------------------------------------------------------------------
    // Getters e setters
    // ------------------------------------------------------------------

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Motorista getMotorista() {
        return motorista;
    }

    public void setMotorista(Motorista motorista) {
        this.motorista = motorista;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public Combustivel getCombustivel() {
        return combustivel;
    }

    public void setCombustivel(Combustivel combustivel) {
        this.combustivel = combustivel;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        Validador.exigir(dataInicio != null, "Data e hora de saida sao obrigatorias.");
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public StatusViagem getStatus() {
        return status;
    }

    public void setStatus(StatusViagem status) {
        this.status = status;
    }

    public int getVagas() {
        return vagas;
    }

    public void setVagas(int vagas) {
        Validador.exigir(vagas >= 1, "A viagem precisa ter pelo menos 1 vaga.");
        this.vagas = vagas;
    }

    public BigDecimal getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(BigDecimal distanciaKm) {
        Validador.exigir(distanciaKm != null && distanciaKm.signum() > 0, "A distancia deve ser maior que zero.");
        this.distanciaKm = distanciaKm;
    }

    public BigDecimal getConsumoKmL() {
        return consumoKmL;
    }

    public void setConsumoKmL(BigDecimal consumoKmL) {
        Validador.exigir(consumoKmL != null && consumoKmL.signum() > 0, "O consumo deve ser maior que zero.");
        this.consumoKmL = consumoKmL;
    }

    public BigDecimal getPrecoLitroAplicado() {
        return precoLitroAplicado;
    }

    public void setPrecoLitroAplicado(BigDecimal precoLitroAplicado) {
        this.precoLitroAplicado = precoLitroAplicado;
    }

    public BigDecimal getCustoTotal() {
        return custoTotal;
    }

    public void setCustoTotal(BigDecimal custoTotal) {
        this.custoTotal = custoTotal;
    }

    public int getQtdParticipantes() {
        return qtdParticipantes;
    }

    public void setQtdParticipantes(int qtdParticipantes) {
        this.qtdParticipantes = qtdParticipantes;
    }

    @Override
    public String toString() {
        String origem = getOrigem() == null ? "?" : getOrigem().getCidade();
        String destino = getDestino() == null ? "?" : getDestino().getCidade();
        return "Viagem " + codigo + ": " + origem + " -> " + destino + " (" + status.getDescricao() + ")";
    }
}
