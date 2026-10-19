package vamojunto.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Contrato basico do padrao de projeto DAO (Data Access Object): toda a
 * conversa com o banco fica nas classes *DAO, e as telas so lidam com objetos.
 *
 * POO: INTERFACE GENERICA - cada DAO informa o tipo que manipula (Dao&lt;Veiculo&gt;).
 */
public interface Dao<T> {

    T buscarPorCodigo(int codigo) throws SQLException;

    List<T> listar() throws SQLException;

    /** Grava e devolve o codigo gerado pelo AUTO_INCREMENT. */
    int inserir(T objeto) throws SQLException;
}
