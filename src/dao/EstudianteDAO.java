package dao;

import modelo.Estudiante;

/**
 * Acceso a datos de {@link Estudiante}. El {@code update} no modifica el RUT:
 * es el vínculo con el usuario del estudiante.
 */
public interface EstudianteDAO extends CrudDAO<Estudiante> {

    /**
     * Busca la ficha del estudiante con el RUT dado.
     *
     * @return el estudiante, o {@code null} si no existe ninguno con ese RUT.
     * @throws PersistenciaException si falla el acceso a la base de datos.
     */
    Estudiante buscarPorRut(String rut);
}