package gesmant.modelo;

/** Usuario del sistema (administrador o tecnico). */
public class Usuario {

    private int idUsuario;
    private String nombreUsuario;
    private String rol;
    private Integer idTecnico;

    public Usuario(int idUsuario, String nombreUsuario, String rol, Integer idTecnico) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.idTecnico = idTecnico;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getRol() {
        return rol;
    }

    public Integer getIdTecnico() {
        return idTecnico;
    }
}
