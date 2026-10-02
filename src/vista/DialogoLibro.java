package vista;

import controlador.ControladorLibros;
import dao.PersistenciaException;
import modelo.Categoria;
import modelo.Libro;
import utils.Validador;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Diálogo modal para crear o editar un libro, según el método de fábrica usado. El stock
 * se ingresa solo al crear: al editar se muestra bloqueado, porque se cambia con "Ajustar stock".
 */
public class DialogoLibro extends JDialog {

    private static final int ANCHO = 420;
    private static final int ALTO = 300;
    private static final int MARGEN = 10;

    private final ControladorLibros controlador;
    /** Libro que se edita; {@code null} en modo crear. */
    private final Libro aEditar;
    private final LibroListener listener;

    private final JTextField campoTitulo = new JTextField();
    private final JTextField campoAutor = new JTextField();
    private final JTextField campoIsbn = new JTextField();
    private final JTextField campoEditorial = new JTextField();
    private final JComboBox<Categoria> comboCategoria = new JComboBox<>();
    private final JTextField campoStock = new JTextField();

    private DialogoLibro(Window propietario, String titulo, ControladorLibros controlador,
                         List<Categoria> categorias, Libro aEditar, LibroListener listener) {
        super(propietario, titulo, ModalityType.APPLICATION_MODAL);
        this.controlador = controlador;
        this.aEditar = aEditar;
        this.listener = listener;
        categorias.forEach(comboCategoria::addItem);
        configurarVentana();
        agregarComponentes();
        cargarDatos();
    }

    /**
     * @param categorias opciones del combo; el libro debe pertenecer a una de ellas.
     * @return un diálogo para registrar un libro nuevo.
     */
    public static DialogoLibro paraCrear(Window propietario, ControladorLibros controlador,
                                         List<Categoria> categorias, LibroListener listener) {
        return new DialogoLibro(propietario, "Nuevo libro", controlador, categorias, null, listener);
    }

    /** @return un diálogo con el libro cargado para editarlo. */
    public static DialogoLibro paraEditar(Window propietario, ControladorLibros controlador,
                                          List<Categoria> categorias, Libro libro, LibroListener listener) {
        Validador.objetoNoNulo(libro, "libro");
        return new DialogoLibro(propietario, "Editar libro #" + libro.getId(), controlador, categorias, libro, listener);
    }

    private void configurarVentana() {
        setSize(ANCHO, ALTO);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(MARGEN, MARGEN));
    }

    private void agregarComponentes() {
        comboCategoria.setRenderer(Renderizadores.conTexto(Categoria::getNombre));

        JPanel formulario = new JPanel(new GridLayout(0, 2, MARGEN, MARGEN));
        formulario.setBorder(BorderFactory.createEmptyBorder(MARGEN, MARGEN, MARGEN, MARGEN));
        formulario.add(new JLabel("Título:"));
        formulario.add(campoTitulo);
        formulario.add(new JLabel("Autor:"));
        formulario.add(campoAutor);
        formulario.add(new JLabel("ISBN:"));
        formulario.add(campoIsbn);
        formulario.add(new JLabel("Editorial:"));
        formulario.add(campoEditorial);
        formulario.add(new JLabel("Categoría:"));
        formulario.add(comboCategoria);
        formulario.add(new JLabel("Stock:"));
        formulario.add(campoStock);

        JButton botonGuardar = new JButton("Guardar");
        botonGuardar.addActionListener(e -> guardar());
        getRootPane().setDefaultButton(botonGuardar);

        add(formulario, BorderLayout.CENTER);
        add(botonGuardar, BorderLayout.SOUTH);
    }

    /** En modo editar carga el libro y bloquea el stock. */
    private void cargarDatos() {
        if (aEditar == null) {
            return;
        }
        campoTitulo.setText(aEditar.getTitulo());
        campoAutor.setText(aEditar.getAutor());
        campoIsbn.setText(aEditar.getIsbn());
        campoEditorial.setText(aEditar.getEditorial());
        for (int i = 0; i < comboCategoria.getItemCount(); i++) {
            if (comboCategoria.getItemAt(i).getId() == aEditar.getIdCategoria()) {
                comboCategoria.setSelectedIndex(i);
            }
        }
        campoStock.setText(String.valueOf(aEditar.getStock()));
        campoStock.setEnabled(false);
        campoStock.setToolTipText("El stock no se edita aquí: usa \"Ajustar stock\".");
    }

    /** Valida presencia y formato; las demás reglas las valida el modelo y la BD. */
    private void guardar() {
        String titulo = campoTitulo.getText().trim();
        String autor = campoAutor.getText().trim();
        String isbn = campoIsbn.getText().trim();
        String editorial = campoEditorial.getText().trim();
        Categoria categoria = comboCategoria.getItemAt(comboCategoria.getSelectedIndex());
        if (titulo.isEmpty() || autor.isEmpty() || isbn.isEmpty() || editorial.isEmpty()) {
            Dialogos.errorValidacion(this, "Completa título, autor, ISBN y editorial.");
            return;
        }
        if (categoria == null) {
            Dialogos.errorValidacion(this, "Elige una categoría (si no hay, créala primero).");
            return;
        }
        try {
            if (aEditar == null) {
                crear(titulo, autor, isbn, editorial, categoria);
            } else {
                actualizar(titulo, autor, isbn, editorial, categoria);
            }
        } catch (IllegalArgumentException e) {
            Dialogos.errorValidacion(this, e.getMessage());
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
        }
    }

    private void crear(String titulo, String autor, String isbn, String editorial, Categoria categoria) {
        int stock;
        try {
            stock = Integer.parseInt(campoStock.getText().trim());
        } catch (NumberFormatException e) {
            Dialogos.errorValidacion(this, "El stock debe ser un número entero (por ejemplo, 3).");
            campoStock.requestFocusInWindow();
            return;
        }
        Libro nuevo = controlador.registrarLibro(titulo, autor, isbn, editorial, stock, categoria.getId());
        listener.onLibrosCambiados();
        Dialogos.exito(this, "Libro " + nuevo.getTitulo() + " #" + nuevo.getId() + " registrado.");
        dispose();
    }

    private void actualizar(String titulo, String autor, String isbn, String editorial, Categoria categoria) {
        boolean actualizado = controlador.actualizarLibro(aEditar, titulo, autor, isbn, editorial, categoria.getId());
        listener.onLibrosCambiados(); // también si ya no existía: la tabla debe reflejarlo
        if (actualizado) {
            Dialogos.exito(this, "Libro #" + aEditar.getId() + " actualizado.");
        } else {
            Dialogos.operacionNoPermitida(this, "El libro #" + aEditar.getId() + " ya no existe.");
        }
        dispose();
    }
}