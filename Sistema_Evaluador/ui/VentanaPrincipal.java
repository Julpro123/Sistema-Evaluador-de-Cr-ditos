package Sistema_Evaluador.ui;

import Sistema_Evaluador.db.GestorBD;
import Sistema_Evaluador.modelo.Cliente;
import Sistema_Evaluador.modelo.Empresa;
import Sistema_Evaluador.modelo.PersonaFisica;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class VentanaPrincipal extends JFrame {
    private final GestorBD gestor;

    private final JTabbedPane panelPestanias = new JTabbedPane();
    private final JComboBox<String> comboTipoCliente = new JComboBox<>(new String[] { "Persona Física", "Empresa" });
    private final JTextField campoNombre = new JTextField(20);
    private final JSpinner spinnerIngresos = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1_000_000_000.0, 1000.0));
    private final JSpinner spinnerGastos = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1_000_000_000.0, 1000.0));
    private final JCheckBox checkHistorial = new JCheckBox("Tiene buen historial crediticio");
    private final JLabel etiquetaRubro = new JLabel("Rubro:");
    private final JTextField campoRubro = new JTextField(20);
    private final JLabel etiquetaDirector = new JLabel("Director:");
    private final JTextField campoDirector = new JTextField(20);
    private final JButton botonGuardar = new JButton();
    private final JProgressBar barraProgreso = new JProgressBar();
    private final DefaultTableModel modeloClientes = new DefaultTableModel(
            new Object[] { "ID", "Nombre", "Ingresos", "Gastos", "Historial", "Tipo", "Resultado" }, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tablaClientes = new JTable(modeloClientes);

    private Action accionGuardar;
    private Action accionRefrescar;
    private Action accionUsuarios;
    private Action accionSalir;

    public VentanaPrincipal(GestorBD gestor) {
        super("Sistema Evaluador de Créditos");
        this.gestor = gestor;
        crearAcciones();
        configurarComponentes();
        construirVentana();
        actualizarCamposTipo();
        refrescarClientes();
    }

    private void crearAcciones() {
        accionGuardar = new AbstractAction("Guardar") {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarCliente();
            }
        };
        accionRefrescar = new AbstractAction("Refrescar") {
            @Override
            public void actionPerformed(ActionEvent e) {
                refrescarClientes();
            }
        };
        accionUsuarios = new AbstractAction("Usuarios") {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirVentanaUsuarios();
            }
        };
        accionSalir = new AbstractAction("Salir") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                System.exit(0);
            }
        };
    }

    private void configurarComponentes() {
        spinnerIngresos.setEditor(new JSpinner.NumberEditor(spinnerIngresos, "#,##0.00"));
        spinnerGastos.setEditor(new JSpinner.NumberEditor(spinnerGastos, "#,##0.00"));
        botonGuardar.setAction(accionGuardar);
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("Listo");
        tablaClientes.setAutoCreateRowSorter(true);
        tablaClientes.setFillsViewportHeight(true);
        comboTipoCliente.addActionListener(e -> actualizarCamposTipo());
    }

    private void construirVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 560);
        setMinimumSize(new Dimension(700, 480));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setJMenuBar(construirMenu());
        add(construirBarraHerramientas(), BorderLayout.NORTH);

        panelPestanias.addTab("Nuevo análisis", construirFormulario());
        panelPestanias.addTab("Base de datos", new JScrollPane(tablaClientes));
        add(panelPestanias, BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout());
        inferior.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        inferior.add(barraProgreso, BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);
    }

    private JMenuBar construirMenu() {
        JMenuBar barraMenu = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.add(new JMenuItem(accionGuardar));
        menuArchivo.add(new JMenuItem(accionRefrescar));
        menuArchivo.addSeparator();
        menuArchivo.add(new JMenuItem(accionSalir));

        JMenu menuAdministracion = new JMenu("Administración");
        menuAdministracion.add(new JMenuItem(accionUsuarios));

        barraMenu.add(menuArchivo);
        barraMenu.add(menuAdministracion);
        return barraMenu;
    }

    private JToolBar construirBarraHerramientas() {
        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.add(new JButton(accionGuardar));
        barra.add(new JButton(accionRefrescar));
        barra.addSeparator();
        barra.add(new JButton(accionUsuarios));
        return barra;
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int fila = 0;
        agregarFila(panel, c, fila++, new JLabel("Tipo de cliente:"), comboTipoCliente);
        agregarFila(panel, c, fila++, new JLabel("Nombre:"), campoNombre);
        agregarFila(panel, c, fila++, new JLabel("Ingresos:"), spinnerIngresos);
        agregarFila(panel, c, fila++, new JLabel("Gastos:"), spinnerGastos);
        agregarFila(panel, c, fila++, new JLabel("Historial:"), checkHistorial);
        agregarFila(panel, c, fila++, etiquetaRubro, campoRubro);
        agregarFila(panel, c, fila++, etiquetaDirector, campoDirector);

        c.gridx = 0;
        c.gridy = fila;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(18, 6, 6, 6);
        panel.add(botonGuardar, c);

        c.gridy = fila + 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), c);

        return panel;
    }

    private void agregarFila(JPanel panel, GridBagConstraints c, int fila, JLabel etiqueta, java.awt.Component campo) {
        c.gridy = fila;
        c.gridwidth = 1;
        c.weighty = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(etiqueta, c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(campo, c);
    }

    private void actualizarCamposTipo() {
        boolean esEmpresa = "Empresa".equals(comboTipoCliente.getSelectedItem());
        etiquetaRubro.setVisible(esEmpresa);
        campoRubro.setVisible(esEmpresa);
        etiquetaDirector.setVisible(esEmpresa);
        campoDirector.setVisible(esEmpresa);
        if (!esEmpresa) {
            campoRubro.setText("");
            campoDirector.setText("");
        }
        revalidate();
        repaint();
    }

    private void setOcupado(boolean ocupado) {
        accionGuardar.setEnabled(!ocupado);
        accionRefrescar.setEnabled(!ocupado);
        accionUsuarios.setEnabled(!ocupado);
        barraProgreso.setIndeterminate(ocupado);
        barraProgreso.setString(ocupado ? "Procesando..." : "Listo");
    }

    private double leerSpinner(JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (java.text.ParseException ex) {
            spinner.setValue(spinner.getValue());
        }
        return ((Number) spinner.getValue()).doubleValue();
    }

    private void guardarCliente() {
        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Dato faltante", JOptionPane.WARNING_MESSAGE);
            campoNombre.requestFocus();
            return;
        }

        double ingresos = leerSpinner(spinnerIngresos);
        double gastos = leerSpinner(spinnerGastos);
        boolean historial = checkHistorial.isSelected();
        boolean esEmpresa = "Empresa".equals(comboTipoCliente.getSelectedItem());

        final Cliente cliente;
        if (esEmpresa) {
            String rubro = campoRubro.getText().trim();
            String director = campoDirector.getText().trim();
            if (rubro.isEmpty() || director.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El rubro y el director son obligatorios para una empresa.",
                        "Dato faltante", JOptionPane.WARNING_MESSAGE);
                return;
            }
            cliente = new Empresa(nombre, ingresos, gastos, historial, rubro, director);
        } else {
            cliente = new PersonaFisica(nombre, ingresos, gastos, historial);
        }

        setOcupado(true);
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int id = gestor.guardarCliente(cliente);
                gestor.guardarDetalle(id, cliente);
                return id;
            }

            @Override
            protected void done() {
                try {
                    int id = get();
                    boolean apto = cliente.evaluarRiesgo() > 0;
                    JOptionPane.showMessageDialog(VentanaPrincipal.this,
                            "Cliente guardado con ID " + id + ".\nResultado de la evaluación: "
                                    + (apto ? "APTO para préstamo" : "NO APTO para préstamo"),
                            "Evaluación completada",
                            apto ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                    limpiarFormulario();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    mostrarError("No se pudo guardar el cliente", ex.getCause());
                } finally {
                    setOcupado(false);
                }
                refrescarClientes();
            }
        }.execute();
    }

    private void refrescarClientes() {
        setOcupado(true);
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return gestor.listarClientes();
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> filas = get();
                    modeloClientes.setRowCount(0);
                    for (Object[] fila : filas) {
                        modeloClientes.addRow(fila);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    mostrarError("No se pudo cargar el listado de clientes", ex.getCause());
                } finally {
                    setOcupado(false);
                }
            }
        }.execute();
    }

    private void abrirVentanaUsuarios() {
        setOcupado(true);
        SwingUtilities.invokeLater(() -> {
            try {
                VentanaUsuarios ventana = new VentanaUsuarios(gestor);
                ventana.setVisible(true);
            } catch (RuntimeException ex) {
                mostrarError("No se pudo abrir la gestión de usuarios", ex);
            } finally {
                setOcupado(false);
            }
        });
    }

    private void limpiarFormulario() {
        campoNombre.setText("");
        campoRubro.setText("");
        campoDirector.setText("");
        spinnerIngresos.setValue(0.0);
        spinnerGastos.setValue(0.0);
        checkHistorial.setSelected(false);
        comboTipoCliente.setSelectedIndex(0);
    }

    private void mostrarError(String titulo, Throwable causa) {
        String detalle = causa == null ? "Error desconocido." : causa.getMessage();
        JOptionPane.showMessageDialog(this, titulo + ":\n" + detalle, "Error", JOptionPane.ERROR_MESSAGE);
    }
}