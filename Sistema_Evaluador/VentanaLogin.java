package Sistema_Evaluador;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class VentanaLogin extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;

    public static Map<String, String> usuariosRegistrados = new HashMap<>() {{
        put("admin", "1234");
    }};

    public VentanaLogin() {
        setTitle("Login - Sistema Evaluador de Créditos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 280);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblTitulo = new JLabel("Ingrese sus credenciales", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        add(lblTitulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        txtUsuario = new JTextField(15);
        add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        JButton btnLogin = new JButton("Iniciar Sesión");
        btnLogin.addActionListener(e -> validarLogin());
        add(btnLogin, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        JButton btnIrRegistro = new JButton("Registrarse");
        btnIrRegistro.addActionListener(e -> abrirVentanaRegistro());
        add(btnIrRegistro, gbc);
    }

    private void validarLogin() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (usuariosRegistrados.containsKey(usuario) && usuariosRegistrados.get(usuario).equals(password)) {
            JOptionPane.showMessageDialog(this, "¡Login exitoso! Bienvenido, " + usuario, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            // Abre la VentanaPrincipal renombrada
            new VentanaPrincipal().setVisible(true);
            
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
        }
    }

    private void abrirVentanaRegistro() {
        VentanaRegistro ventanaRegistro = new VentanaRegistro(this);
        ventanaRegistro.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new VentanaLogin().setVisible(true);
        });
    }
}