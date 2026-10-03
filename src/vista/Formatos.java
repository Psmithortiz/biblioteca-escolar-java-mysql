package vista;

import modelo.Prestamo;
import modelo.PrestamoDetalle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Textos de fechas y de estados de préstamo, iguales en todas las pantallas. */
final class Formatos {

    static final String SIN_DATO = "—";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /** Clase de utilidad: no se instancia. */
    private Formatos() {
    }

    /** @return la fecha como dd-MM-yyyy, o "—" si es {@code null}. */
    static String fecha(LocalDate fecha) {
        return (fecha == null) ? SIN_DATO : fecha.format(FORMATO_FECHA);
    }

    /** @return el estado del préstamo en palabras, con los días de atraso si los hay. */
    static String estado(Prestamo prestamo, LocalDate hoy) {
        long atraso = prestamo.diasAtraso(hoy);
        if (prestamo.estaDevuelto()) {
            return atraso > 0 ? "Devuelto con " + atraso + " día(s) de atraso" : "Devuelto a tiempo";
        }
        return atraso > 0 ? "Atrasado " + atraso + " día(s)" : "Prestado";
    }

    /** @return un resumen como "5 préstamo(s) · 2 sin devolver · 1 atrasado(s)". */
    static String resumen(List<PrestamoDetalle> detalles, LocalDate hoy) {
        long activos = detalles.stream().filter(d -> !d.prestamo().estaDevuelto()).count();
        long atrasados = detalles.stream()
                .filter(d -> !d.prestamo().estaDevuelto() && d.prestamo().estaAtrasado(hoy))
                .count();
        return detalles.size() + " préstamo(s) · " + activos + " sin devolver · " + atrasados + " atrasado(s)";
    }
}