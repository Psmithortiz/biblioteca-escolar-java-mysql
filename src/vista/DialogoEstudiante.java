package vista;

import controlador.ControladorEstudiantes;
import dao.PersistenciaException;
import modelo.Estudiante;
import utils.Validador;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para crear o editar un estudiante, según el método de fábrica usado.
 * Al editar, el RUT se muestra bloqueado: es el vínculo con el usuario del estudiante.
 */
public class DialogoEstudiante extends JDialog {

    private static final int ANCHO = 420;
    private static final int ALTO = 230;
    private static final int MARGEN = 10;

    private final ControladorEstudiantes controlador;
    /** Estudiante que se edita; {@code null} en modo crear. */
    private final Estudiante aEditar;
    private final EstudianteListener listener;

    private final JTextField campoNombre = new JTextField();
    private final JTextField campoRut = new JTextField();
    private final JTextField campoCurso = new JTextField();
    private final JTextField campoCorreo = new JTextField();

    private DialogoEstudiante(Window propietario, String titulo, ControladorEstudiantes controlador,
                              Estudiante aEditar, EstudianteListener listener) {
        super(propietario, titulo, ModalityType.APPLICATION_MODAL);
        this.controlador = controlador;
        this.aEditar = aEditar;
        this.listener = listener;
        configurarVentana();
        agregarComponentes();
        cargarDatos();
    }

    /** @return un diálogo para registrar un estudiante nuevo. */
    public static DialogoEstudiante paraCrear(Window propietario, ControladorEstudiantes controlador,
                                              EstudianteListener listener) {
        return new DialogoEstudiante(propietario, "Nuevo estudiante", controlador, null, listener);
    }

    /** @return un diálogo con el estudiante cargado para editarlo. */
    public static DialogoEstudiante paraEditar(Window propietario, ControladorEstudiantes controlador,
                                               Estudiante estudiante, EstudianteListener listener) {
        Validador.objetoNoNulo(estudiante, "estudiante");
        return new DialogoEstudiante(propietario, "Editar estudiante #" + estudiante.getId(),
                controlador, estudiante, listener);
    }

    private void configurarVentana() {
        setSize(ANCHO, ALTO);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(MARGEN, MARGEN));
    }

    private void agregarComponentes() {
        campoRut.setToolTipText("Sin puntos y con guion, por ejemplo 12345678-9");

        JPanel formulario = new JPanel(new GridLayout(0, 2, MARGEN, MARGEN));
        formulario.setBorder(BorderFactory.createEmptyBorder(MARGEN, MARGEN, MARGEN, MARGEN));
        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);
        formulario.add(new JLabel("RUT (12345678-9):"));
        formulario.add(campoRut);
        formulario.add(new JLabel("Curso (opcional):"));
        formulario.add(campoCurso);
        formulario.add(new JLabel("Correo:"));
        formulario.add(campoCorreo);

        JButton botonGuardar = new JButton("Guardar");
        botonGuardar.addActionListener(e -> guardar());
        getRootPane().setDefaultButton(botonGuardar);

        add(formulario, BorderLayout.CENTER);
        add(botonGuardar, BorderLayout.SOUTH);
    }

    /** En modo editar carga el estudiante y bloquea el RUT. */
    private void cargarDatos() {
        if (aEditar == null) {
            return;
        }
        campoNombre.setText(aEditar.getNombre());
        campoRut.setText(aEditar.getRut());
        campoRut.setEnabled(false);
        campoRut.setToolTipText("El RUT no se edita: vincula al estudiante con su usuario.");
        campoCurso.setText(aEditar.getCurso() == null ? "" : aEditar.getCurso());
        campoCorreo.setText(aEditar.getCorreo());
    }

    /** Valida presencia; formato y largos los valida el modelo, y el RUT repetido la BD. */
    private void guardar() {
        String nombre = campoNombre.getText().trim();
        String rut = campoRut.getText().trim();
        String curso = campoCurso.getText().trim();
        String correo = campoCorreo.getText().trim();
        if (nombre.isEmpty() || rut.isEmpty() || correo.isEmpty()) {
            Dialogos.errorValidacion(this, "Completa nombre, RUT y correo (el curso es opcional).");
            return;
        }
        try {
            if (aEditar == null) {
                Estudiante nuevo = controlador.registrarEstudiante(nombre, rut, curso, correo);
                listener.onEstudiantesCambiados();
                Dialogos.exito(this, "Estudiante " + nuevo.getNombre() + " #" + nuevo.getId() + " registrado.");
            } else {
                boolean actualizado = controlador.actualizarEstudiante(aEditar, nombre, curso, correo);
                listener.onEstudiantesCambiados(); // también si ya no existía: la tabla debe reflejarlo
                if (actualizado) {
                    Dialogos.exito(this, "Estudiante #" + aEditar.getId() + " actualizado.");
                } else {
                    Dialogos.operacionNoPermitida(this, "El estudiante #" + aEditar.getId() + " ya no existe.");
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