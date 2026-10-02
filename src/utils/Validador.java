package utils;

import java.util.regex.Pattern;

/**
 * Centraliza las validaciones. Todas lanzan {@link IllegalArgumentException}
 * con un mensaje que nombra el campo inválido.
 */
public final class Validador {

    /** 7 u 8 dígitos, guion y dígito verificador (0-9 o K). No se valida el DV. */
    private static final Pattern FORMATO_RUT = Pattern.compile("\\d{7,8}-[0-9kK]");

    /** Forma mínima nombre@dominio.ext, sin espacios. */
    private static final Pattern FORMATO_CORREO = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    /** Clase de utilidad: no se instancia. */
    private Validador() {
    }

    /** @throws IllegalArgumentException si el valor es nulo o está en blanco. */
    public static void cadenaNoVacia(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
    }

    /** @throws IllegalArgumentException si el objeto es nulo. */
    public static void objetoNoNulo(Object objeto, String campo) {
        if (objeto == null) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede ser nulo.");
        }
    }

    /** @throws IllegalArgumentException si el valor supera el largo máximo permitido. */
    public static void largoMaximo(String valor, int maximo, String campo) {
        if (valor != null && valor.length() > maximo) {
            throw new IllegalArgumentException(
                    "El campo '" + campo + "' no puede superar " + maximo + " caracteres.");
        }
    }

    /** @throws IllegalArgumentException si el valor no es mayor que cero. */
    public static void positivo(int valor, String campo) {
        if (valor <= 0) {
            throw new IllegalArgumentException("El campo '" + campo + "' debe ser mayor que cero.");
        }
    }

    /** @throws IllegalArgumentException si el valor es negativo. */
    public static void noNegativo(int valor, String campo) {
        if (valor < 0) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede ser negativo.");
        }
    }

    /** @throws IllegalArgumentException si el RUT no tiene el formato 12345678-9 (sin puntos, con guion). */
    public static void formatoRut(String rut, String campo) {
        if (rut == null || !FORMATO_RUT.matcher(rut).matches()) {
            throw new IllegalArgumentException(
                    "El campo '" + campo + "' debe tener el formato 12345678-9 (sin puntos, con guion).");
        }
    }

    /** @throws IllegalArgumentException si el correo no tiene la forma nombre@dominio.ext. */
    public static void formatoCorreo(String correo, String campo) {
        if (correo == null || !FORMATO_CORREO.matcher(correo).matches()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no es un correo válido.");
        }
    }
}