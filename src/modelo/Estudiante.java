package modelo;

import utils.Validador;

/** Ficha de un estudiante. Se vincula con su usuario por RUT. */
public class Estudiante extends Entidad {

    private static final int LARGO_MAXIMO_NOMBRE = 100;
    private static final int LARGO_MAXIMO_CURSO = 20;
    private static final int LARGO_MAXIMO_CORREO = 100;

    private final String nombre;
    private final String rut;
    private final String curso;
    private final String correo;

    /**
     * @param curso opcional: {@code null} o en blanco se guarda como {@code null}.
     * @throws IllegalArgumentException si algún dato es inválido.
     */
    public Estudiante(String nombre, String rut, String curso, String correo) {
        Validador.cadenaNoVacia(nombre, "nombre");
        Validador.largoMaximo(nombre.strip(), LARGO_MAXIMO_NOMBRE, "nombre");
        Validador.formatoRut(rut, "rut");
        Validador.largoMaximo(curso == null ? null : curso.strip(), LARGO_MAXIMO_CURSO, "curso");
        Validador.formatoCorreo(correo, "correo");
        Validador.largoMaximo(correo, LARGO_MAXIMO_CORREO, "correo");
        this.nombre = nombre.strip();
        this.rut = rut;
        this.curso = (curso == null || curso.isBlank()) ? null : curso.strip();
        this.correo = correo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRut() {
        return rut;
    }

    /** @return el curso, o {@code null} si no tiene. */
    public String getCurso() {
        return curso;
    }

    public String getCorreo() {
        return correo;
    }

    @Override
    public String toString() {
        return "Estudiante{id=" + getId() + ", rut=" + rut + "}";
    }
}