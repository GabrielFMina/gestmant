package gesmant.consola;

import gesmant.datos.UsuarioDAO;
import gesmant.modelo.Usuario;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Version de consola del prototipo (TP3). Pide usuario y clave y, segun el rol,
 * abre el menu del administrador o el del tecnico.
 */
public class AppConsola {

    private static final int INTENTOS = 3;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        try {
            System.out.println("GesMant - Gestión de órdenes de trabajo de mantenimiento");
            Usuario usuario = null;
            int intento = 1;
            while (usuario == null && intento <= INTENTOS) {
                System.out.print("Usuario: ");
                String nombre = teclado.nextLine().trim();
                System.out.print("Clave: ");
                String clave = teclado.nextLine().trim();
                usuario = new UsuarioDAO().login(nombre, clave);
                if (usuario == null) {
                    System.out.println("Usuario o clave incorrectos (intento " + intento + " de " + INTENTOS + ").");
                }
                intento++;
            }
            if (usuario == null) {
                System.out.println("Se superó la cantidad de intentos.");
                return;
            }
            // Polimorfismo: la variable es de tipo Menu, el objeto depende del rol
            Menu menu;
            if ("ADMINISTRADOR".equals(usuario.getRol())) {
                menu = new MenuAdministrador(teclado, usuario);
            } else {
                menu = new MenuTecnico(teclado, usuario);
            }
            menu.ejecutar();
        } catch (SQLException e) {
            System.out.println("No se pudo conectar con la base de datos: " + e.getMessage());
            System.out.println("Verifique que MySQL esté iniciado y que exista la base gesmant.");
        } finally {
            teclado.close();
        }
    }
}
