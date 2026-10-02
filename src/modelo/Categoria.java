package modelo;

import utils.Validador;

/** Categoría de libros. El nombre es único en la BD. */
public class Categoria extends Entidad {

    private static final int LARGO_MAXIMO_NOMBRE = 100;

    private final String nombre;

    /** @throws IllegalArgumentException si el nombre es inválido. */
    public Categoria(String nombre) {
        Validador.cadenaNoVacia(nombre, "nombre");
        Validador.largoMaximo(nombre.strip(), LARGO_MAXIMO_NOMBRE, "nombre");
        this.nombre = nombre.strip();
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return "Categoria{id=" + getId() + ", nombre=" + nombre + "}";
    }
}