package dao.impl;

import dao.UsuarioDAO;
import modelo.FabricaUsuarios;
import modelo.Rol;
import modelo.Usuario;
import utils.DatabaseConnection;
import utils.HashUtil;
import utils.Validador;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Implementación JDBC de {@link UsuarioDAO} sobre la tabla {@code usuarios}.
 * Todo acceso a la conexión compartida se sincroniza sobre la instancia de
 * {@link DatabaseConnection}, el candado común a todos los DAO.
 */
public class UsuarioDAOImpl implements UsuarioDAO {

    /**
     * El mismo identificador se compara con rut y con correo: no hay ambigüedad,
     * porque un RUT nunca tiene '@' y un correo siempre lo tiene.
     */
    private static final String SQL_AUTENTICAR = """
            SELECT id, nombre, rut, correo, rol
              FROM usuarios
             WHERE (rut = ? OR correo = ?) AND contraseña = ?
            """;

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public Usuario autenticar(String identificador, String contrasena) {
        Validador.cadenaNoVacia(identificador, "RUT o correo");
        Validador.cadenaNoVacia(contrasena, "contraseña");
        String id = identificador.strip();
        String hash = HashUtil.sha256(contrasena);
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(SQL_AUTENTICAR)) {

                sentencia.setString(1, id);
                sentencia.setString(2, id);
                sentencia.setString(3, hash);
                try (ResultSet resultado = sentencia.executeQuery()) {
                    return resultado.next() ? mapear(resultado) : null; // rut y correo son UNIQUE: a lo más una fila
                }
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudo iniciar sesión.", e);
            }
        }
    }

    /** Convierte la fila actual en la subclase de {@link Usuario} que corresponde a su rol. */
    private Usuario mapear(ResultSet fila) throws SQLException {
        // La BD guarda el rol en minúsculas ('bibliotecario'); el enum, en mayúsculas.
        Rol rol = Rol.valueOf(fila.getString("rol").toUpperCase(Locale.ROOT));
        return FabricaUsuarios.reconstruir(
                fila.getInt("id"), rol, fila.getString("nombre"), fila.getString("rut"), fila.getString("correo"));
    }
}