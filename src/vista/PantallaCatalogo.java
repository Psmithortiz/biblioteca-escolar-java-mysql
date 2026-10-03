package vista;

import controlador.ControladorCategorias;
import controlador.ControladorLibros;
import dao.PersistenciaException;
import modelo.Categoria;
import modelo.Libro;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Catálogo para estudiantes: búsqueda de libros por texto y categoría, con su disponibilidad. Solo lectura. */
public class PantallaCatalogo extends Pantalla {

    private static final String[] COLUMNAS = {"Título", "Autor", "Editorial", "Categoría", "Disponibles"};
    private static final String TEXTO_TODAS = "Todas";
    private static final int COLUMNAS_CAMPO_TEXTO = 15;
    private static final int ANCHO_COLUMNA_DISPONIBLES = 90;

    private final ControladorLibros controladorLibros;
    private final ControladorCategorias controladorCategorias;

    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTextField campoTexto = new JTextField(COLUMNAS_CAMPO_TEXTO);
    /** La opción {@code null} ("Todas") significa sin filtro. */
    private final JComboBox<Categoria> comboCategoria = new JComboBox<>();
    private final Map<Integer, String> nombresCategoria = new HashMap<>();

    /** Filtros aplicados con el último "Buscar"; {@code null} = sin filtro. */
    private String filtroTexto;
    private Integer filtroCategoria;

    public PantallaCatalogo(ControladorLibros controladorLibros, ControladorCategorias controladorCategorias) {
        this.controladorLibros = controladorLibros;
        this.controladorCategorias = controladorCategorias;
        add(crearPanelFiltros(), BorderLayout.NORTH);
        JTable tabla = new JTable(modelo);
        tabla.getColumnModel().getColumn(4).setMaxWidth(ANCHO_COLUMNA_DISPONIBLES);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    private JPanel crearPanelFiltros() {
        comboCategoria.setRenderer(Renderizadores.conTexto(Categoria::getNombre, TEXTO_TODAS));
        JButton botonBuscar = new JButton("Buscar");
        JButton botonLimpiar = new JButton("Limpiar");
        botonBuscar.addActionListener(e -> buscar());
        campoTexto.addActionListener(e -> buscar());
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

    @Override
    public void refrescar() {
        recargarCategorias();
        cargarLibros();
    }

    private void recargarCategorias() {
        List<Categoria> categorias;
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
            for (Libro libro : controladorLibros.buscarLibros(filtroTexto, filtroCategoria)) {
                modelo.addRow(new Object[]{
                        libro.getTitulo(),
                        libro.getAutor(),
                        libro.getEditorial(),
                        nombresCategoria.getOrDefault(libro.getIdCategoria(), Formatos.SIN_DATO),
                        libro.hayStock() ? libro.getStock() : "Agotado"
                });
            }
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
        }
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
}