package Sistema_Evaluador.ui;

import Sistema_Evaluador.db.GestorBD;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class VentanaLogin extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;

    // Gestor que se encarga de hablar con la base de datos
    private final GestorBD gestor = new GestorBD();

    public VentanaLogin() {
        setTitle("Login");
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

        // Enter en cualquiera de los campos equivale a "Iniciar Sesión"
        getRootPane().setDefaultButton(btnLogin);
    }

    // Valida el usuario y la clave contra la tabla "usuarios" de la base
    private void validarLogin() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese usuario y contraseña.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (gestor.validarAcceso(usuario, password)) {
                JOptionPane.showMessageDialog(this, "Bienvenido, " + usuario, "Éxito", JOptionPane.INFORMATION_MESSAGE);

                new VentanaPrincipal(gestor).setVisible(true);

                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
            }
        } catch (SQLException ex) {
            // Problema con la base (servidor apagado, credenciales de BD incorrectas, etc.)
            JOptionPane.showMessageDialog(this, "No se pudo conectar con la base de datos: " + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirVentanaRegistro() {
        VentanaRegistro ventanaRegistro = new VentanaRegistro(this);
        ventanaRegistro.setVisible(true);
    }

}