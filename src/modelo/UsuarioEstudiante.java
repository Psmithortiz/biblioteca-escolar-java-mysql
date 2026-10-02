package modelo;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Usuario estudiante: solo consulta el catálogo y su historial.
 * Se vincula con su ficha de {@link Estudiante} por RUT.
 */
public final class UsuarioEstudiante extends Usuario {

    private static final Set<Permiso> PERMISOS = Collections.unmodifiableSet(EnumSet.of(
            Permiso.CONSULTAR_CATALOGO,
            Permiso.VER_HISTORIAL_PROPIO));

    /** Solo {@link FabricaUsuarios} lo crea. */
    UsuarioEstudiante(String nombre, String rut, String correo) {
        super(nombre, rut, correo);
    }

    @Override
    public Rol getRol() {
        return Rol.ESTUDIANTE;
    }

    @Override
    public Set<Permiso> getPermisos() {
        return PERMISOS;
    }
}