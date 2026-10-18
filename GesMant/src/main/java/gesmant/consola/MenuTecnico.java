package gesmant.consola;

import gesmant.estructuras.Algoritmos;
import gesmant.estructuras.ListaEnlazada;
import gesmant.logica.ReglaNegocioException;
import gesmant.modelo.OrdenTrabajo;
import gesmant.modelo.Usuario;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Scanner;

/** Menu del tecnico (CU09, CU12, CU14, CU15). Solo ve sus propias ordenes. */
public class MenuTecnico extends Menu {

    public MenuTecnico(Scanner teclado, Usuario usuario) {
        super(teclado, usuario);
    }

    @Override
    protected String titulo() {
        return "GesMant - Técnico";
    }

    @Override
    protected String[] opciones() {
        return new String[]{
            "Ver mis tareas (más urgentes primero)",
            "Iniciar tarea",
            "Registrar material utilizado",
            "Devolver llaves",
            "Finalizar orden",
            "Ver últimas acciones"
        };
    }

    @Override
    protected void procesar(int opcion) throws SQLException, ReglaNegocioException {
        switch (opcion) {
            case 1:
                ListaEnlazada<OrdenTrabajo> mias = misOrdenes();
                Algoritmos.ordenarPorPrioridad(mias);
                mostrarOrdenes(mias);
                break;
            case 2:
                OrdenTrabajo oi = pedirOrden(misOrdenes());
                gestor.iniciarTarea(oi.getIdOrden(), LocalDateTime.now());
                registrarAccion("Tarea iniciada: " + oi.getCodigo());
                break;
            case 3:
                OrdenTrabajo om = pedirOrden(misOrdenes());
                String nombre = leerTexto("Material: ");
                int cantidad = leerEntero("Cantidad: ");
                double costo = leerDecimal("Costo unitario: ");
                gestor.registrarMaterial(om.getIdOrden(), nombre, cantidad, costo);
                registrarAccion("Material registrado en " + om.getCodigo() + ": " + cantidad + " x " + nombre);
                break;
            case 4:
                OrdenTrabajo ol = pedirOrden(misOrdenes());
                gestor.registrarDevolucionLlaves(ol.getIdOrden(), LocalDateTime.now());
                registrarAccion("Llaves devueltas de " + ol.getCodigo());
                break;
            case 5:
                OrdenTrabajo of = pedirOrden(misOrdenes());
                String trabajo = leerTexto("Trabajo realizado: ");
                gestor.finalizarOrden(of.getIdOrden(), trabajo, LocalDateTime.now());
                registrarAccion("Orden finalizada: " + of.getCodigo());
                break;
            case 6:
                mostrarAcciones();
                break;
            default:
                System.out.println("Opción inválida.");
        }
    }

    private ListaEnlazada<OrdenTrabajo> misOrdenes() throws SQLException {
        return aLista(ordenDAO.listarPorTecnico(usuario.getIdTecnico()));
    }
}
