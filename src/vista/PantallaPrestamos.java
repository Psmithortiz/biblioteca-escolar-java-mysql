package vista;

import controlador.ControladorEstudiantes;
import controlador.ControladorLibros;
import controlador.ControladorPrestamos;
import dao.PersistenciaException;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;
import modelo.PrestamoDetalle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Préstamos y devoluciones: por defecto muestra los préstamos activos (del vencimiento más
 * antiguo al más nuevo); con "Incluir devueltos" muestra todos. Cada fila indica su estado
 * y sus días de atraso.
 */
public class PantallaPrestamos extends Pantalla {

    private static final String[] COLUMNAS = {"ID", "Estudiante", "RUT", "Libro", "Prestado", "Vence", "Devuelto", "Estado"};
    private static final String SIN_DATO = "—";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final int ANCHO_COLUMNA_ID = 50;

    private final ControladorPrestamos controladorPrestamos;
    private final ControladorEstudiantes controladorEstudiantes;
    private final ControladorLibros controladorLibros;

    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JCheckBox checkDevueltos = new JCheckBox("Incluir devueltos");
    private final JButton botonNuevo = new JButton("Nuevo préstamo");
    private final JButton botonDevolver = new JButton("Registrar devolución");
    private final JButton botonSimular = new JButton("Simular préstamos simultáneos");

    /** Préstamos en el mismo orden que las filas de la tabla. */
    private List<PrestamoDetalle> detalles = List.of();

    public PantallaPrestamos(ControladorPrestamos controladorPrestamos,
                             ControladorEstudiantes controladorEstudiantes,
                             ControladorLibros controladorLibros) {
        this.controladorPrestamos = controladorPrestamos;
        this.controladorEstudiantes = controladorEstudiantes;
        this.controladorLibros = controladorLibros;
        agregarComponentes();
    }

    private void agregarComponentes() {
        checkDevueltos.addActionListener(e -> refrescar());
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, MARGEN, 0));
        filtros.add(checkDevueltos);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
        tabla.getColumnModel().getColumn(0).setMaxWidth(ANCHO_COLUMNA_ID);

        botonNuevo.addActionListener(e -> nuevo());
        botonDevolver.addActionListener(e -> devolver());
        botonSimular.addActionListener(e -> simular());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, MARGEN, 0));
        botones.add(botonNuevo);
        botones.add(botonDevolver);
        botones.add(botonSimular);

        add(filtros, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    @Override
    public void refrescar() {
        modelo.setRowCount(0);
        try {
            detalles = checkDevueltos.isSelected()
                    ? controladorPrestamos.verPrestamos()
                    : controladorPrestamos.verPrestamosActivos();
        } catch (PersistenciaException e) {
            detalles = List.of();
            Dialogos.errorBaseDatos(this, e);
        }
        LocalDate hoy = LocalDate.now();
        for (PrestamoDetalle detalle : detalles) {
            Prestamo prestamo = detalle.prestamo();
            modelo.addRow(new Object[]{
                    prestamo.getId(),
                    detalle.nombreEstudiante(),
                    detalle.rutEstudiante(),
                    detalle.tituloLibro(),
                    formatear(prestamo.getFechaPrestamo()),
                    formatear(prestamo.getFechaVencimiento()),
                    formatear(prestamo.getFechaDevolucionReal()),
                    estado(prestamo, hoy)
            });
        }
        actualizarBotones();
    }

    /** @return el estado en palabras, con los días de atraso si los hay. */
    private static String estado(Prestamo prestamo, LocalDate hoy) {
        long atraso = prestamo.diasAtraso(hoy);
        if (prestamo.estaDevuelto()) {
            return atraso > 0 ? "Devuelto con " + atraso + " día(s) de atraso" : "Devuelto a tiempo";
        }
        return atraso > 0 ? "Atrasado " + atraso + " día(s)" : "Prestado";
    }

    private static String formatear(LocalDate fecha) {
        return (fecha == null) ? SIN_DATO : fecha.format(FORMATO_FECHA);
    }

    /** @return el préstamo de la fila seleccionada, o {@code null} si no hay selección. */
    private PrestamoDetalle seleccionado() {
        int fila = tabla.getSelectedRow();
        return (fila >= 0 && fila < detalles.size()) ? detalles.get(fila) : null;
    }

    /** Solo se devuelve un préstamo que aún no está devuelto. */
    private void actualizarBotones() {
        PrestamoDetalle detalle = seleccionado();
        botonDevolver.setEnabled(detalle != null && !detalle.prestamo().estaDevuelto());
    }

    /** Carga estudiantes y libros vigentes y abre el diálogo de préstamo. */
    private void nuevo() {
        List<Estudiante> estudiantes;
        List<Libro> libros;
        try {
            estudiantes = controladorEstudiantes.verEstudiantes();
            libros = controladorLibros.buscarLibros(null, null);
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
            return;
        }
        new DialogoPrestamo(ventana(), controladorPrestamos, estudiantes, libros, this::refrescar).setVisible(true);
    }

    /** Carga estudiantes y libros vigentes y abre la simulación de préstamos simultáneos. */
    private void simular() {
        List<Estudiante> estudiantes;
        List<Libro> libros;
        try {
            estudiantes = controladorEstudiantes.verEstudiantes();
            libros = controladorLibros.buscarLibros(null, null);
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
            return;
        }
        new DialogoSimulacion(ventana(), controladorPrestamos, controladorLibros, estudiantes, libros,
                this::refrescar).setVisible(true);
    }

    /** Pide confirmación y registra la devolución con fecha de hoy, informando el atraso. */
    private void devolver() {
        PrestamoDetalle detalle = seleccionado();
        if (detalle == null) {
            return;
        }
        boolean confirmado = Dialogos.confirmar(this, "Registrar devolución",
                "¿Registrar hoy la devolución de " + detalle.tituloLibro() + " (" + detalle.nombreEstudiante() + ")?");
        if (!confirmado) {
            return;
        }
        try {
            Prestamo devuelto = controladorPrestamos.registrarDevolucion(detalle.prestamo());
            if (devuelto == null) {
                Dialogos.operacionNoPermitida(this, "El préstamo #" + detalle.prestamo().getId()
                        + " ya estaba devuelto o ya no existe.");
            } else {
                long atraso = devuelto.diasAtraso(LocalDate.now());
                Dialogos.exito(this, detalle.tituloLibro() + " devuelto "
                        + (atraso > 0 ? "con " + atraso + " día(s) de atraso." : "a tiempo."));
            }
        } catch (IllegalStateException e) {
            Dialogos.operacionNoPermitida(this, e.getMessage());
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
        }
        refrescar();
    }

    private Window ventana() {
        return SwingUtilities.getWindowAncestor(this);
    }
}