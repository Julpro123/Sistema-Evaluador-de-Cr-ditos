package Sistema_Evaluador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VentanaPrincipal extends JFrame {

    private JProgressBar progressBar;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtNombre;
    private JTextField txtEmail;
    private JSpinner spinnerIngresos;
    private JFormattedTextField txtCuil;

    public VentanaPrincipal() {
        setTitle("Panel General");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setJMenuBar(crearMenuBar());
        add(crearToolBar(), BorderLayout.NORTH);
        add(crearSplitPane(), BorderLayout.CENTER);
        add(crearPanelEstado(), BorderLayout.SOUTH);

        configurarMenuContextual();
    }

    private JMenuBar crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem itemGuardar = new JMenuItem("Guardar Todo");
        JMenuItem itemSalir = new JMenuItem("Cerrar Sesión / Salir");

        itemGuardar.addActionListener(e -> simularOperacionesBD("Guardando datos en la base de datos..."));
        itemSalir.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Desea cerrar sesión?", "Salir", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        menuArchivo.add(itemGuardar);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        JMenu menuGestion = new JMenu("Gestión");
        JMenuItem itemUsuarios = new JMenuItem("Módulo de Usuarios");
        itemUsuarios.addActionListener(e -> JOptionPane.showMessageDialog(this, "Módulo de usuarios activo."));
        menuGestion.add(itemUsuarios);

        menuBar.add(menuArchivo);
        menuBar.add(menuGestion);

        return menuBar;
    }

    private JToolBar crearToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton btnGuardar = new JButton("Guardar");
        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnUsuarios = new JButton("Usuarios");

        btnGuardar.addActionListener(e -> simularOperacionesBD("Guardando cambios..."));
        btnRefrescar.addActionListener(e -> simularOperacionesBD("Cargando lista de usuarios desde la BD..."));
        btnUsuarios.addActionListener(e -> JOptionPane.showMessageDialog(this, "Se encuentra en el gestor de usuarios."));

        toolBar.add(btnGuardar);
        toolBar.add(btnRefrescar);
        toolBar.addSeparator();
        toolBar.add(btnUsuarios);

        return toolBar;
    }

    private JSplitPane crearSplitPane() {
        JPanel panelIzquierdo = crearFormularioCliente();
        JPanel panelDerecho = crearPanelTablaUsuarios();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdo, panelDerecho);
        splitPane.setDividerLocation(380);
        return splitPane;
    }

    private JPanel crearFormularioCliente() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Edición de Cliente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(15);
        panel.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(15);
        panel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("CUIL/DNI:"), gbc);
        gbc.gridx = 1;
        try {
            javax.swing.text.MaskFormatter mf = new javax.swing.text.MaskFormatter("##-########-#");
            mf.setPlaceholderCharacter('_');
            txtCuil = new JFormattedTextField(mf);
        } catch (Exception e) {
            txtCuil = new JFormattedTextField();
        }
        panel.add(txtCuil, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Ingresos ($):"), gbc);
        gbc.gridx = 1;
        SpinnerNumberModel modelIngresos = new SpinnerNumberModel(150000.0, 0.0, 10000000.0, 10000.0);
        spinnerIngresos = new JSpinner(modelIngresos);
        panel.add(spinnerIngresos, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JButton btnRegistrar = new JButton("Registrar Usuario");
        btnRegistrar.addActionListener(e -> agregarUsuario());
        panel.add(btnRegistrar, gbc);

        return panel;
    }

    private JPanel crearPanelTablaUsuarios() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Listado de Usuarios Registrados"));

        String[] columnas = {"ID", "Nombre", "Email", "CUIL", "Ingresos"};
        Object[][] datos = {
                {1, "Juan Pérez", "juan@mail.com", "20-35123456-8", 250000.0},
                {2, "Ana López", "ana@mail.com", "27-38987654-4", 410000.0},
                {3, "Carlos Gómez", "carlos@mail.com", "20-29111222-3", 180000.0}
        };

        modeloTabla = new DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaUsuarios = new JTable(modeloTabla);
        sorter = new TableRowSorter<>(modeloTabla);
        tablaUsuarios.setRowSorter(sorter);

        panel.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);
        return panel;
    }

    private void configurarMenuContextual() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem itemVerDetalle = new JMenuItem("Ver Detalle");
        JMenuItem itemEliminar = new JMenuItem("Eliminar Usuario");

        itemVerDetalle.addActionListener(e -> {
            int row = tablaUsuarios.getSelectedRow();
            if (row != -1) {
                int modelRow = tablaUsuarios.convertRowIndexToModel(row);
                String nombre = (String) modeloTabla.getValueAt(modelRow, 1);
                JOptionPane.showMessageDialog(this, "Detalle del usuario: " + nombre, "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        itemEliminar.addActionListener(e -> {
            int row = tablaUsuarios.getSelectedRow();
            if (row != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Eliminar usuario seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    int modelRow = tablaUsuarios.convertRowIndexToModel(row);
                    modeloTabla.removeRow(modelRow);
                    JOptionPane.showMessageDialog(this, "Usuario eliminado correctamente.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una fila primero.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        popupMenu.add(itemVerDetalle);
        popupMenu.add(itemEliminar);

        tablaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { evaluarPopup(e); }
            @Override
            public void mouseReleased(MouseEvent e) { evaluarPopup(e); }

            private void evaluarPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = tablaUsuarios.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < tablaUsuarios.getRowCount()) {
                        tablaUsuarios.setRowSelectionInterval(row, row);
                    }
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private JPanel crearPanelEstado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        panel.add(progressBar, BorderLayout.EAST);
        return panel;
    }

    private void simularOperacionesBD(String mensajeTarea) {
        progressBar.setVisible(true);
        progressBar.setValue(0);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i <= 100; i += 20) {
                    Thread.sleep(150);
                    publish(i);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                progressBar.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                JOptionPane.showMessageDialog(VentanaPrincipal.this, mensajeTarea + "\nOperación finalizada.", "BD Info", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        worker.execute();
    }

    private void agregarUsuario() {
        String nombre = txtNombre.getText().trim();
        String email = txtEmail.getText().trim();
        String cuil = txtCuil.getText().trim();
        Double ingresos = (Double) spinnerIngresos.getValue();

        if (nombre.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar Nombre y Email.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int nuevoId = modeloTabla.getRowCount() + 1;
        modeloTabla.addRow(new Object[]{nuevoId, nombre, email, cuil, ingresos});

        txtNombre.setText("");
        txtEmail.setText("");
        txtCuil.setValue(null);
        spinnerIngresos.setValue(150000.0);

        JOptionPane.showMessageDialog(this, "Usuario registrado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
}