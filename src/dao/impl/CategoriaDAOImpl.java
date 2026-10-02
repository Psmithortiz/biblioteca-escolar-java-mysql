package dao.impl;

import dao.CategoriaDAO;
import modelo.Categoria;
import utils.DatabaseConnection;
import utils.Validador;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC de {@link CategoriaDAO} sobre la tabla {@code categorias}.
 * Todo acceso a la conexión compartida se sincroniza sobre la instancia de
 * {@link DatabaseConnection}, el candado común a todos los DAO.
 */
public class CategoriaDAOImpl implements CategoriaDAO {

    private static final String SQL_INSERTAR = "INSERT INTO categorias (nombre) VALUES (?)";

    private static final String SQL_LISTAR = "SELECT id, nombre FROM categorias ORDER BY nombre";

    private static final String SQL_ACTUALIZAR = "UPDATE categorias SET nombre = ? WHERE id = ?";

    private static final String SQL_ELIMINAR = "DELETE FROM categorias WHERE id = ?";

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public void create(Categoria categoria) {
        Validador.objetoNoNulo(categoria, "categoría");
        if (categoria.getId() != null) {
            throw new IllegalStateException("La categoría ya está guardada (id " + categoria.getId() + ").");
        }
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(
                    SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

                sentencia.setString(1, categoria.getNombre());
                sentencia.executeUpdate();

                categoria.asignarId(UtilJdbc.leerIdGenerado(sentencia));
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudo guardar la categoría.", e);
            }
        }
    }

    @Override
    public List<Categoria> readAll() {
        List<Categoria> categorias = new ArrayList<>();
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(SQL_LISTAR);
                 ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {
                    categorias.add(mapear(resultado));
                }
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudieron listar las categorías.", e);
            }
        }
        return categorias;
    }

    @Override
    public boolean update(Categoria categoria) {
        Validador.objetoNoNulo(categoria, "categoría");
        if (categoria.getId() == null) {
            throw new IllegalStateException("La categoría no está guardada: no se puede actualizar.");
        }
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(SQL_ACTUALIZAR)) {

                sentencia.setString(1, categoria.getNombre());
                sentencia.setInt(2, categoria.getId());
                return sentencia.executeUpdate() > 0; // 0 filas afectadas = no existe ese id
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudo actualizar la categoría.", e);
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
                throw ErroresSql.traducir("No se pudo eliminar la categoría.", e);
            }
        }
    }

    /** Convierte la fila actual del {@link ResultSet} en una {@link Categoria} con su id. */
    private Categoria mapear(ResultSet fila) throws SQLException {
        Categoria categoria = new Categoria(fila.getString("nombre"));
        categoria.asignarId(fila.getInt("id"));
        return categoria;
    }
}