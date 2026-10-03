package controlador;

import dao.PrestamoDAO;
import modelo.LibroPrestamos;
import modelo.Prestamo;
import modelo.PrestamoDetalle;
import modelo.ResultadoSimulacion;
import utils.Validador;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Registra préstamos y devoluciones, y entrega el historial y los reportes, a través de
 * {@link PrestamoDAO}. La fecha de préstamo y la de devolución son siempre la de hoy.
 */
public class ControladorPrestamos {

    private static final int SEGUNDOS_ESPERA_SIMULACION = 30;

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

    /**
     * Lanza varias solicitudes de préstamo del mismo libro al mismo tiempo, cada una en su
     * propio hilo, para comprobar que el stock nunca se descuenta de más. Una barrera
     * ({@link CountDownLatch}) hace que todos los hilos partan juntos. Registra préstamos reales.
     *
     * @param idsEstudiantes estudiantes a quienes se asignan las solicitudes, en orden circular.
     * @return cuántas se aprobaron, cuántas se rechazaron por falta de stock y cuántas fallaron.
     * @throws IllegalArgumentException si no hay estudiantes o la cantidad no es positiva.
     * @throws InterruptedException     si el hilo que espera la simulación es interrumpido.
     */
    public ResultadoSimulacion simularPrestamosSimultaneos(int idLibro, List<Integer> idsEstudiantes,
                                                           int solicitudes) throws InterruptedException {
        Validador.positivo(solicitudes, "solicitudes");
        Validador.objetoNoNulo(idsEstudiantes, "estudiantes");
        if (idsEstudiantes.isEmpty()) {
            throw new IllegalArgumentException("Se necesita al menos un estudiante para simular.");
        }
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(solicitudes);
        try {
            List<Future<Intento>> futuros = new ArrayList<>();
            for (int i = 0; i < solicitudes; i++) {
                int idEstudiante = idsEstudiantes.get(i % idsEstudiantes.size());
                futuros.add(executor.submit(solicitud(idEstudiante, idLibro, largada)));
            }
            largada.countDown(); // todos los hilos parten a la vez
            return contar(futuros);
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(SEGUNDOS_ESPERA_SIMULACION, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    /** Resultado de una solicitud: qué hilo la atendió y el préstamo obtenido ({@code null} = sin stock). */
    private record Intento(String hilo, Prestamo prestamo) {
    }

    /** Una solicitud: espera la largada y pide el préstamo. */
    private Callable<Intento> solicitud(int idEstudiante, int idLibro, CountDownLatch largada) {
        return () -> {
            largada.await();
            return new Intento(Thread.currentThread().getName(), registrarPrestamo(idEstudiante, idLibro));
        };
    }

    /** Espera cada solicitud y clasifica su resultado. */
    private ResultadoSimulacion contar(List<Future<Intento>> futuros) throws InterruptedException {
        int aprobados = 0;
        int rechazados = 0;
        int errores = 0;
        List<String> registro = new ArrayList<>();
        for (Future<Intento> futuro : futuros) {
            try {
                Intento intento = futuro.get();
                if (intento.prestamo() != null) {
                    aprobados++;
                    registro.add(intento.hilo() + " → aprobado (préstamo #" + intento.prestamo().getId() + ")");
                } else {
                    rechazados++;
                    registro.add(intento.hilo() + " → rechazado: sin stock");
                }
            } catch (ExecutionException e) {
                errores++;
                registro.add("error: " + e.getCause().getMessage());
            }
        }
        return new ResultadoSimulacion(futuros.size(), aprobados, rechazados, errores, registro);
    }
}