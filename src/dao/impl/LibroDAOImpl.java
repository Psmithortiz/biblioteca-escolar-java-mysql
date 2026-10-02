package dao.impl;

import dao.LibroDAO;
import modelo.Libro;
import utils.DatabaseConnection;
import utils.Validador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC de {@link LibroDAO} sobre la tabla {@code libros}.
 * Todo acceso a la conexión compartida se sincroniza sobre la instancia de
 * {@link DatabaseConnection}, el candado común a todos los DAO.
 */
public class LibroDAOImpl implements LibroDAO {

    private static final String COLUMNAS = "id, titulo, autor, isbn, editorial, stock, id_categoria";

    private static final String SQL_INSERTAR =
            "INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_LISTAR = "SELECT " + COLUMNAS + " FROM libros ORDER BY titulo";

    /** Cada filtro se anula si su parámetro es NULL. */
    private static final String SQL_BUSCAR = "SELECT " + COLUMNAS + """
             FROM libros
            WHERE (? IS NULL OR titulo LIKE ? OR autor LIKE ?)
              AND (? IS NULL OR id_categoria = ?)
            ORDER BY titulo
            """;

    /** Sin la columna stock: el stock solo cambia con SQL_AJUSTAR_STOCK. */
    private static final String SQL_ACTUALIZAR =
            "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, id_categoria = ? WHERE id = ?";

    /** Ajusta sobre el valor actual y no lo deja negativo: 0 filas = no existe o no alcanza. */
    private static final String SQL_AJUSTAR_STOCK =
            "UPDATE libros SET stock = stock + ? WHERE id = ? AND stock + ? >= 0";

    private static final String SQL_ELIMINAR = "DELETE FROM libros WHERE id = ?";

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public void create(Libro libro) {
        Validador.objetoNoNulo(libro, "libro");
        if (libro.getId() != null) {
            throw new IllegalStateException("El libro ya está guardado (id " + libro.getId() + ").");
        }
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(
                    SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

                sentencia.setString(1, libro.getTitulo());
                sentencia.setString(2, libro.getAutor());
                sentencia.setString(3, libro.getIsbn());
                sentencia.setString(4, libro.getEditorial());
                sentencia.setInt(5, libro.getStock());
                sentencia.setInt(6, libro.getIdCategoria());
                sentencia.executeUpdate();

                libro.asignarId(UtilJdbc.leerIdGenerado(sentencia));
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudo guardar el libro.", e);
            }
        }
    }

    @Override
    public List<Libro> readAll() {
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(SQL_LISTAR)) {
                return listar(sentencia);
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudieron listar los libros.", e);
            }
        }
    }

    @Override
    public List<Libro> buscar(String texto, Integer idCategoria) {
        String patron = (texto == null || texto.isBlank()) ? null : "%" + texto.strip() + "%";
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(SQL_BUSCAR)) {

                sentencia.setString(1, patron);
                sentencia.setString(2, patron);
                sentencia.setString(3, patron);
                sentencia.setObject(4, idCategoria, Types.INTEGER);
                sentencia.setObject(5, idCategoria, Types.INTEGER);
                return listar(sentencia);
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudieron buscar los libros.", e);
            }
        }
    }

    @Override
    public boolean update(Libro libro) {
        Validador.objetoNoNulo(libro, "libro");
        if (libro.getId() == null) {
            throw new IllegalStateException("El libro no está guardado: no se puede actualizar.");
        }
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(SQL_ACTUALIZAR)) {

                sentencia.setString(1, libro.getTitulo());
                sentencia.setString(2, libro.getAutor());
                sentencia.setString(3, libro.getIsbn());
                sentencia.setString(4, libro.getEditorial());
                sentencia.setInt(5, libro.getIdCategoria());
                sentencia.setInt(6, libro.getId());
                return sentencia.executeUpdate() > 0; // 0 filas afectadas = no existe ese id
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudo actualizar el libro.", e);
            }
        }
    }

    @Override
    public boolean ajustarStock(int idLibro, int cantidad) {
        if (cantidad == 0) {
            throw new IllegalArgumentException("La cantidad a ajustar no puede ser 0.");
        }
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(SQL_AJUSTAR_STOCK)) {

                sentencia.setInt(1, cantidad);
                sentencia.setInt(2, idLibro);
                sentencia.setInt(3, cantidad);
                return sentencia.executeUpdate() > 0;
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudo ajustar el stock del libro.", e);
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
                throw ErroresSql.traducir("No se pudo eliminar el libro.", e);
            }
        }
    }

    /** Ejecuta una consulta ya preparada y convierte cada fila en un {@link Libro}. */
    private List<Libro> listar(PreparedStatement sentencia) throws SQLException {
        List<Libro> libros = new ArrayList<>();
        try (ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                libros.add(mapear(resultado));
            }
        }
        return libros;
    }

    /** Convierte la fila actual del {@link ResultSet} en un {@link Libro} con su id. */
    private Libro mapear(ResultSet fila) throws SQLException {
        Libro libro = new Libro(
                fila.getString("titulo"),
                fila.getString("autor"),
                fila.getString("isbn"),
                fila.getString("editorial"),
                fila.getInt("stock"),
                fila.getInt("id_categoria"));
        libro.asignarId(fila.getInt("id"));
        return libro;
    }
}