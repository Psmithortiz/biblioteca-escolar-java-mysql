package vista;

import controlador.ControladorEstudiantes;
import controlador.ControladorPrestamos;
import dao.PersistenciaException;
import modelo.Estudiante;
import modelo.LibroPrestamos;
import modelo.PrestamoDetalle;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;

/**
 * Reportes del bibliotecario, en tres pestañas: libros más prestados, libros actualmente
 * en préstamo e historial por estudiante.
 */
public class PantallaReportes extends Pantalla {

    private static final String[] COLUMNAS_MAS_PRESTADOS = {"#", "Título", "Autor", "Veces prestado"};
    private static final int ANCHO_COLUMNA_POSICION = 40;

    private final ControladorPrestamos controladorPrestamos;
    private final ControladorEstudiantes controladorEstudiantes;

    private final DefaultTableModel modeloMasPrestados = new DefaultTableModel(COLUMNAS_MAS_PRESTADOS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final TablaPrestamos tablaActivos = new TablaPrestamos(true);
    private final JLabel resumenActivos = new JLabel(" ");
    private final JComboBox<Estudiante> comboEstudiante = new JComboBox<>();
    private final TablaPrestamos tablaHistorial = new TablaPrestamos(false);
    private final JLabel resumenHistorial = new JLabel(" ");

    /** Evita recargar el historial por cada ítem mientras se rellena el combo. */
    private boolean rellenandoCombo;

    public PantallaReportes(ControladorPrestamos controladorPrestamos, ControladorEstudiantes controladorEstudiantes) {
        this.controladorPrestamos = controladorPrestamos;
        this.controladorEstudiantes = controladorEstudiantes;
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Libros más prestados", crearPestanaMasPrestados());
        pestanas.addTab("Libros en préstamo", crearPestanaActivos());
        pestanas.addTab("Historial por estudiante", crearPestanaHistorial());
        add(pestanas, BorderLayout.CENTER);
    }

    private JScrollPane crearPestanaMasPrestados() {
        JTable tabla = new JTable(modeloMasPrestados);
        tabla.getColumnModel().getColumn(0).setMaxWidth(ANCHO_COLUMNA_POSICION);
        return new JScrollPane(tabla);
    }

    private JPanel crearPestanaActivos() {
        JPanel panel = new JPanel(new BorderLayout(MARGEN, MARGEN));
        panel.add(resumenActivos, BorderLayout.NORTH);
        panel.add(tablaActivos, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPestanaHistorial() {
        comboEstudiante.setRenderer(Renderizadores.conTexto((Estudiante e) -> e.getNombre() + " · " + e.getRut()));
        comboEstudiante.addActionListener(e -> {
            if (!rellenandoCombo) {
                cargarHistorial();
            }
        });
        JPanel seleccion = new JPanel(new FlowLayout(FlowLayout.LEFT, MARGEN, 0));
        seleccion.add(new JLabel("Estudiante:"));
        seleccion.add(comboEstudiante);
        seleccion.add(resumenHistorial);

        JPanel panel = new JPanel(new BorderLayout(MARGEN, MARGEN));
        panel.add(seleccion, BorderLayout.NORTH);
        panel.add(tablaHistorial, BorderLayout.CENTER);
        return panel;
    }

    /** Recarga los tres reportes; en el historial conserva el estudiante elegido. */
    @Override
    public void refrescar() {
        cargarMasPrestados();
        cargarActivos();
        recargarEstudiantes();
        cargarHistorial();
    }

    private void cargarMasPrestados() {
        modeloMasPrestados.setRowCount(0);
        try {
            List<LibroPrestamos> filas = controladorPrestamos.verLibrosMasPrestados();
            for (int i = 0; i < filas.size(); i++) {
                LibroPrestamos fila = filas.get(i);
                modeloMasPrestados.addRow(new Object[]{i + 1, fila.titulo(), fila.autor(), fila.vecesPrestado()});
            }
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
        }
    }

    private void cargarActivos() {
        try {
            List<PrestamoDetalle> activos = controladorPrestamos.verPrestamosActivos();
            tablaActivos.mostrar(activos);
            resumenActivos.setText(Formatos.resumen(activos, LocalDate.now()));
        } catch (PersistenciaException e) {
            tablaActivos.mostrar(List.of());
            Dialogos.errorBaseDatos(this, e);
        }
    }

    /** Vuelve a llenar el combo y selecciona de nuevo al estudiante que estaba elegido. */
    private void recargarEstudiantes() {
        Estudiante anterior = comboEstudiante.getItemAt(comboEstudiante.getSelectedIndex());
        rellenandoCombo = true;
        try {
            comboEstudiante.removeAllItems();
            for (Estudiante estudiante : controladorEstudiantes.verEstudiantes()) {
                comboEstudiante.addItem(estudiante);
                if (estudiante.equals(anterior)) {
                    comboEstudiante.setSelectedItem(estudiante);
                }
            }
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
        } finally {
            rellenandoCombo = false;
        }
    }

    private void cargarHistorial() {
        Estudiante estudiante = comboEstudiante.getItemAt(comboEstudiante.getSelectedIndex());
        if (estudiante == null) {
            tablaHistorial.mostrar(List.of());
            resumenHistorial.setText(" ");
            return;
        }
        try {
            List<PrestamoDetalle> historial = controladorPrestamos.verHistorial(estudiante.getId());
            tablaHistorial.mostrar(historial);
            resumenHistorial.setText(Formatos.resumen(historial, LocalDate.now()));
        } catch (PersistenciaException e) {
            tablaHistorial.mostrar(List.of());
            Dialogos.errorBaseDatos(this, e);
        }
    }
}