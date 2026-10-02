package vista;

/** Avisa que los libros guardados cambiaron (creación o edición). */
@FunctionalInterface
public interface LibroListener {

    /** Se invoca cuando los libros acaban de cambiar. */
    void onLibrosCambiados();
}