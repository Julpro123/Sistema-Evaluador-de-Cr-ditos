package Sistema_Evaluador.db;

import Sistema_Evaluador.modelo.Cliente;
import Sistema_Evaluador.modelo.Empresa;
import Sistema_Evaluador.modelo.PersonaFisica;
import Sistema_Evaluador.modelo.Usuario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Clase principal del gestor de base de datos.
// Encargada de realizar operaciones de conexión y consultas para clientes y usuarios.
public class GestorBD {
    // Datos de conexión a la base de datos.
    private final String url;
    private final String usuario;
    private final String clave;

    // Constructor por defecto: toma la configuración desde variables de entorno.
    public GestorBD() {
        this(
            valorEntorno("DB_URL", "jdbc:mysql://localhost:3306/evaluador_creditos"),
            valorEntorno("DB_USER", "root"),
            valorEntorno("DB_PASS", "")
        );
    }

    // Constructor con parámetros explícitos para conexiones personalizadas.
    public GestorBD(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    // Obtiene el valor de una variable de entorno y usa un valor por defecto si no existe.
    private static String valorEntorno(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        return valor == null ? porDefecto : valor;
    }

    // Crea la conexión JDBC usando la URL, usuario y contraseña configurados.
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    // Sección: manejo de clientes.
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

    // Guarda datos adicionales dependiendo del tipo de cliente.
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

    // Consulta y devuelve una lista resumida de clientes, con el resultado de su evaluación.
    public List<Object[]> listarClientes() throws SQLException {
        String sql = "SELECT c.id_cliente, c.nombre, c.ingresos, c.gastos, c.historial, "
                + "e.id_empresa, e.rubro, e.director "
                + "FROM clientes c LEFT JOIN empresas e ON e.id_cliente = c.id_cliente "
                + "ORDER BY c.id_cliente";
        List<Object[]> filas = new ArrayList<>();
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nombre = rs.getString("nombre");
                double ingresos = rs.getDouble("ingresos");
                double gastos = rs.getDouble("gastos");
                boolean historial = rs.getBoolean("historial");
                boolean esEmpresa = rs.getObject("id_empresa") != null;

                // Se arma el objeto según su tipo para evaluarlo con su propia regla (polimorfismo)
                Cliente cliente;
                if (esEmpresa) {
                    cliente = new Empresa(nombre, ingresos, gastos, historial, rs.getString("rubro"), rs.getString("director"));
                } else {
                    cliente = new PersonaFisica(nombre, ingresos, gastos, historial);
                }

                filas.add(new Object[] {
                    rs.getInt("id_cliente"),
                    nombre,
                    ingresos,
                    gastos,
                    historial ? "Bueno" : "Malo",
                    esEmpresa ? "Empresa" : "Persona Física",
                    cliente.evaluarRiesgo() > 0 ? "APTO" : "NO APTO"
                });
            }
        }
        return filas;
    }

    // Sección: manejo de usuarios del sistema.
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

    // Registra un nuevo usuario en la tabla de usuarios.
    public void insertarUsuario(String nombreUsuario, String claveUsuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre_usuario, clave) VALUES (?, ?)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, claveUsuario);
            ps.executeUpdate();
        }
    }

    // Muestra todos los usuarios registrados ordenados por su identificador.
    public List<Usuario> listarUsuarios() throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario FROM usuarios ORDER BY id_usuario";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(new Usuario(rs.getInt("id_usuario"), rs.getString("nombre_usuario")));
            }
        }
        return usuarios;
    }

    // Modifica el nombre y la clave de un usuario. Si la clave viene vacía, solo cambia el nombre.
    // Devuelve true si se modificó una fila.
    public boolean modificarUsuario(int idUsuario, String nuevoNombre, String nuevaClave) throws SQLException {
        boolean cambiaClave = !nuevaClave.isEmpty();
        String sql = cambiaClave
                ? "UPDATE usuarios SET nombre_usuario = ?, clave = ? WHERE id_usuario = ?"
                : "UPDATE usuarios SET nombre_usuario = ? WHERE id_usuario = ?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre);
            if (cambiaClave) {
                ps.setString(2, nuevaClave);
                ps.setInt(3, idUsuario);
            } else {
                ps.setInt(2, idUsuario);
            }
            return ps.executeUpdate() > 0;
        }
    }

    // Elimina un usuario por su identificador y devuelve si se realizó la eliminación.
    public boolean eliminarUsuario(int idUsuario) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }
}