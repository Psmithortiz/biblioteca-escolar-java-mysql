package modelo;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Usuario con gestión total de la biblioteca. */
public final class Bibliotecario extends Usuario {

    private static final Set<Permiso> PERMISOS = Collections.unmodifiableSet(EnumSet.of(
            Permiso.GESTIONAR_LIBROS,
            Permiso.GESTIONAR_ESTUDIANTES,
            Permiso.GESTIONAR_CATEGORIAS,
            Permiso.GESTIONAR_PRESTAMOS,
            Permiso.VER_REPORTES));

    /** Solo {@link FabricaUsuarios} lo crea. */
    Bibliotecario(String nombre, String rut, String correo) {
        super(nombre, rut, correo);
    }

    @Override
    public Rol getRol() {
        return Rol.BIBLIOTECARIO;
    }

    @Override
    public Set<Permiso> getPermisos() {
        return PERMISOS;
    }
}