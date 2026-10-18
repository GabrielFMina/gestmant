package gesmant.modelo;

/** Persona que ejecuta los trabajos. Hereda nombre y telefono de Persona. */
public class Tecnico extends Persona {

    private int idTecnico;
    private String especialidad;
    private boolean activo;

    public Tecnico(int idTecnico, String nombre, String especialidad, String telefono, boolean activo) {
        super(nombre, telefono);
        this.idTecnico = idTecnico;
        this.especialidad = especialidad;
        this.activo = activo;
    }

    public int getIdTecnico() {
        return idTecnico;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public String getTipo() {
        return "Técnico";
    }

    @Override
    public String getDescripcion() {
        return especialidad + (activo ? "" : " (dado de baja)");
    }
}
