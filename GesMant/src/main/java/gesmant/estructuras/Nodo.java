package gesmant.estructuras;

/** Nodo que usan la lista enlazada, la cola y la pila. Guarda un dato y el enlace al siguiente. */
class Nodo<T> {

    T dato;
    Nodo<T> siguiente;

    Nodo(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }
}
