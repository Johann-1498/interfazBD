import java.sql.*;

public class Main {
    // Parámetros de conexión
    static final String URL = "jdbc:mysql://localhost:3306/LABBDCASO11";
    static final String USUARIO = "root";
    static final String CONTRASEÑA = "UJKMjuandi128980";

    public static void main(String[] args) {
        try (Connection conexion = DriverManager.getConnection(URL, USUARIO, CONTRASEÑA)) {
            // Crear
            insertarTrabajador(conexion, 1006, "Luis Torres", 1, "2024-03-01", null, "2024-07-15", 1, "1", "Activo");

            // Leer
            leerTrabajadores(conexion);

            // Actualizar
            actualizarTrabajador(conexion, 1005, "Luis Alberto Torres", "Inactivo");

            // Leer después de actualizar
            leerTrabajadores(conexion);

            // Eliminar
            eliminarTrabajador(conexion, 1002);

            // Leer después de eliminar
            leerTrabajadores(conexion);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // MÉTODO: Crear
    public static void insertarTrabajador(Connection conexion, int cod, String nombre, int tipo, String fecIng,
                                        String fecCes, String fecVac, int cenCod, String estado, String regEst) throws SQLException {
        String sql = "INSERT INTO R1M_TRABAJADOR (TraCod, TraNom, TraTip, TraFecIng, TraFecCes, TraFecVac, TraCenCosCod, TraEst, TraRegEst) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, cod);
            ps.setString(2, nombre);
            ps.setInt(3, tipo);
            ps.setDate(4, java.sql.Date.valueOf(fecIng));
            if (fecCes != null) {
                ps.setDate(5, java.sql.Date.valueOf(fecCes));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setDate(6, java.sql.Date.valueOf(fecVac));
            ps.setInt(7, cenCod);
            ps.setString(8, estado);
            ps.setString(9, regEst);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                System.out.println("Trabajador" + cod + "insertado correctamente.");
            }
        }
    }

    // MÉTODO: Leer
    public static void leerTrabajadores(Connection conexion) throws SQLException {
        String sql = "SELECT * FROM R1M_TRABAJADOR";
        try (Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Lista de trabajadores:");
            while (rs.next()) {
                System.out.println("Código: " + rs.getInt("TraCod") +
                        ", Nombre: " + rs.getString("TraNom") +
                        ", Estado: " + rs.getString("TraRegEst"));
            }
        }
    }

    // MÉTODO: Actualizar
    public static void actualizarTrabajador(Connection conexion, int cod, String nuevoNombre, String nuevoEstado) throws SQLException {
        String sql = "UPDATE R1M_TRABAJADOR SET TraNom = ?, TraRegEst = ? WHERE TraCod = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre);
            ps.setString(2, nuevoEstado);
            ps.setInt(3, cod);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                System.out.println("Trabajador " + cod + " actualizado correctamente.");
            }
        }
    }

    // MÉTODO: Eliminar
    public static void eliminarTrabajador(Connection conexion, int cod) throws SQLException {
        String sql = "DELETE FROM R1M_TRABAJADOR WHERE TraCod = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, cod);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                System.out.println("Trabajador" + cod +"eliminado correctamente.");
            }
        }
    }
}
