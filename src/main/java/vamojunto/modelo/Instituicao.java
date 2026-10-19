package vamojunto.modelo;

import vamojunto.util.Validador;

/**
 * Instituicao de ensino a que o usuario pode estar vinculado (opcional).
 * POO: classe, objeto, encapsulamento (atributos privados + get/set com validacao).
 */
public class Instituicao {

    private int codigo;
    private String nome;
    private String sigla;
    private String cidade;
    private String estado;

    public Instituicao() {
    }

    public Instituicao(String nome, String sigla, String cidade, String estado) {
        setNome(nome);
        setSigla(sigla);
        setCidade(cidade);
        setEstado(estado);
    }

    public Instituicao(int codigo, String nome, String sigla, String cidade, String estado) {
        this(nome, sigla, cidade, estado);
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        Validador.exigir(!Validador.vazio(nome), "Nome da instituicao e obrigatorio.");
        this.nome = nome.trim();
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = Validador.vazio(sigla) ? null : sigla.trim();
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

    @Override
    public String toString() {
        return (sigla != null ? sigla + " - " : "") + nome + " (" + cidade + "/" + estado + ")";
    }
}
