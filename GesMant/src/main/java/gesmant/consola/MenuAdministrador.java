package gesmant.consola;

import gesmant.datos.TecnicoDAO;
import gesmant.datos.UnidadDAO;
import gesmant.estructuras.Algoritmos;
import gesmant.estructuras.Cola;
import gesmant.estructuras.ListaEnlazada;
import gesmant.logica.ReglaNegocioException;
import gesmant.modelo.Inquilino;
import gesmant.modelo.OrdenTrabajo;
import gesmant.modelo.Persona;
import gesmant.modelo.Tecnico;
import gesmant.modelo.UnidadFuncional;
import gesmant.modelo.Usuario;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/** Menu del administrador (CU02, CU03, CU08, CU16, CU18). */
public class MenuAdministrador extends Menu {

    /** Reclamos registrados que esperan tecnico, en orden de llegada. */
    private final Cola<OrdenTrabajo> pendientes = new Cola<>();

    public MenuAdministrador(Scanner teclado, Usuario usuario) throws SQLException {
        super(teclado, usuario);
        cargarPendientes();
    }

    @Override
    protected String titulo() {
        return "GesMant - Administración";
    }

    @Override
    protected String[] opciones() {
        return new String[]{
            "Listar órdenes por prioridad",
            "Buscar orden por código",
            "Listar órdenes por estado",
            "Registrar reclamo",
            "Asignar el próximo reclamo pendiente (" + pendientes.tamanio() + " en espera)",
            "Aprobar presupuesto",
            "Verificar trabajo y habilitar pago",
            "Ver contactos (técnicos e inquilinos)",
            "Ver últimas acciones"
        };
    }

    @Override
    protected void procesar(int opcion) throws SQLException, ReglaNegocioException {
        switch (opcion) {
            case 1:
                ListaEnlazada<OrdenTrabajo> todas = aLista(ordenDAO.listar());
                Algoritmos.ordenarPorPrioridad(todas);
                mostrarOrdenes(todas);
                break;
            case 2:
                mostrarDetalle(pedirOrden(aLista(ordenDAO.listar())));
                break;
            case 3:
                String estado = leerTexto("Estado (REGISTRADA, ASIGNADA, PRESUPUESTADA, APROBADA, EN_EJECUCION, FINALIZADA, PAGADA): ");
                mostrarOrdenes(Algoritmos.buscarPorEstado(aLista(ordenDAO.listar()), estado));
                break;
            case 4:
                registrarReclamo();
                break;
            case 5:
                asignarPendiente();
                break;
            case 6:
                OrdenTrabajo op = pedirOrden(aLista(ordenDAO.listar()));
                gestor.aprobarPresupuesto(op.getIdOrden());
                registrarAccion("Presupuesto aprobado en " + op.getCodigo());
                break;
            case 7:
                OrdenTrabajo of = pedirOrden(aLista(ordenDAO.listar()));
                gestor.verificarYHabilitarPago(of.getIdOrden());
                registrarAccion("Pago habilitado en " + of.getCodigo());
                break;
            case 8:
                mostrarContactos();
                break;
            case 9:
                mostrarAcciones();
                break;
            default:
                System.out.println("Opción inválida.");
        }
    }

    private void cargarPendientes() throws SQLException {
        // las ordenes vienen ordenadas por codigo, que es el orden en que se registraron
        for (OrdenTrabajo o : ordenDAO.listar()) {
            if (OrdenTrabajo.REGISTRADA.equals(o.getEstado())) {
                pendientes.encolar(o);
            }
        }
    }

    private void registrarReclamo() throws SQLException, ReglaNegocioException {
        List<UnidadFuncional> unidades = new UnidadDAO().listar();
        for (int i = 0; i < unidades.size(); i++) {
            System.out.println((i + 1) + ") " + unidades.get(i));
        }
        int n = leerEntero("Número de unidad: ");
        if (n < 1 || n > unidades.size()) {
            throw new ReglaNegocioException("La unidad elegida no existe.");
        }
        String descripcion = leerTexto("Problema: ");
        String prioridad = leerTexto("Prioridad (ALTA, MEDIA, BAJA): ").toUpperCase();
        if (!prioridad.equals("ALTA") && !prioridad.equals("MEDIA") && !prioridad.equals("BAJA")) {
            throw new ReglaNegocioException("La prioridad debe ser ALTA, MEDIA o BAJA.");
        }
        String coordinar = leerTexto("¿Hay que coordinar con el inquilino? (S/N): ");
        String codigo = gestor.registrarOrden(unidades.get(n - 1).getIdUnidad(), descripcion, prioridad,
                coordinar.equalsIgnoreCase("S"), LocalDateTime.now());
        pendientes.encolar(ordenDAO.buscarPorCodigo(codigo));
        registrarAccion("Reclamo registrado: " + codigo + " (queda en la cola de pendientes)");
    }

    private void asignarPendiente() throws SQLException, ReglaNegocioException {
        if (pendientes.estaVacia()) {
            System.out.println("No hay reclamos pendientes de asignar.");
            return;
        }
        OrdenTrabajo o = pendientes.verFrente();
        System.out.println("Próximo reclamo: " + o.getCodigo() + " - " + o.getNumeroUnidad() + " - " + o.getDescripcionProblema());
        List<Tecnico> tecnicos = new TecnicoDAO().listar(true);
        for (int i = 0; i < tecnicos.size(); i++) {
            Tecnico t = tecnicos.get(i);
            System.out.println((i + 1) + ") " + t.getNombre() + " - " + t.getDescripcion());
        }
        int n = leerEntero("Técnico: ");
        if (n < 1 || n > tecnicos.size()) {
            throw new ReglaNegocioException("El técnico elegido no existe.");
        }
        gestor.asignarTecnico(o.getIdOrden(), tecnicos.get(n - 1).getIdTecnico());
        pendientes.desencolar();
        registrarAccion(o.getCodigo() + " asignada a " + tecnicos.get(n - 1).getNombre());
    }

    /**
     * Polimorfismo: la lista guarda objetos Persona, pero cada uno es un Tecnico o un Inquilino
     * y responde getTipo() y getDescripcion() a su manera.
     */
    private void mostrarContactos() throws SQLException {
        ListaEnlazada<Persona> contactos = new ListaEnlazada<>();
        for (Tecnico t : new TecnicoDAO().listar(false)) {
            contactos.agregar(t);
        }
        for (UnidadFuncional u : new UnidadDAO().listar()) {
            if (!u.isEspacioComun() && u.getNombreInquilino() != null) {
                contactos.agregar(new Inquilino(u.getNombreInquilino(), u.getTelefonoInquilino(), u.toString()));
            }
        }
        System.out.printf("%-10s %-16s %-14s %s%n", "Tipo", "Nombre", "Teléfono", "Detalle");
        for (int i = 0; i < contactos.tamanio(); i++) {
            Persona p = contactos.obtener(i);
            System.out.printf("%-10s %-16s %-14s %s%n", p.getTipo(), p.getNombre(), p.getTelefono(), p.getDescripcion());
        }
    }

    private void mostrarDetalle(OrdenTrabajo o) {
        System.out.println(o.getCodigo() + " | " + o.getNombreConsorcio() + " - " + o.getNumeroUnidad());
        System.out.println("Problema: " + o.getDescripcionProblema());
        System.out.println("Prioridad: " + o.getPrioridad() + " | Estado: " + o.getEstado()
                + " | Técnico: " + (o.getNombreTecnico() == null ? "-" : o.getNombreTecnico()));
    }
}
