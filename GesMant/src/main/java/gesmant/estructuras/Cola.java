package gesmant.estructuras;

/**
 * Cola (FIFO): el primero que entra es el primero que sale.
 * Se usa para los reclamos que esperan ser asignados a un tecnico, en orden de llegada.
 */
public class Cola<T> {

    private Nodo<T> frente;
    private Nodo<T> fin;
    private int cantidad;

    public void encolar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        cantidad++;
    }

    public T desencolar() {
        if (estaVacia()) {
            throw new IllegalStateException("No hay reclamos pendientes.");
        }
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        cantidad--;
        return dato;
    }

    public T verFrente() {
        if (estaVacia()) {
            throw new IllegalStateException("No hay reclamos pendientes.");
        }
        return frente.dato;
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int tamanio() {
        return cantidad;
    }
}
