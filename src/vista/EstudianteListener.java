package vista;

/** Avisa que los estudiantes guardados cambiaron (creación o edición). */
@FunctionalInterface
public interface EstudianteListener {

    /** Se invoca cuando los estudiantes acaban de cambiar. */
    void onEstudiantesCambiados();
}