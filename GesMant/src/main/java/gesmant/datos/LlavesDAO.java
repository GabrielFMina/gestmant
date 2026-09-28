package gesmant.datos;

import gesmant.modelo.RegistroLlaves;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla registro_llaves: entrega y devolucion de llaves (CU05 y CU14).
 * Una fecha_devolucion nula significa que la llave todavia no se devolvio.
 */
public class LlavesDAO {

    public void registrarEntrega(int idOrden, String detalle, LocalDateTime fecha) throws SQLException {
        String sql = "INSERT INTO registro_llaves (id_orden, detalle_juego, fecha_entrega) VALUES (?, ?, ?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            ps.setString(2, detalle);
            ps.setTimestamp(3, Conexion.aTimestamp(fecha));
            ps.executeUpdate();
        }
    }

    public void registrarDevolucion(int idRegistro, LocalDateTime fecha) throws SQLException {
        String sql = "UPDATE registro_llaves SET fecha_devolucion = ? WHERE id_registro = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Conexion.aTimestamp(fecha));
            ps.setInt(2, idRegistro);
            ps.executeUpdate();
        }
    }

    public List<RegistroLlaves> listarPorOrden(int idOrden) throws SQLException {
        List<RegistroLlaves> lista = new ArrayList<>();
        String sql = "SELECT * FROM registro_llaves WHERE id_orden = ? ORDER BY id_registro";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new RegistroLlaves(rs.getInt("id_registro"), rs.getInt("id_orden"),
                            rs.getString("detalle_juego"), Conexion.aFecha(rs.getTimestamp("fecha_entrega")),
                            Conexion.aFecha(rs.getTimestamp("fecha_devolucion"))));
                }
            }
        }
        return lista;
    }

    /** Llaves de la orden que todavia no se devolvieron. */
    public List<RegistroLlaves> pendientesDeDevolucion(int idOrden) throws SQLException {
        List<RegistroLlaves> pendientes = new ArrayList<>();
        for (RegistroLlaves r : listarPorOrden(idOrden)) {
            if (r.getFechaDevolucion() == null) {
                pendientes.add(r);
            }
        }
        return pendientes;
    }
}
