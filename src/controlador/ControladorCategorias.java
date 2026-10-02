package controlador;

import dao.CategoriaDAO;
import modelo.Categoria;
import utils.Validador;

import java.util.List;

/** Gestiona las categorías a través de {@link CategoriaDAO}. */
public class ControladorCategorias {

    private final CategoriaDAO categoriaDAO;

    /** @param categoriaDAO acceso a las categorías; no puede ser nulo. */
    public ControladorCategorias(CategoriaDAO categoriaDAO) {
        Validador.objetoNoNulo(categoriaDAO, "categoriaDAO");
        this.categoriaDAO = categoriaDAO;
    }

    /**
     * @return la categoría ya guardada, con su id.
     * @throws IllegalArgumentException  si el nombre es inválido (no se guarda nada).
     * @throws dao.PersistenciaException si falla la base de datos, incluido un nombre repetido.
     */
    public Categoria registrarCategoria(String nombre) {
        Categoria categoria = new Categoria(nombre);
        categoriaDAO.create(categoria);
        return categoria;
    }

    /**
     * @return todas las categorías, ordenadas por nombre.
     * @throws dao.PersistenciaException si falla la base de datos.
     */
    public List<Categoria> verCategorias() {
        return categoriaDAO.readAll();
    }

    /**
     * Arma una categoría nueva con el mismo id en vez de modificar la que tiene la vista.
     *
     * @return {@code true} si se actualizó; {@code false} si ya no existe.
     * @throws IllegalArgumentException  si el nombre es inválido.
     * @throws dao.PersistenciaException si falla la base de datos, incluido un nombre repetido.
     */
    public boolean actualizarCategoria(Categoria actual, String nuevoNombre) {
        Validador.objetoNoNulo(actual, "categoría");
        Categoria actualizada = new Categoria(nuevoNombre);
        actualizada.asignarId(actual.getId());
        return categoriaDAO.update(actualizada);
    }

    /**
     * @return {@code true} si se eliminó; {@code false} si ya no existía.
     * @throws dao.PersistenciaException si falla la base de datos, incluido el caso en que tiene libros.
     */
    public boolean eliminarCategoria(int id) {
        return categoriaDAO.delete(id);
    }
}