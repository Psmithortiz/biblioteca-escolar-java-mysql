package vista;

import dao.PersistenciaException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Mensajes al usuario con títulos e íconos uniformes. Distingue los tres tipos de error:
 * dato inválido ({@link IllegalArgumentException}), operación que el estado actual no permite
 * ({@link IllegalStateException}) y falla de la base de datos ({@link PersistenciaException}).
 */
final class Dialogos {

    private static final String TITULO_ERROR_VALIDACION = "Error de validación";
    private static final String TITULO_OPERACION_NO_PERMITIDA = "Operación no permitida";
    private static final String TITULO_ERROR_BD = "Error de base de datos";
    private static final String TITULO_EXITO = "Operación exitosa";

    /** Clase de utilidad: no se instancia. */
    private Dialogos() {
    }

    /** Un dato ingresado es inválido (campo vacío, formato incorrecto, fuera de rango). */
    static void errorValidacion(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, TITULO_ERROR_VALIDACION, JOptionPane.ERROR_MESSAGE);
    }

    /** La operación no corresponde al estado actual (por ejemplo, devolver un libro ya devuelto). */
    static void operacionNoPermitida(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, TITULO_OPERACION_NO_PERMITIDA, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Falló la base de datos. Si la BD rechazó los datos por una regla (duplicado, registros
     * asociados), se muestra como advertencia; si es una falla técnica, como error, y la traza
     * completa queda en la consola.
     */
    static void errorBaseDatos(Component padre, PersistenciaException e) {
        if (!e.esFallaTecnica()) {
            operacionNoPermitida(padre, e.getMessage());
            return;
        }
        e.printStackTrace();
        JOptionPane.showMessageDialog(padre, e.getMessage(), TITULO_ERROR_BD, JOptionPane.ERROR_MESSAGE);
    }

    /** La operación terminó bien. */
    static void exito(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, TITULO_EXITO, JOptionPane.INFORMATION_MESSAGE);
    }

    /** @return {@code true} solo si el usuario eligió "Sí"; cerrar el diálogo cuenta como "No". */
    static boolean confirmar(Component padre, String titulo, String pregunta) {
        int respuesta = JOptionPane.showConfirmDialog(padre, pregunta, titulo,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return respuesta == JOptionPane.YES_OPTION;
    }

    /**
     * Muestra un selector (un combo o un campo) bajo un mensaje, con Aceptar/Cancelar.
     *
     * @return {@code true} solo si el usuario eligió "Aceptar".
     */
    static boolean elegir(Component padre, String titulo, String mensaje, JComponent selector) {
        Object[] contenido = {mensaje, selector};
        int respuesta = JOptionPane.showConfirmDialog(padre, contenido, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        return respuesta == JOptionPane.OK_OPTION;
    }
}