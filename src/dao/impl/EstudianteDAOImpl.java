package dao.impl;

import dao.EstudianteDAO;
import modelo.Estudiante;
import utils.DatabaseConnection;
import utils.Validador;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC de {@link EstudianteDAO} sobre la tabla {@code estudiantes}.
 * Todo acceso a la conexión compartida se sincroniza sobre la instancia de
 * {@link DatabaseConnection}, el candado común a todos los DAO.
 */
public class EstudianteDAOImpl implements EstudianteDAO {

    private static final String COLUMNAS = "id, nombre, rut, curso, correo";

    private static final String SQL_INSERTAR =
            "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";

    private static final String SQL_LISTAR = "SELECT " + COLUMNAS + " FROM estudiantes ORDER BY nombre";

    private static final String SQL_BUSCAR_POR_RUT = "SELECT " + COLUMNAS + " FROM estudiantes WHERE rut = ?";

    /** Sin la columna rut: el RUT no se edita. */
    private static final String SQL_ACTUALIZAR =
            "UPDATE estudiantes SET nombre = ?, curso = ?, correo = ? WHERE id = ?";

    private static final String SQL_ELIMINAR = "DELETE FROM estudiantes WHERE id = ?";

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public void create(Estudiante estudiante) {
        Validador.objetoNoNulo(estudiante, "estudiante");
        if (estudiante.getId() != null) {
            throw new IllegalStateException("El estudiante ya está guardado (id " + estudiante.getId() + ").");
        }
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(
                    SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

                sentencia.setString(1, estudiante.getNombre());
                sentencia.setString(2, estudiante.getRut());
                sentencia.setString(3, estudiante.getCurso()); // null si no tiene curso
                sentencia.setString(4, estudiante.getCorreo());
                sentencia.executeUpdate();

                estudiante.asignarId(UtilJdbc.leerIdGenerado(sentencia));
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudo guardar el estudiante.", e);
            }
        }
    }

    @Override
    public List<Estudiante> readAll() {
        List<Estudiante> estudiantes = new ArrayList<>();
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(SQL_LISTAR);
                 ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {
                    estudiantes.add(mapear(resultado));
                }
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudieron listar los estudiantes.", e);
            }
        }
        return estudiantes;
    }

    @Override
    public Estudiante buscarPorRut(String rut) {
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(SQL_BUSCAR_POR_RUT)) {

                sentencia.setString(1, rut);
                try (ResultSet resultado = sentencia.executeQuery()) {
                    return resultado.next() ? mapear(resultado) : null; // RUT es UNIQUE: a lo más una fila
                }
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudo buscar el estudiante.", e);
            }
        }
    }

    @Override
    public boolean update(Estudiante estudiante) {
        Validador.objetoNoNulo(estudiante, "estudiante");
        if (estudiante.getId() == null) {
            throw new IllegalStateException("El estudiante no está guardado: no se puede actualizar.");
        }
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(SQL_ACTUALIZAR)) {

                sentencia.setString(1, estudiante.getNombre());
                sentencia.setString(2, estudiante.getCurso()); // null si no tiene curso
                sentencia.setString(3, estudiante.getCorreo());
                sentencia.setInt(4, estudiante.getId());
                return sentencia.executeUpdate() > 0; // 0 filas afectadas = no existe ese id
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudo actualizar el estudiante.", e);
            }
        }
    }

    @Override
    public boolean delete(int id) {
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(SQL_ELIMINAR)) {

                sentencia.setInt(1, id);
                return sentencia.executeUpdate() > 0; // 0 filas afectadas = no existía
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudo eliminar el estudiante.", e);
            }
        }
    }

    /** Convierte la fila actual del {@link ResultSet} en un {@link Estudiante} con su id. */
    private Estudiante mapear(ResultSet fila) throws SQLException {
        Estudiante estudiante = new Estudiante(
                fila.getString("nombre"),
                fila.getString("rut"),
                fila.getString("curso"),
                fila.getString("correo"));
        estudiante.asignarId(fila.getInt("id"));
        return estudiante;
    }
}