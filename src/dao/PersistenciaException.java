package dao;

/**
 * Error al leer o escribir en la base de datos. Envuelve la {@link java.sql.SQLException}
 * original para que las capas superiores no dependan de JDBC.
 */
public class PersistenciaException extends RuntimeException {

    /**
     * @param mensaje explicación para el usuario, sin detalles de JDBC.
     * @param causa   la excepción original, para depurar.
     */
    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}