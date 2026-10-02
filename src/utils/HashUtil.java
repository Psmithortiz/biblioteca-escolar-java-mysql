package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Calcula hashes de contraseñas, para que la contraseña en texto plano nunca salga de la app. */
public final class HashUtil {

    private static final String ALGORITMO = "SHA-256";

    /** Clase de utilidad: no se instancia. */
    private HashUtil() {
    }

    /**
     * @return el hash SHA-256 en hexadecimal (64 caracteres en minúscula),
     *         igual al que calcula {@code SHA2(texto, 256)} en MySQL.
     * @throws IllegalArgumentException si el texto es nulo.
     */
    public static String sha256(String texto) {
        Validador.objetoNoNulo(texto, "texto");
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITMO);
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // Toda JVM está obligada a soportar SHA-256: si falta, la instalación de Java está rota.
            throw new IllegalStateException("La JVM no soporta " + ALGORITMO + ".", e);
        }
    }
}