package vista;

/** Avisa que los préstamos cambiaron (préstamo o devolución). */
@FunctionalInterface
public interface PrestamoListener {

    /** Se invoca cuando los préstamos acaban de cambiar. */
    void onPrestamosCambiados();
}