package gesmant.datos;

import gesmant.modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acceso a la tabla usuario: inicio de sesion (CU01) y alta de usuarios para nuevos tecnicos.
 */
public class UsuarioDAO {

    /** Devuelve el usuario si el nombre y la clave coinciden, o null si no. */
    public Usuario login(String nombreUsuario, String clave) throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario, rol, id_tecnico FROM usuario "
                   + "WHERE nombre_usuario = ? AND clave = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, clave);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idTec = rs.getInt("id_tecnico");
                    Integer idTecnico = rs.wasNull() ? null : idTec;
                    return new Usuario(rs.getInt("id_usuario"), rs.getString("nombre_usuario"),
                            rs.getString("rol"), idTecnico);
                }
            }
        }
        return null;
    }

    public void insertar(String nombreUsuario, String clave, String rol, Integer idTecnico) throws SQLException {
        String sql = "INSERT INTO usuario (nombre_usuario, clave, rol, id_tecnico) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, clave);
            ps.setString(3, rol);
            if (idTecnico == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, idTecnico);
            }
            ps.executeUpdate();
        }
    }
}
