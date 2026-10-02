package vista;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Base de las pantallas de la ventana principal: comparten el layout y los márgenes,
 * y cada una sabe recargar sus datos cuando se muestra.
 */
public abstract class Pantalla extends JPanel {

    protected static final int MARGEN = 10;

    protected Pantalla() {
        setLayout(new BorderLayout(MARGEN, MARGEN));
        setBorder(BorderFactory.createEmptyBorder(MARGEN, MARGEN, MARGEN, MARGEN));
    }

    /** Vuelve a leer sus datos desde la BD. La ventana lo llama cada vez que la muestra. */
    public abstract void refrescar();
}