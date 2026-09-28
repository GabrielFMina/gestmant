package gesmant.modelo;

import java.time.LocalDateTime;

/** Orden de trabajo: registro central del sistema. */
public class OrdenTrabajo {

    // Estados posibles de una orden (ver diagrama de estados)
    public static final String REGISTRADA = "REGISTRADA";
    public static final String ASIGNADA = "ASIGNADA";
    public static final String PRESUPUESTADA = "PRESUPUESTADA";
    public static final String APROBADA = "APROBADA";
    public static final String EN_EJECUCION = "EN_EJECUCION";
    public static final String FINALIZADA = "FINALIZADA";
    public static final String PAGADA = "PAGADA";

    private int idOrden;
    private String codigo;
    private int idUnidad;
    private Integer idTecnico;          // puede ser null si todavia no se asigno
    private String descripcionProblema;
    private String prioridad;
    private String estado;
    private boolean requiereCoordinacion;
    private String diagnostico;
    private String trabajoRealizado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    // Datos descriptivos que se completan con las consultas (no son columnas de la tabla)
    private String nombreConsorcio;
    private String numeroUnidad;
    private String nombreTecnico;

    public OrdenTrabajo() {
    }

    public boolean estaCerrada() {
        return FINALIZADA.equals(estado) || PAGADA.equals(estado);
    }

    public int getIdOrden() { return idOrden; }
    public void setIdOrden(int idOrden) { this.idOrden = idOrden; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public int getIdUnidad() { return idUnidad; }
    public void setIdUnidad(int idUnidad) { this.idUnidad = idUnidad; }

    public Integer getIdTecnico() { return idTecnico; }
    public void setIdTecnico(Integer idTecnico) { this.idTecnico = idTecnico; }

    public String getDescripcionProblema() { return descripcionProblema; }
    public void setDescripcionProblema(String d) { this.descripcionProblema = d; }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isRequiereCoordinacion() { return requiereCoordinacion; }
    public void setRequiereCoordinacion(boolean r) { this.requiereCoordinacion = r; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getTrabajoRealizado() { return trabajoRealizado; }
    public void setTrabajoRealizado(String t) { this.trabajoRealizado = t; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime f) { this.fechaCreacion = f; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime f) { this.fechaInicio = f; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime f) { this.fechaFin = f; }

    public String getNombreConsorcio() { return nombreConsorcio; }
    public void setNombreConsorcio(String n) { this.nombreConsorcio = n; }

    public String getNumeroUnidad() { return numeroUnidad; }
    public void setNumeroUnidad(String n) { this.numeroUnidad = n; }

    public String getNombreTecnico() { return nombreTecnico; }
    public void setNombreTecnico(String n) { this.nombreTecnico = n; }
}
