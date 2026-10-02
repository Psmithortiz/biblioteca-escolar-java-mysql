package modelo;

/**
 * Base de las entidades que se persisten como una fila con id. Nacen sin id y lo reciben
 * una sola vez, cuando la BD se lo asigna. Dos entidades son iguales si son de la misma
 * clase y tienen el mismo id, es decir, si representan la misma fila.
 */
public abstract class Entidad {

    /** {@code null} mientras la entidad no se haya guardado en la BD. */
    private Integer id;

    /**
     * Registra el id que le asignó la BD. Solo puede hacerse una vez.
     *
     * @throws IllegalArgumentException si el id no es positivo.
     * @throws IllegalStateException    si la entidad ya tenía id.
     */
    public final void asignarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser positivo.");
        }
        if (this.id != null) {
            throw new IllegalStateException(
                    getClass().getSimpleName() + " ya tiene el id " + this.id + ".");
        }
        this.id = id;
    }

    /** @return el id, o {@code null} si aún no se guarda en la BD. */
    public final Integer getId() {
        return id;
    }

    /** Iguales si son de la misma clase y ambas tienen el mismo id; sin id, solo es igual a sí misma. */
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id != null && id.equals(((Entidad) o).id);
    }

    /** Depende solo de la clase, no del id: el id puede asignarse después de crear el objeto. */
    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}