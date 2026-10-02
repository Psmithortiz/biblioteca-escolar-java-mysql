package vista;

/** Avisa que las categorías guardadas cambiaron (creación o edición). */
@FunctionalInterface
public interface CategoriaListener {

    /** Se invoca cuando las categorías acaban de cambiar. */
    void onCategoriasCambiadas();
}