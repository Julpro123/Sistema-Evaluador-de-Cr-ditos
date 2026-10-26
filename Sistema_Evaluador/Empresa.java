package Sistema_Evaluador;

public class Empresa extends Cliente {
    private String rubro;
    private String director;

    public Empresa(String nombre, double ingresos, double gastos, boolean tieneBuenHistorial, String rubro, String director) {
        super(nombre, ingresos, gastos, tieneBuenHistorial);
        this.rubro = rubro;
        this.director = director;
    }

    // Métodos de acceso propios de Empresa
    public String getRubro() {
        return rubro;
    }

    public String getDirector() {
        return director;
    }

    @Override
    public double evaluarRiesgo() {
        // Controlar el caso en que los gastos sean cero para evitar división inválida
        if (getGastos() == 0) {
            return (getIngresos() > 0) ? 0.0 : 1.0;
        }

        // Se calcula el ratio entre ingresos y gastos
        double ratio = getIngresos() / getGastos();
        double umbralRatio = 1.5; // Umbral requerido para empresas

        // Si el ratio supera el umbral, el riesgo es bajo; de lo contrario, es alto
        return (ratio >= umbralRatio) ? 0.2 : 0.8;
    }
}