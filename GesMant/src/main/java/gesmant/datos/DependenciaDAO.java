package gesmant.datos;

import gesmant.modelo.DependenciaOrden;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla dependencia_orden: registra que una orden depende de otra (CU10)
 * y lista las ordenes previas de una orden para saber si puede iniciar (CU09).
 */
public class DependenciaDAO {

    public void insertar(int idOrdenBloqueada, int idOrdenPrevia, int horasEspera) throws SQLException {
        String sql = "INSERT INTO dependencia_orden (id_orden_bloqueada, id_orden_previa, tiempo_espera_horas) "
                   + "VALUES (?, ?, ?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrdenBloqueada);
            ps.setInt(2, idOrdenPrevia);
            ps.setInt(3, horasEspera);
            ps.executeUpdate();
        }
    }

    /** Ordenes previas de las que depende la orden indicada. */
    public List<DependenciaOrden> listarPrevias(int idOrdenBloqueada) throws SQLException {
        List<DependenciaOrden> lista = new ArrayList<>();
        String sql = "SELECT d.id_orden_previa, o.codigo, o.estado, o.fecha_fin, d.tiempo_espera_horas "
                   + "FROM dependencia_orden d JOIN orden_trabajo o ON o.id_orden = d.id_orden_previa "
                   + "WHERE d.id_orden_bloqueada = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrdenBloqueada);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DependenciaOrden(rs.getInt("id_orden_previa"), rs.getString("codigo"),
                            rs.getString("estado"), Conexion.aFecha(rs.getTimestamp("fecha_fin")),
                            rs.getInt("tiempo_espera_horas")));
                }
            }
        }
        return lista;
    }
}
