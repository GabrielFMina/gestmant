package gesmant.datos;

import gesmant.modelo.Tecnico;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla tecnico. Los tecnicos no se borran: se dan de baja (activo = FALSE).
 */
public class TecnicoDAO {

    public List<Tecnico> listar(boolean soloActivos) throws SQLException {
        List<Tecnico> lista = new ArrayList<>();
        String sql = "SELECT id_tecnico, nombre, especialidad, telefono, activo FROM tecnico "
                   + (soloActivos ? "WHERE activo = TRUE " : "") + "ORDER BY nombre";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Tecnico(rs.getInt("id_tecnico"), rs.getString("nombre"),
                        rs.getString("especialidad"), rs.getString("telefono"), rs.getBoolean("activo")));
            }
        }
        return lista;
    }

    /** Inserta el tecnico y devuelve el id generado. */
    public int insertar(String nombre, String especialidad, String telefono) throws SQLException {
        String sql = "INSERT INTO tecnico (nombre, especialidad, telefono, activo) VALUES (?, ?, ?, TRUE)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, especialidad);
            ps.setString(3, telefono);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Baja logica: el tecnico queda inactivo pero se conserva su historial. */
    public void darDeBaja(int idTecnico) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement("UPDATE tecnico SET activo = FALSE WHERE id_tecnico = ?")) {
            ps.setInt(1, idTecnico);
            ps.executeUpdate();
        }
    }
}
