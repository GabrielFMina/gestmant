package gesmant.pruebas;

import gesmant.datos.Conexion;
import gesmant.datos.OrdenDAO;
import gesmant.datos.PresupuestoDAO;
import gesmant.datos.UsuarioDAO;
import gesmant.logica.GestorOrdenes;
import gesmant.logica.ReglaNegocioException;
import gesmant.modelo.ItemPresupuesto;
import gesmant.modelo.OrdenTrabajo;
import gesmant.modelo.Usuario;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pruebas automaticas del prototipo (sin librerias externas).
 * IMPORTANTE: ejecutar sobre una base recien cargada con los scripts 01 y 02,
 * porque las pruebas modifican los datos.
 */
public class PruebasSistema {

    private static int correctas = 0;
    private static int fallidas = 0;

    private static final GestorOrdenes gestor = new GestorOrdenes();
    private static final OrdenDAO ordenDAO = new OrdenDAO();

    /** Una prueba: si no lanza error, se considera correcta. */
    interface Prueba {
        void ejecutar() throws Exception;
    }

    public static void main(String[] args) {
        LocalDateTime hoy = LocalDateTime.of(2026, 9, 28, 10, 0);

        probar("CP01", "Login correcto del administrador", () -> {
            Usuario u = new UsuarioDAO().login("admin", "admin123");
            verificar(u != null && "ADMINISTRADOR".equals(u.getRol()), "debía ingresar como ADMINISTRADOR");
        });
        probar("CP02", "Login con clave incorrecta es rechazado", () -> {
            verificar(new UsuarioDAO().login("gmina", "otra") == null, "no debía permitir el ingreso");
        });
        probar("CP03", "No se registra un reclamo sin descripción", () -> {
            esperarRechazo(() -> gestor.registrarOrden(3, "  ", "MEDIA", true, hoy));
        });
        probar("CP04", "Registrar reclamo genera código y estado REGISTRADA", () -> {
            String codigo = gestor.registrarOrden(3, "Timbre del 1A no funciona", "BAJA", true, hoy);
            OrdenTrabajo o = ordenDAO.buscarPorCodigo(codigo);
            verificar("OT-0009".equals(codigo) && OrdenTrabajo.REGISTRADA.equals(o.getEstado()),
                    "código " + codigo + ", estado " + o.getEstado());
        });
        probar("CP05", "Asignar técnico cambia la orden a ASIGNADA", () -> {
            gestor.asignarTecnico(9, 1);
            verificarEstado(9, OrdenTrabajo.ASIGNADA);
        });
        probar("CP06", "No se presupuesta sin diagnóstico previo", () -> {
            esperarRechazo(() -> gestor.cargarPresupuesto(9, "TECNICO", unItem(), hoy));
        });
        probar("CP07", "Diagnóstico + presupuesto: orden PRESUPUESTADA y total correcto", () -> {
            gestor.registrarDiagnostico(9, "Pulsador roto");
            List<ItemPresupuesto> items = new ArrayList<>();
            items.add(new ItemPresupuesto("Mano de obra electricidad", "MANO_OBRA", 1, 20000));
            items.add(new ItemPresupuesto("Pulsador de timbre", "MATERIAL", 2, 3500));
            gestor.cargarPresupuesto(9, "TECNICO", items, hoy);
            verificarEstado(9, OrdenTrabajo.PRESUPUESTADA);
            double total = new PresupuestoDAO().buscarPendiente(9).getTotal();
            verificar(total == 27000, "total calculado " + total);
        });
        probar("CP08", "No se inicia una tarea sin presupuesto aprobado", () -> {
            esperarRechazo(() -> gestor.iniciarTarea(9, hoy));
        });
        probar("CP09", "Aprobar presupuesto habilita la orden (APROBADA)", () -> {
            gestor.aprobarPresupuesto(9);
            verificarEstado(9, OrdenTrabajo.APROBADA);
        });
        probar("CP10", "Rechazar presupuesto devuelve la orden al técnico", () -> {
            gestor.rechazarPresupuesto(4, "Precio de bisagras alto");
            verificarEstado(4, OrdenTrabajo.ASIGNADA);
        });
        probar("CP11", "Orden bloqueada: la orden previa no terminó (OT-0006)", () -> {
            esperarRechazo(() -> gestor.iniciarTarea(6, hoy));
        });
        probar("CP12", "Orden bloqueada: no pasó el tiempo de secado (OT-0002)", () -> {
            esperarRechazo(() -> gestor.iniciarTarea(2, LocalDateTime.of(2026, 9, 6, 10, 0)));
        });
        probar("CP13", "Orden dependiente inicia cuando se cumple la condición", () -> {
            gestor.iniciarTarea(2, hoy);
            verificarEstado(2, OrdenTrabajo.EN_EJECUCION);
        });
        probar("CP14", "Una orden no puede depender de sí misma", () -> {
            esperarRechazo(() -> gestor.vincularDependencia(9, "OT-0009", 0));
        });
        probar("CP15", "No se finaliza con llaves sin devolver (OT-0003)", () -> {
            gestor.adjuntarFoto(3, "fotos/OT-0003/final_1.jpg", "FINAL", hoy);
            esperarRechazo(() -> gestor.finalizarOrden(3, "Se cambiaron tres equipos", hoy));
        });
        probar("CP16", "Devolver llaves y finalizar la orden", () -> {
            gestor.registrarDevolucionLlaves(3, hoy);
            gestor.finalizarOrden(3, "Se cambiaron tres equipos", hoy);
            verificarEstado(3, OrdenTrabajo.FINALIZADA);
        });
        probar("CP17", "No se finaliza sin foto de la etapa FINAL", () -> {
            gestor.registrarMaterial(2, "Bolsa de revoque grueso", 2, 9500);
            esperarRechazo(() -> gestor.finalizarOrden(2, "Se rehizo el revoque", hoy));
        });
        probar("CP18", "Ampliación de presupuesto pendiente impide finalizar", () -> {
            gestor.cargarPresupuesto(2, "TECNICO", unItem(), hoy);
            gestor.adjuntarFoto(2, "fotos/OT-0002/final_1.jpg", "FINAL", hoy);
            esperarRechazo(() -> gestor.finalizarOrden(2, "Se rehizo el revoque", hoy));
            verificarEstado(2, OrdenTrabajo.EN_EJECUCION);
        });
        probar("CP19", "Verificar trabajo y habilitar pago (OT-0007)", () -> {
            gestor.verificarYHabilitarPago(7);
            verificarEstado(7, OrdenTrabajo.PAGADA);
        });
        probar("CP20", "Rechazar trabajo reabre la orden (OT-0003)", () -> {
            gestor.rechazarTrabajo(3);
            verificarEstado(3, OrdenTrabajo.EN_EJECUCION);
        });
        probar("CP21", "No se registra material en una orden no iniciada", () -> {
            esperarRechazo(() -> gestor.registrarMaterial(9, "Cable", 1, 100));
        });
        probar("CP22", "Historial de la unidad 3B muestra OT-0001", () -> {
            List<OrdenTrabajo> h = ordenDAO.historialPorUnidad(1);
            verificar(h.size() == 1 && "OT-0001".equals(h.get(0).getCodigo()), "historial con " + h.size() + " órdenes");
        });
        probar("CP23", "No se elimina una orden con presupuesto; sí una registrada por error", () -> {
            esperarRechazo(() -> gestor.eliminarOrden(4));
            gestor.eliminarOrden(8);
            verificar(ordenDAO.buscar(8) == null, "OT-0008 debía borrarse");
        });
        probar("CP24", "La base rechaza un ítem con cantidad 0 (CHECK)", () -> {
            esperarErrorBD("INSERT INTO item_presupuesto (id_presupuesto, descripcion, tipo, cantidad, precio_unitario) "
                    + "VALUES (1, 'x', 'MATERIAL', 0, 10)");
        });
        probar("CP25", "La base rechaza un usuario repetido (UNIQUE)", () -> {
            esperarErrorBD("INSERT INTO usuario (nombre_usuario, clave, rol) VALUES ('admin', 'x', 'ADMINISTRADOR')");
        });
        probar("CP26", "La base impide borrar un consorcio con unidades (FOREIGN KEY)", () -> {
            esperarErrorBD("DELETE FROM consorcio WHERE id_consorcio = 1");
        });

        // Caso CPF04 del informe: particion de equivalencia y valores de frontera
        // sobre las horas transcurridas desde el fin de OT-0001 (espera de 72 h).
        LocalDateTime finPrevia = LocalDateTime.of(2026, 9, 4, 17, 0);
        int[] horas = {-1, 0, 1, 71, 72, 73};
        boolean[] debeIniciar = {false, false, false, false, true, true};
        for (int i = 0; i < horas.length; i++) {
            final int h = horas[i];
            final boolean esperado = debeIniciar[i];
            probar("CPF04-" + h, "Frontera: " + h + " h después del fin de la previa", () -> {
                String motivo = gestor.motivoBloqueo(2, finPrevia.plusHours(h));
                verificar((motivo == null) == esperado, "resultado inesperado: " + motivo);
            });
        }

        System.out.println();
        System.out.println("Resultado: " + correctas + " correctas, " + fallidas + " fallidas.");
    }

    // ------------------------------------------------------------ auxiliares
    private static void probar(String codigo, String descripcion, Prueba prueba) {
        try {
            prueba.ejecutar();
            correctas++;
            System.out.println(codigo + "  OK     " + descripcion);
        } catch (Exception e) {
            fallidas++;
            System.out.println(codigo + "  FALLA  " + descripcion + "  -> " + e.getMessage());
        }
    }

    private static void verificar(boolean condicion, String detalle) {
        if (!condicion) {
            throw new AssertionError(detalle);
        }
    }

    private static void verificarEstado(int idOrden, String esperado) throws SQLException {
        String real = ordenDAO.buscar(idOrden).getEstado();
        verificar(esperado.equals(real), "estado esperado " + esperado + ", real " + real);
    }

    /** La operacion tiene que ser rechazada por una regla del negocio. */
    private static void esperarRechazo(Prueba p) throws Exception {
        try {
            p.ejecutar();
        } catch (ReglaNegocioException e) {
            System.out.println("        (rechazada: " + e.getMessage() + ")");
            return;
        }
        throw new AssertionError("la operación debía ser rechazada");
    }

    /** La sentencia tiene que fallar por una restriccion de la base. */
    private static void esperarErrorBD(String sql) throws Exception {
        try (Connection con = Conexion.obtener(); Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("        (error de la base: " + e.getMessage() + ")");
            return;
        }
        throw new AssertionError("la base debía rechazar la sentencia");
    }

    private static List<ItemPresupuesto> unItem() {
        List<ItemPresupuesto> items = new ArrayList<>();
        items.add(new ItemPresupuesto("Bolsa de revoque fino", "MATERIAL", 1, 8000));
        return items;
    }
}
