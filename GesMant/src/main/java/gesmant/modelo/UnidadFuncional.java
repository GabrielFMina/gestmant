package gesmant.modelo;

/** Departamento o espacio comun de un consorcio. */
public class UnidadFuncional {

    private int idUnidad;
    private int idConsorcio;
    private String numero;
    private String piso;
    private boolean espacioComun;
    private String nombreInquilino;
    private String telefonoInquilino;
    private String nombreConsorcio;

    public UnidadFuncional(int idUnidad, int idConsorcio, String numero, String piso, boolean espacioComun, String nombreInquilino, String telefonoInquilino, String nombreConsorcio) {
        this.idUnidad = idUnidad;
        this.idConsorcio = idConsorcio;
        this.numero = numero;
        this.piso = piso;
        this.espacioComun = espacioComun;
        this.nombreInquilino = nombreInquilino;
        this.telefonoInquilino = telefonoInquilino;
        this.nombreConsorcio = nombreConsorcio;
    }

    public int getIdUnidad() {
        return idUnidad;
    }

    public int getIdConsorcio() {
        return idConsorcio;
    }

    public String getNumero() {
        return numero;
    }

    public String getPiso() {
        return piso;
    }

    public boolean isEspacioComun() {
        return espacioComun;
    }

    public String getNombreInquilino() {
        return nombreInquilino;
    }

    public String getTelefonoInquilino() {
        return telefonoInquilino;
    }

    public String getNombreConsorcio() {
        return nombreConsorcio;
    }

    @Override
    public String toString() {
        return nombreConsorcio + " - " + numero;
    }
}
