package dao;

import modelo.LibroPrestamos;
import modelo.Prestamo;
import modelo.PrestamoDetalle;

import java.util.List;

/**
 * Acceso a datos de {@link Prestamo}. No es un CRUD libre: un préstamo se registra y se
 * devuelve, y cada operación mantiene el stock del libro en la misma transacción.
 */
public interface PrestamoDAO {

    /**
     * Descuenta un ejemplar del libro e inserta el préstamo, en una sola transacción.
     * Si se guarda, le asigna el id generado.
     *
     * @return {@code true} si se registró; {@code false} si el libro no tenía stock.
     * @throws IllegalStateException si el préstamo ya tiene id o ya está devuelto.
     * @throws PersistenciaException si falla el acceso a la base de datos (no se modifica nada).
     */
    boolean registrarPrestamo(Prestamo prestamo);

    /**
     * Guarda la devolución ya registrada en el préstamo y suma un ejemplar al libro,
     * en una sola transacción.
     *
     * @return {@code true} si se guardó; {@code false} si ya estaba devuelto en la BD o no existe.
     * @throws IllegalStateException si el préstamo no tiene id o no tiene la devolución registrada.
     * @throws PersistenciaException si falla el acceso a la base de datos (no se modifica nada).
     */
    boolean registrarDevolucion(Prestamo prestamo);

    /** @return todos los préstamos, del más reciente al más antiguo. */
    List<PrestamoDetalle> listarTodos();

    /** @return los préstamos no devueltos (libros actualmente en préstamo), del vencimiento más antiguo al más nuevo. */
    List<PrestamoDetalle> listarActivos();

    /** @return el historial del estudiante, del préstamo más reciente al más antiguo. */
    List<PrestamoDetalle> historialPorEstudiante(int idEstudiante);

    /** @return todos los libros con su cantidad de préstamos, del más prestado al menos prestado. */
    List<LibroPrestamos> librosMasPrestados();
}