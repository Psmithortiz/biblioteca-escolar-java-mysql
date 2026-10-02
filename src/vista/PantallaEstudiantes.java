package vista;

import controlador.ControladorEstudiantes;
import dao.PersistenciaException;
import modelo.Estudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Gestión de estudiantes: tabla y botones para crear, editar y eliminar. */
public class PantallaEstudiantes extends Pantalla {

    private static final String[] COLUMNAS = {"ID", "Nombre", "RUT", "Curso", "Correo"};
    private static final String SIN_DATO = "—";
    private static final int ANCHO_COLUMNA_ID = 50;

    private final ControladorEstudiantes controlador;

    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JButton botonNuevo = new JButton("Nuevo");
    private final JButton botonEditar = new JButton("Editar");
    private final JButton botonEliminar = new JButton("Eliminar");

    /** Estudiantes en el mismo orden que las filas de la tabla. */
    private List<Estudiante> estudiantes = List.of();

    public PantallaEstudiantes(ControladorEstudiantes controlador) {
        this.controlador = controlador;
        agregarComponentes();
    }

    private void agregarComponentes() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
        tabla.getColumnModel().getColumn(0).setMaxWidth(ANCHO_COLUMNA_ID);

        botonNuevo.addActionListener(e -> nuevo());
        botonEditar.addActionListener(e -> editar());
        botonEliminar.addActionListener(e -> eliminar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, MARGEN, 0));
        botones.add(botonNuevo);
        botones.add(botonEditar);
        botones.add(botonEliminar);

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    @Override
    public void refrescar() {
        modelo.setRowCount(0);
        try {
            estudiantes = controlador.verEstudiantes();
        } catch (PersistenciaException e) {
            estudiantes = List.of();
            Dialogos.errorBaseDatos(this, e);
        }
        for (Estudiante estudiante : estudiantes) {
            modelo.addRow(new Object[]{
                    estudiante.getId(),
                    estudiante.getNombre(),
                    estudiante.getRut(),
                    estudiante.getCurso() == null ? SIN_DATO : estudiante.getCurso(),
                    estudiante.getCorreo()
            });
        }
        actualizarBotones();
    }

    /** @return el estudiante de la fila seleccionada, o {@code null} si no hay selección. */
    private Estudiante seleccionado() {
        int fila = tabla.getSelectedRow();
        return (fila >= 0 && fila < estudiantes.size()) ? estudiantes.get(fila) : null;
    }

    private void actualizarBotones() {
        boolean haySeleccion = seleccionado() != null;
        botonEditar.setEnabled(haySeleccion);
        botonEliminar.setEnabled(haySeleccion);
    }

    private void nuevo() {
        DialogoEstudiante.paraCrear(ventana(), controlador, this::refrescar).setVisible(true);
    }

    private void editar() {
        Estudiante estudiante = seleccionado();
        if (estudiante != null) {
            DialogoEstudiante.paraEditar(ventana(), controlador, estudiante, this::refrescar).setVisible(true);
        }
    }

    /** Pide confirmación y elimina; si el estudiante tiene préstamos, la BD lo impide. */
    private void eliminar() {
        Estudiante estudiante = seleccionado();
        if (estudiante == null) {
            return;
        }
        boolean confirmado = Dialogos.confirmar(this, "Eliminar estudiante",
                "¿Eliminar a " + estudiante.getNombre() + " (" + estudiante.getRut() + ")?");
        if (!confirmado) {
            return;
        }
        try {
            if (controlador.eliminarEstudiante(estudiante.getId())) {
                Dialogos.exito(this, "Estudiante " + estudiante.getNombre() + " eliminado.");
            } else {
                Dialogos.operacionNoPermitida(this, "El estudiante #" + estudiante.getId() + " ya no existe.");
            }
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
        }
        refrescar();
    }

    private Window ventana() {
        return SwingUtilities.getWindowAncestor(this);
    }
}