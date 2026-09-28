package gesmant.datos;

import gesmant.modelo.UnidadFuncional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla unidad_funcional: departamentos y espacios comunes de cada consorcio.
 */
public class UnidadDAO {

    private static final String SELECT_BASE =
            "SELECT u.id_unidad, u.id_consorcio, u.numero, u.piso, u.es_espacio_comun, "
          + "u.nombre_inquilino, u.telefono_inquilino, c.nombre AS consorcio "
          + "FROM unidad_funcional u JOIN consorcio c ON c.id_consorcio = u.id_consorcio ";

    public List<UnidadFuncional> listar() throws SQLException {
        return consultar(SELECT_BASE + "ORDER BY c.nombre, u.numero", 0);
    }

    public List<UnidadFuncional> listarPorConsorcio(int idConsorcio) throws SQLException {
        return consultar(SELECT_BASE + "WHERE u.id_consorcio = ? ORDER BY u.numero", idConsorcio);
    }

    private List<UnidadFuncional> consultar(String sql, int idConsorcio) throws SQLException {
        List<UnidadFuncional> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (idConsorcio > 0) {
                ps.setInt(1, idConsorcio);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new UnidadFuncional(rs.getInt("id_unidad"), rs.getInt("id_consorcio"),
                            rs.getString("numero"), rs.getString("piso"), rs.getBoolean("es_espacio_comun"),
                            rs.getString("nombre_inquilino"), rs.getString("telefono_inquilino"),
                            rs.getString("consorcio")));
                }
            }
        }
        return lista;
    }

    public UnidadFuncional buscar(int idUnidad) throws SQLException {
        for (UnidadFuncional u : listar()) {
            if (u.getIdUnidad() == idUnidad) {
                return u;
            }
        }
        return null;
    }

    public void insertar(int idConsorcio, String numero, String piso, boolean espacioComun,
                         String inquilino, String telefono) throws SQLException {
        String sql = "INSERT INTO unidad_funcional (id_consorcio, numero, piso, es_espacio_comun, "
                   + "nombre_inquilino, telefono_inquilino) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idConsorcio);
            ps.setString(2, numero);
            ps.setString(3, piso);
            ps.setBoolean(4, espacioComun);
            ps.setString(5, inquilino);
            ps.setString(6, telefono);
            ps.executeUpdate();
        }
    }

    /** Falla (SQLException) si la unidad tiene ordenes asociadas. */
    public void eliminar(int idUnidad) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement("DELETE FROM unidad_funcional WHERE id_unidad = ?")) {
            ps.setInt(1, idUnidad);
            ps.executeUpdate();
        }
    }
}
