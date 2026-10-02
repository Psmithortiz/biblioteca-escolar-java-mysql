package vista;

import controlador.ControladorCategorias;
import dao.PersistenciaException;
import modelo.Categoria;
import utils.Validador;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para crear o editar una categoría, según el método de fábrica usado.
 * La vista valida la presencia del nombre; el modelo, el largo; la BD, que no se repita.
 */
public class DialogoCategoria extends JDialog {

    private static final int ANCHO = 340;
    private static final int ALTO = 130;
    private static final int MARGEN = 10;

    private final ControladorCategorias controlador;
    /** Categoría que se edita; {@code null} en modo crear. */
    private final Categoria aEditar;
    private final CategoriaListener listener;

    private final JTextField campoNombre = new JTextField();

    private DialogoCategoria(Window propietario, String titulo, ControladorCategorias controlador,
                             Categoria aEditar, CategoriaListener listener) {
        super(propietario, titulo, ModalityType.APPLICATION_MODAL);
        this.controlador = controlador;
        this.aEditar = aEditar;
        this.listener = listener;
        configurarVentana();
        agregarComponentes();
        if (aEditar != null) {
            campoNombre.setText(aEditar.getNombre());
        }
    }

    /** @return un diálogo para registrar una categoría nueva. */
    public static DialogoCategoria paraCrear(Window propietario, ControladorCategorias controlador,
                                             CategoriaListener listener) {
        return new DialogoCategoria(propietario, "Nueva categoría", controlador, null, listener);
    }

    /** @return un diálogo con la categoría cargada para editarla. */
    public static DialogoCategoria paraEditar(Window propietario, ControladorCategorias controlador,
                                              Categoria categoria, CategoriaListener listener) {
        Validador.objetoNoNulo(categoria, "categoría");
        return new DialogoCategoria(propietario, "Editar categoría #" + categoria.getId(),
                controlador, categoria, listener);
    }

    private void configurarVentana() {
        setSize(ANCHO, ALTO);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(MARGEN, MARGEN));
    }

    private void agregarComponentes() {
        JPanel formulario = new JPanel(new GridLayout(0, 2, MARGEN, MARGEN));
        formulario.setBorder(BorderFactory.createEmptyBorder(MARGEN, MARGEN, MARGEN, MARGEN));
        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        JButton botonGuardar = new JButton("Guardar");
        botonGuardar.addActionListener(e -> guardar());
        getRootPane().setDefaultButton(botonGuardar); // Enter guarda

        add(formulario, BorderLayout.CENTER);
        add(botonGuardar, BorderLayout.SOUTH);
    }

    /** Valida y guarda según el modo; si falla, muestra el error y el diálogo sigue abierto. */
    private void guardar() {
        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            Dialogos.errorValidacion(this, "Ingresa el nombre de la categoría.");
            campoNombre.requestFocusInWindow();
            return;
        }
        try {
            if (aEditar == null) {
                Categoria nueva = controlador.registrarCategoria(nombre);
                listener.onCategoriasCambiadas();
                Dialogos.exito(this, "Categoría " + nueva.getNombre() + " registrada.");
            } else {
                boolean actualizada = controlador.actualizarCategoria(aEditar, nombre);
                listener.onCategoriasCambiadas(); // también si ya no existía: la tabla debe reflejarlo
                if (actualizada) {
                    Dialogos.exito(this, "Categoría #" + aEditar.getId() + " actualizada.");
                } else {
                    Dialogos.operacionNoPermitida(this, "La categoría #" + aEditar.getId() + " ya no existe.");
                }
            }
            dispose();
        } catch (IllegalArgumentException e) {
            Dialogos.errorValidacion(this, e.getMessage());
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
        }
    }
}