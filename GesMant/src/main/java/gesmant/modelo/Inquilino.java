package gesmant.modelo;

/** Persona que vive en una unidad. Con ella se coordina el acceso (CU04). */
public class Inquilino extends Persona {

    private String unidad;

    public Inquilino(String nombre, String telefono, String unidad) {
        super(nombre, telefono);
        this.unidad = unidad;
    }

    public String getUnidad() {
        return unidad;
    }

    @Override
    public String getTipo() {
        return "Inquilino";
    }

    @Override
    public String getDescripcion() {
        return "Unidad " + unidad;
    }
}
