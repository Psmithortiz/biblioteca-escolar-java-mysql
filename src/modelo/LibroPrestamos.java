package modelo;

/** Fila del reporte de libros más prestados: cuántas veces se ha prestado cada libro. */
public record LibroPrestamos(int idLibro, String titulo, String autor, int vecesPrestado) {
}