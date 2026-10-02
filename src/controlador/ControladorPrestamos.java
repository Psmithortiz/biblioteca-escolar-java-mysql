package controlador;

import dao.PrestamoDAO;
import modelo.LibroPrestamos;
import modelo.Prestamo;
import modelo.PrestamoDetalle;
import utils.Validador;

import java.time.LocalDate;
import java.util.List;

/**
 * Registra préstamos y devoluciones, y entrega el historial y los reportes, a través de
 * {@link PrestamoDAO}. La fecha de préstamo y la de devolución son siempre la de hoy.
 */
public class ControladorPrestamos {

    private final PrestamoDAO prestamoDAO;

    /** @param prestamoDAO acceso a los préstamos; no puede ser nulo. */
    public ControladorPrestamos(PrestamoDAO prestamoDAO) {
        Validador.objetoNoNulo(prestamoDAO, "prestamoDAO");
        this.prestamoDAO = prestamoDAO;
    }

    /**
     * Presta hoy el libro al estudiante, con vencimiento a {@link Prestamo#PLAZO_DIAS} días.
     *
     * @return el préstamo ya guardado, con su id; {@code null} si el libro no tenía stock.
     * @throws IllegalArgumentException  si algún id es inválido.
     * @throws dao.PersistenciaException si falla la base de datos (no se modifica nada).
     */
    public Prestamo registrarPrestamo(int idEstudiante, int idLibro) {
        Prestamo prestamo = Prestamo.nuevo(idEstudiante, idLibro, LocalDate.now());
        return prestamoDAO.registrarPrestamo(prestamo) ? prestamo : null;
    }

    /**
     * Registra hoy la devolución. Trabaja sobre una copia del préstamo, así el que tiene la
     * vista no cambia si la operación falla.
     *
     * @return el préstamo devuelto (para consultar su atraso); {@code null} si ya estaba
     *         devuelto en la BD o ya no existe.
     * @throws IllegalStateException     si el préstamo ya estaba devuelto.
     * @throws dao.PersistenciaException si falla la base de datos (no se modifica nada).
     */
    public Prestamo registrarDevolucion(Prestamo actual) {
        Validador.objetoNoNulo(actual, "préstamo");
        Prestamo copia = Prestamo.reconstruir(actual.getId(), actual.getIdEstudiante(), actual.getIdLibro(),
                actual.getFechaPrestamo(), actual.getFechaVencimiento(), actual.getFechaDevolucionReal());
        copia.registrarDevolucion(LocalDate.now());
        return prestamoDAO.registrarDevolucion(copia) ? copia : null;
    }

    /** @return todos los préstamos, del más reciente al más antiguo. */
    public List<PrestamoDetalle> verPrestamos() {
        return prestamoDAO.listarTodos();
    }

    /** @return los libros actualmente en préstamo, del vencimiento más antiguo al más nuevo. */
    public List<PrestamoDetalle> verPrestamosActivos() {
        return prestamoDAO.listarActivos();
    }

    /** @return el historial del estudiante, del préstamo más reciente al más antiguo. */
    public List<PrestamoDetalle> verHistorial(int idEstudiante) {
        return prestamoDAO.historialPorEstudiante(idEstudiante);
    }

    /** @return los libros con su cantidad de préstamos, del más prestado al menos prestado. */
    public List<LibroPrestamos> verLibrosMasPrestados() {
        return prestamoDAO.librosMasPrestados();
    }
}