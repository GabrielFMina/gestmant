package gesmant.modelo;

/** Edificio administrado. */
public class Consorcio {

    private int idConsorcio;
    private String nombre;
    private String direccion;

    public Consorcio(int idConsorcio, String nombre, String direccion) {
        this.idConsorcio = idConsorcio;
        this.nombre = nombre;
        this.direccion = direccion;
    }

    public int getIdConsorcio() {
        return idConsorcio;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
