package vista;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;

/** Temporal: ocupa el lugar de una pantalla que aún no se construye. */
class PantallaProvisoria extends Pantalla {

    PantallaProvisoria(String nombre) {
        add(new JLabel(nombre + " (en construcción)", SwingConstants.CENTER), BorderLayout.CENTER);
    }

    @Override
    public void refrescar() {
        // Sin datos que recargar.
    }
}