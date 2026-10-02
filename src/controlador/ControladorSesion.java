package controlador;

import dao.UsuarioDAO;
import modelo.Usuario;
import utils.Validador;

/** Inicia sesión a través de {@link UsuarioDAO}. */
public class ControladorSesion {

    private final UsuarioDAO usuarioDAO;

    /** @param usuarioDAO acceso a los usuarios; no puede ser nulo. */
    public ControladorSesion(UsuarioDAO usuarioDAO) {
        Validador.objetoNoNulo(usuarioDAO, "usuarioDAO");
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * @param identificador RUT o correo.
     * @return el usuario autenticado, o {@code null} si el identificador o la contraseña no coinciden.
     * @throws IllegalArgumentException     si algún dato está vacío.
     * @throws dao.PersistenciaException    si falla la base de datos.
     */
    public Usuario iniciarSesion(String identificador, String contrasena) {
        return usuarioDAO.autenticar(identificador, contrasena);
    }
}