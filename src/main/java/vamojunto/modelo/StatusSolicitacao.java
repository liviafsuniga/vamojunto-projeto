package vamojunto.modelo;

/**
 * Status do pedido de carona. Transicoes validas (iguais a trg_solicitacao_bu):
 * PENDENTE -> ACEITA, RECUSADA ou CANCELADA;  ACEITA -> CANCELADA.
 */
public enum StatusSolicitacao {
    PENDENTE, ACEITA, RECUSADA, CANCELADA;

    public boolean podeMudarPara(StatusSolicitacao novo) {
        switch (this) {
            case PENDENTE:
                return novo == ACEITA || novo == RECUSADA || novo == CANCELADA;
            case ACEITA:
                return novo == CANCELADA;
            default:
                return false;
        }
    }
}
