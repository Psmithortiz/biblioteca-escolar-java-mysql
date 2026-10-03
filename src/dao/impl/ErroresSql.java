package dao.impl;

import dao.PersistenciaException;
import dao.RestriccionException;

import java.sql.SQLException;
import java.util.Map;

/**
 * Traduce las {@link SQLException} de MySQL a {@link PersistenciaException} con un mensaje
 * claro según el código de error. Si el error es una regla de los datos (duplicado, registros
 * asociados, referencia inexistente, CHECK), devuelve una {@link RestriccionException}.
 * Cada DAO puede aportar un texto específico por código; si no, se usa el genérico.
 */
final class ErroresSql {

    /** No se puede borrar la fila: otra tabla la referencia por clave foránea. */
    static final int FILA_REFERENCIADA = 1451;
    /** La fila apunta a un registro que no existe. */
    static final int REFERENCIA_INEXISTENTE = 1452;
    /** Se violó una restricción UNIQUE o PRIMARY KEY. */
    static final int DUPLICADO = 1062;
    /** Se violó una restricción CHECK (por ejemplo, stock negativo). */
    static final int CHECK_VIOLADO = 3819;
    /** El servidor rechazó el usuario o la contraseña. */
    private static final int ACCESO_DENEGADO = 1045;
    /** La base de datos indicada en la URL no existe. */
    private static final int BD_INEXISTENTE = 1049;
    /** Clase de SQLState de los errores de conexión. */
    private static final String CLASE_ERROR_CONEXION = "08";

    /** Clase de utilidad: no se instancia. */
    private ErroresSql() {
    }

    /**
     * @param contexto qué se intentaba hacer, p. ej. {@code "No se pudo eliminar la categoría."}
     * @return la excepción lista para lanzar con {@code throw ErroresSql.traducir(...)}.
     */
    static PersistenciaException traducir(String contexto, SQLException causa) {
        return traducir(contexto, causa, Map.of());
    }

    /**
     * @param especificos texto para el usuario según el código de error, p. ej.
     *                    {@code Map.of(DUPLICADO, "Ya existe una categoría con ese nombre.")};
     *                    los códigos que no estén usan el texto genérico.
     * @return una {@link RestriccionException} si el error es una regla de los datos;
     *         si no, una {@link PersistenciaException}.
     */
    static PersistenciaException traducir(String contexto, SQLException causa, Map<Integer, String> especificos) {
        String motivo = especificos.getOrDefault(causa.getErrorCode(), motivo(causa));
        String mensaje = contexto + " " + motivo;
        return esRestriccion(causa)
                ? new RestriccionException(mensaje, causa)
                : new PersistenciaException(mensaje, causa);
    }

    /** @return {@code true} si la BD rechazó los datos por una regla, no por una falla técnica. */
    private static boolean esRestriccion(SQLException e) {
        return switch (e.getErrorCode()) {
            case DUPLICADO, FILA_REFERENCIADA, REFERENCIA_INEXISTENTE, CHECK_VIOLADO -> true;
            default -> false;
        };
    }

    private static String motivo(SQLException e) {
        if (e.getSQLState() != null && e.getSQLState().startsWith(CLASE_ERROR_CONEXION)) {
            return "No se pudo conectar con la base de datos: revisa que el servidor MySQL esté iniciado.";
        }
        return switch (e.getErrorCode()) {
            case FILA_REFERENCIADA -> "Tiene registros asociados (por ejemplo, libros o préstamos) que deben conservarse.";
            case REFERENCIA_INEXISTENTE -> "Hace referencia a un registro que ya no existe.";
            case DUPLICADO -> "Ya existe un registro con esos datos (por ejemplo, el mismo nombre, RUT o ISBN).";
            case CHECK_VIOLADO -> "Los datos no cumplen una regla de la base de datos (por ejemplo, stock negativo).";
            case ACCESO_DENEGADO -> "La base de datos rechazó el usuario o la contraseña de db.properties.";
            case BD_INEXISTENTE -> "La base de datos no existe: ejecuta los scripts de la carpeta sql.";
            default -> e.getMessage();
        };
    }
}