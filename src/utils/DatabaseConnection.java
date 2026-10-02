package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton que mantiene la única conexión a la base de datos.
 * La conexión es compartida: no debe cerrarse.
 */
public final class DatabaseConnection {

    private static final String ARCHIVO_CONFIGURACION = "db.properties";
    private static final int SEGUNDOS_VALIDACION = 2;

    private static DatabaseConnection instancia;

    private final String url;
    private final String usuario;
    private final String password;
    private Connection conexion;

    private DatabaseConnection() {
        Properties props = cargarConfiguracion();
        this.url = obligatoria(props, "db.url");
        this.usuario = obligatoria(props, "db.usuario");
        this.password = props.getProperty("db.password", "");
    }

    /**
     * Devuelve la única instancia, creándola en el primer llamado.
     *
     * @throws IllegalStateException si falta db.properties o una clave obligatoria
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instancia == null) {
            instancia = new DatabaseConnection();
        }
        return instancia;
    }

    /**
     * Devuelve la conexión compartida, reabriéndola si no existe o se cortó.
     *
     * @throws SQLException si no se puede conectar a MySQL
     */
    public synchronized Connection getConnection() throws SQLException {
        if (conexion == null || !conexion.isValid(SEGUNDOS_VALIDACION)) {
            conexion = DriverManager.getConnection(url, usuario, password);
        }
        return conexion;
    }

    private static Properties cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream entrada = new FileInputStream(ARCHIVO_CONFIGURACION)) {
            props.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo leer " + ARCHIVO_CONFIGURACION
                            + ". Copia db.properties.example como db.properties y completa tus credenciales.", e);
        }
        return props;
    }

    private static String obligatoria(Properties props, String clave) {
        String valor = props.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la clave '" + clave + "' en " + ARCHIVO_CONFIGURACION);
        }
        return valor;
    }
}