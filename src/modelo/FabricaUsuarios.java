package modelo;

import utils.Validador;

/**
 * Único lugar que decide qué subclase de {@link Usuario} corresponde a cada {@link Rol},
 * para que ni la vista ni los DAO conozcan las subclases.
 */
public final class FabricaUsuarios {

    /** Clase de utilidad: no se instancia. */
    private FabricaUsuarios() {
    }

    /**
     * Reconstruye un usuario leído desde la BD, con su id.
     *
     * @throws IllegalArgumentException si el rol es nulo o algún dato es inválido.
     */
    public static Usuario reconstruir(int id, Rol rol, String nombre, String rut, String correo) {
        Validador.objetoNoNulo(rol, "rol");
        Usuario usuario = switch (rol) {
            case BIBLIOTECARIO -> new Bibliotecario(nombre, rut, correo);
            case ESTUDIANTE -> new UsuarioEstudiante(nombre, rut, correo);
        };
        usuario.asignarId(id);
        return usuario;
    }
}