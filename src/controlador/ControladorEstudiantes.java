package controlador;

import dao.EstudianteDAO;
import modelo.Estudiante;
import utils.Validador;

import java.util.List;

/** Gestiona las fichas de estudiantes a través de {@link EstudianteDAO}. El RUT no se edita. */
public class ControladorEstudiantes {

    private final EstudianteDAO estudianteDAO;

    /** @param estudianteDAO acceso a los estudiantes; no puede ser nulo. */
    public ControladorEstudiantes(EstudianteDAO estudianteDAO) {
        Validador.objetoNoNulo(estudianteDAO, "estudianteDAO");
        this.estudianteDAO = estudianteDAO;
    }

    /**
     * @param curso opcional: {@code null} o en blanco = sin curso.
     * @return el estudiante ya guardado, con su id.
     * @throws IllegalArgumentException  si algún dato es inválido (no se guarda nada).
     * @throws dao.PersistenciaException si falla la base de datos, incluido un RUT repetido.
     */
    public Estudiante registrarEstudiante(String nombre, String rut, String curso, String correo) {
        Estudiante estudiante = new Estudiante(nombre, rut, curso, correo);
        estudianteDAO.create(estudiante);
        return estudiante;
    }

    /**
     * @return todos los estudiantes, ordenados por nombre.
     * @throws dao.PersistenciaException si falla la base de datos.
     */
    public List<Estudiante> verEstudiantes() {
        return estudianteDAO.readAll();
    }

    /**
     * @return la ficha del estudiante, o {@code null} si no existe ninguna con ese RUT.
     * @throws dao.PersistenciaException si falla la base de datos.
     */
    public Estudiante buscarPorRut(String rut) {
        return estudianteDAO.buscarPorRut(rut);
    }

    /**
     * Cambia nombre, curso y correo; conserva el RUT del estudiante actual.
     *
     * @return {@code true} si se actualizó; {@code false} si ya no existe.
     * @throws IllegalArgumentException  si algún dato es inválido.
     * @throws dao.PersistenciaException si falla la base de datos.
     */
    public boolean actualizarEstudiante(Estudiante actual, String nombre, String curso, String correo) {
        Validador.objetoNoNulo(actual, "estudiante");
        return estudianteDAO.update(new Estudiante(actual.getId(), nombre, actual.getRut(), curso, correo));
    }

    /**
     * @return {@code true} si se eliminó; {@code false} si ya no existía.
     * @throws dao.PersistenciaException si falla la base de datos, incluido el caso en que tiene préstamos.
     */
    public boolean eliminarEstudiante(int id) {
        return estudianteDAO.delete(id);
    }
}