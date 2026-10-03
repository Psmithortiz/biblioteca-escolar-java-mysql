package dao.impl;

import dao.PrestamoDAO;
import modelo.LibroPrestamos;
import modelo.Prestamo;
import modelo.PrestamoDetalle;
import utils.DatabaseConnection;
import utils.Validador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementación JDBC de {@link PrestamoDAO}. Todo acceso a la conexión compartida se
 * sincroniza sobre la instancia de {@link DatabaseConnection}, el candado común a todos los DAO:
 * así dos préstamos simultáneos no pueden mezclar sus transacciones.
 */
public class PrestamoDAOImpl implements PrestamoDAO {

    /** 0 filas afectadas = no queda stock. La BD revisa y descuenta en la misma sentencia. */
    private static final String SQL_DESCONTAR_STOCK =
            "UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0";

    private static final String SQL_SUMAR_STOCK = "UPDATE libros SET stock = stock + 1 WHERE id = ?";

    private static final String SQL_INSERTAR = """
            INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto)
            VALUES (?, ?, ?, ?, FALSE)
            """;

    /** Solo si aún no estaba devuelto: 0 filas = ya devuelto o no existe. */
    private static final String SQL_MARCAR_DEVUELTO = """
            UPDATE prestamos SET devuelto = TRUE, fecha_devolucion_real = ?
             WHERE id = ? AND devuelto = FALSE
            """;

    private static final String SQL_DETALLE = """
            SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion,
                   p.fecha_devolucion_real, e.nombre AS estudiante, e.rut, l.titulo
              FROM prestamos p
              JOIN estudiantes e ON e.id = p.id_estudiante
              JOIN libros l      ON l.id = p.id_libro
            """;

    private static final String SQL_LISTAR_TODOS = SQL_DETALLE + " ORDER BY p.fecha_prestamo DESC, p.id DESC";

    private static final String SQL_LISTAR_ACTIVOS =
            SQL_DETALLE + " WHERE p.devuelto = FALSE ORDER BY p.fecha_devolucion, p.id";

    private static final String SQL_HISTORIAL =
            SQL_DETALLE + " WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC, p.id DESC";

    /** LEFT JOIN para incluir los libros nunca prestados (con 0). */
    private static final String SQL_MAS_PRESTADOS = """
            SELECT l.id, l.titulo, l.autor, COUNT(p.id) AS veces
              FROM libros l
              LEFT JOIN prestamos p ON p.id_libro = l.id
             GROUP BY l.id, l.titulo, l.autor
             ORDER BY veces DESC, l.titulo
            """;

    /** Texto para el usuario si el estudiante o el libro se borraron mientras se prestaba. */
    private static final Map<Integer, String> SI_FALLA_PRESTAR =
            Map.of(ErroresSql.REFERENCIA_INEXISTENTE, "El estudiante o el libro ya no existe.");

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public boolean registrarPrestamo(Prestamo prestamo) {
        Validador.objetoNoNulo(prestamo, "préstamo");
        if (prestamo.getId() != null) {
            throw new IllegalStateException("El préstamo ya está guardado (id " + prestamo.getId() + ").");
        }
        if (prestamo.estaDevuelto()) {
            throw new IllegalStateException("No se puede registrar un préstamo ya devuelto.");
        }
        synchronized (db) {
            try {
                Connection conexion = db.getConnection();
                conexion.setAutoCommit(false);
                try {
                    if (!descontarStock(conexion, prestamo.getIdLibro())) {
                        conexion.rollback();
                        return false; // sin stock: no se inserta nada
                    }
                    int id = insertar(conexion, prestamo);
                    conexion.commit();
                    prestamo.asignarId(id); // recién ahora: si el commit fallara, el préstamo no quedaría con un id falso
                    return true;
                } catch (SQLException e) {
                    deshacer(conexion, e);
                    throw e;
                } finally {
                    restaurarAutoCommit(conexion);
                }
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudo registrar el préstamo.", e, SI_FALLA_PRESTAR);
            }
        }
    }

    @Override
    public boolean registrarDevolucion(Prestamo prestamo) {
        Validador.objetoNoNulo(prestamo, "préstamo");
        if (prestamo.getId() == null) {
            throw new IllegalStateException("El préstamo no está guardado: no se puede devolver.");
        }
        if (!prestamo.estaDevuelto()) {
            throw new IllegalStateException("Registra la devolución en el préstamo antes de guardarla.");
        }
        synchronized (db) {
            try {
                Connection conexion = db.getConnection();
                conexion.setAutoCommit(false);
                try {
                    if (!marcarDevuelto(conexion, prestamo)) {
                        conexion.rollback();
                        return false; // ya estaba devuelto: no se suma stock dos veces
                    }
                    sumarStock(conexion, prestamo.getIdLibro());
                    conexion.commit();
                    return true;
                } catch (SQLException e) {
                    deshacer(conexion, e);
                    throw e;
                } finally {
                    restaurarAutoCommit(conexion);
                }
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudo registrar la devolución.", e);
            }
        }
    }

    @Override
    public List<PrestamoDetalle> listarTodos() {
        return listarDetalle(SQL_LISTAR_TODOS, null, "No se pudieron listar los préstamos.");
    }

    @Override
    public List<PrestamoDetalle> listarActivos() {
        return listarDetalle(SQL_LISTAR_ACTIVOS, null, "No se pudieron listar los préstamos activos.");
    }

    @Override
    public List<PrestamoDetalle> historialPorEstudiante(int idEstudiante) {
        return listarDetalle(SQL_HISTORIAL, idEstudiante, "No se pudo obtener el historial del estudiante.");
    }

    @Override
    public List<LibroPrestamos> librosMasPrestados() {
        List<LibroPrestamos> filas = new ArrayList<>();
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(SQL_MAS_PRESTADOS);
                 ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {
                    filas.add(new LibroPrestamos(
                            resultado.getInt("id"),
                            resultado.getString("titulo"),
                            resultado.getString("autor"),
                            resultado.getInt("veces")));
                }
            } catch (SQLException e) {
                throw ErroresSql.traducir("No se pudo obtener el reporte de libros más prestados.", e);
            }
        }
        return filas;
    }

    // ---- Pasos de las transacciones: reciben la conexión para participar de la misma transacción ----

    private boolean descontarStock(Connection conexion, int idLibro) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_DESCONTAR_STOCK)) {
            sentencia.setInt(1, idLibro);
            return sentencia.executeUpdate() > 0;
        }
    }

    private void sumarStock(Connection conexion, int idLibro) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_SUMAR_STOCK)) {
            sentencia.setInt(1, idLibro);
            if (sentencia.executeUpdate() == 0) {
                throw new SQLException("El libro " + idLibro + " no existe.");
            }
        }
    }

    private int insertar(Connection conexion, Prestamo prestamo) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, prestamo.getIdEstudiante());
            sentencia.setInt(2, prestamo.getIdLibro());
            sentencia.setObject(3, prestamo.getFechaPrestamo());
            sentencia.setObject(4, prestamo.getFechaVencimiento());
            sentencia.executeUpdate();
            return UtilJdbc.leerIdGenerado(sentencia);
        }
    }

    private boolean marcarDevuelto(Connection conexion, Prestamo prestamo) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_MARCAR_DEVUELTO)) {
            sentencia.setObject(1, prestamo.getFechaDevolucionReal());
            sentencia.setInt(2, prestamo.getId());
            return sentencia.executeUpdate() > 0;
        }
    }

    /** Deshace la transacción; si el rollback también falla, lo agrega a la excepción original. */
    private void deshacer(Connection conexion, SQLException original) {
        try {
            conexion.rollback();
        } catch (SQLException e) {
            original.addSuppressed(e);
        }
    }

    /**
     * Devuelve la conexión compartida a modo autocommit para el próximo DAO. Si falla, la
     * conexión está rota: {@link DatabaseConnection#getConnection()} la reemplazará en el próximo uso.
     */
    private void restaurarAutoCommit(Connection conexion) {
        try {
            conexion.setAutoCommit(true);
        } catch (SQLException ignorada) {
            // Ver Javadoc: no hay nada que rescatar de una conexión rota.
        }
    }

    // ---- Consultas de lectura ----

    /** Ejecuta una consulta de detalle con un parámetro entero opcional (null = sin parámetro). */
    private List<PrestamoDetalle> listarDetalle(String sql, Integer parametro, String contextoError) {
        List<PrestamoDetalle> detalles = new ArrayList<>();
        synchronized (db) {
            try (PreparedStatement sentencia = db.getConnection().prepareStatement(sql)) {
                if (parametro != null) {
                    sentencia.setInt(1, parametro);
                }
                try (ResultSet resultado = sentencia.executeQuery()) {
                    while (resultado.next()) {
                        detalles.add(mapearDetalle(resultado));
                    }
                }
            } catch (SQLException e) {
                throw ErroresSql.traducir(contextoError, e);
            }
        }
        return detalles;
    }

    private PrestamoDetalle mapearDetalle(ResultSet fila) throws SQLException {
        Prestamo prestamo = Prestamo.reconstruir(
                fila.getInt("id"),
                fila.getInt("id_estudiante"),
                fila.getInt("id_libro"),
                fila.getObject("fecha_prestamo", LocalDate.class),
                fila.getObject("fecha_devolucion", LocalDate.class),
                fila.getObject("fecha_devolucion_real", LocalDate.class)); // null si no se ha devuelto
        return new PrestamoDetalle(
                prestamo, fila.getString("estudiante"), fila.getString("rut"), fila.getString("titulo"));
    }
}