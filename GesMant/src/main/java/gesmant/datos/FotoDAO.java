package gesmant.datos;

import gesmant.modelo.EvidenciaFoto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla evidencia_foto. Solo se guarda la ruta del archivo, no la imagen (CU13).
 */
public class FotoDAO {

    public void insertar(int idOrden, String ruta, String etapa, LocalDateTime fecha) throws SQLException {
        String sql = "INSERT INTO evidencia_foto (id_orden, ruta_archivo, etapa, fecha_subida) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            ps.setString(2, ruta);
            ps.setString(3, etapa);
            ps.setTimestamp(4, Conexion.aTimestamp(fecha));
            ps.executeUpdate();
        }
    }

    public List<EvidenciaFoto> listarPorOrden(int idOrden) throws SQLException {
        List<EvidenciaFoto> lista = new ArrayList<>();
        String sql = "SELECT ruta_archivo, etapa, fecha_subida FROM evidencia_foto WHERE id_orden = ? ORDER BY id_foto";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new EvidenciaFoto(rs.getString("ruta_archivo"), rs.getString("etapa"),
                            Conexion.aFecha(rs.getTimestamp("fecha_subida"))));
                }
            }
        }
        return lista;
    }

    public int contarPorEtapa(int idOrden, String etapa) throws SQLException {
        int cantidad = 0;
        for (EvidenciaFoto f : listarPorOrden(idOrden)) {
            if (f.getEtapa().equals(etapa)) {
                cantidad++;
            }
        }
        return cantidad;
    }
}
