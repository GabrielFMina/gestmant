package gesmant.estructuras;

/**
 * Pila (LIFO): el ultimo que entra es el primero que sale.
 * Se usa para guardar las acciones de la sesion y mostrar primero la mas reciente.
 */
public class Pila<T> {

    private Nodo<T> tope;
    private int cantidad;

    public void apilar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = tope;
        tope = nuevo;
        cantidad++;
    }

    public T desapilar() {
        if (estaVacia()) {
            throw new IllegalStateException("La pila está vacía.");
        }
        T dato = tope.dato;
        tope = tope.siguiente;
        cantidad--;
        return dato;
    }

    public T verTope() {
        if (estaVacia()) {
            throw new IllegalStateException("La pila está vacía.");
        }
        return tope.dato;
    }

    public boolean estaVacia() {
        return tope == null;
    }

    public int tamanio() {
        return cantidad;
    }
}
