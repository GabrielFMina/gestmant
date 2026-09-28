package gesmant.datos;

import gesmant.modelo.ItemPresupuesto;
import gesmant.modelo.Presupuesto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a las tablas presupuesto e item_presupuesto (CU07 y CU08).
 * El total no se guarda: se calcula sumando los items (ver Presupuesto.getTotal()).
 */
public class PresupuestoDAO {

    /**
     * Guarda el presupuesto y todos sus items. Se usa una transaccion:
     * si falla algun item no queda un presupuesto a medias.
     */
    public int insertar(int idOrden, String materialesACargoDe, String observacion,
                        List<ItemPresupuesto> items, LocalDateTime fecha) throws SQLException {
        String sqlP = "INSERT INTO presupuesto (id_orden, fecha_creacion, estado, materiales_a_cargo_de, observacion) "
                    + "VALUES (?, ?, 'PENDIENTE', ?, ?)";
        String sqlI = "INSERT INTO item_presupuesto (id_presupuesto, descripcion, tipo, cantidad, precio_unitario) "
                    + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                int idPresupuesto;
                try (PreparedStatement ps = con.prepareStatement(sqlP, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idOrden);
                    ps.setTimestamp(2, Conexion.aTimestamp(fecha));
                    ps.setString(3, materialesACargoDe);
                    ps.setString(4, observacion);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        idPresupuesto = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(sqlI)) {
                    for (ItemPresupuesto item : items) {
                        ps.setInt(1, idPresupuesto);
                        ps.setString(2, item.getDescripcion());
                        ps.setString(3, item.getTipo());
                        ps.setInt(4, item.getCantidad());
                        ps.setDouble(5, item.getPrecioUnitario());
                        ps.executeUpdate();
                    }
                }
                con.commit();
                return idPresupuesto;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Presupuestos de una orden con sus items, del mas viejo al mas nuevo. */
    public List<Presupuesto> listarPorOrden(int idOrden) throws SQLException {
        List<Presupuesto> lista = new ArrayList<>();
        String sql = "SELECT * FROM presupuesto WHERE id_orden = ? ORDER BY id_presupuesto";
        String sqlItems = "SELECT descripcion, tipo, cantidad, precio_unitario FROM item_presupuesto "
                        + "WHERE id_presupuesto = ? ORDER BY id_item";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             PreparedStatement psItems = con.prepareStatement(sqlItems)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Presupuesto p = new Presupuesto(rs.getInt("id_presupuesto"), rs.getInt("id_orden"),
                            Conexion.aFecha(rs.getTimestamp("fecha_creacion")), rs.getString("estado"),
                            rs.getString("materiales_a_cargo_de"), rs.getString("observacion"));
                    psItems.setInt(1, p.getIdPresupuesto());
                    try (ResultSet ri = psItems.executeQuery()) {
                        while (ri.next()) {
                            p.getItems().add(new ItemPresupuesto(ri.getString("descripcion"), ri.getString("tipo"),
                                    ri.getInt("cantidad"), ri.getDouble("precio_unitario")));
                        }
                    }
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    /** Devuelve el presupuesto pendiente de aprobacion de la orden, o null. */
    public Presupuesto buscarPendiente(int idOrden) throws SQLException {
        for (Presupuesto p : listarPorOrden(idOrden)) {
            if ("PENDIENTE".equals(p.getEstado())) {
                return p;
            }
        }
        return null;
    }

    public void cambiarEstado(int idPresupuesto, String estado, String observacion) throws SQLException {
        String sql = "UPDATE presupuesto SET estado = ?, observacion = ? WHERE id_presupuesto = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setString(2, observacion);
            ps.setInt(3, idPresupuesto);
            ps.executeUpdate();
        }
    }
}
