import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GestorBD {
    private final String url;
    private final String usuario;
    private final String clave;

    public GestorBD() {
        this(
            valorEntorno("DB_URL", "jdbc:mysql://localhost:3306/evaluador_creditos"),
            valorEntorno("DB_USER", "root"),
            valorEntorno("DB_PASS", "")
        );
    }

    public GestorBD(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    private static String valorEntorno(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        return valor == null ? porDefecto : valor;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    public int guardarCliente(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO clientes (nombre, ingresos, gastos, historial) VALUES (?, ?, ?, ?)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNombre());
            ps.setDouble(2, cliente.getIngresos());
            ps.setDouble(3, cliente.getGastos());
            ps.setBoolean(4, cliente.isTieneBuenHistorial());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el identificador generado del cliente.");
    }

    public void guardarDetalle(int idCliente, Cliente cliente) throws SQLException {
        if (cliente instanceof Empresa) {
            Empresa empresa = (Empresa) cliente;
            String sql = "INSERT INTO empresas (id_cliente, rubro, director) VALUES (?, ?, ?)";
            try (Connection cn = getConnection();
                 PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, idCliente);
                ps.setString(2, empresa.getRubro());
                ps.setString(3, empresa.getDirector());
                ps.executeUpdate();
            }
        } else {
            String sql = "INSERT INTO personas_fisicas (id_cliente) VALUES (?)";
            try (Connection cn = getConnection();
                 PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, idCliente);
                ps.executeUpdate();
            }
        }
    }

    public List<Object[]> listarClientes() throws SQLException {
        String sql = "SELECT c.id_cliente, c.nombre, c.ingresos, c.gastos, c.historial, "
                + "CASE WHEN e.id_empresa IS NOT NULL THEN 'Empresa' ELSE 'Persona Física' END AS tipo "
                + "FROM clientes c LEFT JOIN empresas e ON e.id_cliente = c.id_cliente "
                + "ORDER BY c.id_cliente";
        List<Object[]> filas = new ArrayList<>();
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filas.add(new Object[] {
                    rs.getInt("id_cliente"),
                    rs.getString("nombre"),
                    rs.getDouble("ingresos"),
                    rs.getDouble("gastos"),
                    rs.getBoolean("historial") ? "Bueno" : "Malo",
                    rs.getString("tipo")
                });
            }
        }
        return filas;
    }

    public boolean validarAcceso(String nombreUsuario, String claveUsuario) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE nombre_usuario = ? AND clave = ?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, claveUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insertarUsuario(String nombreUsuario, String claveUsuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre_usuario, clave) VALUES (?, ?)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, claveUsuario);
            ps.executeUpdate();
        }
    }

    public List<Object[]> listarUsuarios() throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario FROM usuarios ORDER BY id_usuario";
        List<Object[]> filas = new ArrayList<>();
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filas.add(new Object[] { rs.getInt("id_usuario"), rs.getString("nombre_usuario") });
            }
        }
        return filas;
    }

    public boolean eliminarUsuario(int idUsuario) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }
}