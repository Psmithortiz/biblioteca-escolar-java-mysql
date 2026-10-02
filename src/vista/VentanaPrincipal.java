package vista;

import controlador.ControladorCategorias;
import controlador.ControladorLibros;
import dao.impl.CategoriaDAOImpl;
import dao.impl.LibroDAOImpl;
import modelo.Permiso;
import modelo.Usuario;
import utils.Validador;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.EnumMap;
import java.util.Map;

/**
 * Ventana principal. Arma un menú y una pantalla por cada permiso del usuario, y muestra
 * una pantalla a la vez con un {@link CardLayout}. No pregunta por el rol: solo recorre los permisos.
 * Es la raíz de composición: el único lugar que elige las implementaciones de los DAO.
 */
public class VentanaPrincipal extends JFrame {

    private static final String TITULO = "Biblioteca Escolar";
    private static final int ANCHO = 1000;
    private static final int ALTO = 560;

    private final Usuario usuario;
    private final SesionListener sesionListener;

    private final ControladorCategorias controladorCategorias = new ControladorCategorias(new CategoriaDAOImpl());
    private final ControladorLibros controladorLibros = new ControladorLibros(new LibroDAOImpl());

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenedor = new JPanel(cartas);
    /** Pantallas por permiso, en el orden de los permisos del usuario. */
    private final Map<Permiso, Pantalla> pantallas = new EnumMap<>(Permiso.class);

    /**
     * @param usuario        usuario que inició sesión; define qué pantallas hay.
     * @param sesionListener a quien avisar al cerrar sesión.
     */
    public VentanaPrincipal(Usuario usuario, SesionListener sesionListener) {
        Validador.objetoNoNulo(usuario, "usuario");
        Validador.objetoNoNulo(sesionListener, "sesionListener");
        this.usuario = usuario;
        this.sesionListener = sesionListener;
        configurarVentana();
        crearPantallas();
        setJMenuBar(crearMenu());
        add(contenedor, BorderLayout.CENTER);
        mostrar(pantallas.keySet().iterator().next()); // la primera pantalla permitida
        setVisible(true);
    }

    private void configurarVentana() {
        setSize(ANCHO, ALTO);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // sin ventanas abiertas, la app termina
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    /** Crea una pantalla por permiso y la agrega como carta, con el nombre del permiso. */
    private void crearPantallas() {
        for (Permiso permiso : usuario.getPermisos()) {
            Pantalla pantalla = crearPantalla(permiso);
            pantallas.put(permiso, pantalla);
            contenedor.add(pantalla, permiso.name());
        }
    }

    /** Menú "Ir a" con una opción por permiso, y menú "Sesión". */
    private JMenuBar crearMenu() {
        JMenu menuIrA = new JMenu("Ir a");
        for (Permiso permiso : pantallas.keySet()) {
            JMenuItem opcion = new JMenuItem(texto(permiso));
            opcion.addActionListener(e -> mostrar(permiso));
            menuIrA.add(opcion);
        }

        JMenuItem opcionCerrarSesion = new JMenuItem("Cerrar sesión");
        opcionCerrarSesion.addActionListener(e -> cerrarSesion());
        JMenuItem opcionSalir = new JMenuItem("Salir");
        opcionSalir.addActionListener(e -> dispose());
        JMenu menuSesion = new JMenu("Sesión");
        menuSesion.add(opcionCerrarSesion);
        menuSesion.add(opcionSalir);

        JMenuBar barra = new JMenuBar();
        barra.add(menuIrA);
        barra.add(menuSesion);
        return barra;
    }

    /** Muestra la pantalla del permiso, recarga sus datos y actualiza el título. */
    private void mostrar(Permiso permiso) {
        cartas.show(contenedor, permiso.name());
        pantallas.get(permiso).refrescar();
        setTitle(TITULO + " - " + texto(permiso) + " · " + usuario.getNombre());
    }

    private void cerrarSesion() {
        if (Dialogos.confirmar(this, "Cerrar sesión", "¿Cerrar la sesión de " + usuario.getNombre() + "?")) {
            dispose();
            sesionListener.onSesionCerrada();
        }
    }

    /** Pantalla que corresponde a cada permiso. */
    private Pantalla crearPantalla(Permiso permiso) {
        return switch (permiso) {
            case GESTIONAR_LIBROS -> new PantallaLibros(controladorLibros, controladorCategorias);
            case GESTIONAR_CATEGORIAS -> new PantallaCategorias(controladorCategorias);
            default -> new PantallaProvisoria(texto(permiso)); // temporal, hasta tener las demás
        };
    }

    /** Texto del menú y del título para cada permiso. */
    private static String texto(Permiso permiso) {
        return switch (permiso) {
            case GESTIONAR_LIBROS -> "Libros";
            case GESTIONAR_ESTUDIANTES -> "Estudiantes";
            case GESTIONAR_CATEGORIAS -> "Categorías";
            case GESTIONAR_PRESTAMOS -> "Préstamos y devoluciones";
            case VER_REPORTES -> "Reportes";
            case CONSULTAR_CATALOGO -> "Catálogo";
            case VER_HISTORIAL_PROPIO -> "Mi historial";
        };
    }
}