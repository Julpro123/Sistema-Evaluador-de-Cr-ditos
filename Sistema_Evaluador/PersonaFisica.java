package Sistema_Evaluador;

public class PersonaFisica extends Cliente {

    public PersonaFisica(String nombre, double ingresos, double gastos, boolean tieneBuenHistorial) {
        super(nombre, ingresos, gastos, tieneBuenHistorial);
    }

    @Override
    public double evaluarRiesgo() {
        // Convención: 0 = no apto, 1 = apto
        // Regla: si no tiene buen historial, el riesgo es nulo (no apto)
        if (!isTieneBuenHistorial()) {
            return 0.0;
        }

        // Se calcula el margen entre ingresos y gastos y se compara con un umbral relativo (30% de los ingresos)
        double margen = getIngresos() - getGastos();
        double umbral = getIngresos() * 0.30;

        // Si el margen alcanza el umbral es apto; si no, no apto
        return (margen >= umbral) ? 1.0 : 0.0;
    }
}
