package gesmant.modelo;

import java.time.LocalDateTime;

/** Relacion entre una orden bloqueada y la orden previa de la que depende. */
public class DependenciaOrden {

    private int idOrdenPrevia;
    private String codigoPrevia;
    private String estadoPrevia;
    private LocalDateTime finPrevia;
    private int tiempoEsperaHoras;

    public DependenciaOrden(int idOrdenPrevia, String codigoPrevia, String estadoPrevia, LocalDateTime finPrevia, int tiempoEsperaHoras) {
        this.idOrdenPrevia = idOrdenPrevia;
        this.codigoPrevia = codigoPrevia;
        this.estadoPrevia = estadoPrevia;
        this.finPrevia = finPrevia;
        this.tiempoEsperaHoras = tiempoEsperaHoras;
    }

    public int getIdOrdenPrevia() {
        return idOrdenPrevia;
    }

    public String getCodigoPrevia() {
        return codigoPrevia;
    }

    public String getEstadoPrevia() {
        return estadoPrevia;
    }

    public LocalDateTime getFinPrevia() {
        return finPrevia;
    }

    public int getTiempoEsperaHoras() {
        return tiempoEsperaHoras;
    }
}
