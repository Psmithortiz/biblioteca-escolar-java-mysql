package modelo;

import utils.Validador;

/**
 * Préstamo junto con los datos para mostrarlo: nombre y RUT del estudiante y título del libro.
 * Solo de lectura: se arma con un JOIN al consultar.
 */
public record PrestamoDetalle(Prestamo prestamo, String nombreEstudiante, String rutEstudiante, String tituloLibro) {

    /** @throws IllegalArgumentException si el préstamo es nulo. */
    public PrestamoDetalle {
        Validador.objetoNoNulo(prestamo, "préstamo");
    }
}