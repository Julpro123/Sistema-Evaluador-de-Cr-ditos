package Sistema_Evaluador;

public abstract class Cliente {
    // Atributos privados para garantizar el encapsulamiento
    private String nombre;
    private double ingresos;
    private double gastos;
    private boolean tieneBuenHistorial;

    // Constructor base
    public Cliente(String nombre, double ingresos, double gastos, boolean tieneBuenHistorial) {
        this.nombre = nombre;
        this.ingresos = ingresos;
        this.gastos = gastos;
        this.tieneBuenHistorial = tieneBuenHistorial;
    }

    // Métodos de acceso (Getters) solicitados por la capa de persistencia/presentación
    public String getNombre() {
        return nombre;
    }

    public double getIngresos() {
        return ingresos;
    }

    public double getGastos() {
        return gastos;
    }

    public boolean isTieneBuenHistorial() {
        return tieneBuenHistorial;
    }

    // Función abstracta para la evaluación de riesgo (cada subclase la implementa)
    public abstract double evaluarRiesgo();
}
