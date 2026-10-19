package vamojunto.conexao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Abre conexoes JDBC com o banco vamojunto (aula "Acessando MySQL JDBC").
 *
 * Usa o usuario restrito vamojunto_app (menor privilegio), nunca o root.
 * Os valores podem ser trocados sem mexer no codigo, rodando com:
 *   -Ddb.url=jdbc:mysql://localhost:3307/vamojunto  -Ddb.usuario=...  -Ddb.senha=...
 *
 * POO: ATRIBUTOS E METODOS ESTATICOS - a classe nao precisa ser instanciada.
 */
public final class Conexao {

    public static final String URL_PADRAO =
            "jdbc:mysql://localhost:3306/vamojunto?sslMode=DISABLED&allowPublicKeyRetrieval=true";
    public static final String USUARIO_PADRAO = "vamojunto_app";
    public static final String SENHA_PADRAO = "VamoJunto#2026";

    private static String url = System.getProperty("db.url", URL_PADRAO);
    private static String usuario = System.getProperty("db.usuario", USUARIO_PADRAO);
    private static String senha = System.getProperty("db.senha", SENHA_PADRAO);
    private static int conexoesAbertas = 0;

    private Conexao() {
    }

    /** Sempre usar com try-with-resources: try (Connection con = Conexao.obter()) { ... } */
    public static Connection obter() throws SQLException {
        conexoesAbertas++;
        return DriverManager.getConnection(url, usuario, senha);
    }

    /** Testa a conexao e devolve a versao do servidor, para mostrar ao iniciar. */
    public static String testar() throws SQLException {
        try (Connection con = obter();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT VERSION(), CURRENT_USER()")) {
            DatabaseMetaData meta = con.getMetaData();
            rs.next();
            return meta.getDatabaseProductName() + " " + rs.getString(1) + " - usuario " + rs.getString(2);
        }
    }

    public static void configurar(String novaUrl, String novoUsuario, String novaSenha) {
        url = novaUrl;
        usuario = novoUsuario;
        senha = novaSenha;
    }

    public static String getUrl() {
        return url;
    }

    public static String getUsuario() {
        return usuario;
    }

    public static int getConexoesAbertas() {
        return conexoesAbertas;
    }
}
