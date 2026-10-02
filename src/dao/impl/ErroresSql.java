package dao.impl;

import dao.PersistenciaException;

import java.sql.SQLException;

/**
 * Traduce las {@link SQLException} de MySQL a {@link PersistenciaException} con un mensaje
 * claro según el código de error. Es el único lugar que conoce esos códigos.
 */
final class ErroresSql {

    /** No se puede borrar la fila: otra tabla la referencia por clave foránea. */
    private static final int FILA_REFERENCIADA = 1451;
    /** La fila apunta a un registro que no existe. */
    private static final int REFERENCIA_INEXISTENTE = 1452;
    /** Se violó una restricción UNIQUE o PRIMARY KEY. */
    private static final int DUPLICADO = 1062;
    /** Se violó una restricción CHECK (por ejemplo, stock negativo). */
    private static final int CHECK_VIOLADO = 3819;
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
        return new PersistenciaException(contexto + " " + motivo(causa), causa);
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