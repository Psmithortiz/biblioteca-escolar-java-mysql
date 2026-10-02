package vista;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;
import java.util.function.Function;

/**
 * Renderers para los combos: muestran cada opción con el texto que indica una función,
 * en vez de su {@code toString()}, que en las entidades es técnico.
 */
final class Renderizadores {

    /** Clase de utilidad: no se instancia. */
    private Renderizadores() {
    }

    /** Renderer para combos sin opción nula. */
    static <T> DefaultListCellRenderer conTexto(Function<T, String> texto) {
        return conTexto(texto, "");
    }

    /**
     * Renderer para combos con una opción nula con significado propio, como "Todas" en un filtro.
     *
     * @param textoSiNulo cómo mostrar la opción {@code null}.
     */
    static <T> DefaultListCellRenderer conTexto(Function<T, String> texto, String textoSiNulo) {
        return new DefaultListCellRenderer() {
            @Override
            @SuppressWarnings("unchecked") // el combo solo contiene elementos de tipo T (o null)
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean conFoco) {
                String mostrado = (valor == null) ? textoSiNulo : texto.apply((T) valor);
                return super.getListCellRendererComponent(lista, mostrado, indice, seleccionado, conFoco);
            }
        };
    }
}