package gesmant;

import gesmant.vista.LoginVista;
import javax.swing.SwingUtilities;

/** Punto de entrada del prototipo GesMant.
 * USUARIO PARA INGRESAR: admin, pass: admin123 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginVista().setVisible(true));
    }
}
