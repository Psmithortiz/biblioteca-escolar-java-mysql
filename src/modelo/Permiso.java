package modelo;

/**
 * Acciones que un usuario puede realizar. El modelo define qué puede hacer cada rol;
 * la vista decide cómo mostrarlo.
 */
public enum Permiso {
    GESTIONAR_LIBROS,
    GESTIONAR_ESTUDIANTES,
    GESTIONAR_CATEGORIAS,
    GESTIONAR_PRESTAMOS,
    VER_REPORTES,
    CONSULTAR_CATALOGO,
    VER_HISTORIAL_PROPIO
}
