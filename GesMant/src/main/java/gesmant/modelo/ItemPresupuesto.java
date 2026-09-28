package gesmant.modelo;

/** Renglon de un presupuesto (mano de obra o material). */
public class ItemPresupuesto {

    private String descripcion;
    private String tipo;
    private int cantidad;
    private double precioUnitario;

    public ItemPresupuesto(String descripcion, String tipo, int cantidad, double precioUnitario) {
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getSubtotal() {
        return cantidad * precioUnitario;
    }
}
