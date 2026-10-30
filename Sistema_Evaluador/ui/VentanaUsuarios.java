package Sistema_Evaluador.ui;

import Sistema_Evaluador.db.GestorBD;
import Sistema_Evaluador.modelo.Usuario;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.util.List;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.sql.SQLIntegrityConstraintViolationException;

public class VentanaUsuarios extends JFrame {

    // formulario de alta
    private JPanel panelSuperior;
    // listado + botones
    private JPanel panelInferior;
    // datos de la tabla
    private DefaultTableModel modelo;
    // tabla visible
    private JTable tablaUsuarios;
    // ordena la tabla
    private TableRowSorter<DefaultTableModel> sorter;
    // acceso a la base de datos
    private GestorBD gestor;
    // campos y botones del formulario de arriba
    private JTextField campoNombreUsuario;
    private JPasswordField campoClave;
    private JButton botonCrearUsuario;
    private JButton botonModificarUsuario;
    private JButton botonLimpiar;
    // botones del listado
    private JButton botonEliminarUsuario;
    private JButton botonRefrescar;
    // indica que hay una operación con la base en curso
    private JProgressBar barraProgreso;


    // Constructor sin parámetros: crea su propio gestor
    public VentanaUsuarios() {
        this(new GestorBD());
    }

    // Constructor que recibe el gestor de la ventana principal (así se comparte el mismo)
    public VentanaUsuarios(GestorBD gestor)
    {
        this.gestor = gestor;

        // Definicion de nombre, tamaño, lugar donde aparece y accion al cerrar
        setTitle("Gestion de Usuarios");
        setSize(600,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        /* Formulario con GridBagLayout: etiqueta + campo para usuario y clave
           (las etiquetas no se estiran y los campos sí) y abajo los botones crear, modificar y limpiar */
        panelSuperior = new JPanel(new GridBagLayout());
        campoNombreUsuario = new JTextField(20);
        campoClave = new JPasswordField(20);
        botonCrearUsuario = new JButton("Crear usuario");
        botonModificarUsuario = new JButton("Modificar usuario");
        botonLimpiar = new JButton("Limpiar");

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        panelSuperior.add(new JLabel("Usuario:"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        panelSuperior.add(campoNombreUsuario, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        panelSuperior.add(new JLabel("Clave:"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        panelSuperior.add(campoClave, c);

        JPanel panelBotonesFormulario = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonesFormulario.add(botonLimpiar);
        panelBotonesFormulario.add(botonModificarUsuario);
        panelBotonesFormulario.add(botonCrearUsuario);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        panelSuperior.add(panelBotonesFormulario, c);

        botonCrearUsuario.addActionListener(e -> crearUsuario());
        botonModificarUsuario.addActionListener(e -> modificarUsuario());
        botonLimpiar.addActionListener(e -> limpiarFormulario());

        panelInferior = new JPanel(new BorderLayout());

        /* Division de la ventana con JSplitPane (formulario arriba, tabla abajo.
           Se define la altura inicial del formulario, se agregan las flechitas para colapsar y
           el espacio extra al agrandar la ventana a la tabla*/
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelSuperior, panelInferior);
        split.setDividerLocation(150);
        split.setOneTouchExpandable(true);
        split.setResizeWeight(0.0);
        add(split);

        /* Modelo con dos columnas: ID y Usuario (la clave no se muestra).
           isCellEditable se sobreescribe y devuelve false para que no se puedan editar las celdas.
           getColumnClass. Tipo de dato de cada columna: el ID es numero y el usuario es texto*/
        modelo = new DefaultTableModel(new Object[]{"ID", "Usuario"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columna) {
                return columna == 0 ? Integer.class : String.class;
            }
        };

        tablaUsuarios = new JTable(modelo);

        /* Ordena la tabla al hacer clic en los encabezados. Es por usuario se ordena sin importar mayúsculas */
        sorter = new TableRowSorter<>(modelo);
        sorter.setSortable(0, false);
        sorter.setComparator(1, String.CASE_INSENSITIVE_ORDER);
        tablaUsuarios.setRowSorter(sorter);

        // Oculta la columna ID de la vista, pero el dato sigue en el modelo para poder eliminar
        tablaUsuarios.removeColumn(tablaUsuarios.getColumnModel().getColumn(0));

        // Al seleccionar una fila, carga el nombre en el formulario para poder modificarlo
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });

        // La tabla va dentro de un JScrollPane para tener barra de desplazamiento
        panelInferior.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);

        /* Menú contextual de la tabla con dos opciones: eliminar y refrescar.
           Cada opción llama a un método de la ventana, el mismo que van a usar los botones */
        JPopupMenu menuTabla = new JPopupMenu();
        JMenuItem itemEliminar = new JMenuItem("Eliminar usuario");
        JMenuItem itemRefrescar = new JMenuItem("Refrescar listado");

        itemEliminar.addActionListener(e -> eliminarUsuario());
        itemRefrescar.addActionListener(e -> cargarUsuarios());

        menuTabla.add(itemEliminar);
        menuTabla.add(itemRefrescar);

        // Muestra el menú al hacer clic derecho sobre la tabla
        tablaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mostrarMenu(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                mostrarMenu(e);
            }
            private void mostrarMenu(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    // Selecciona la fila donde se hizo clic derecho
                    int fila = tablaUsuarios.rowAtPoint(e.getPoint());
                    if (fila != -1) {
                        tablaUsuarios.setRowSelectionInterval(fila, fila);
                    }
                    menuTabla.show(tablaUsuarios, e.getX(), e.getY());
                }
            }
        });



        // Botones debajo de la tabla. Llaman a los mismos métodos que el menú contextual
        botonEliminarUsuario = new JButton("Eliminar usuario");
        botonRefrescar = new JButton("Refrescar");

        botonEliminarUsuario.addActionListener(e -> eliminarUsuario());
        botonRefrescar.addActionListener(e -> cargarUsuarios());

        JPanel panelBotones = new JPanel();
        panelBotones.add(botonEliminarUsuario);
        panelBotones.add(botonRefrescar);

        // Barra de progreso debajo de los botones
        barraProgreso = new JProgressBar();
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("Listo");

        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.add(panelBotones, BorderLayout.CENTER);
        panelSur.add(barraProgreso, BorderLayout.SOUTH);
        panelInferior.add(panelSur, BorderLayout.SOUTH);

        // Carga el listado al abrir la ventana
        cargarUsuarios();
    }


    /* Lee los campos, valida que no estén vacíos, inserta el usuario, limpia el formulario y refresca el listado.
       Si el usuario ya existe, avisa sin borrar lo escrito */
    private void crearUsuario() {
        String nombre = campoNombreUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());

        if (nombre.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completá usuario y clave.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Mismo largo máximo que las columnas de la tabla usuarios (VARCHAR(50) y VARCHAR(100))
        if (nombre.length() > 50) {
            JOptionPane.showMessageDialog(this, "El usuario no puede tener más de 50 caracteres.",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (clave.length() > 100) {
            JOptionPane.showMessageDialog(this, "La clave no puede tener más de 100 caracteres.",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setOcupado(true);
        try {
            gestor.insertarUsuario(nombre, clave);
            limpiarFormulario();
            cargarUsuarios();
        } catch (SQLIntegrityConstraintViolationException ex) {
            // El nombre ya existe (restricción UNIQUE de la tabla)
            JOptionPane.showMessageDialog(this, "El usuario \"" + nombre + "\" ya existe.",  "Usuario repetido", JOptionPane.WARNING_MESSAGE);
            campoNombreUsuario.requestFocus();
            campoNombreUsuario.selectAll();
        } catch (SQLException ex) {
            // Cualquier otro error de la base (conexión caída, etc.)
            JOptionPane.showMessageDialog(this, "No se pudo crear el usuario: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            setOcupado(false);
        }
    }

    // Copia el nombre del usuario seleccionado al campo de arriba y limpia la clave
    private void cargarSeleccionEnFormulario() {
        int filaVista = tablaUsuarios.getSelectedRow();
        if (filaVista == -1) {
            return;
        }
        int filaModelo = tablaUsuarios.convertRowIndexToModel(filaVista);
        campoNombreUsuario.setText((String) modelo.getValueAt(filaModelo, 1));
        campoClave.setText("");
    }

    /* Modifica el usuario seleccionado en la tabla con los datos del formulario de arriba.
       Si la clave queda vacía, se conserva la anterior */
    private void modificarUsuario() {
        int filaVista = tablaUsuarios.getSelectedRow();

        if (filaVista == -1) {
            JOptionPane.showMessageDialog(this, "Seleccioná en la tabla el usuario a modificar.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtiene el ID de la fila seleccionada
        int filaModelo = tablaUsuarios.convertRowIndexToModel(filaVista);
        int id = (int) modelo.getValueAt(filaModelo, 0);

        String nuevoNombre = campoNombreUsuario.getText().trim();
        String nuevaClave = new String(campoClave.getPassword());

        if (nuevoNombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de usuario no puede estar vacío.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Mismo largo máximo que las columnas de la tabla usuarios (VARCHAR(50) y VARCHAR(100))
        if (nuevoNombre.length() > 50) {
            JOptionPane.showMessageDialog(this, "El usuario no puede tener más de 50 caracteres.",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (nuevaClave.length() > 100) {
            JOptionPane.showMessageDialog(this, "La clave no puede tener más de 100 caracteres.",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setOcupado(true);
        try {
            boolean modificado = gestor.modificarUsuario(id, nuevoNombre, nuevaClave);
            if (!modificado) {
                JOptionPane.showMessageDialog(this, "El usuario ya no existe en la base.",
                        "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }
            limpiarFormulario();
            cargarUsuarios();
        } catch (SQLIntegrityConstraintViolationException ex) {
            // El nombre nuevo ya lo tiene otro usuario (restricción UNIQUE)
            JOptionPane.showMessageDialog(this, "El usuario \"" + nuevoNombre + "\" ya existe.",
                    "Usuario repetido", JOptionPane.WARNING_MESSAGE);
            campoNombreUsuario.requestFocus();
            campoNombreUsuario.selectAll();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo modificar el usuario: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            setOcupado(false);
        }
    }

    /* Elimina el usuario seleccionado. Si no hay fila seleccionada avisa,
       si hay, recupera el ID desde el modelo y pide confirmación */
    private void eliminarUsuario() {
        int filaVista = tablaUsuarios.getSelectedRow();

        if (filaVista == -1) {
            JOptionPane.showMessageDialog(this, "Seleccioná un usuario de la tabla.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtiene los datos de la fila
        int filaModelo = tablaUsuarios.convertRowIndexToModel(filaVista);
        int id = (int) modelo.getValueAt(filaModelo, 0);
        String nombre = (String) modelo.getValueAt(filaModelo, 1);

        // No se permite borrar el último usuario: nadie podría volver a iniciar sesión
        if (modelo.getRowCount() <= 1) {
            JOptionPane.showMessageDialog(this, "No se puede eliminar el último usuario del sistema.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,  "¿Eliminar al usuario \"" + nombre + "\"?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        // el operador canceló
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        setOcupado(true);
        try {
            boolean eliminado = gestor.eliminarUsuario(id);
            if (!eliminado) {
                JOptionPane.showMessageDialog(this,  "El usuario ya no existe en la base.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }
            // si no se limpia, el nombre del usuario borrado queda escrito en el formulario
            limpiarFormulario();
            cargarUsuarios();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar el usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            setOcupado(false);
        }
    }

    /* Trae los usuarios desde la base y los vuelca en la tabla.
       Se usa al abrir la ventana y después de crear o eliminar */
    private void cargarUsuarios() {
        setOcupado(true);
        try {
            List<Usuario> usuarios = gestor.listarUsuarios();
            // borra las filas anteriores
            modelo.setRowCount(0);
            for (Usuario u : usuarios) {
                modelo.addRow(new Object[]{u.getId(), u.getNombreUsuario()});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el listado: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // se ejecuta siempre, haya error o no
            setOcupado(false);
        }
    }

    // Activa o detiene la barra de progreso y bloquea los botones mientras hay una operación con la base
    private void setOcupado(boolean ocupado) {
        barraProgreso.setIndeterminate(ocupado);
        barraProgreso.setString(ocupado ? "Procesando..." : "Listo");
        botonCrearUsuario.setEnabled(!ocupado);
        botonEliminarUsuario.setEnabled(!ocupado);
        botonModificarUsuario.setEnabled(!ocupado);
        botonLimpiar.setEnabled(!ocupado);
        botonRefrescar.setEnabled(!ocupado);
        // Dibuja la barra en el momento; si no, Swing la pinta recién cuando termina la consulta y nunca se ve
        barraProgreso.paintImmediately(0, 0, barraProgreso.getWidth(), barraProgreso.getHeight());
    }

    // Vacía los campos y quita la selección de la tabla para dejar el formulario listo para un usuario nuevo
    private void limpiarFormulario() {
        campoNombreUsuario.setText("");
        campoClave.setText("");
        tablaUsuarios.clearSelection();
        campoNombreUsuario.requestFocusInWindow();
    }
}