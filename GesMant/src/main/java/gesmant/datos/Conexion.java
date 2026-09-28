package gesmant.datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Abre la conexion con la base de datos MySQL mediante JDBC.
 * Modificar URL, USUARIO y CLAVE segun la instalacion local (por ejemplo, se uso XAMPP).
 */
public class Conexion {

    private static final String URL =
            "jdbc:mysql://localhost:3306/gesmant?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Argentina/Cordoba";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    /** Convierte una fecha de Java al tipo que usa JDBC (acepta null). */
    public static Timestamp aTimestamp(LocalDateTime fecha) {
        return fecha == null ? null : Timestamp.valueOf(fecha);
    }

    /** Convierte un DATETIME leido de la base a fecha de Java (acepta null). */
    public static LocalDateTime aFecha(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
