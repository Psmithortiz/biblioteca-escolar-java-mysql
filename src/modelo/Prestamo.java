package modelo;

import utils.Validador;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Préstamo de un libro a un estudiante. Vence {@link #PLAZO_DIAS} días después de la
 * fecha de préstamo. Está devuelto si tiene fecha real de devolución.
 */
public class Prestamo extends Entidad {

    public static final int PLAZO_DIAS = 7;

    private final int idEstudiante;
    private final int idLibro;
    private final LocalDate fechaPrestamo;
    private final LocalDate fechaVencimiento;
    /** {@code null} mientras el libro no se devuelva. */
    private LocalDate fechaDevolucionReal;

    private Prestamo(int idEstudiante, int idLibro, LocalDate fechaPrestamo,
                     LocalDate fechaVencimiento, LocalDate fechaDevolucionReal) {
        Validador.positivo(idEstudiante, "estudiante");
        Validador.positivo(idLibro, "libro");
        Validador.objetoNoNulo(fechaPrestamo, "fecha de préstamo");
        Validador.objetoNoNulo(fechaVencimiento, "fecha de vencimiento");
        if (fechaVencimiento.isBefore(fechaPrestamo)) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la de préstamo.");
        }
        validarFechaDevolucion(fechaPrestamo, fechaDevolucionReal);
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    /**
     * Crea un préstamo nuevo, sin id ni devolución, con vencimiento a {@link #PLAZO_DIAS} días.
     *
     * @throws IllegalArgumentException si algún dato es inválido.
     */
    public static Prestamo nuevo(int idEstudiante, int idLibro, LocalDate fechaPrestamo) {
        Validador.objetoNoNulo(fechaPrestamo, "fecha de préstamo");
        return new Prestamo(idEstudiante, idLibro, fechaPrestamo, fechaPrestamo.plusDays(PLAZO_DIAS), null);
    }

    /**
     * Reconstruye un préstamo leído desde la BD. Conserva el vencimiento guardado,
     * aunque el plazo haya cambiado después.
     *
     * @param fechaDevolucionReal {@code null} si el libro no se ha devuelto.
     * @throws IllegalArgumentException si algún dato es inválido.
     */
    public static Prestamo reconstruir(int id, int idEstudiante, int idLibro, LocalDate fechaPrestamo,
                                       LocalDate fechaVencimiento, LocalDate fechaDevolucionReal) {
        Prestamo prestamo = new Prestamo(idEstudiante, idLibro, fechaPrestamo, fechaVencimiento, fechaDevolucionReal);
        prestamo.asignarId(id);
        return prestamo;
    }

    /**
     * Registra la devolución en la fecha indicada.
     *
     * @throws IllegalArgumentException si la fecha es nula o anterior a la de préstamo.
     * @throws IllegalStateException    si el préstamo ya estaba devuelto.
     */
    public void registrarDevolucion(LocalDate fecha) {
        Validador.objetoNoNulo(fecha, "fecha de devolución");
        if (estaDevuelto()) {
            throw new IllegalStateException("El préstamo " + getId() + " ya fue devuelto.");
        }
        validarFechaDevolucion(fechaPrestamo, fecha);
        this.fechaDevolucionReal = fecha;
    }

    public boolean estaDevuelto() {
        return fechaDevolucionReal != null;
    }

    /**
     * Días de atraso: si está devuelto, al momento de la devolución; si no, a la fecha {@code hoy}.
     *
     * @return 0 si no hay atraso.
     */
    public long diasAtraso(LocalDate hoy) {
        LocalDate referencia = estaDevuelto() ? fechaDevolucionReal : hoy;
        Validador.objetoNoNulo(referencia, "fecha de referencia");
        return Math.max(0, ChronoUnit.DAYS.between(fechaVencimiento, referencia));
    }

    public boolean estaAtrasado(LocalDate hoy) {
        return diasAtraso(hoy) > 0;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    /** @return la fecha real de devolución, o {@code null} si no se ha devuelto. */
    public LocalDate getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    private static void validarFechaDevolucion(LocalDate fechaPrestamo, LocalDate fechaDevolucion) {
        if (fechaDevolucion != null && fechaDevolucion.isBefore(fechaPrestamo)) {
            throw new IllegalArgumentException("La fecha de devolución no puede ser anterior a la de préstamo.");
        }
    }

    @Override
    public String toString() {
        return "Prestamo{id=" + getId() + ", idEstudiante=" + idEstudiante + ", idLibro=" + idLibro
                + ", devuelto=" + estaDevuelto() + "}";
    }
}