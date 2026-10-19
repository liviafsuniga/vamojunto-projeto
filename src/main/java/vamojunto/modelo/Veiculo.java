package vamojunto.modelo;

import java.time.Year;
import vamojunto.util.Validador;

/** Veiculo usado nas viagens (RF03). POO: encapsulamento e sobrecarga de construtor. */
public class Veiculo {

    private int codigo;
    private String marca;
    private String modelo;
    private String cor;
    private String placa;
    private int ano;
    private String renavam;
    private int qtdLugares;

    public Veiculo() {
    }

    public Veiculo(String marca, String modelo, String cor, String placa, int ano, String renavam, int qtdLugares) {
        setMarca(marca);
        setModelo(modelo);
        setCor(cor);
        setPlaca(placa);
        setAno(ano);
        setRenavam(renavam);
        setQtdLugares(qtdLugares);
    }

    public Veiculo(int codigo, String marca, String modelo, String cor, String placa, int ano,
                   String renavam, int qtdLugares) {
        this(marca, modelo, cor, placa, ano, renavam, qtdLugares);
        this.codigo = codigo;
    }

    /** Lugares menos o motorista: maximo de vagas que uma viagem pode oferecer. */
    public int getVagasMaximas() {
        return qtdLugares - 1;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        Validador.exigir(!Validador.vazio(marca), "Marca e obrigatoria.");
        this.marca = marca.trim();
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        Validador.exigir(!Validador.vazio(modelo), "Modelo e obrigatorio.");
        this.modelo = modelo.trim();
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        Validador.exigir(!Validador.vazio(cor), "Cor e obrigatoria.");
        this.cor = cor.trim();
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        String p = placa == null ? null : placa.replaceAll("[\\s-]", "").toUpperCase();
        Validador.exigir(Validador.placaValida(p), "Placa invalida. Use ABC1234 ou ABC1D23 (Mercosul).");
        this.placa = p;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        int limite = Year.now().getValue() + 1;
        Validador.exigir(ano >= 1950 && ano <= limite, "Ano deve estar entre 1950 e " + limite + ".");
        this.ano = ano;
    }

    public String getRenavam() {
        return renavam;
    }

    public void setRenavam(String renavam) {
        String digitos = Validador.apenasDigitos(renavam);
        Validador.exigir(Validador.onzeDigitos(digitos), "RENAVAM deve ter 11 digitos.");
        this.renavam = digitos;
    }

    public int getQtdLugares() {
        return qtdLugares;
    }

    public void setQtdLugares(int qtdLugares) {
        Validador.exigir(qtdLugares >= 2 && qtdLugares <= 9, "O veiculo deve ter de 2 a 9 lugares (com o motorista).");
        this.qtdLugares = qtdLugares;
    }

    @Override
    public String toString() {
        return marca + " " + modelo + " " + cor + " - " + placa + " (" + qtdLugares + " lugares)";
    }
}
