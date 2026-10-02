package dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Utilidades JDBC compartidas por las implementaciones de los DAO. */
final class UtilJdbc {

    /** Clase de utilidad: no se instancia. */
    private UtilJdbc() {
    }

    /**
     * Lee el id autogenerado por un INSERT preparado con {@code RETURN_GENERATED_KEYS}.
     *
     * @throws SQLException si la BD no devolvió un id.
     */
    static int leerIdGenerado(PreparedStatement sentencia) throws SQLException {
        try (ResultSet claves = sentencia.getGeneratedKeys()) {
            if (!claves.next()) {
                throw new SQLException("La base de datos no devolvió el id generado.");
            }
            return claves.getInt(1);
        }
    }
}