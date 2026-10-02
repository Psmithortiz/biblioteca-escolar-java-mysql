package dao;

import modelo.Usuario;

/**
 * Acceso a datos de {@link Usuario}. Los usuarios son de solo lectura en la app:
 * no hay CRUD, solo autenticación.
 */
public interface UsuarioDAO {

    /**
     * Autentica con RUT o correo y contraseña.
     *
     * @param identificador RUT o correo del usuario.
     * @param contrasena    contraseña en texto plano; se compara su hash.
     * @return el usuario autenticado, o {@code null} si el identificador o la contraseña no coinciden.
     * @throws IllegalArgumentException si algún dato está vacío.
     * @throws PersistenciaException    si falla el acceso a la base de datos.
     */
    Usuario autenticar(String identificador, String contrasena);
}