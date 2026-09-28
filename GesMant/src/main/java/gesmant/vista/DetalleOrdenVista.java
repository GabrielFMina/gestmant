package gesmant.vista;

import gesmant.datos.DependenciaDAO;
import gesmant.datos.FotoDAO;
import gesmant.datos.LlavesDAO;
import gesmant.datos.MaterialDAO;
import gesmant.datos.PresupuestoDAO;
import gesmant.logica.GestorOrdenes;
import gesmant.modelo.DependenciaOrden;
import gesmant.modelo.EvidenciaFoto;
import gesmant.modelo.ItemPresupuesto;
import gesmant.modelo.MaterialUtilizado;
import gesmant.modelo.OrdenTrabajo;
import gesmant.modelo.Presupuesto;
import gesmant.modelo.RegistroLlaves;
import java.awt.Font;
import java.awt.Window;
import java.time.LocalDateTime;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Muestra en texto toda la informacion de una orden (trazabilidad). */
public class DetalleOrdenVista extends JDialog {

    public DetalleOrdenVista(Window padre, int idOrden) {
        super(padre, "Detalle de la orden", ModalityType.APPLICATION_MODAL);
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        try {
            area.setText(armarTexto(idOrden));
        } catch (Exception e) {
            area.setText("No se pudo leer la orden: " + e.getMessage());
        }
        area.setCaretPosition(0);
        add(new JScrollPane(area));
        setSize(680, 520);
        setLocationRelativeTo(padre);
    }

    private String armarTexto(int idOrden) throws Exception {
        GestorOrdenes gestor = new GestorOrdenes();
        OrdenTrabajo o = gestor.obtener(idOrden);
        StringBuilder sb = new StringBuilder();
        sb.append("ORDEN ").append(o.getCodigo()).append("   Estado: ").append(o.getEstado()).append("\n");
        sb.append("Consorcio: ").append(o.getNombreConsorcio()).append("   Unidad: ").append(o.getNumeroUnidad()).append("\n");
        sb.append("Problema: ").append(o.getDescripcionProblema()).append("\n");
        sb.append("Prioridad: ").append(o.getPrioridad()).append("   Coordinar con inquilino: ")
          .append(o.isRequiereCoordinacion() ? "Sí" : "No").append("\n");
        sb.append("Técnico: ").append(Util.texto(o.getNombreTecnico())).append("\n");
        sb.append("Creada: ").append(Util.fecha(o.getFechaCreacion())).append("   Inicio: ")
          .append(Util.fecha(o.getFechaInicio())).append("   Fin: ").append(Util.fecha(o.getFechaFin())).append("\n");
        sb.append("Diagnóstico: ").append(Util.texto(o.getDiagnostico())).append("\n");
        sb.append("Trabajo realizado: ").append(Util.texto(o.getTrabajoRealizado())).append("\n");

        sb.append("\n--- PRESUPUESTOS ---\n");
        for (Presupuesto p : new PresupuestoDAO().listarPorOrden(idOrden)) {
            sb.append("#").append(p.getIdPresupuesto()).append(" ").append(p.getEstado())
              .append(" (materiales a cargo de ").append(p.getMaterialesACargoDe()).append(")");
            if (p.getObservacion() != null) {
                sb.append(" - ").append(p.getObservacion());
            }
            sb.append("\n");
            for (ItemPresupuesto i : p.getItems()) {
                sb.append(String.format("   %-10s %-32s %3d x %,12.2f = %,12.2f%n", i.getTipo(),
                        i.getDescripcion(), i.getCantidad(), i.getPrecioUnitario(), i.getSubtotal()));
            }
            sb.append(String.format("   Mano de obra: %,.2f   Materiales: %,.2f   TOTAL: %,.2f%n",
                    p.getTotalPorTipo("MANO_OBRA"), p.getTotalPorTipo("MATERIAL"), p.getTotal()));
        }

        sb.append("\n--- LLAVES ---\n");
        for (RegistroLlaves r : new LlavesDAO().listarPorOrden(idOrden)) {
            sb.append(r.getDetalleJuego()).append(" | entregada: ").append(Util.fecha(r.getFechaEntrega()))
              .append(" | devuelta: ").append(r.getFechaDevolucion() == null ? "PENDIENTE" : Util.fecha(r.getFechaDevolucion()))
              .append("\n");
        }

        sb.append("\n--- MATERIALES UTILIZADOS ---\n");
        for (MaterialUtilizado m : new MaterialDAO().listarPorOrden(idOrden)) {
            sb.append(String.format("%-35s %3d x %,10.2f%n", m.getNombre(), m.getCantidad(), m.getCostoUnitario()));
        }

        sb.append("\n--- EVIDENCIA FOTOGRÁFICA ---\n");
        for (EvidenciaFoto f : new FotoDAO().listarPorOrden(idOrden)) {
            sb.append(f.getEtapa()).append("  ").append(f.getRutaArchivo()).append("  (")
              .append(Util.fecha(f.getFechaSubida())).append(")\n");
        }

        sb.append("\n--- DEPENDENCIAS ---\n");
        for (DependenciaOrden d : new DependenciaDAO().listarPrevias(idOrden)) {
            sb.append("Depende de ").append(d.getCodigoPrevia()).append(" (").append(d.getEstadoPrevia())
              .append("), espera de ").append(d.getTiempoEsperaHoras()).append(" h\n");
        }
        String bloqueo = gestor.motivoBloqueo(idOrden, LocalDateTime.now());
        sb.append(bloqueo == null ? "Sin bloqueos para iniciar.\n" : "BLOQUEADA: " + bloqueo + "\n");
        return sb.toString();
    }
}
