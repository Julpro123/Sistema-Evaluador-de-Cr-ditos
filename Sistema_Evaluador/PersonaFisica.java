package Sistema_Evaluador;

public class PersonaFisica extends Cliente {

    public PersonaFisica(String nombre, double ingresos, double gastos, boolean tieneBuenHistorial) {
        super(nombre, ingresos, gastos, tieneBuenHistorial);
    }

    @Override
    public double evaluarRiesgo() {
        // Regla: Si no tiene buen historial, el riesgo es nulo (o máximo según la interpretación, 
        // aquí lo manejamos como 0.0 o un indicador base)
        if (!isTieneBuenHistorial()) {
            return 0.0; 
        }
        
        // Se calcula el margen entre ingresos y gastos y se compara con un umbral relativo (ej. 30% de los ingresos)
        double margen = getIngresos() - getGastos();
        double umbral = getIngresos() * 0.30;
        
        // Retorna un puntaje de riesgo simulado
        return (margen >= umbral) ? 0.1 : 0.7; 
    }
}
