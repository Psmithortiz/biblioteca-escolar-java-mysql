package controlador;

import dao.LibroDAO;
import modelo.Libro;
import utils.Validador;

import java.util.List;

/**
 * Gestiona los libros a través de {@link LibroDAO}. El stock no se edita con los demás datos:
 * se ajusta sobre el valor actual con {@link #ajustarStock}.
 */
public class ControladorLibros {

    private final LibroDAO libroDAO;

    /** @param libroDAO acceso a los libros; no puede ser nulo. */
    public ControladorLibros(LibroDAO libroDAO) {
        Validador.objetoNoNulo(libroDAO, "libroDAO");
        this.libroDAO = libroDAO;
    }

    /**
     * @return el libro ya guardado, con su id.
     * @throws IllegalArgumentException  si algún dato es inválido (no se guarda nada).
     * @throws dao.PersistenciaException si falla la base de datos, incluido un ISBN repetido.
     */
    public Libro registrarLibro(String titulo, String autor, String isbn, String editorial,
                                int stockInicial, int idCategoria) {
        Libro libro = new Libro(titulo, autor, isbn, editorial, stockInicial, idCategoria);
        libroDAO.create(libro);
        return libro;
    }

    /**
     * @param texto       parte del título o del autor; {@code null} o en blanco = sin filtro.
     * @param idCategoria categoría exacta; {@code null} = todas.
     * @return los libros que cumplen los filtros, ordenados por título.
     * @throws dao.PersistenciaException si falla la base de datos.
     */
    public List<Libro> buscarLibros(String texto, Integer idCategoria) {
        return libroDAO.buscar(texto, idCategoria);
    }

    /**
     * Cambia los datos del libro, salvo el stock. Arma un libro nuevo con el mismo id.
     *
     * @return {@code true} si se actualizó; {@code false} si ya no existe.
     * @throws IllegalArgumentException  si algún dato es inválido.
     * @throws dao.PersistenciaException si falla la base de datos, incluido un ISBN repetido.
     */
    public boolean actualizarLibro(Libro actual, String titulo, String autor, String isbn,
                                   String editorial, int idCategoria) {
        Validador.objetoNoNulo(actual, "libro");
        // El stock se copia solo para construir un libro válido: el DAO no lo guarda.
        Libro actualizado = new Libro(titulo, autor, isbn, editorial, actual.getStock(), idCategoria);
        actualizado.asignarId(actual.getId());
        return libroDAO.update(actualizado);
    }

    /**
     * Suma (o resta, si es negativa) la cantidad al stock actual del libro.
     *
     * @return {@code true} si se ajustó; {@code false} si el stock quedaría negativo o el libro ya no existe.
     * @throws IllegalArgumentException  si la cantidad es 0.
     * @throws dao.PersistenciaException si falla la base de datos.
     */
    public boolean ajustarStock(Libro libro, int cantidad) {
        Validador.objetoNoNulo(libro, "libro");
        return libroDAO.ajustarStock(libro.getId(), cantidad);
    }

    /**
     * @return {@code true} si se eliminó; {@code false} si ya no existía.
     * @throws dao.PersistenciaException si falla la base de datos, incluido el caso en que tiene préstamos.
     */
    public boolean eliminarLibro(int id) {
        return libroDAO.delete(id);
    }
}