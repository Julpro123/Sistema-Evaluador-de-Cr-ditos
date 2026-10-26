package sistema_evaluador;

// Representa un usuario del listado (sin la clave)
public class Usuario {

    private int id;
    private String nombreUsuario;

    public Usuario(int id, String nombreUsuario) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
    }

    public int getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}