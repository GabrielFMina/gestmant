package gesmant.datos;

import gesmant.modelo.Consorcio;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla consorcio: listar, insertar y eliminar edificios (CU18).
 */
public class ConsorcioDAO {

    public List<Consorcio> listar() throws SQLException {
        List<Consorcio> lista = new ArrayList<>();
        String sql = "SELECT id_consorcio, nombre, direccion FROM consorcio ORDER BY nombre";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Consorcio(rs.getInt("id_consorcio"), rs.getString("nombre"),
                        rs.getString("direccion")));
            }
        }
        return lista;
    }

    /** En el prototipo hay una sola administracion (id 1). */
    public void insertar(String nombre, String direccion) throws SQLException {
        String sql = "INSERT INTO consorcio (id_administracion, nombre, direccion) VALUES (1, ?, ?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, direccion);
            ps.executeUpdate();
        }
    }

    /** Falla (SQLException) si el consorcio tiene unidades cargadas. */
    public void eliminar(int idConsorcio) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement("DELETE FROM consorcio WHERE id_consorcio = ?")) {
            ps.setInt(1, idConsorcio);
            ps.executeUpdate();
        }
    }
}
