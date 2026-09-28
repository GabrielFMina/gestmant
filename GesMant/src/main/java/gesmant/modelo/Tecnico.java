package gesmant.modelo;

/** Persona que ejecuta los trabajos. */
public class Tecnico {

    private int idTecnico;
    private String nombre;
    private String especialidad;
    private String telefono;
    private boolean activo;

    public Tecnico(int idTecnico, String nombre, String especialidad, String telefono, boolean activo) {
        this.idTecnico = idTecnico;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.activo = activo;
    }

    public int getIdTecnico() {
        return idTecnico;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getTelefono() {
        return telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
