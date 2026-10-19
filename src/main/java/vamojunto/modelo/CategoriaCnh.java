package vamojunto.modelo;

/** Categorias de CNH que permitem dirigir carro (ck_motorista_categoria). "A" (so moto) nao entra. */
public enum CategoriaCnh {
    B, C, D, E, AB, AC, AD, AE;

    /** Converte o texto digitado ("ab", " B ") para o enum, ou lanca erro claro. */
    public static CategoriaCnh deTexto(String texto) {
        try {
            return valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Categoria invalida. Use B, C, D, E, AB, AC, AD ou AE.");
        }
    }
}
