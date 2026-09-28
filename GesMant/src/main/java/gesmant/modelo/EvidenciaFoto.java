package gesmant.modelo;

import java.time.LocalDateTime;

/** Foto de respaldo de un trabajo (solo se guarda la ruta). */
public class EvidenciaFoto {

    private String rutaArchivo;
    private String etapa;
    private LocalDateTime fechaSubida;

    public EvidenciaFoto(String rutaArchivo, String etapa, LocalDateTime fechaSubida) {
        this.rutaArchivo = rutaArchivo;
        this.etapa = etapa;
        this.fechaSubida = fechaSubida;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public String getEtapa() {
        return etapa;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }
}
