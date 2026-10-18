package gesmant.estructuras;

import gesmant.modelo.OrdenTrabajo;

/** Algoritmos de ordenacion y busqueda sobre la lista de ordenes. */
public class Algoritmos {

    /** Convierte la prioridad en un numero para poder compararla (ALTA es la mas urgente). */
    public static int valorPrioridad(String prioridad) {
        switch (prioridad) {
            case "ALTA":
                return 1;
            case "MEDIA":
                return 2;
            default:
                return 3;
        }
    }

    /**
     * Ordenamiento burbuja por prioridad (y por codigo si tienen la misma prioridad).
     * Si en una pasada no hubo cambios, la lista ya esta ordenada y se corta antes.
     */
    public static void ordenarPorPrioridad(ListaEnlazada<OrdenTrabajo> lista) {
        int n = lista.tamanio();
        boolean huboCambio = true;
        for (int pasada = 0; pasada < n - 1 && huboCambio; pasada++) {
            huboCambio = false;
            for (int i = 0; i < n - 1 - pasada; i++) {
                OrdenTrabajo a = lista.obtener(i);
                OrdenTrabajo b = lista.obtener(i + 1);
                int pa = valorPrioridad(a.getPrioridad());
                int pb = valorPrioridad(b.getPrioridad());
                if (pa > pb || (pa == pb && a.getCodigo().compareTo(b.getCodigo()) > 0)) {
                    lista.establecer(i, b);
                    lista.establecer(i + 1, a);
                    huboCambio = true;
                }
            }
        }
    }

    /** Ordenamiento por insercion segun el codigo (OT-0001, OT-0002, ...). */
    public static void ordenarPorCodigo(ListaEnlazada<OrdenTrabajo> lista) {
        for (int i = 1; i < lista.tamanio(); i++) {
            OrdenTrabajo actual = lista.obtener(i);
            int j = i - 1;
            while (j >= 0 && lista.obtener(j).getCodigo().compareTo(actual.getCodigo()) > 0) {
                lista.establecer(j + 1, lista.obtener(j));
                j--;
            }
            lista.establecer(j + 1, actual);
        }
    }

    /**
     * Busqueda binaria por codigo. La lista tiene que estar ordenada por codigo.
     * Devuelve la orden o null si no existe.
     */
    public static OrdenTrabajo buscarPorCodigo(ListaEnlazada<OrdenTrabajo> lista, String codigo) {
        int inicio = 0;
        int fin = lista.tamanio() - 1;
        while (inicio <= fin) {
            int medio = (inicio + fin) / 2;
            OrdenTrabajo o = lista.obtener(medio);
            int comparacion = o.getCodigo().compareToIgnoreCase(codigo);
            if (comparacion == 0) {
                return o;
            } else if (comparacion < 0) {
                inicio = medio + 1;
            } else {
                fin = medio - 1;
            }
        }
        return null;
    }

    /** Busqueda secuencial: todas las ordenes que estan en un estado dado. */
    public static ListaEnlazada<OrdenTrabajo> buscarPorEstado(ListaEnlazada<OrdenTrabajo> lista, String estado) {
        ListaEnlazada<OrdenTrabajo> resultado = new ListaEnlazada<>();
        for (int i = 0; i < lista.tamanio(); i++) {
            if (lista.obtener(i).getEstado().equalsIgnoreCase(estado)) {
                resultado.agregar(lista.obtener(i));
            }
        }
        return resultado;
    }
}
