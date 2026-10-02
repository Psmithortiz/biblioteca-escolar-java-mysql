package vista;

import controlador.ControladorCategorias;
import dao.PersistenciaException;
import modelo.Categoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Gestión de categorías: tabla y botones para crear, editar y eliminar. La lista se guarda
 * en el mismo orden que las filas, así la fila seleccionada se traduce directo al objeto.
 */
public class PantallaCategorias extends Pantalla {

    private static final String[] COLUMNAS = {"ID", "Nombre"};
    private static final int ANCHO_COLUMNA_ID = 60;

    private final ControladorCategorias controlador;

    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JButton botonNuevo = new JButton("Nueva");
    private final JButton botonEditar = new JButton("Editar");
    private final JButton botonEliminar = new JButton("Eliminar");

    /** Categorías en el mismo orden que las filas de la tabla. */
    private List<Categoria> categorias = List.of();

    public PantallaCategorias(ControladorCategorias controlador) {
        this.controlador = controlador;
        agregarComponentes();
    }

    private void agregarComponentes() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
        tabla.getColumnModel().getColumn(0).setMaxWidth(ANCHO_COLUMNA_ID);

        botonNuevo.addActionListener(e -> nueva());
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
            categorias = controlador.verCategorias();
        } catch (PersistenciaException e) {
            categorias = List.of();
            Dialogos.errorBaseDatos(this, e);
        }
        for (Categoria categoria : categorias) {
            modelo.addRow(new Object[]{categoria.getId(), categoria.getNombre()});
        }
        actualizarBotones();
    }

    /** @return la categoría de la fila seleccionada, o {@code null} si no hay selección. */
    private Categoria seleccionada() {
        int fila = tabla.getSelectedRow();
        return (fila >= 0 && fila < categorias.size()) ? categorias.get(fila) : null;
    }

    private void actualizarBotones() {
        boolean haySeleccion = seleccionada() != null;
        botonEditar.setEnabled(haySeleccion);
        botonEliminar.setEnabled(haySeleccion);
    }

    private void nueva() {
        DialogoCategoria.paraCrear(ventana(), controlador, this::refrescar).setVisible(true);
    }

    private void editar() {
        Categoria categoria = seleccionada();
        if (categoria != null) {
            DialogoCategoria.paraEditar(ventana(), controlador, categoria, this::refrescar).setVisible(true);
        }
    }

    /** Pide confirmación y elimina; si la categoría tiene libros, la BD lo impide. */
    private void eliminar() {
        Categoria categoria = seleccionada();
        if (categoria == null) {
            return;
        }
        boolean confirmado = Dialogos.confirmar(this, "Eliminar categoría",
                "¿Eliminar la categoría " + categoria.getNombre() + " (#" + categoria.getId() + ")?");
        if (!confirmado) {
            return;
        }
        try {
            if (controlador.eliminarCategoria(categoria.getId())) {
                Dialogos.exito(this, "Categoría " + categoria.getNombre() + " eliminada.");
            } else {
                Dialogos.operacionNoPermitida(this, "La categoría #" + categoria.getId() + " ya no existe.");
            }
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
        }
        refrescar();
    }

    /** @return la ventana que contiene esta pantalla, propietaria de los diálogos que abre. */
    private Window ventana() {
        return SwingUtilities.getWindowAncestor(this);
    }
}