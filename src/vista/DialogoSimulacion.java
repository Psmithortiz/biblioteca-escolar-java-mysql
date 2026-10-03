package vista;

import controlador.ControladorLibros;
import controlador.ControladorPrestamos;
import dao.PersistenciaException;
import modelo.Estudiante;
import modelo.Libro;
import modelo.ResultadoSimulacion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Window;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Demostración de concurrencia: lanza varias solicitudes de préstamo del mismo libro al mismo
 * tiempo y muestra el stock antes y después, leído desde la BD. La simulación corre en un
 * {@link SwingWorker}, así la ventana sigue respondiendo mientras los hilos compiten.
 */
public class DialogoSimulacion extends JDialog {

    private static final String TITULO = "Simular préstamos simultáneos";
    private static final int ANCHO = 620;
    private static final int ALTO = 460;
    private static final int MARGEN = 10;
    private static final int SOLICITUDES_POR_DEFECTO = 10;
    private static final int SOLICITUDES_MINIMO = 2;
    private static final int SOLICITUDES_MAXIMO = 50;

    private final ControladorPrestamos controladorPrestamos;
    private final ControladorLibros controladorLibros;
    private final List<Integer> idsEstudiantes;
    private final PrestamoListener listener;

    private final JComboBox<Libro> comboLibro = new JComboBox<>();
    private final JSpinner selectorSolicitudes = new JSpinner(new SpinnerNumberModel(
            SOLICITUDES_POR_DEFECTO, SOLICITUDES_MINIMO, SOLICITUDES_MAXIMO, 1));
    private final JButton botonSimular = new JButton("Simular");
    private final JTextArea areaResultado = new JTextArea();

    /**
     * @param estudiantes a quienes se asignan las solicitudes, en orden circular.
     * @param libros      opciones de libro; solo se ofrecen los que tienen stock.
     */
    public DialogoSimulacion(Window propietario, ControladorPrestamos controladorPrestamos,
                             ControladorLibros controladorLibros, List<Estudiante> estudiantes,
                             List<Libro> libros, PrestamoListener listener) {
        super(propietario, TITULO, ModalityType.APPLICATION_MODAL);
        this.controladorPrestamos = controladorPrestamos;
        this.controladorLibros = controladorLibros;
        this.idsEstudiantes = estudiantes.stream().map(Estudiante::getId).toList();
        this.listener = listener;
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
        comboLibro.setRenderer(Renderizadores.conTexto((Libro l) -> l.getTitulo() + " (stock " + l.getStock() + ")"));
        botonSimular.addActionListener(e -> simular());
        botonSimular.setEnabled(comboLibro.getItemCount() > 0 && !idsEstudiantes.isEmpty());

        JPanel opciones = new JPanel(new FlowLayout(FlowLayout.LEFT, MARGEN, MARGEN));
        opciones.add(new JLabel("Libro:"));
        opciones.add(comboLibro);
        opciones.add(new JLabel("Solicitudes simultáneas:"));
        opciones.add(selectorSolicitudes);

        areaResultado.setEditable(false);
        areaResultado.setBorder(BorderFactory.createEmptyBorder(MARGEN, MARGEN, MARGEN, MARGEN));
        areaResultado.setText("Elige un libro con poco stock y más solicitudes que ejemplares.");

        add(opciones, BorderLayout.NORTH);
        add(new JScrollPane(areaResultado), BorderLayout.CENTER);
        add(botonSimular, BorderLayout.SOUTH);
    }

    /** Lee el stock actual, lanza la simulación en segundo plano y bloquea el formulario mientras corre. */
    private void simular() {
        Libro libro = comboLibro.getItemAt(comboLibro.getSelectedIndex());
        if (libro == null) {
            return;
        }
        try {
            selectorSolicitudes.commitEdit();
        } catch (ParseException e) {
            // texto inválido: el selector conserva su último valor válido
        }
        int solicitudes = (Integer) selectorSolicitudes.getValue();
        int stockAntes;
        try {
            stockAntes = stockActual(libro.getId());
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
            return;
        }
        ocupado(true);
        areaResultado.setText("Lanzando " + solicitudes + " solicitudes simultáneas de " + libro.getTitulo() + "...");
        new SwingWorker<ResultadoSimulacion, Void>() {
            @Override
            protected ResultadoSimulacion doInBackground() throws InterruptedException {
                // Otro hilo: aquí no se toca la interfaz.
                return controladorPrestamos.simularPrestamosSimultaneos(libro.getId(), idsEstudiantes, solicitudes);
            }

            @Override
            protected void done() {
                ocupado(false);
                mostrarResultado(this, libro, stockAntes);
            }
        }.execute();
    }

    /** Muestra los conteos, el stock antes y después, y el registro de cada hilo. */
    private void mostrarResultado(SwingWorker<ResultadoSimulacion, Void> worker, Libro libro, int stockAntes) {
        try {
            ResultadoSimulacion resultado = worker.get();
            int stockDespues = stockActual(libro.getId());
            boolean cuadra = stockAntes - resultado.aprobados() == stockDespues;
            StringBuilder texto = new StringBuilder()
                    .append("Libro: ").append(libro.getTitulo()).append('\n')
                    .append("Stock antes: ").append(stockAntes).append('\n')
                    .append("Solicitudes simultáneas: ").append(resultado.solicitudes()).append('\n')
                    .append("Aprobadas: ").append(resultado.aprobados()).append('\n')
                    .append("Rechazadas por falta de stock: ").append(resultado.rechazados()).append('\n')
                    .append("Errores: ").append(resultado.errores()).append('\n')
                    .append("Stock después: ").append(stockDespues).append('\n')
                    .append(cuadra
                            ? "✔ El stock cuadra: antes − aprobadas = después."
                            : "✘ El stock NO cuadra: revisar la sincronización.")
                    .append("\n\nRegistro por hilo:\n");
            resultado.registro().forEach(linea -> texto.append("  ").append(linea).append('\n'));
            areaResultado.setText(texto.toString());
            areaResultado.setCaretPosition(0);
            listener.onPrestamosCambiados();
        } catch (ExecutionException e) {
            Throwable causa = e.getCause();
            areaResultado.setText("La simulación falló: " + causa.getMessage());
            if (causa instanceof PersistenciaException persistencia) {
                Dialogos.errorBaseDatos(this, persistencia);
            }
        } catch (PersistenciaException e) {
            Dialogos.errorBaseDatos(this, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** @return el stock del libro según la BD en este momento. */
    private int stockActual(int idLibro) {
        return controladorLibros.buscarLibros(null, null).stream()
                .filter(libro -> libro.getId() == idLibro)
                .findFirst()
                .map(Libro::getStock)
                .orElse(0);
    }

    private void ocupado(boolean ocupado) {
        botonSimular.setEnabled(!ocupado);
        comboLibro.setEnabled(!ocupado);
        selectorSolicitudes.setEnabled(!ocupado);
        setCursor(ocupado ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        setDefaultCloseOperation(ocupado ? DO_NOTHING_ON_CLOSE : DISPOSE_ON_CLOSE);
    }
}