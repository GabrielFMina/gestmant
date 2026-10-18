package gesmant.modelo;

/**
 * Clase abstracta que representa a cualquier persona con la que trabaja el sistema.
 * No se pueden crear objetos Persona directamente: solo de sus subclases
 * (Tecnico e Inquilino), que definen su propio tipo y descripcion.
 */
public abstract class Persona {

    private String nombre;
    private String telefono;

    protected Persona(String nombre, String telefono) {
        setNombre(nombre);
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    /** Valida el dato antes de guardarlo (encapsulamiento). */
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public String getTelefono() {
        return telefono == null ? "-" : telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /** Cada subclase indica que tipo de persona es. */
    public abstract String getTipo();

    /** Cada subclase arma su propia descripcion (polimorfismo). */
    public abstract String getDescripcion();

    @Override
    public String toString() {
        return nombre;
    }
}
