package dao;

import modelo.Entidad;

import java.util.List;

/**
 * Contrato CRUD común a los DAO. Que un id no exista es un resultado normal y se informa
 * con {@code false}; una falla de la base de datos, con {@link PersistenciaException}.
 *
 * @param <T> tipo de entidad que persiste el DAO.
 */
public interface CrudDAO<T extends Entidad> {

    /**
     * Inserta una entidad nueva y le asigna el id generado por la BD.
     *
     * @throws IllegalStateException si la entidad ya tiene id.
     * @throws PersistenciaException si falla el acceso a la base de datos.
     */
    void create(T entidad);

    /**
     * @return todas las entidades guardadas; lista vacía si no hay ninguna.
     * @throws PersistenciaException si falla el acceso a la base de datos.
     */
    List<T> readAll();

    /**
     * Guarda los datos actuales de una entidad ya existente.
     *
     * @return {@code true} si se actualizó; {@code false} si no existe un registro con su id.
     * @throws IllegalStateException si la entidad no tiene id.
     * @throws PersistenciaException si falla el acceso a la base de datos.
     */
    boolean update(T entidad);

    /**
     * Elimina el registro con el id dado.
     *
     * @return {@code true} si se eliminó; {@code false} si no existía.
     * @throws PersistenciaException si falla el acceso a la base de datos o hay registros asociados.
     */
    boolean delete(int id);
}