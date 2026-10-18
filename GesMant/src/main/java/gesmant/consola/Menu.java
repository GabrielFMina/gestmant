package gesmant.consola;

import gesmant.datos.OrdenDAO;
import gesmant.estructuras.Algoritmos;
import gesmant.estructuras.ListaEnlazada;
import gesmant.estructuras.Pila;
import gesmant.logica.GestorOrdenes;
import gesmant.logica.ReglaNegocioException;
import gesmant.modelo.OrdenTrabajo;
import gesmant.modelo.Usuario;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Scanner;

/**
 * Menu de consola. Es una clase abstracta: define como se muestra y se recorre el menu,
 * pero cada subclase (MenuAdministrador y MenuTecnico) define sus opciones y que hace cada una.
 */
public abstract class Menu {

    protected final Scanner teclado;
    protected final Usuario usuario;
    protected final GestorOrdenes gestor = new GestorOrdenes();
    protected final OrdenDAO ordenDAO = new OrdenDAO();
    private final Pila<String> acciones = new Pila<>();

    protected Menu(Scanner teclado, Usuario usuario) {
        this.teclado = teclado;
        this.usuario = usuario;
    }

    /** Titulo que se muestra arriba del menu. */
    protected abstract String titulo();

    /** Opciones del menu, sin contar la opcion 0 (Salir). */
    protected abstract String[] opciones();

    /** Ejecuta la opcion elegida. */
    protected abstract void procesar(int opcion) throws SQLException, ReglaNegocioException;

    /** Muestra el menu hasta que el usuario elige 0. */
    public final void ejecutar() {
        int opcion;
        do {
            System.out.println();
            System.out.println("===== " + titulo() + " =====");
            String[] ops = opciones();
            for (int i = 0; i < ops.length; i++) {
                System.out.println((i + 1) + ". " + ops[i]);
            }
            System.out.println("0. Salir");
            opcion = leerEntero("Opción: ");
            if (opcion < 0 || opcion > ops.length) {
                System.out.println("Opción inválida, intente de nuevo.");
            } else if (opcion != 0) {
                try {
                    procesar(opcion);
                } catch (ReglaNegocioException e) {
                    System.out.println("No se pudo realizar la operación: " + e.getMessage());
                } catch (SQLException e) {
                    System.out.println("Error de base de datos: " + e.getMessage());
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println("Atención: " + e.getMessage());
                }
            }
        } while (opcion != 0);
        System.out.println("Sesión cerrada.");
    }

    // ------------------------------------------------------- lectura de datos

    /** Lee un numero entero. Si el usuario escribe otra cosa, lo vuelve a pedir. */
    protected int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = teclado.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número entero.");
            }
        }
    }

    protected double leerDecimal(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = teclado.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número (por ejemplo 1500.50).");
            }
        }
    }

    protected String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return teclado.nextLine().trim();
    }

    // ------------------------------------------------------ opciones comunes

    /** Busca una orden por codigo con busqueda binaria (antes la ordena por codigo). */
    protected OrdenTrabajo pedirOrden(ListaEnlazada<OrdenTrabajo> ordenes) throws ReglaNegocioException {
        Algoritmos.ordenarPorCodigo(ordenes);
        String codigo = leerTexto("Código de la orden (ej. OT-0002): ");
        OrdenTrabajo o = Algoritmos.buscarPorCodigo(ordenes, codigo);
        if (o == null) {
            throw new ReglaNegocioException("No existe la orden " + codigo + ".");
        }
        return o;
    }

    protected void mostrarOrdenes(ListaEnlazada<OrdenTrabajo> ordenes) {
        if (ordenes.estaVacia()) {
            System.out.println("No hay órdenes para mostrar.");
            return;
        }
        System.out.printf("%-8s %-22s %-8s %-6s %-14s %s%n", "Código", "Consorcio", "Unidad", "Prior.", "Estado", "Técnico");
        for (int i = 0; i < ordenes.tamanio(); i++) {
            OrdenTrabajo o = ordenes.obtener(i);
            System.out.printf("%-8s %-22s %-8s %-6s %-14s %s%n", o.getCodigo(), o.getNombreConsorcio(),
                    o.getNumeroUnidad(), o.getPrioridad(), o.getEstado(),
                    o.getNombreTecnico() == null ? "-" : o.getNombreTecnico());
        }
    }

    protected ListaEnlazada<OrdenTrabajo> aLista(java.util.List<OrdenTrabajo> origen) {
        ListaEnlazada<OrdenTrabajo> lista = new ListaEnlazada<>();
        for (OrdenTrabajo o : origen) {
            lista.agregar(o);
        }
        return lista;
    }

    /** Guarda la accion en la pila de la sesion. */
    protected void registrarAccion(String accion) {
        String hora = LocalTime.now().withNano(0).toString();
        acciones.apilar(hora + " - " + accion);
        System.out.println("OK: " + accion);
    }

    /**
     * Muestra las acciones de la sesion, de la mas reciente a la mas vieja.
     * Se desapilan para mostrarlas y despues se vuelven a apilar para no perderlas.
     */
    protected void mostrarAcciones() {
        if (acciones.estaVacia()) {
            System.out.println("Todavía no se realizaron acciones en esta sesión.");
            return;
        }
        Pila<String> auxiliar = new Pila<>();
        System.out.println("Últimas acciones (la más reciente primero):");
        while (!acciones.estaVacia()) {
            String a = acciones.desapilar();
            System.out.println("  " + a);
            auxiliar.apilar(a);
        }
        while (!auxiliar.estaVacia()) {
            acciones.apilar(auxiliar.desapilar());
        }
    }
}
