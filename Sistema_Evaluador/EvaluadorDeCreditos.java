package Sistema_Evaluador;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// Punto de entrada del sistema(tiene mas sentido ventanaLogin): abre la ventana de login en el hilo de eventos de Swing
public class EvaluadorDeCreditos {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new VentanaLogin().setVisible(true);
        });
    }
}
