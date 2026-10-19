package vamojunto.modelo;

import java.math.BigDecimal;
import vamojunto.util.Validador;

/** Ponto da rota (origem, parada ou destino), com endereco e coordenadas opcionais. */
public class Ponto {

    private int codigo;
    private int codViagem;
    private int ordem;
    private TipoPonto tipo;
    private String logradouro;
    private String numero;
    private String bairro;
    private String cep;
    private String cidade;
    private String estado;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public Ponto() {
    }

    public Ponto(TipoPonto tipo, String logradouro, String cidade, String estado) {
        this.tipo = tipo;
        setLogradouro(logradouro);
        setCidade(cidade);
        setEstado(estado);
    }

    public Ponto(TipoPonto tipo, String logradouro, String numero, String bairro, String cidade, String estado) {
        this(tipo, logradouro, cidade, estado);
        setNumero(numero);
        setBairro(bairro);
    }

    /** Texto curto para mostrar na tela. */
    public String getEnderecoResumido() {
        StringBuilder sb = new StringBuilder(logradouro);
        sb.append(", ").append(numero == null ? "s/n" : numero);
        if (bairro != null) {
            sb.append(" - ").append(bairro);
        }
        sb.append(" (").append(cidade).append(")");
        return sb.toString();
    }

    public boolean temCoordenadas() {
        return latitude != null && longitude != null;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public int getCodViagem() {
        return codViagem;
    }

    public void setCodViagem(int codViagem) {
        this.codViagem = codViagem;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public TipoPonto getTipo() {
        return tipo;
    }

    public void setTipo(TipoPonto tipo) {
        this.tipo = tipo;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        Validador.exigir(!Validador.vazio(logradouro), "Logradouro (rua/avenida) e obrigatorio.");
        this.logradouro = logradouro.trim();
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = Validador.vazio(numero) ? null : numero.trim();
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = Validador.vazio(bairro) ? null : bairro.trim();
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        String digitos = Validador.vazio(cep) ? null : Validador.apenasDigitos(cep);
        Validador.exigir(Validador.cepValido(digitos), "CEP deve ter 8 digitos.");
        this.cep = digitos;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        Validador.exigir(!Validador.vazio(cidade), "Cidade e obrigatoria.");
        this.cidade = cidade.trim();
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        Validador.exigir(Validador.ufValida(estado), "UF invalida.");
        this.estado = estado.trim().toUpperCase();
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        Validador.exigir(latitude == null || latitude.abs().compareTo(BigDecimal.valueOf(90)) <= 0,
                "Latitude deve estar entre -90 e 90.");
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        Validador.exigir(longitude == null || longitude.abs().compareTo(BigDecimal.valueOf(180)) <= 0,
                "Longitude deve estar entre -180 e 180.");
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return ordem + ". " + tipo + " - " + getEnderecoResumido();
    }
}
