package main;

import controlador.ControladorSesion;
import dao.impl.UsuarioDAOImpl;
import modelo.Usuario;
import vista.DialogoLogin;
import vista.VentanaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Punto de entrada: abre el login en el hilo de Swing y, tras iniciar sesión, la ventana principal. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::iniciar);
    }

    private static void iniciar() {
        ControladorSesion sesion;
        try {
            sesion = new ControladorSesion(new UsuarioDAOImpl());
        } catch (IllegalStateException e) {
            // Falta db.properties o una clave: sin configuración no hay app.
            JOptionPane.showMessageDialog(null, e.getMessage(), "Configuración incompleta", JOptionPane.ERROR_MESSAGE);
            return;
        }
        abrirSesion(sesion);
    }

    /** Muestra el login y, si alguien inicia sesión, la ventana principal; al cerrar sesión, vuelve aquí. */
    private static void abrirSesion(ControladorSesion sesion) {
        Usuario usuario = DialogoLogin.mostrar(null, sesion);
        if (usuario == null) {
            return; // cerró el login: al no quedar ventanas abiertas, la app termina
        }
        new VentanaPrincipal(usuario, () -> abrirSesion(sesion));
    }
}