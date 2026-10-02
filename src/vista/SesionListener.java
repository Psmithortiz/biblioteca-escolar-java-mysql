package vista;

/** Avisa que el usuario cerró sesión, para volver a mostrar el login. */
@FunctionalInterface
public interface SesionListener {

    /** Se invoca después de cerrar la ventana principal. */
    void onSesionCerrada();
}