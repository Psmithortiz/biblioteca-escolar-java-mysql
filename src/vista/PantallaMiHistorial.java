package vista;

import controlador.ControladorEstudiantes;
import controlador.ControladorPrestamos;
import dao.PersistenciaException;
import modelo.Estudiante;
import modelo.PrestamoDetalle;
import modelo.Usuario;

import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.List;

/** Historial del estudiante que inició sesión. Su ficha se busca por el RUT del usuario. */
public class PantallaMiHistorial extends Pantalla {

    private final Usuario usuario;
    private final ControladorEstudiantes controladorEstudiantes;
    private final ControladorPrestamos controladorPrestamos;

    private final TablaPrestamos tabla = new TablaPrestamos(false);
    private final JLabel resumen = new JLabel(" ");

    public PantallaMiHistorial(Usuario usuario, ControladorEstudiantes controladorEstudiantes,
                               ControladorPrestamos controladorPrestamos) {
        this.usuario = usuario;
        this.controladorEstudiantes = controladorEstudiantes;
        this.controladorPrestamos = controladorPrestamos;
        add(resumen, BorderLayout.NORTH);
        add(tabla, BorderLayout.CENTER);
    }

    @Override
    public void refrescar() {
        try {
            Estudiante ficha = controladorEstudiantes.buscarPorRut(usuario.getRut());
            if (ficha == null) {
                tabla.mostrar(List.of());
                resumen.setText("Tu usuario no tiene una ficha de estudiante asociada: consulta en la biblioteca.");
                return;
            }
            List<PrestamoDetalle> historial = controladorPrestamos.verHistorial(ficha.getId());
            tabla.mostrar(historial);
            resumen.setText(Formatos.resumen(historial, LocalDate.now()));
        } catch (PersistenciaException e) {
            tabla.mostrar(List.of());
            Dialogos.errorBaseDatos(this, e);
        }
    }
}