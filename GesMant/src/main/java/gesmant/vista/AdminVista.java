package gesmant.vista;

import gesmant.datos.ConsorcioDAO;
import gesmant.datos.OrdenDAO;
import gesmant.datos.TecnicoDAO;
import gesmant.datos.UnidadDAO;
import gesmant.datos.UsuarioDAO;
import gesmant.logica.GestorOrdenes;
import gesmant.modelo.Consorcio;
import gesmant.modelo.OrdenTrabajo;
import gesmant.modelo.Tecnico;
import gesmant.modelo.UnidadFuncional;
import gesmant.modelo.Usuario;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Modulo de administracion (se usa desde la PC de la oficina).
 * Pestañas: ordenes de trabajo (CU02, CU03, CU08, CU16), consorcios y unidades (CU18),
 * tecnicos (CU18) e historial (CU17). Las acciones sobre ordenes pasan por GestorOrdenes.
 */
public class AdminVista extends JFrame {

    private final GestorOrdenes gestor = new GestorOrdenes();
    private final OrdenDAO ordenDAO = new OrdenDAO();
    private final ConsorcioDAO consorcioDAO = new ConsorcioDAO();
    private final UnidadDAO unidadDAO = new UnidadDAO();
    private final TecnicoDAO tecnicoDAO = new TecnicoDAO();

    private final DefaultTableModel modeloOrdenes =
            Util.modeloTabla("Id", "Código", "Consorcio", "Unidad", "Problema", "Prioridad", "Estado", "Técnico");
    private final JTable tablaOrdenes = new JTable(modeloOrdenes);

    private final DefaultTableModel modeloConsorcios = Util.modeloTabla("Id", "Nombre", "Dirección");
    private final JTable tablaConsorcios = new JTable(modeloConsorcios);
    private final DefaultTableModel modeloUnidades =
            Util.modeloTabla("Id", "Consorcio", "Unidad", "Piso", "Espacio común", "Inquilino", "Teléfono");
    private final JTable tablaUnidades = new JTable(modeloUnidades);

    private final DefaultTableModel modeloTecnicos =
            Util.modeloTabla("Id", "Nombre", "Especialidad", "Teléfono", "Activo");
    private final JTable tablaTecnicos = new JTable(modeloTecnicos);

    private final JComboBox<UnidadFuncional> comboHistorial = new JComboBox<>();
    private final DefaultTableModel modeloHistorial =
            Util.modeloTabla("Id", "Código", "Fin", "Técnico", "Diagnóstico", "Trabajo realizado", "Estado");
    private final JTable tablaHistorial = new JTable(modeloHistorial);

    public AdminVista(Usuario usuario) {
        super("GesMant - Administración (usuario: " + usuario.getNombreUsuario() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 480);
        setLocationRelativeTo(null);

        JTabbedPane pestanias = new JTabbedPane();
        pestanias.addTab("Órdenes de trabajo", crearPanelOrdenes());
        pestanias.addTab("Consorcios y unidades", crearPanelConsorcios());
        pestanias.addTab("Técnicos", crearPanelTecnicos());
        pestanias.addTab("Historial", crearPanelHistorial());
        add(pestanias);
        Util.achicarColumnaId(tablaOrdenes);
        Util.achicarColumnaId(tablaConsorcios);
        Util.achicarColumnaId(tablaUnidades);
        Util.achicarColumnaId(tablaTecnicos);
        Util.achicarColumnaId(tablaHistorial);

        cargarOrdenes();
        cargarConsorciosYUnidades();
        cargarTecnicos();
    }

    // ================================================================ ORDENES
    private JPanel crearPanelOrdenes() {
        JPanel botones = new JPanel(new GridLayout(2, 5, 5, 5));
        agregarBoton(botones, "Actualizar", () -> cargarOrdenes());
        agregarBoton(botones, "Nueva orden", () -> nuevaOrden());
        agregarBoton(botones, "Asignar técnico", () -> asignarTecnico());
        agregarBoton(botones, "Ver detalle", () -> verDetalle());
        agregarBoton(botones, "Eliminar orden", () -> eliminarOrden());
        agregarBoton(botones, "Aprobar presupuesto", () -> aprobarPresupuesto());
        agregarBoton(botones, "Rechazar presupuesto", () -> rechazarPresupuesto());
        agregarBoton(botones, "Verificar y habilitar pago", () -> habilitarPago());
        agregarBoton(botones, "Rechazar trabajo", () -> rechazarTrabajo());

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JScrollPane(tablaOrdenes), BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarOrdenes() {
        try {
            modeloOrdenes.setRowCount(0);
            for (OrdenTrabajo o : ordenDAO.listar()) {
                modeloOrdenes.addRow(new Object[]{o.getIdOrden(), o.getCodigo(), o.getNombreConsorcio(),
                        o.getNumeroUnidad(), o.getDescripcionProblema(), o.getPrioridad(), o.getEstado(),
                        Util.texto(o.getNombreTecnico())});
            }
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void nuevaOrden() {
        try {
            JComboBox<UnidadFuncional> comboUnidad = new JComboBox<>();
            for (UnidadFuncional u : unidadDAO.listar()) {
                comboUnidad.addItem(u);
            }
            JTextField txtDescripcion = new JTextField(25);
            JComboBox<String> comboPrioridad = new JComboBox<>(new String[]{"ALTA", "MEDIA", "BAJA"});
            JCheckBox chkCoordinacion = new JCheckBox("Sí (hay que coordinar con el inquilino)");
            boolean ok = Util.formulario(this, "Registrar reclamo",
                    new String[]{"Unidad:", "Problema:", "Prioridad:", "Requiere coordinación:"},
                    new java.awt.Component[]{comboUnidad, txtDescripcion, comboPrioridad, chkCoordinacion});
            if (!ok) {
                return;
            }
            UnidadFuncional u = (UnidadFuncional) comboUnidad.getSelectedItem();
            String codigo = gestor.registrarOrden(u.getIdUnidad(), txtDescripcion.getText(),
                    (String) comboPrioridad.getSelectedItem(), chkCoordinacion.isSelected(), LocalDateTime.now());
            Util.ok(this, "Se registró la orden " + codigo + ".");
            cargarOrdenes();
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void asignarTecnico() {
        int id = Util.idSeleccionado(this, tablaOrdenes);
        if (id < 0) {
            return;
        }
        try {
            List<Tecnico> tecnicos = tecnicoDAO.listar(true);
            Tecnico t = (Tecnico) JOptionPane.showInputDialog(this, "Elija el técnico:", "Asignar orden",
                    JOptionPane.PLAIN_MESSAGE, null, tecnicos.toArray(), null);
            if (t != null) {
                gestor.asignarTecnico(id, t.getIdTecnico());
                Util.ok(this, "Orden asignada a " + t.getNombre() + ".");
                cargarOrdenes();
            }
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void verDetalle() {
        int id = Util.idSeleccionado(this, tablaOrdenes);
        if (id >= 0) {
            new DetalleOrdenVista(this, id).setVisible(true);
        }
    }

    private void eliminarOrden() {
        int id = Util.idSeleccionado(this, tablaOrdenes);
        if (id < 0) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "¿Eliminar la orden seleccionada?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            try {
                gestor.eliminarOrden(id);
                cargarOrdenes();
            } catch (Exception e) {
                Util.error(this, e);
            }
        }
    }

    private void aprobarPresupuesto() {
        int id = Util.idSeleccionado(this, tablaOrdenes);
        if (id < 0) {
            return;
        }
        try {
            gestor.aprobarPresupuesto(id);
            Util.ok(this, "Presupuesto aprobado.");
            cargarOrdenes();
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void rechazarPresupuesto() {
        int id = Util.idSeleccionado(this, tablaOrdenes);
        if (id < 0) {
            return;
        }
        String motivo = JOptionPane.showInputDialog(this, "Motivo del rechazo:");
        if (motivo == null) {
            return;
        }
        try {
            gestor.rechazarPresupuesto(id, motivo);
            Util.ok(this, "Presupuesto rechazado. La orden vuelve al técnico.");
            cargarOrdenes();
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void habilitarPago() {
        int id = Util.idSeleccionado(this, tablaOrdenes);
        if (id < 0) {
            return;
        }
        try {
            gestor.verificarYHabilitarPago(id);
            Util.ok(this, "Trabajo verificado. Pago habilitado.");
            cargarOrdenes();
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void rechazarTrabajo() {
        int id = Util.idSeleccionado(this, tablaOrdenes);
        if (id < 0) {
            return;
        }
        try {
            gestor.rechazarTrabajo(id);
            Util.ok(this, "Trabajo rechazado. La orden vuelve a ejecución y no se habilita el pago.");
            cargarOrdenes();
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    // ================================================== CONSORCIOS Y UNIDADES
    private JPanel crearPanelConsorcios() {
        JPanel tablas = new JPanel(new GridLayout(2, 1, 5, 5));
        tablas.add(new JScrollPane(tablaConsorcios));
        tablas.add(new JScrollPane(tablaUnidades));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        agregarBoton(botones, "Nuevo consorcio", () -> nuevoConsorcio());
        agregarBoton(botones, "Eliminar consorcio", () -> eliminarConsorcio());
        agregarBoton(botones, "Nueva unidad", () -> nuevaUnidad());
        agregarBoton(botones, "Eliminar unidad", () -> eliminarUnidad());

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(tablas, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarConsorciosYUnidades() {
        try {
            modeloConsorcios.setRowCount(0);
            for (Consorcio c : consorcioDAO.listar()) {
                modeloConsorcios.addRow(new Object[]{c.getIdConsorcio(), c.getNombre(), c.getDireccion()});
            }
            modeloUnidades.setRowCount(0);
            comboHistorial.removeAllItems();
            for (UnidadFuncional u : unidadDAO.listar()) {
                modeloUnidades.addRow(new Object[]{u.getIdUnidad(), u.getNombreConsorcio(), u.getNumero(),
                        Util.texto(u.getPiso()), u.isEspacioComun() ? "Sí" : "No",
                        Util.texto(u.getNombreInquilino()), Util.texto(u.getTelefonoInquilino())});
                comboHistorial.addItem(u);
            }
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void nuevoConsorcio() {
        JTextField txtNombre = new JTextField(20);
        JTextField txtDireccion = new JTextField(20);
        if (!Util.formulario(this, "Nuevo consorcio", new String[]{"Nombre:", "Dirección:"},
                new java.awt.Component[]{txtNombre, txtDireccion})) {
            return;
        }
        try {
            if (txtNombre.getText().trim().isEmpty() || txtDireccion.getText().trim().isEmpty()) {
                Util.ok(this, "Complete nombre y dirección.");
                return;
            }
            consorcioDAO.insertar(txtNombre.getText().trim(), txtDireccion.getText().trim());
            cargarConsorciosYUnidades();
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void eliminarConsorcio() {
        int id = Util.idSeleccionado(this, tablaConsorcios);
        if (id < 0) {
            return;
        }
        try {
            consorcioDAO.eliminar(id);
            cargarConsorciosYUnidades();
        } catch (Exception e) {
            Util.ok(this, "No se puede eliminar: el consorcio tiene unidades u órdenes asociadas.");
        }
    }

    private void nuevaUnidad() {
        try {
            JComboBox<Consorcio> comboConsorcio = new JComboBox<>();
            for (Consorcio c : consorcioDAO.listar()) {
                comboConsorcio.addItem(c);
            }
            JTextField txtNumero = new JTextField(8);
            JTextField txtPiso = new JTextField(5);
            JCheckBox chkComun = new JCheckBox("Es espacio común");
            JTextField txtInquilino = new JTextField(15);
            JTextField txtTelefono = new JTextField(15);
            if (!Util.formulario(this, "Nueva unidad funcional",
                    new String[]{"Consorcio:", "Número / nombre:", "Piso:", "", "Inquilino:", "Teléfono:"},
                    new java.awt.Component[]{comboConsorcio, txtNumero, txtPiso, chkComun, txtInquilino, txtTelefono})) {
                return;
            }
            if (txtNumero.getText().trim().isEmpty()) {
                Util.ok(this, "El número de unidad es obligatorio.");
                return;
            }
            Consorcio c = (Consorcio) comboConsorcio.getSelectedItem();
            unidadDAO.insertar(c.getIdConsorcio(), txtNumero.getText().trim(), txtPiso.getText().trim(),
                    chkComun.isSelected(), vacioANull(txtInquilino.getText()), vacioANull(txtTelefono.getText()));
            cargarConsorciosYUnidades();
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void eliminarUnidad() {
        int id = Util.idSeleccionado(this, tablaUnidades);
        if (id < 0) {
            return;
        }
        try {
            unidadDAO.eliminar(id);
            cargarConsorciosYUnidades();
        } catch (Exception e) {
            Util.ok(this, "No se puede eliminar: la unidad tiene órdenes de trabajo registradas.");
        }
    }

    // ================================================================ TECNICOS
    private JPanel crearPanelTecnicos() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        agregarBoton(botones, "Nuevo técnico", () -> nuevoTecnico());
        agregarBoton(botones, "Dar de baja", () -> bajaTecnico());

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JScrollPane(tablaTecnicos), BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarTecnicos() {
        try {
            modeloTecnicos.setRowCount(0);
            for (Tecnico t : tecnicoDAO.listar(false)) {
                modeloTecnicos.addRow(new Object[]{t.getIdTecnico(), t.getNombre(), t.getEspecialidad(),
                        Util.texto(t.getTelefono()), t.isActivo() ? "Sí" : "No"});
            }
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    /** Da de alta el tecnico y tambien su usuario para entrar al sistema. */
    private void nuevoTecnico() {
        JTextField txtNombre = new JTextField(20);
        JTextField txtEspecialidad = new JTextField(20);
        JTextField txtTelefono = new JTextField(20);
        JTextField txtUsuario = new JTextField(20);
        JTextField txtClave = new JTextField(20);
        if (!Util.formulario(this, "Nuevo técnico",
                new String[]{"Nombre:", "Especialidad:", "Teléfono:", "Usuario:", "Clave:"},
                new java.awt.Component[]{txtNombre, txtEspecialidad, txtTelefono, txtUsuario, txtClave})) {
            return;
        }
        try {
            if (txtNombre.getText().trim().isEmpty() || txtUsuario.getText().trim().isEmpty()
                    || txtClave.getText().trim().isEmpty()) {
                Util.ok(this, "Nombre, usuario y clave son obligatorios.");
                return;
            }
            int idTecnico = tecnicoDAO.insertar(txtNombre.getText().trim(), txtEspecialidad.getText().trim(),
                    txtTelefono.getText().trim());
            new UsuarioDAO().insertar(txtUsuario.getText().trim(), txtClave.getText().trim(), "TECNICO", idTecnico);
            cargarTecnicos();
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    private void bajaTecnico() {
        int id = Util.idSeleccionado(this, tablaTecnicos);
        if (id < 0) {
            return;
        }
        try {
            tecnicoDAO.darDeBaja(id);
            cargarTecnicos();
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    // =============================================================== HISTORIAL
    private JPanel crearPanelHistorial() {
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        arriba.add(new JLabel("Unidad:"));
        arriba.add(comboHistorial);
        agregarBoton(arriba, "Consultar historial", () -> consultarHistorial());
        agregarBoton(arriba, "Ver detalle", () -> {
            int id = Util.idSeleccionado(this, tablaHistorial);
            if (id >= 0) {
                new DetalleOrdenVista(this, id).setVisible(true);
            }
        });

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(arriba, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaHistorial), BorderLayout.CENTER);
        return panel;
    }

    private void consultarHistorial() {
        UnidadFuncional u = (UnidadFuncional) comboHistorial.getSelectedItem();
        if (u == null) {
            return;
        }
        try {
            modeloHistorial.setRowCount(0);
            for (OrdenTrabajo o : ordenDAO.historialPorUnidad(u.getIdUnidad())) {
                modeloHistorial.addRow(new Object[]{o.getIdOrden(), o.getCodigo(), Util.fecha(o.getFechaFin()),
                        Util.texto(o.getNombreTecnico()), Util.texto(o.getDiagnostico()),
                        Util.texto(o.getTrabajoRealizado()), o.getEstado()});
            }
            if (modeloHistorial.getRowCount() == 0) {
                Util.ok(this, "La unidad no tiene trabajos terminados.");
            }
        } catch (Exception e) {
            Util.error(this, e);
        }
    }

    // ============================================================= auxiliares
    private void agregarBoton(JPanel panel, String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> accion.run());
        panel.add(b);
    }

    private String vacioANull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
