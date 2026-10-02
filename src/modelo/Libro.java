package modelo;

import utils.Validador;

/** Libro del catálogo. El stock es la cantidad de ejemplares disponibles para préstamo. */
public class Libro extends Entidad {

    private static final int LARGO_MAXIMO_TITULO = 200;
    private static final int LARGO_MAXIMO_AUTOR = 100;
    private static final int LARGO_MAXIMO_ISBN = 20;
    private static final int LARGO_MAXIMO_EDITORIAL = 100;

    private final String titulo;
    private final String autor;
    private final String isbn;
    private final String editorial;
    private final int stock;
    private final int idCategoria;

    /** @throws IllegalArgumentException si algún dato es inválido. */
    public Libro(String titulo, String autor, String isbn, String editorial, int stock, int idCategoria) {
        Validador.cadenaNoVacia(titulo, "título");
        Validador.largoMaximo(titulo.strip(), LARGO_MAXIMO_TITULO, "título");
        Validador.cadenaNoVacia(autor, "autor");
        Validador.largoMaximo(autor.strip(), LARGO_MAXIMO_AUTOR, "autor");
        Validador.cadenaNoVacia(isbn, "ISBN");
        Validador.largoMaximo(isbn.strip(), LARGO_MAXIMO_ISBN, "ISBN");
        Validador.cadenaNoVacia(editorial, "editorial");
        Validador.largoMaximo(editorial.strip(), LARGO_MAXIMO_EDITORIAL, "editorial");
        Validador.noNegativo(stock, "stock");
        Validador.positivo(idCategoria, "categoría");
        this.titulo = titulo.strip();
        this.autor = autor.strip();
        this.isbn = isbn.strip();
        this.editorial = editorial.strip();
        this.stock = stock;
        this.idCategoria = idCategoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getEditorial() {
        return editorial;
    }

    public int getStock() {
        return stock;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public boolean hayStock() {
        return stock > 0;
    }

    @Override
    public String toString() {
        return "Libro{id=" + getId() + ", isbn=" + isbn + ", stock=" + stock + "}";
    }
}