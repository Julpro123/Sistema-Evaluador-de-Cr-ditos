package Sistema_Evaluador;

import javax.swing.*;
import java.awt.*;

public class VentanaRegistro extends JDialog {
    private JTextField txtNuevoUsuario;
    private JPasswordField txtNuevoPassword;
    private JPasswordField txtConfirmPassword;

    public VentanaRegistro(JFrame parent) {
        super(parent, "Registro de Nuevo Usuario", true);
        setSize(380, 240);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Nuevo Usuario:"), gbc);
        gbc.gridx = 1;
        txtNuevoUsuario = new JTextField(15);
        add(txtNuevoUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        txtNuevoPassword = new JPasswordField(15);
        add(txtNuevoPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Confirmar Clave:"), gbc);
        gbc.gridx = 1;
        txtConfirmPassword = new JPasswordField(15);
        add(txtConfirmPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton btnRegistrar = new JButton("Crear Cuenta");
        btnRegistrar.addActionListener(e -> registrarNuevoUsuario());
        add(btnRegistrar, gbc);
    }

    private void registrarNuevoUsuario() {
        String usuario = txtNuevoUsuario.getText().trim();
        String password = new String(txtNuevoPassword.getPassword());
        String confirmar = new String(txtConfirmPassword.getPassword());

        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmar)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            new GestorBD().insertarUsuario(usuario, password);
            JOptionPane.showMessageDialog(this, "Cuenta creada con éxito! Ya puede iniciar sesión.", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        } catch (java.sql.SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "El nombre de usuario ya existe. Elija otro.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo crear la cuenta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}