package vista;

import controlador.ControladorCategorias;
import controlador.ControladorLibros;
import dao.PersistenciaException;
import modelo.Categoria;
import modelo.Libro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestión de libros: filtros por texto y categoría, tabla con el nombre de la categoría,
 * y botones para crear, editar, ajustar stock y eliminar. Los filtros se aplican con
 * "Buscar" y se conservan al refrescar.
 */
public class PantallaLibros extends Pantalla {

    private static final String[] COLUMNAS = {"ID", "Título", "Autor", "ISBN", "Editorial", "Categoría", "Stock"};
    private static final String TEXTO_TODAS = "Todas";
    private static final int ANCHO_COLUMNA_ID = 50;
    private static final int ANCHO_COLUMNA_STOCK = 60;
    private static final int COLUMNAS_CAMPO_TEXTO = 15;
    private static final int MAXIMO_AJUSTE = 1000;

    private final ControladorLibros controladorLibros;
    private final ControladorCategorias controladorCategorias;

    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    private final JTextField campoTexto = new JTextField(COLUMNAS_CAMPO_TEXTO);
    /** La opción {@code null} ("Todas") significa sin filtro. */
    private final JComboBox<Categoria> comboCategoria = new JComboBox<>();

    private final JButton botonNuevo = new JButton("Nuevo");
    private final JButton botonEditar = new JButton("Editar");
    private final JButton botonStock = new JButton("Ajustar stock");
    private final JButton botonEliminar = new JButton("Eliminar");

    /** Libros en el mismo orden que las filas de la tabla. */
    private List<Libro> libros = List.of();
    /** Categorías vigentes: opciones del formulario y fuente de los nombres de la tabla. */
    private List<Categoria> categorias = List.of();
    private final Map<Integer, String> nombresCategoria = new HashMap<>();

    /** Filtros aplicados con el último "Buscar"; {@code null} = sin filtro. */
    private String filtroTexto;
    private Integer filtroCategoria;

    public PantallaLibros(ControladorLibros controladorLibros, ControladorCategorias controladorCategorias) {
        this.controladorLibros = controladorLibros;
        this.controladorCategorias = controladorCategorias;
        add(crearPanelFiltros(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);
        add(crearPanelBotones(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelFiltros() {
        comboCategoria.setRenderer(Renderizadores.conTexto(Categoria::getNombre, TEXTO_TODAS));
        JButton botonBuscar = new JButton("Buscar");
        JButton botonLimpiar = new JButton("Limpiar");
        botonBuscar.addActionListener(e -> buscar());
        campoTexto.addActionListener(e -> buscar()); // Enter también busca
        botonLimpiar.addActionListener(e -> limpiar());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, MARGEN, 0));
        panel.add(new JLabel("Título o autor:"));
        panel.add(campoTexto);
        panel.add(new JLabel("Categoría:"));
        panel.add(comboCategoria);
        panel.add(botonBuscar);
        panel.add(botonLimpiar);
        return panel;
    }

    private JScrollPane crearTabla() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
        tabla.getColumnModel().getColumn(0).setMaxWidth(ANCHO_COLUMNA_ID);
        tabla.getColumnModel().getColumn(6).setMaxWidth(ANCHO_COLUMNA_STOCK);
        return new JScrollPane(tabla);
    }

    private JPanel crearPanelBotones() {
        botonNuevo.addActionListener(e -> nuevo());
        botonEditar.addActionListener(e -> editar());
        botonStock.addActionListener(e -> ajustarStock());
        botonEliminar.addActionListener(e -> eliminar());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, MARGEN, 0));
        panel.add(botonNuevo);
        panel.add(botonEditar);
        panel.add(botonStock);
        panel.add(botonEliminar);
        return panel;
    }

    /** Recarga las categorías (combo y nombres) y los libros, conservando los filtros aplicados. */
    @Override
    public void refrescar() {
        recargarCategorias();
        cargarLibros();
    }

    private void recargarCategorias() {
        try {
            categorias = controladorCategorias.verCategorias();
        } catch (PersistenciaException e) {
            categorias = List.of();
            Dialogos.errorBaseDatos(this, e);
        }
        nombresCategoria.clear();
        comboCategoria.removeAllItems();
        comboCategoria.addItem(null);
        Categoria aSeleccionar = null;
        for (Categoria categoria : categorias) {
            nombresCategoria.put(categoria.getId(), categoria.getNombre());
            comboCategoria.addItem(categoria);
            if (categoria.getId().equals(filtroCategoria)) {
                aSeleccionar = categoria;
            }
        }
        comboCategoria.setSelectedItem(aSeleccionar);
    }

    private void cargarLibros() {
        modelo.setRowCount(0);
        try {
            libros = controladorLibros.buscarLibros(filtroTexto, filtroCategoria);
        } catch (PersistenciaException e) {
            libros = List.of();
            Dialogos.errorBaseDatos(this, e);
        }
        for (Libro libro : libros) {
            modelo.addRow(new Object[]{
                    libro.getId(),
                    libro.getTitulo(),
                    libro.getAutor(),
                    libro.getIsbn(),
                    libro.getEditorial(),
                    nombresCategoria.getOrDefault(libro.getIdCategoria(), "—"),
                    libro.getStock()
            });
        }
        actualizarBotones();
    }

    private void buscar() {
        String texto = campoTexto.getText().trim();
        Categoria categoria = comboCategoria.getItemAt(comboCategoria.getSelectedIndex());
        filtroTexto = texto.isEmpty() ? null : texto;
        filtroCategoria = (categoria == null) ? null : categoria.getId();
        cargarLibros();
    }

    private void limpiar() {
        campoTexto.setText("");
        comboCategoria.setSelectedIndex(0);
        filtroTexto = null;
        filtroCategoria = null;
        cargarLibros();
    }

    /** @return el libro de la fila seleccionada, o {@code null} si no hay selección. */
    private Libro seleccionado() {
        int fila = tabla.getSelectedRow();
        return (fila >= 0 && fila < libros.size()) ? libros.get(fila) : null;
    }

    private void actualizarBotones() {
        boolean haySeleccion = seleccionado() != null;
        botonEditar.setEnabled(haySeleccion);
        botonStock.setEnabled(haySeleccion);
        botonEliminar.setEnabled(haySeleccion);
    }

    private void nuevo() {
        DialogoLibro.paraCrear(ventana(), controladorLibros, categorias, this::refrescar).setVisible(true);
    }

    private void editar() {
        Libro libro = seleccionado();
        if (libro != null) {
            DialogoLibro.paraEditar(ventana(), controladorLibros, categorias, libro, this::refrescar).setVisible(true);
        }
    }

    /**
     * Pide la cantidad a sumar (positiva) o restar (negativa). El mínimo del selector impide
     * pedir más de lo que hay; la BD lo revisa de nuevo sobre el valor actual.
     */
    private void ajustarStock() {
        Libro libro = seleccionado();
        if (libro == null) {
            return;
        }
        JSpinner selector = new JSpinner(new SpinnerNumberModel(0, -libro.getStock(), MAXIMO_AJUSTE, 1));
        boolean aceptado = Dialogos.elegir(this, "Ajustar stock",
                libro.getTitulo() + " (stock actual: " + libro.getStock()
                        + "). Ejemplares a sumar (o restar, con negativo):", selector);
        try {
            selector.commitEdit(); // toma lo escrito a mano aunque no se haya presionado Enter
        } catch (ParseException e) {
            // texto inválido: el selector conserva su último valor válido
        }
        int cantidad = (Integer) selector.getValue();
        if (!aceptado || cantidad == 0) {
            return;
        }
        try {
            if (controladorLibros.ajustarStock(libro, cantidad)) {
                Dialogos.exito(this, "Stock de " + libro.getTitulo() + " ajustado en " + cantidad + ".");
            } else {
                Dialogos.operacionNoPermitida(this, "No se pudo ajustar: el stock quedaría negativo "
                        + "(pudo cambiar por un préstamo) o el libro ya no existe.");
            }
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
        }
        refrescar();
    }

    /** Pide confirmación y elimina; si el libro tiene préstamos, la BD lo impide. */
    private void eliminar() {
        Libro libro = seleccionado();
        if (libro == null) {
            return;
        }
        boolean confirmado = Dialogos.confirmar(this, "Eliminar libro",
                "¿Eliminar " + libro.getTitulo() + " (#" + libro.getId() + ")?");
        if (!confirmado) {
            return;
        }
        try {
            if (controladorLibros.eliminarLibro(libro.getId())) {
                Dialogos.exito(this, "Libro " + libro.getTitulo() + " eliminado.");
            } else {
                Dialogos.operacionNoPermitida(this, "El libro #" + libro.getId() + " ya no existe.");
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