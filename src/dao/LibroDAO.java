package dao;

import modelo.Libro;

import java.util.List;

/**
 * Acceso a datos de {@link Libro}. El {@code update} no modifica el stock:
 * el stock solo se ajusta sobre el valor actual, para no pisar préstamos concurrentes.
 */
public interface LibroDAO extends CrudDAO<Libro> {

    /**
     * Busca libros por texto y categoría; un filtro nulo no se aplica.
     *
     * @param texto       parte del título o del autor, sin distinguir mayúsculas; {@code null} o en blanco = todos.
     * @param idCategoria categoría exacta; {@code null} = todas.
     * @return los libros que cumplen ambos filtros, ordenados por título.
     * @throws PersistenciaException si falla el acceso a la base de datos.
     */
    List<Libro> buscar(String texto, Integer idCategoria);

    /**
     * Suma (o resta, si es negativa) la cantidad al stock actual del libro.
     *
     * @return {@code true} si se ajustó; {@code false} si el libro no existe
     *         o el stock quedaría negativo.
     * @throws IllegalArgumentException si la cantidad es 0.
     * @throws PersistenciaException    si falla el acceso a la base de datos.
     */
    boolean ajustarStock(int idLibro, int cantidad);
}