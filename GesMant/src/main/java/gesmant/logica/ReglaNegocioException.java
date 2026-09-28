package gesmant.logica;

/** Se lanza cuando una operacion no respeta alguna regla del negocio. */
public class ReglaNegocioException extends Exception {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
