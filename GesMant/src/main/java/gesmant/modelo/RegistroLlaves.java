package gesmant.modelo;

import java.time.LocalDateTime;

/** Constancia de entrega y devolucion de llaves. */
public class RegistroLlaves {

    private int idRegistro;
    private int idOrden;
    private String detalleJuego;
    private LocalDateTime fechaEntrega;
    private LocalDateTime fechaDevolucion;

    public RegistroLlaves(int idRegistro, int idOrden, String detalleJuego, LocalDateTime fechaEntrega, LocalDateTime fechaDevolucion) {
        this.idRegistro = idRegistro;
        this.idOrden = idOrden;
        this.detalleJuego = detalleJuego;
        this.fechaEntrega = fechaEntrega;
        this.fechaDevolucion = fechaDevolucion;
    }

    public int getIdRegistro() {
        return idRegistro;
    }

    public int getIdOrden() {
        return idOrden;
    }

    public String getDetalleJuego() {
        return detalleJuego;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }
}
