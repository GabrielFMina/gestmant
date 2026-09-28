package gesmant.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Presupuesto de una orden, formado por items de mano de obra y materiales. */
public class Presupuesto {

    private int idPresupuesto;
    private int idOrden;
    private LocalDateTime fechaCreacion;
    private String estado;              // PENDIENTE, APROBADO o RECHAZADO
    private String materialesACargoDe;  // TECNICO o ADMINISTRACION
    private String observacion;
    private List<ItemPresupuesto> items = new ArrayList<>();

    public Presupuesto(int idPresupuesto, int idOrden, LocalDateTime fechaCreacion, String estado,
                       String materialesACargoDe, String observacion) {
        this.idPresupuesto = idPresupuesto;
        this.idOrden = idOrden;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.materialesACargoDe = materialesACargoDe;
        this.observacion = observacion;
    }

    /** El total no se guarda en la base: se calcula sumando los items. */
    public double getTotal() {
        double total = 0;
        for (ItemPresupuesto item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public double getTotalPorTipo(String tipo) {
        double total = 0;
        for (ItemPresupuesto item : items) {
            if (item.getTipo().equals(tipo)) {
                total += item.getSubtotal();
            }
        }
        return total;
    }

    public int getIdPresupuesto() { return idPresupuesto; }
    public int getIdOrden() { return idOrden; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public String getEstado() { return estado; }
    public String getMaterialesACargoDe() { return materialesACargoDe; }
    public String getObservacion() { return observacion; }
    public List<ItemPresupuesto> getItems() { return items; }
}
