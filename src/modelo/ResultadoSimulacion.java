package modelo;

import java.util.List;

/**
 * Resultado de una simulación de préstamos simultáneos sobre un mismo libro.
 *
 * @param registro una línea por solicitud: qué hilo la atendió y cómo terminó.
 */
public record ResultadoSimulacion(int solicitudes, int aprobados, int rechazados, int errores, List<String> registro) {

    public ResultadoSimulacion {
        registro = List.copyOf(registro); // copia defensiva: el record no cambia desde afuera
    }
}