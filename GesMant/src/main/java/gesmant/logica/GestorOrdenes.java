package gesmant.logica;

import gesmant.datos.DependenciaDAO;
import gesmant.datos.FotoDAO;
import gesmant.datos.LlavesDAO;
import gesmant.datos.MaterialDAO;
import gesmant.datos.OrdenDAO;
import gesmant.datos.PresupuestoDAO;
import gesmant.modelo.DependenciaOrden;
import gesmant.modelo.ItemPresupuesto;
import gesmant.modelo.MaterialUtilizado;
import gesmant.modelo.OrdenTrabajo;
import gesmant.modelo.Presupuesto;
import gesmant.modelo.RegistroLlaves;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase de control: concentra las reglas del negocio del ciclo de vida de una orden.
 * Las vistas nunca modifican la base directamente, siempre pasan por aca.
 */
public class GestorOrdenes {

    private final OrdenDAO ordenDAO = new OrdenDAO();
    private final PresupuestoDAO presupuestoDAO = new PresupuestoDAO();
    private final LlavesDAO llavesDAO = new LlavesDAO();
    private final MaterialDAO materialDAO = new MaterialDAO();
    private final FotoDAO fotoDAO = new FotoDAO();
    private final DependenciaDAO dependenciaDAO = new DependenciaDAO();

    // ---------------------------------------------------------------- CU02
    public String registrarOrden(int idUnidad, String descripcion, String prioridad,
                                 boolean requiereCoordinacion, LocalDateTime ahora)
            throws SQLException, ReglaNegocioException {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new ReglaNegocioException("Debe describir el problema.");
        }
        OrdenTrabajo o = new OrdenTrabajo();
        o.setCodigo(ordenDAO.siguienteCodigo());
        o.setIdUnidad(idUnidad);
        o.setDescripcionProblema(descripcion.trim());
        o.setPrioridad(prioridad);
        o.setEstado(OrdenTrabajo.REGISTRADA);
        o.setRequiereCoordinacion(requiereCoordinacion);
        o.setFechaCreacion(ahora);
        ordenDAO.insertar(o);
        return o.getCodigo();
    }

    // ---------------------------------------------------------------- CU03
    public void asignarTecnico(int idOrden, int idTecnico) throws SQLException, ReglaNegocioException {
        OrdenTrabajo o = obtener(idOrden);
        exigirEstado(o, OrdenTrabajo.REGISTRADA, OrdenTrabajo.ASIGNADA);
        ordenDAO.asignarTecnico(idOrden, idTecnico, OrdenTrabajo.ASIGNADA);
    }

    // ---------------------------------------------------------------- CU06
    public void registrarDiagnostico(int idOrden, String diagnostico) throws SQLException, ReglaNegocioException {
        OrdenTrabajo o = obtener(idOrden);
        exigirEstado(o, OrdenTrabajo.ASIGNADA);
        if (diagnostico == null || diagnostico.trim().isEmpty()) {
            throw new ReglaNegocioException("El diagnóstico no puede quedar vacío.");
        }
        ordenDAO.registrarDiagnostico(idOrden, diagnostico.trim());
    }

    // ---------------------------------------------------------------- CU07
    /**
     * Carga un presupuesto. Si la orden esta ASIGNADA es el presupuesto original.
     * Si ya esta EN_EJECUCION es una ampliacion (falto un material no previsto).
     */
    public void cargarPresupuesto(int idOrden, String materialesACargoDe, List<ItemPresupuesto> items,
                                  LocalDateTime ahora) throws SQLException, ReglaNegocioException {
        OrdenTrabajo o = obtener(idOrden);
        exigirEstado(o, OrdenTrabajo.ASIGNADA, OrdenTrabajo.EN_EJECUCION);
        if (o.getDiagnostico() == null) {
            throw new ReglaNegocioException("Primero hay que registrar el diagnóstico de la visita.");
        }
        if (items == null || items.isEmpty()) {
            throw new ReglaNegocioException("El presupuesto debe tener al menos un ítem.");
        }
        if (presupuestoDAO.buscarPendiente(idOrden) != null) {
            throw new ReglaNegocioException("Ya hay un presupuesto pendiente de aprobación.");
        }
        String obs = OrdenTrabajo.EN_EJECUCION.equals(o.getEstado()) ? "Ampliación de presupuesto" : null;
        presupuestoDAO.insertar(idOrden, materialesACargoDe, obs, items, ahora);
        if (OrdenTrabajo.ASIGNADA.equals(o.getEstado())) {
            ordenDAO.cambiarEstado(idOrden, OrdenTrabajo.PRESUPUESTADA);
        }
    }

    // ---------------------------------------------------------------- CU08
    public void aprobarPresupuesto(int idOrden) throws SQLException, ReglaNegocioException {
        OrdenTrabajo o = obtener(idOrden);
        Presupuesto p = exigirPresupuestoPendiente(idOrden);
        presupuestoDAO.cambiarEstado(p.getIdPresupuesto(), "APROBADO", p.getObservacion());
        if (OrdenTrabajo.PRESUPUESTADA.equals(o.getEstado())) {
            ordenDAO.cambiarEstado(idOrden, OrdenTrabajo.APROBADA);
        }
    }

    public void rechazarPresupuesto(int idOrden, String motivo) throws SQLException, ReglaNegocioException {
        OrdenTrabajo o = obtener(idOrden);
        Presupuesto p = exigirPresupuestoPendiente(idOrden);
        presupuestoDAO.cambiarEstado(p.getIdPresupuesto(), "RECHAZADO", motivo);
        if (OrdenTrabajo.PRESUPUESTADA.equals(o.getEstado())) {
            // vuelve al tecnico para que arme un presupuesto nuevo
            ordenDAO.cambiarEstado(idOrden, OrdenTrabajo.ASIGNADA);
        }
    }

    // ---------------------------------------------------------- CU05 / CU14
    public void registrarEntregaLlaves(int idOrden, String detalle, LocalDateTime ahora)
            throws SQLException, ReglaNegocioException {
        OrdenTrabajo o = obtener(idOrden);
        if (o.getIdTecnico() == null || o.estaCerrada()) {
            throw new ReglaNegocioException("Solo se entregan llaves para órdenes asignadas y abiertas.");
        }
        if (detalle == null || detalle.trim().isEmpty()) {
            throw new ReglaNegocioException("Hay que detallar qué juego de llaves se entrega.");
        }
        llavesDAO.registrarEntrega(idOrden, detalle.trim(), ahora);
    }

    public void registrarDevolucionLlaves(int idOrden, LocalDateTime ahora)
            throws SQLException, ReglaNegocioException {
        List<RegistroLlaves> pendientes = llavesDAO.pendientesDeDevolucion(idOrden);
        if (pendientes.isEmpty()) {
            throw new ReglaNegocioException("La orden no tiene llaves pendientes de devolución.");
        }
        for (RegistroLlaves r : pendientes) {
            llavesDAO.registrarDevolucion(r.getIdRegistro(), ahora);
        }
    }

    // ---------------------------------------------------------------- CU10
    public void vincularDependencia(int idOrden, String codigoPrevia, int horasEspera)
            throws SQLException, ReglaNegocioException {
        OrdenTrabajo o = obtener(idOrden);
        if (OrdenTrabajo.EN_EJECUCION.equals(o.getEstado()) || o.estaCerrada()) {
            throw new ReglaNegocioException("Solo se pueden vincular órdenes que todavía no empezaron.");
        }
        OrdenTrabajo previa = ordenDAO.buscarPorCodigo(codigoPrevia);
        if (previa == null) {
            throw new ReglaNegocioException("No existe la orden " + codigoPrevia + ".");
        }
        if (previa.getIdOrden() == idOrden) {
            throw new ReglaNegocioException("Una orden no puede depender de sí misma.");
        }
        if (horasEspera < 0) {
            throw new ReglaNegocioException("El tiempo de espera no puede ser negativo.");
        }
        for (DependenciaOrden d : dependenciaDAO.listarPrevias(idOrden)) {
            if (d.getIdOrdenPrevia() == previa.getIdOrden()) {
                throw new ReglaNegocioException("Esa dependencia ya estaba registrada.");
            }
        }
        dependenciaDAO.insertar(idOrden, previa.getIdOrden(), horasEspera);
    }

    /**
     * Devuelve el motivo por el que la orden no puede iniciar por sus dependencias,
     * o null si todas las ordenes previas estan cumplidas.
     */
    public String motivoBloqueo(int idOrden, LocalDateTime ahora) throws SQLException {
        for (DependenciaOrden d : dependenciaDAO.listarPrevias(idOrden)) {
            boolean previaTerminada = OrdenTrabajo.FINALIZADA.equals(d.getEstadoPrevia())
                    || OrdenTrabajo.PAGADA.equals(d.getEstadoPrevia());
            if (!previaTerminada) {
                return "Depende de " + d.getCodigoPrevia() + ", que todavía no terminó.";
            }
            LocalDateTime habilitada = d.getFinPrevia().plusHours(d.getTiempoEsperaHoras());
            if (ahora.isBefore(habilitada)) {
                return "Depende de " + d.getCodigoPrevia() + ": hay que esperar hasta " + habilitada
                        .toString().replace('T', ' ') + " (" + d.getTiempoEsperaHoras() + " h de espera).";
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- CU09
    /**
     * Inicia la tarea: exige presupuesto aprobado (estado APROBADA) y dependencias cumplidas.
     * Si todo esta bien registra la fecha de inicio y pasa la orden a EN_EJECUCION.
     */
    public void iniciarTarea(int idOrden, LocalDateTime ahora) throws SQLException, ReglaNegocioException {
        OrdenTrabajo o = obtener(idOrden);
        if (!OrdenTrabajo.APROBADA.equals(o.getEstado())) {
            throw new ReglaNegocioException("La tarea solo puede iniciar con el presupuesto aprobado "
                    + "(estado actual: " + o.getEstado() + ").");
        }
        String bloqueo = motivoBloqueo(idOrden, ahora);
        if (bloqueo != null) {
            throw new ReglaNegocioException(bloqueo);
        }
        ordenDAO.registrarInicio(idOrden, ahora);
    }

    // ---------------------------------------------------------------- CU12
    public void registrarMaterial(int idOrden, String nombre, int cantidad, double costo)
            throws SQLException, ReglaNegocioException {
        exigirEstado(obtener(idOrden), OrdenTrabajo.EN_EJECUCION);
        if (nombre == null || nombre.trim().isEmpty() || cantidad <= 0 || costo < 0) {
            throw new ReglaNegocioException("Datos del material incompletos o inválidos.");
        }
        materialDAO.insertar(idOrden, new MaterialUtilizado(nombre.trim(), cantidad, costo));
    }

    // ---------------------------------------------------------------- CU13
    public void adjuntarFoto(int idOrden, String ruta, String etapa, LocalDateTime ahora)
            throws SQLException, ReglaNegocioException {
        exigirEstado(obtener(idOrden), OrdenTrabajo.EN_EJECUCION);
        if (ruta == null || ruta.trim().isEmpty()) {
            throw new ReglaNegocioException("Debe indicar el archivo de la foto.");
        }
        fotoDAO.insertar(idOrden, ruta.trim(), etapa, ahora);
    }

    // ---------------------------------------------------------- CU11 / CU15
    public void finalizarOrden(int idOrden, String trabajoRealizado, LocalDateTime ahora)
            throws SQLException, ReglaNegocioException {
        exigirEstado(obtener(idOrden), OrdenTrabajo.EN_EJECUCION);
        if (trabajoRealizado == null || trabajoRealizado.trim().isEmpty()) {
            throw new ReglaNegocioException("Hay que describir el trabajo realizado.");
        }
        if (!llavesDAO.pendientesDeDevolucion(idOrden).isEmpty()) {
            throw new ReglaNegocioException("No se puede finalizar: hay llaves sin devolver.");
        }
        if (presupuestoDAO.buscarPendiente(idOrden) != null) {
            throw new ReglaNegocioException("No se puede finalizar: hay una ampliación de presupuesto sin aprobar.");
        }
        if (fotoDAO.contarPorEtapa(idOrden, "FINAL") == 0) {
            throw new ReglaNegocioException("No se puede finalizar: falta al menos una foto de la etapa FINAL.");
        }
        ordenDAO.registrarFin(idOrden, ahora, trabajoRealizado.trim());
    }

    // ---------------------------------------------------------------- CU16
    public void verificarYHabilitarPago(int idOrden) throws SQLException, ReglaNegocioException {
        exigirEstado(obtener(idOrden), OrdenTrabajo.FINALIZADA);
        ordenDAO.cambiarEstado(idOrden, OrdenTrabajo.PAGADA);
    }

    /** El trabajo no quedo bien: la orden se reabre y no se habilita el pago. */
    public void rechazarTrabajo(int idOrden) throws SQLException, ReglaNegocioException {
        exigirEstado(obtener(idOrden), OrdenTrabajo.FINALIZADA);
        ordenDAO.reabrir(idOrden);
    }

    /** Solo se borran ordenes cargadas por error, antes de que tengan presupuesto o fotos. */
    public void eliminarOrden(int idOrden) throws SQLException, ReglaNegocioException {
        OrdenTrabajo o = obtener(idOrden);
        exigirEstado(o, OrdenTrabajo.REGISTRADA, OrdenTrabajo.ASIGNADA);
        if (!presupuestoDAO.listarPorOrden(idOrden).isEmpty()) {
            throw new ReglaNegocioException("La orden ya tiene presupuestos y no puede eliminarse.");
        }
        ordenDAO.eliminar(idOrden);
    }

    // ------------------------------------------------------------ auxiliares
    public OrdenTrabajo obtener(int idOrden) throws SQLException, ReglaNegocioException {
        OrdenTrabajo o = ordenDAO.buscar(idOrden);
        if (o == null) {
            throw new ReglaNegocioException("La orden no existe.");
        }
        return o;
    }

    private void exigirEstado(OrdenTrabajo o, String... permitidos) throws ReglaNegocioException {
        for (String e : permitidos) {
            if (e.equals(o.getEstado())) {
                return;
            }
        }
        throw new ReglaNegocioException("Operación no permitida: la orden " + o.getCodigo()
                + " está en estado " + o.getEstado() + ".");
    }

    private Presupuesto exigirPresupuestoPendiente(int idOrden) throws SQLException, ReglaNegocioException {
        Presupuesto p = presupuestoDAO.buscarPendiente(idOrden);
        if (p == null) {
            throw new ReglaNegocioException("La orden no tiene presupuestos pendientes de aprobación.");
        }
        return p;
    }
}
