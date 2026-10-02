package modelo;

import utils.Validador;

import java.util.Set;

/**
 * Usuario autenticado del sistema. Cada subclase define su rol y sus permisos.
 * No guarda la contraseña: el login se resuelve en la BD.
 */
public abstract class Usuario extends Entidad {

    private static final int LARGO_MAXIMO_NOMBRE = 100;
    private static final int LARGO_MAXIMO_CORREO = 100;

    private final String nombre;
    private final String rut;
    private final String correo;

    /** @throws IllegalArgumentException si algún dato es inválido. */
    protected Usuario(String nombre, String rut, String correo) {
        Validador.cadenaNoVacia(nombre, "nombre");
        Validador.largoMaximo(nombre, LARGO_MAXIMO_NOMBRE, "nombre");
        Validador.formatoRut(rut, "rut");
        Validador.formatoCorreo(correo, "correo");
        Validador.largoMaximo(correo, LARGO_MAXIMO_CORREO, "correo");
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
    }

    public abstract Rol getRol();

    /** @return los permisos del rol, en orden de declaración; el conjunto no se puede modificar. */
    public abstract Set<Permiso> getPermisos();

    public final boolean puede(Permiso permiso) {
        return getPermisos().contains(permiso);
    }

    public String getNombre() {
        return nombre;
    }

    public String getRut() {
        return rut;
    }

    public String getCorreo() {
        return correo;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + getId() + ", rut=" + rut + "}";
    }
}