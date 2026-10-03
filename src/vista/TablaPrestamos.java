package vista;

import modelo.Prestamo;
import modelo.PrestamoDetalle;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Tabla de solo lectura con préstamos, con o sin las columnas del estudiante. */
final class TablaPrestamos extends JScrollPane {

    private static final String[] COLUMNAS_CON_ESTUDIANTE =
            {"ID", "Estudiante", "RUT", "Libro", "Prestado", "Vence", "Devuelto", "Estado"};
    private static final String[] COLUMNAS_SIN_ESTUDIANTE =
            {"ID", "Libro", "Prestado", "Vence", "Devuelto", "Estado"};
    private static final int ANCHO_COLUMNA_ID = 50;

    private final boolean conEstudiante;
    private final DefaultTableModel modelo;

    /** @param conEstudiante si se muestran el nombre y el RUT del estudiante. */
    TablaPrestamos(boolean conEstudiante) {
        this.conEstudiante = conEstudiante;
        this.modelo = new DefaultTableModel(conEstudiante ? COLUMNAS_CON_ESTUDIANTE : COLUMNAS_SIN_ESTUDIANTE, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        JTable tabla = new JTable(modelo);
        tabla.getColumnModel().getColumn(0).setMaxWidth(ANCHO_COLUMNA_ID);
        setViewportView(tabla);
    }

    /** Reemplaza las filas por los préstamos dados, con su estado a la fecha de hoy. */
    void mostrar(List<PrestamoDetalle> detalles) {
        modelo.setRowCount(0);
        LocalDate hoy = LocalDate.now();
        for (PrestamoDetalle detalle : detalles) {
            Prestamo prestamo = detalle.prestamo();
            List<Object> fila = new ArrayList<>();
            fila.add(prestamo.getId());
            if (conEstudiante) {
                fila.add(detalle.nombreEstudiante());
                fila.add(detalle.rutEstudiante());
            }
            fila.add(detalle.tituloLibro());
            fila.add(Formatos.fecha(prestamo.getFechaPrestamo()));
            fila.add(Formatos.fecha(prestamo.getFechaVencimiento()));
            fila.add(Formatos.fecha(prestamo.getFechaDevolucionReal()));
            fila.add(Formatos.estado(prestamo, hoy));
            modelo.addRow(fila.toArray());
        }
    }
}