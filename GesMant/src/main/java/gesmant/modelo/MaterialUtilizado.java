package gesmant.modelo;

/** Material usado realmente en un trabajo. */
public class MaterialUtilizado {

    private String nombre;
    private int cantidad;
    private double costoUnitario;

    public MaterialUtilizado(String nombre, int cantidad, double costoUnitario) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }
}
