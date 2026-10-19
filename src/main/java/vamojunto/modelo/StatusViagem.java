package vamojunto.modelo;

/** Status da viagem (mesma lista do ck_viagem_status). POO: enum com atributo e metodo. */
public enum StatusViagem {
    AGENDADA("Agendada"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDA("Concluida"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusViagem(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Concluida e cancelada sao finais (regra do trg_viagem_bu). */
    public boolean isFinal() {
        return this == CONCLUIDA || this == CANCELADA;
    }
}
