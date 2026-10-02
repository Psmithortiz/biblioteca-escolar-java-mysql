package vista;

import controlador.ControladorSesion;
import dao.PersistenciaException;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;

/** Diálogo modal de inicio de sesión con RUT o correo y contraseña. */
public class DialogoLogin extends JDialog {

    private static final String TITULO = "Biblioteca Escolar - Iniciar sesión";
    private static final int ANCHO = 400;
    private static final int ALTO = 160;
    private static final int MARGEN = 10;

    private final ControladorSesion controlador;

    private final JTextField campoIdentificador = new JTextField();
    private final JPasswordField campoContrasena = new JPasswordField();

    /** {@code null} hasta que el usuario inicie sesión con éxito. */
    private Usuario usuario;

    private DialogoLogin(Window propietario, ControladorSesion controlador) {
        super(propietario, TITULO, ModalityType.APPLICATION_MODAL);
        this.controlador = controlador;
        configurarVentana();
        agregarComponentes();
    }

    /**
     * Muestra el login y espera a que se cierre (es modal).
     *
     * @param propietario ventana sobre la que se abre; {@code null} si aún no hay ninguna.
     * @return el usuario autenticado, o {@code null} si se cerró sin iniciar sesión.
     */
    public static Usuario mostrar(Window propietario, ControladorSesion controlador) {
        DialogoLogin dialogo = new DialogoLogin(propietario, controlador);
        dialogo.setVisible(true); // bloquea aquí hasta que el diálogo se cierre
        return dialogo.usuario;
    }

    private void configurarVentana() {
        setSize(ANCHO, ALTO);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getOwner()); // sin propietario: centrado en la pantalla
        setResizable(false);
        setLayout(new BorderLayout(MARGEN, MARGEN));
    }

    private void agregarComponentes() {
        JPanel formulario = new JPanel(new GridLayout(0, 2, MARGEN, MARGEN));
        formulario.setBorder(BorderFactory.createEmptyBorder(MARGEN, MARGEN, 0, MARGEN));
        formulario.add(new JLabel("RUT o correo:"));
        formulario.add(campoIdentificador);
        formulario.add(new JLabel("Contraseña:"));
        formulario.add(campoContrasena);

        JButton botonIngresar = new JButton("Ingresar");
        botonIngresar.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(botonIngresar); // Enter ingresa

        add(formulario, BorderLayout.CENTER);
        add(botonIngresar, BorderLayout.SOUTH);
    }

    /** Valida la presencia de ambos campos e intenta iniciar sesión; si falla, el diálogo sigue abierto. */
    private void ingresar() {
        String identificador = campoIdentificador.getText().trim();
        String contrasena = new String(campoContrasena.getPassword());
        if (identificador.isEmpty()) {
            Dialogos.errorValidacion(this, "Ingresa tu RUT o tu correo.");
            campoIdentificador.requestFocusInWindow();
            return;
        }
        if (contrasena.isEmpty()) {
            Dialogos.errorValidacion(this, "Ingresa tu contraseña.");
            campoContrasena.requestFocusInWindow();
            return;
        }
        try {
            Usuario autenticado = controlador.iniciarSesion(identificador, contrasena);
            if (autenticado == null) {
                // Un solo mensaje para ambos casos: no revela si el RUT o correo existe.
                Dialogos.errorValidacion(this, "RUT/correo o contraseña incorrectos.");
                campoContrasena.setText("");
                campoContrasena.requestFocusInWindow();
                return;
            }
            usuario = autenticado;
            dispose();
        } catch (IllegalArgumentException e) {
            Dialogos.errorValidacion(this, e.getMessage());
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
        }
    }
}