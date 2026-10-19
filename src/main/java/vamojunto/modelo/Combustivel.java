package vamojunto.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import vamojunto.util.Formatador;
import vamojunto.util.Validador;

/** Combustivel e preco do litro. O preco e "congelado" na viagem quando ela e criada. */
public class Combustivel {

    private int codigo;
    private TipoCombustivel tipo;
    private BigDecimal precoLitro;
    private LocalDateTime dataAtualizacao;

    public Combustivel() {
    }

    public Combustivel(int codigo, TipoCombustivel tipo, BigDecimal precoLitro) {
        this.codigo = codigo;
        this.tipo = tipo;
        setPrecoLitro(precoLitro);
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public TipoCombustivel getTipo() {
        return tipo;
    }

    public void setTipo(TipoCombustivel tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getPrecoLitro() {
        return precoLitro;
    }

    public void setPrecoLitro(BigDecimal precoLitro) {
        Validador.exigir(precoLitro != null && precoLitro.signum() > 0, "O preco do litro deve ser maior que zero.");
        this.precoLitro = precoLitro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    @Override
    public String toString() {
        return tipo + " - " + Formatador.moeda(precoLitro) + "/litro";
    }
}
