package gesmant.pruebas;

import gesmant.estructuras.Algoritmos;
import gesmant.estructuras.Cola;
import gesmant.estructuras.ListaEnlazada;
import gesmant.estructuras.Pila;
import gesmant.modelo.Inquilino;
import gesmant.modelo.OrdenTrabajo;
import gesmant.modelo.Persona;
import gesmant.modelo.Tecnico;

/**
 * Pruebas de las estructuras de datos, los algoritmos y las clases del modelo (TP3).
 * No necesitan base de datos.
 */
public class PruebasEstructuras {

    private static int correctas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {
        // Lista enlazada
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        lista.agregar("A");
        lista.agregar("B");
        lista.agregar("C");
        verificar("PE01", "La lista guarda los elementos en orden", lista.tamanio() == 3 && lista.obtener(2).equals("C"));
        boolean fuera = false;
        try {
            lista.obtener(5);
        } catch (IndexOutOfBoundsException e) {
            fuera = true;
        }
        verificar("PE02", "Pedir una posición inexistente lanza una excepción", fuera);

        // Cola: el primero que entra es el primero que sale
        Cola<String> cola = new Cola<>();
        cola.encolar("OT-0008");
        cola.encolar("OT-0009");
        verificar("PE03", "La cola respeta el orden de llegada (FIFO)", cola.desencolar().equals("OT-0008") && cola.tamanio() == 1);

        // Pila: el ultimo que entra es el primero que sale
        Pila<String> pila = new Pila<>();
        pila.apilar("primera acción");
        pila.apilar("segunda acción");
        verificar("PE04", "La pila devuelve primero lo último (LIFO)", pila.desapilar().equals("segunda acción"));
        pila.desapilar();
        boolean vacia = false;
        try {
            pila.desapilar();
        } catch (IllegalStateException e) {
            vacia = true;
        }
        verificar("PE05", "Desapilar una pila vacía lanza una excepción", vacia);

        // Ordenacion y busqueda
        ListaEnlazada<OrdenTrabajo> ordenes = new ListaEnlazada<>();
        ordenes.agregar(orden("OT-0004", "BAJA"));
        ordenes.agregar(orden("OT-0002", "MEDIA"));
        ordenes.agregar(orden("OT-0005", "ALTA"));
        ordenes.agregar(orden("OT-0001", "ALTA"));
        Algoritmos.ordenarPorPrioridad(ordenes);
        verificar("PE06", "Burbuja: ALTA primero y, a igual prioridad, por código",
                ordenes.obtener(0).getCodigo().equals("OT-0001") && ordenes.obtener(1).getCodigo().equals("OT-0005")
                && ordenes.obtener(3).getCodigo().equals("OT-0004"));
        Algoritmos.ordenarPorCodigo(ordenes);
        verificar("PE07", "Inserción: queda ordenada por código",
                ordenes.obtener(0).getCodigo().equals("OT-0001") && ordenes.obtener(3).getCodigo().equals("OT-0005"));
        verificar("PE08", "Búsqueda binaria encuentra OT-0004", Algoritmos.buscarPorCodigo(ordenes, "OT-0004") != null);
        verificar("PE09", "Búsqueda binaria devuelve null si no existe", Algoritmos.buscarPorCodigo(ordenes, "OT-0099") == null);

        // Herencia, polimorfismo y encapsulamiento
        Persona[] personas = {
            new Tecnico(1, "Gabriel Mina", "Mantenimiento general", "351-6001001", true),
            new Inquilino("Laura Giménez", "351-5550101", "3B")
        };
        verificar("PE10", "Cada subclase responde getTipo() a su manera",
                personas[0].getTipo().equals("Técnico") && personas[1].getTipo().equals("Inquilino"));
        boolean rechazado = false;
        try {
            new Inquilino("  ", null, "1A");
        } catch (IllegalArgumentException e) {
            rechazado = true;
        }
        verificar("PE11", "No se puede crear una persona sin nombre", rechazado);

        System.out.println();
        System.out.println("Resultado: " + correctas + " correctas, " + fallidas + " fallidas.");
    }

    private static OrdenTrabajo orden(String codigo, String prioridad) {
        OrdenTrabajo o = new OrdenTrabajo();
        o.setCodigo(codigo);
        o.setPrioridad(prioridad);
        o.setEstado(OrdenTrabajo.ASIGNADA);
        return o;
    }

    private static void verificar(String codigo, String descripcion, boolean condicion) {
        if (condicion) {
            correctas++;
            System.out.println(codigo + "  OK     " + descripcion);
        } else {
            fallidas++;
            System.out.println(codigo + "  FALLA  " + descripcion);
        }
    }
}
