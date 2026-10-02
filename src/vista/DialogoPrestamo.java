package vista;

import controlador.ControladorPrestamos;
import dao.PersistenciaException;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Diálogo modal para registrar un préstamo. El registro corre en segundo plano con un
 * {@link SwingWorker}: la transacción no ocupa el hilo de Swing y la interfaz no se congela.
 */
public class DialogoPrestamo extends JDialog {

    private static final String TITULO = "Nuevo préstamo";
    private static final int ANCHO = 480;
    private static final int ALTO = 190;
    private static final int MARGEN = 10;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final ControladorPrestamos controlador;
    private final PrestamoListener listener;

    private final JComboBox<Estudiante> comboEstudiante = new JComboBox<>();
    private final JComboBox<Libro> comboLibro = new JComboBox<>();
    private final JButton botonPrestar = new JButton("Prestar");
    private final JLabel etiquetaEstado = new JLabel(" ");

    /**
     * @param estudiantes opciones de estudiante.
     * @param libros      opciones de libro; solo se ofrecen los que tienen stock.
     */
    public DialogoPrestamo(Window propietario, ControladorPrestamos controlador,
                           List<Estudiante> estudiantes, List<Libro> libros, PrestamoListener listener) {
        super(propietario, TITULO, ModalityType.APPLICATION_MODAL);
        this.controlador = controlador;
        this.listener = listener;
        estudiantes.forEach(comboEstudiante::addItem);
        libros.stream().filter(Libro::hayStock).forEach(comboLibro::addItem);
        configurarVentana();
        agregarComponentes();
    }

    private void configurarVentana() {
        setSize(ANCHO, ALTO);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(MARGEN, MARGEN));
    }

    private void agregarComponentes() {
        comboEstudiante.setRenderer(Renderizadores.conTexto((Estudiante e) -> e.getNombre() + " · " + e.getRut()));
        comboLibro.setRenderer(Renderizadores.conTexto((Libro l) -> l.getTitulo() + " (stock " + l.getStock() + ")"));

        JPanel formulario = new JPanel(new GridLayout(0, 2, MARGEN, MARGEN));
        formulario.setBorder(BorderFactory.createEmptyBorder(MARGEN, MARGEN, 0, MARGEN));
        formulario.add(new JLabel("Estudiante:"));
        formulario.add(comboEstudiante);
        formulario.add(new JLabel("Libro:"));
        formulario.add(comboLibro);
        formulario.add(new JLabel("Vence en:"));
        formulario.add(new JLabel(Prestamo.PLAZO_DIAS + " días"));

        botonPrestar.addActionListener(e -> prestar());
        botonPrestar.setEnabled(comboEstudiante.getItemCount() > 0 && comboLibro.getItemCount() > 0);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(etiquetaEstado, BorderLayout.CENTER);
        sur.add(botonPrestar, BorderLayout.SOUTH);
        etiquetaEstado.setBorder(BorderFactory.createEmptyBorder(0, MARGEN, 0, MARGEN));

        add(formulario, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
    }

    /** Registra el préstamo en segundo plano; mientras tanto, bloquea el formulario. */
    private void prestar() {
        Estudiante estudiante = comboEstudiante.getItemAt(comboEstudiante.getSelectedIndex());
        Libro libro = comboLibro.getItemAt(comboLibro.getSelectedIndex());
        if (estudiante == null || libro == null) {
            return;
        }
        ocupado(true);
        new SwingWorker<Prestamo, Void>() {
            @Override
            protected Prestamo doInBackground() {
                // Otro hilo: aquí no se toca la interfaz.
                return controlador.registrarPrestamo(estudiante.getId(), libro.getId());
            }

            @Override
            protected void done() {
                // De vuelta en el hilo de Swing.
                ocupado(false);
                mostrarResultado(this, estudiante, libro);
            }
        }.execute();
    }

    /** Lee el resultado del worker y lo informa; si salió bien, avisa y cierra. */
    private void mostrarResultado(SwingWorker<Prestamo, Void> worker, Estudiante estudiante, Libro libro) {
        try {
            Prestamo prestamo = worker.get();
            if (prestamo == null) {
                // Sin stock: otro préstamo pudo llevarse el último ejemplar después de abrir este diálogo.
                listener.onPrestamosCambiados();
                Dialogos.operacionNoPermitida(this, "No quedan ejemplares disponibles de " + libro.getTitulo() + ".");
                return;
            }
            listener.onPrestamosCambiados();
            Dialogos.exito(this, libro.getTitulo() + " prestado a " + estudiante.getNombre()
                    + " (préstamo #" + prestamo.getId() + "). Vence el "
                    + prestamo.getFechaVencimiento().format(FORMATO_FECHA) + ".");
            dispose();
        } catch (ExecutionException e) {
            Throwable causa = e.getCause(); // la excepción que lanzó doInBackground
            if (causa instanceof PersistenciaException persistencia) {
                Dialogos.errorBaseDatos(this, persistencia);
            } else {
                Dialogos.errorValidacion(this, causa.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Bloquea o libera el formulario mientras el préstamo se registra. */
    private void ocupado(boolean ocupado) {
        botonPrestar.setEnabled(!ocupado);
        comboEstudiante.setEnabled(!ocupado);
        comboLibro.setEnabled(!ocupado);
        etiquetaEstado.setText(ocupado ? "Registrando préstamo..." : " ");
        setCursor(ocupado ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        // Mientras trabaja, la X no cierra el diálogo: el resultado debe llegar a alguien.
        setDefaultCloseOperation(ocupado ? DO_NOTHING_ON_CLOSE : DISPOSE_ON_CLOSE);
    }
}