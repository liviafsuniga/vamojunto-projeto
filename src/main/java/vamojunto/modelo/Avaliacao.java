package vamojunto.modelo;

import java.time.LocalDateTime;
import vamojunto.util.Validador;

/** Nota de 1 a 5 e comentario, dados apos a viagem concluida (RF11). */
public class Avaliacao {

    public static final int NOTA_MINIMA = 1;
    public static final int NOTA_MAXIMA = 5;

    private int codigo;
    private int codSolicitacao;
    private AutorAvaliacao autor;
    private int nota;
    private String comentario;
    private LocalDateTime dataAvaliacao;

    // dados de apoio para a tela (vem da consulta de pendencias)
    private String nomeAvaliado;
    private int codViagem;
    private String dataViagem;

    public Avaliacao() {
    }

    public Avaliacao(int codSolicitacao, AutorAvaliacao autor, int nota, String comentario) {
        this.codSolicitacao = codSolicitacao;
        this.autor = autor;
        setNota(nota);
        setComentario(comentario);
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public int getCodSolicitacao() {
        return codSolicitacao;
    }

    public void setCodSolicitacao(int codSolicitacao) {
        this.codSolicitacao = codSolicitacao;
    }

    public AutorAvaliacao getAutor() {
        return autor;
    }

    public void setAutor(AutorAvaliacao autor) {
        this.autor = autor;
    }

    public int getNota() {
        return nota;
    }

    public void setNota(int nota) {
        Validador.exigir(nota >= NOTA_MINIMA && nota <= NOTA_MAXIMA, "A nota deve ser de 1 a 5.");
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        Validador.exigir(comentario == null || comentario.length() <= 500, "Comentario com no maximo 500 caracteres.");
        this.comentario = Validador.vazio(comentario) ? null : comentario.trim();
    }

    public LocalDateTime getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDateTime dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }

    public String getNomeAvaliado() {
        return nomeAvaliado;
    }

    public void setNomeAvaliado(String nomeAvaliado) {
        this.nomeAvaliado = nomeAvaliado;
    }

    public int getCodViagem() {
        return codViagem;
    }

    public void setCodViagem(int codViagem) {
        this.codViagem = codViagem;
    }

    public String getDataViagem() {
        return dataViagem;
    }

    public void setDataViagem(String dataViagem) {
        this.dataViagem = dataViagem;
    }

    /** Estrelas para mostrar no console: ***** */
    public String getEstrelas() {
        return "*".repeat(nota) + ".".repeat(NOTA_MAXIMA - nota);
    }
}
