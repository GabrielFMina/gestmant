package gesmant.vista;

import gesmant.datos.OrdenDAO;
import gesmant.logica.GestorOrdenes;
import gesmant.modelo.ItemPresupuesto;
import gesmant.modelo.OrdenTrabajo;
import gesmant.modelo.Usuario;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Pantalla del tecnico. En la version final estas funciones van en la
 * aplicacion movil; en el prototipo se prueban desde esta ventana.
 * Los botones siguen el orden real del trabajo: diagnostico (CU06), presupuesto (CU07),
 * llaves (CU05/CU14), dependencia (CU10), inicio (CU09), materiales y fotos (CU12/CU13)
 * y finalizacion (CU15).
 */
public class TecnicoVista extends JFrame {

    private final Usuario usuario;
    private final GestorOrdenes gestor = new GestorOrdenes();
    private final OrdenDAO ordenDAO = new OrdenDAO();

    private final DefaultTableModel modelo = Util.modeloTabla("Id", "Código", "Consorcio", "Unidad",
            "Problema", "Prioridad", "Estado", "Coordinar con inquilino");
    private final JTable tabla = new JTable(modelo);

    public TecnicoVista(Usuario usuario) {
        super("GesMant - Mis tareas (técnico: " + usuario.getNombreUsuario() + ")");
        this.usuario = usuario;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 460);
        setLocationRelativeTo(null);

        JPanel botones = new JPanel(new GridLayout(3, 5, 5, 5));
        agregarBoton(botones, "Actualizar", () -> cargar());
        agregarBoton(botones, "Ver detalle", () -> verDetalle());
        agregarBoton(botones, "Historial de la unidad", () -> historialUnidad());
        agregarBoton(botones, "Registrar diagnóstico", () -> diagnostico());
        agregarBoton(botones, "Cargar presupuesto", () -> presupuesto());
        agregarBoton(botones, "Entregar llaves", () -> entregarLlaves());
        agregarBoton(botones, "Vincular dependencia", () -> vincular());
        agregarBoton(botones, "Iniciar tarea", () -> iniciar());
        agregarBoton(botones, "Registrar material", () -> material());
        agregarBoton(botones, "Adjuntar foto", () -> foto());
        agregarBoton(botones, "Devolver llaves", () -> devolverLlaves());
        agregarBoton(botones, "Finalizar orden", () -> finalizar());

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Tareas asignadas (las más urgentes primero)"), BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        add(panel);
        Util.achicarColumnaId(tabla);
        cargar();
    }

    private void cargar() {
        try {
            modelo.setRowCount(0);
            for (OrdenTrabajo o : ordenDAO.listarPorTecnico(usuario.getIdTecnico())) {
                modelo.addRow(new Object[]{o.getIdOrden(), o.getCodigo(), o.getNombreConsorcio(),
                        o.getNumeroUnidad(), o.getDescripcionProblema(), o.getPrioridad(), o.getEstado(),
                        o.isRequiereCoordinacion() ? "Sí" : "No (acceso directo)"});
            }
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void verDetalle() {
        int id = Util.idSeleccionado(this, tabla);
        if (id >= 0) {
            new DetalleOrdenVista(this, id).setVisible(true);
        }
    }

    /** CU17: antes de trabajar, el tecnico puede ver que se hizo antes en la unidad. */
    private void historialUnidad() {
        int id = Util.idSeleccionado(this, tabla);
        if (id < 0) {
            return;
        }
        try {
            OrdenTrabajo actual = gestor.obtener(id);
            StringBuilder sb = new StringBuilder("Trabajos anteriores en la unidad " + actual.getNumeroUnidad() + ":\n\n");
            List<OrdenTrabajo> historial = ordenDAO.historialPorUnidad(actual.getIdUnidad());
            for (OrdenTrabajo o : historial) {
                sb.append(o.getCodigo()).append(" (").append(Util.fecha(o.getFechaFin())).append(") - ")
                  .append(Util.texto(o.getTrabajoRealizado())).append(" [").append(Util.texto(o.getNombreTecnico()))
                  .append("]\n");
            }
            if (historial.isEmpty()) {
                sb.append("No hay trabajos terminados registrados.");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Historial", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void diagnostico() {
        int id = Util.idSeleccionado(this, tabla);
        if (id < 0) {
            return;
        }
        String texto = JOptionPane.showInputDialog(this, "Diagnóstico de la visita:");
        if (texto == null) {
            return;
        }
        try {
            gestor.registrarDiagnostico(id, texto);
            Util.ok(this, "Diagnóstico registrado.");
            cargar();
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    /** CU07: se cargan los items uno por uno, separando mano de obra y materiales. */
    private void presupuesto() {
        int id = Util.idSeleccionado(this, tabla);
        if (id < 0) {
            return;
        }
        try {
            JComboBox<String> comboCargo = new JComboBox<>(new String[]{"TECNICO", "ADMINISTRACION"});
            if (!Util.formulario(this, "Nuevo presupuesto", new String[]{"Materiales a cargo de:"},
                    new java.awt.Component[]{comboCargo})) {
                return;
            }
            List<ItemPresupuesto> items = new ArrayList<>();
            boolean seguir = true;
            while (seguir) {
                JTextField txtDesc = new JTextField(20);
                JComboBox<String> comboTipo = new JComboBox<>(new String[]{"MANO_OBRA", "MATERIAL"});
                JTextField txtCant = new JTextField("1");
                JTextField txtPrecio = new JTextField();
                if (!Util.formulario(this, "Ítem " + (items.size() + 1),
                        new String[]{"Descripción:", "Tipo:", "Cantidad:", "Precio unitario:"},
                        new java.awt.Component[]{txtDesc, comboTipo, txtCant, txtPrecio})) {
                    break;
                }
                int cantidad = Integer.parseInt(txtCant.getText().trim());
                double precio = Double.parseDouble(txtPrecio.getText().trim().replace(',', '.'));
                if (txtDesc.getText().trim().isEmpty() || cantidad <= 0 || precio < 0) {
                    Util.ok(this, "Ítem inválido, no se agregó.");
                } else {
                    items.add(new ItemPresupuesto(txtDesc.getText().trim(), (String) comboTipo.getSelectedItem(),
                            cantidad, precio));
                }
                seguir = JOptionPane.showConfirmDialog(this, "¿Agregar otro ítem?", "Presupuesto",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            }
            gestor.cargarPresupuesto(id, (String) comboCargo.getSelectedItem(), items, LocalDateTime.now());
            Util.ok(this, "Presupuesto enviado a la administración para su aprobación.");
            cargar();
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void entregarLlaves() {
        int id = Util.idSeleccionado(this, tabla);
        if (id < 0) {
            return;
        }
        String detalle = JOptionPane.showInputDialog(this, "Detalle del juego de llaves recibido:");
        if (detalle == null) {
            return;
        }
        try {
            gestor.registrarEntregaLlaves(id, detalle, LocalDateTime.now());
            Util.ok(this, "Entrega de llaves registrada.");
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void devolverLlaves() {
        int id = Util.idSeleccionado(this, tabla);
        if (id < 0) {
            return;
        }
        try {
            gestor.registrarDevolucionLlaves(id, LocalDateTime.now());
            Util.ok(this, "Devolución de llaves registrada.");
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void vincular() {
        int id = Util.idSeleccionado(this, tabla);
        if (id < 0) {
            return;
        }
        JTextField txtCodigo = new JTextField("OT-");
        JTextField txtHoras = new JTextField("0");
        if (!Util.formulario(this, "Vincular con la orden de la que depende",
                new String[]{"Código de la orden previa:", "Horas de espera (ej. secado):"},
                new java.awt.Component[]{txtCodigo, txtHoras})) {
            return;
        }
        try {
            gestor.vincularDependencia(id, txtCodigo.getText(), Integer.parseInt(txtHoras.getText().trim()));
            Util.ok(this, "Dependencia registrada. La orden no podrá iniciar hasta que se cumpla.");
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    /**
     * CU09: inicia la tarea seleccionada. Si el presupuesto no esta aprobado o la orden
     * depende de otra que no se cumplio, GestorOrdenes lanza una excepcion y se muestra el motivo.
     */
    private void iniciar() {
        int id = Util.idSeleccionado(this, tabla);
        if (id < 0) {
            return;
        }
        try {
            gestor.iniciarTarea(id, LocalDateTime.now());
            Util.ok(this, "Tarea iniciada.");
            cargar();
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void material() {
        int id = Util.idSeleccionado(this, tabla);
        if (id < 0) {
            return;
        }
        JTextField txtNombre = new JTextField(20);
        JTextField txtCant = new JTextField("1");
        JTextField txtCosto = new JTextField();
        if (!Util.formulario(this, "Material utilizado", new String[]{"Material:", "Cantidad:", "Costo unitario:"},
                new java.awt.Component[]{txtNombre, txtCant, txtCosto})) {
            return;
        }
        try {
            gestor.registrarMaterial(id, txtNombre.getText(), Integer.parseInt(txtCant.getText().trim()),
                    Double.parseDouble(txtCosto.getText().trim().replace(',', '.')));
            Util.ok(this, "Material registrado.");
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    /** En el prototipo se registra la ruta del archivo; la foto queda en la carpeta del servidor. */
    private void foto() {
        int id = Util.idSeleccionado(this, tabla);
        if (id < 0) {
            return;
        }
        JTextField txtRuta = new JTextField("fotos/", 25);
        JComboBox<String> comboEtapa = new JComboBox<>(new String[]{"INICIO", "AVANCE", "FINAL"});
        if (!Util.formulario(this, "Adjuntar evidencia fotográfica", new String[]{"Archivo:", "Etapa:"},
                new java.awt.Component[]{txtRuta, comboEtapa})) {
            return;
        }
        try {
            gestor.adjuntarFoto(id, txtRuta.getText(), (String) comboEtapa.getSelectedItem(), LocalDateTime.now());
            Util.ok(this, "Foto registrada.");
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void finalizar() {
        int id = Util.idSeleccionado(this, tabla);
        if (id < 0) {
            return;
        }
        String trabajo = JOptionPane.showInputDialog(this, "Describa el trabajo realizado:");
        if (trabajo == null) {
            return;
        }
        try {
            gestor.finalizarOrden(id, trabajo, LocalDateTime.now());
            Util.ok(this, "Orden finalizada. La administración debe verificarla para habilitar el pago.");
            cargar();
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void agregarBoton(JPanel panel, String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> accion.run());
        panel.add(b);
    }
}
