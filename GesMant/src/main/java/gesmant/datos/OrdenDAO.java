package gesmant.datos;

import gesmant.modelo.OrdenTrabajo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla orden_trabajo, la tabla central del sistema.
 * Las consultas unen unidad, consorcio y tecnico para mostrar nombres en pantalla.
 */
public class OrdenDAO {

    private static final String SELECT_BASE =
            "SELECT o.*, u.numero AS unidad, c.nombre AS consorcio, t.nombre AS tecnico "
          + "FROM orden_trabajo o "
          + "JOIN unidad_funcional u ON u.id_unidad = o.id_unidad "
          + "JOIN consorcio c ON c.id_consorcio = u.id_consorcio "
          + "LEFT JOIN tecnico t ON t.id_tecnico = o.id_tecnico ";

    public List<OrdenTrabajo> listar() throws SQLException {
        return consultar(SELECT_BASE + "ORDER BY o.codigo", null);
    }

    /** Ordenes de un tecnico, las mas urgentes primero. */
    public List<OrdenTrabajo> listarPorTecnico(int idTecnico) throws SQLException {
        return consultar(SELECT_BASE + "WHERE o.id_tecnico = ? "
                + "ORDER BY CASE o.prioridad WHEN 'ALTA' THEN 1 WHEN 'MEDIA' THEN 2 ELSE 3 END, o.codigo",
                idTecnico);
    }

    /** Historial: ordenes terminadas de una unidad. */
    public List<OrdenTrabajo> historialPorUnidad(int idUnidad) throws SQLException {
        return consultar(SELECT_BASE + "WHERE o.id_unidad = ? AND o.estado IN ('FINALIZADA', 'PAGADA') "
                + "ORDER BY o.fecha_fin DESC", idUnidad);
    }

    public OrdenTrabajo buscar(int idOrden) throws SQLException {
        List<OrdenTrabajo> lista = consultar(SELECT_BASE + "WHERE o.id_orden = ?", idOrden);
        return lista.isEmpty() ? null : lista.get(0);
    }

    public OrdenTrabajo buscarPorCodigo(String codigo) throws SQLException {
        for (OrdenTrabajo o : listar()) {
            if (o.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return o;
            }
        }
        return null;
    }

    private List<OrdenTrabajo> consultar(String sql, Integer parametro) throws SQLException {
        List<OrdenTrabajo> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leer(rs));
                }
            }
        }
        return lista;
    }

    private OrdenTrabajo leer(ResultSet rs) throws SQLException {
        OrdenTrabajo o = new OrdenTrabajo();
        o.setIdOrden(rs.getInt("id_orden"));
        o.setCodigo(rs.getString("codigo"));
        o.setIdUnidad(rs.getInt("id_unidad"));
        int idTec = rs.getInt("id_tecnico");
        o.setIdTecnico(rs.wasNull() ? null : idTec);
        o.setDescripcionProblema(rs.getString("descripcion_problema"));
        o.setPrioridad(rs.getString("prioridad"));
        o.setEstado(rs.getString("estado"));
        o.setRequiereCoordinacion(rs.getBoolean("requiere_coordinacion"));
        o.setDiagnostico(rs.getString("diagnostico"));
        o.setTrabajoRealizado(rs.getString("trabajo_realizado"));
        o.setFechaCreacion(Conexion.aFecha(rs.getTimestamp("fecha_creacion")));
        o.setFechaInicio(Conexion.aFecha(rs.getTimestamp("fecha_inicio")));
        o.setFechaFin(Conexion.aFecha(rs.getTimestamp("fecha_fin")));
        o.setNumeroUnidad(rs.getString("unidad"));
        o.setNombreConsorcio(rs.getString("consorcio"));
        o.setNombreTecnico(rs.getString("tecnico"));
        return o;
    }

    /** Arma el proximo codigo con el formato OT-0001, OT-0002, etc. */
    public String siguienteCodigo() throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement("SELECT MAX(id_orden) FROM orden_trabajo");
             ResultSet rs = ps.executeQuery()) {
            int ultimo = rs.next() ? rs.getInt(1) : 0;
            return String.format("OT-%04d", ultimo + 1);
        }
    }

    /** Inserta la orden y devuelve el id generado. */
    public int insertar(OrdenTrabajo o) throws SQLException {
        String sql = "INSERT INTO orden_trabajo (codigo, id_unidad, descripcion_problema, prioridad, estado, "
                   + "requiere_coordinacion, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, o.getCodigo());
            ps.setInt(2, o.getIdUnidad());
            ps.setString(3, o.getDescripcionProblema());
            ps.setString(4, o.getPrioridad());
            ps.setString(5, o.getEstado());
            ps.setBoolean(6, o.isRequiereCoordinacion());
            ps.setTimestamp(7, Conexion.aTimestamp(o.getFechaCreacion()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void asignarTecnico(int idOrden, int idTecnico, String nuevoEstado) throws SQLException {
        ejecutar("UPDATE orden_trabajo SET id_tecnico = ?, estado = ? WHERE id_orden = ?",
                idTecnico, nuevoEstado, idOrden);
    }

    public void cambiarEstado(int idOrden, String nuevoEstado) throws SQLException {
        ejecutar("UPDATE orden_trabajo SET estado = ? WHERE id_orden = ?", nuevoEstado, idOrden);
    }

    public void registrarDiagnostico(int idOrden, String diagnostico) throws SQLException {
        ejecutar("UPDATE orden_trabajo SET diagnostico = ? WHERE id_orden = ?", diagnostico, idOrden);
    }

    public void registrarInicio(int idOrden, LocalDateTime fecha) throws SQLException {
        ejecutar("UPDATE orden_trabajo SET estado = 'EN_EJECUCION', fecha_inicio = ? WHERE id_orden = ?",
                Conexion.aTimestamp(fecha), idOrden);
    }

    public void registrarFin(int idOrden, LocalDateTime fecha, String trabajoRealizado) throws SQLException {
        ejecutar("UPDATE orden_trabajo SET estado = 'FINALIZADA', fecha_fin = ?, trabajo_realizado = ? "
                + "WHERE id_orden = ?", Conexion.aTimestamp(fecha), trabajoRealizado, idOrden);
    }

    /** Cuando la administracion rechaza el trabajo, la orden vuelve a ejecucion. */
    public void reabrir(int idOrden) throws SQLException {
        ejecutar("UPDATE orden_trabajo SET estado = 'EN_EJECUCION', fecha_fin = NULL WHERE id_orden = ?", idOrden);
    }

    public void eliminar(int idOrden) throws SQLException {
        ejecutar("DELETE FROM orden_trabajo WHERE id_orden = ?", idOrden);
    }

    /** Ejecuta un UPDATE o DELETE cargando los parametros en orden. */
    private void ejecutar(String sql, Object... parametros) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            ps.executeUpdate();
        }
    }
}
