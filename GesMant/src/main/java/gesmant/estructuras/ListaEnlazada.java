package gesmant.estructuras;

/**
 * Lista simplemente enlazada. Se usa para guardar en memoria las ordenes que se leen
 * de la base y poder ordenarlas y buscarlas.
 */
public class ListaEnlazada<T> {

    private Nodo<T> primero;
    private int cantidad;

    public ListaEnlazada() {
        primero = null;
        cantidad = 0;
    }

    /** Agrega un elemento al final de la lista. */
    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (primero == null) {
            primero = nuevo;
        } else {
            Nodo<T> actual = primero;
            while (actual.siguiente != null) {
                actual = actual.siguiente;
            }
            actual.siguiente = nuevo;
        }
        cantidad++;
    }

    public T obtener(int posicion) {
        return buscarNodo(posicion).dato;
    }

    /** Reemplaza el dato de una posicion (lo usan los algoritmos de ordenacion). */
    public void establecer(int posicion, T dato) {
        buscarNodo(posicion).dato = dato;
    }

    public int tamanio() {
        return cantidad;
    }

    public boolean estaVacia() {
        return cantidad == 0;
    }

    private Nodo<T> buscarNodo(int posicion) {
        if (posicion < 0 || posicion >= cantidad) {
            throw new IndexOutOfBoundsException("Posición fuera de la lista: " + posicion);
        }
        Nodo<T> actual = primero;
        for (int i = 0; i < posicion; i++) {
            actual = actual.siguiente;
        }
        return actual;
    }
}
