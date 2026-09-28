package gesmant.datos;

import gesmant.modelo.MaterialUtilizado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla material_utilizado: materiales usados realmente en un trabajo (CU12).
 */
public class MaterialDAO {

    public void insertar(int idOrden, MaterialUtilizado m) throws SQLException {
        String sql = "INSERT INTO material_utilizado (id_orden, nombre, cantidad, costo_unitario) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            ps.setString(2, m.getNombre());
            ps.setInt(3, m.getCantidad());
            ps.setDouble(4, m.getCostoUnitario());
            ps.executeUpdate();
        }
    }

    public List<MaterialUtilizado> listarPorOrden(int idOrden) throws SQLException {
        List<MaterialUtilizado> lista = new ArrayList<>();
        String sql = "SELECT nombre, cantidad, costo_unitario FROM material_utilizado WHERE id_orden = ? ORDER BY id_material";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new MaterialUtilizado(rs.getString("nombre"), rs.getInt("cantidad"),
                            rs.getDouble("costo_unitario")));
                }
            }
        }
        return lista;
    }
}
