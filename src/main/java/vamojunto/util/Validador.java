package vamojunto.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validacoes de formato usadas pelas classes do modelo (encapsulamento).
 * As mesmas regras existem no banco como CHECK; o Java valida antes para
 * dar uma mensagem mais amigavel e evitar ida desnecessaria ao banco.
 *
 * POO: atributos e metodos estaticos (classe utilitaria, nao e instanciada).
 */
public final class Validador {

    public static final List<String> UFS = Arrays.asList(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    // placa antiga (ABC1234) ou Mercosul (ABC1D23) - igual ao ck_veiculo_placa
    private static final Pattern PLACA = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$");

    private Validador() {
        // nao deve ser instanciada
    }

    /** Remove pontos, tracos, espacos e parenteses: "123.456.789-01" vira "12345678901". */
    public static String apenasDigitos(String texto) {
        return texto == null ? null : texto.replaceAll("[^0-9]", "");
    }

    public static boolean vazio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    public static boolean cpfValido(String cpf) {
        return cpf != null && cpf.matches("[0-9]{11}");
    }

    public static boolean emailValido(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    public static boolean telefoneValido(String telefone) {
        return telefone != null && telefone.matches("[0-9]{10,11}");
    }

    public static boolean placaValida(String placa) {
        return placa != null && PLACA.matcher(placa).matches();
    }

    /** CNH e RENAVAM: 11 digitos. */
    public static boolean onzeDigitos(String valor) {
        return valor != null && valor.matches("[0-9]{11}");
    }

    public static boolean cepValido(String cep) {
        return cep == null || cep.matches("[0-9]{8}");
    }

    public static boolean ufValida(String uf) {
        return uf != null && UFS.contains(uf.toUpperCase());
    }

    /** Lanca IllegalArgumentException com a mensagem informada se a condicao for falsa. */
    public static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new IllegalArgumentException(mensagem);
        }
    }
}
