package vamojunto.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Requisito nao funcional: a senha nunca e guardada, apenas o hash.
 * Usa SHA-256, o mesmo algoritmo da funcao SHA2(texto, 256) do MySQL,
 * entao os usuarios de exemplo do script (senha "vamo123") conseguem logar.
 *
 * Observacao: em producao o ideal e um hash com "sal" e custo (BCrypt/Argon2).
 */
public final class Seguranca {

    public static final int TAMANHO_MINIMO_SENHA = 6;

    private Seguranca() {
    }

    public static String gerarHash(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel nesta JVM", e);
        }
    }

    public static boolean senhaForte(String senha) {
        return senha != null && senha.length() >= TAMANHO_MINIMO_SENHA;
    }
}
