package gesmant.vista;

import gesmant.logica.ReglaNegocioException;
import java.awt.Component;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Metodos de ayuda que comparten todas las pantallas. */
public class Util {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String fecha(LocalDateTime f) {
        return f == null ? "-" : f.format(FORMATO);
    }

    public static String texto(String s) {
        return s == null ? "-" : s;
    }

    /** Modelo de tabla que no permite editar las celdas a mano. */
    public static DefaultTableModel modeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    /** Achica la columna del id para dejar mas lugar al resto. */
    public static void achicarColumnaId(JTable tabla) {
        tabla.getColumnModel().getColumn(0).setMaxWidth(40);
    }

    /** Devuelve el valor de la primera columna (el id) de la fila elegida, o -1. */
    public static int idSeleccionado(Component padre, JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(padre, "Primero seleccione una fila de la tabla.");
            return -1;
        }
        return (Integer) tabla.getValueAt(fila, 0);
    }

    /** Muestra un formulario simple (etiqueta + campo) y devuelve true si se acepto. */
    public static boolean formulario(Component padre, String titulo, String[] etiquetas, Component[] campos) {
        JPanel panel = new JPanel(new GridLayout(etiquetas.length, 2, 5, 5));
        for (int i = 0; i < etiquetas.length; i++) {
            panel.add(new JLabel(etiquetas[i]));
            panel.add(campos[i]);
        }
        int r = JOptionPane.showConfirmDialog(padre, panel, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        return r == JOptionPane.OK_OPTION;
    }

    /** Muestra el error de forma entendible para el usuario. */
    public static void error(Component padre, Exception e) {
        if (e instanceof ReglaNegocioException) {
            JOptionPane.showMessageDialog(padre, e.getMessage(), "No se puede realizar la operación",
                    JOptionPane.WARNING_MESSAGE);
        } else if (e instanceof NumberFormatException) {
            JOptionPane.showMessageDialog(padre, "Revise los números ingresados.", "Dato inválido",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(padre, "Error de base de datos: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void ok(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje);
    }
}
