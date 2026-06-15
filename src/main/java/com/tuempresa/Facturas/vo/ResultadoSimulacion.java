package com.tuempresa.Facturas.vo;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Value Object inmutable que encapsula el diagnostico estructurado del motor de viabilidad
 * de produccion ({@code Producto.calcularSimulacion()}).
 *
 * <p>Aplica Clean Architecture: el dominio razona sobre codigos de estado abstractos
 * ({@code codigoDiagnostico}) y delega el formateo de texto para la interfaz a
 * {@link #formatearMensaje(int)}, manteniendo la capa de presentacion de OpenXava
 * desacoplada del nucleo de negocio.</p>
 *
 * <p>Construccion mediante <i>Static Factory Methods</i> semanticos; el constructor es
 * privado para garantizar la inmutabilidad y la coherencia de los estados.</p>
 *
 * Codigos de diagnostico posibles:
 * <ul>
 *   <li>{@code STOCK_OK}         - El stock cubre el lote proyectado completo.</li>
 *   <li>{@code LIMIT_INGREDIENT} - Cuello de botella; produccion truncada dinamicamente.</li>
 *   <li>{@code ERR_STOCK_NULO}   - stockActual = null en BD; produccion forzada a cero (CB-006).</li>
 *   <li>{@code ERR_SIN_RECETA}   - El producto no tiene receta base asignada.</li>
 * </ul>
 */
@Getter
public final class ResultadoSimulacion {

    public static final String STOCK_OK = "STOCK_OK";
    public static final String LIMIT_INGREDIENT = "LIMIT_INGREDIENT";
    public static final String ERR_STOCK_NULO = "ERR_STOCK_NULO";
    public static final String ERR_SIN_RECETA = "ERR_SIN_RECETA";

    private final boolean viable;
    private final BigDecimal cantidadMaximaPosible;
    private final String codigoDiagnostico;
    private final String ingredienteLimitante;

    private ResultadoSimulacion(boolean viable,
                                BigDecimal cantidadMaximaPosible,
                                String codigoDiagnostico,
                                String ingredienteLimitante) {
        this.viable = viable;
        this.cantidadMaximaPosible = cantidadMaximaPosible;
        this.codigoDiagnostico = codigoDiagnostico;
        this.ingredienteLimitante = ingredienteLimitante;
    }

    /** Toda la receta cubre el lote proyectado; produccion plena. */
    public static ResultadoSimulacion viable(BigDecimal cantidadTotal) {
        return new ResultadoSimulacion(true, cantidadTotal, STOCK_OK, null);
    }

    /** Existe un cuello de botella; la produccion se trunca a la capacidad real del insumo limitante. */
    public static ResultadoSimulacion limitado(BigDecimal cantidadMaxima, String nombreIngrediente) {
        return new ResultadoSimulacion(true, cantidadMaxima, LIMIT_INGREDIENT, nombreIngrediente);
    }

    /** CB-006: stock no registrado (null); la capacidad cae a cero y se aborta el procesamiento. */
    public static ResultadoSimulacion errorStockNulo(String nombreIngrediente) {
        return new ResultadoSimulacion(false, BigDecimal.ZERO, ERR_STOCK_NULO, nombreIngrediente);
    }

    /** El producto no tiene receta; no es posible evaluar la viabilidad. */
    public static ResultadoSimulacion sinReceta() {
        return new ResultadoSimulacion(false, BigDecimal.ZERO, ERR_SIN_RECETA, null);
    }

    /**
     * Traduce el codigo de diagnostico a un mensaje legible para la UI de OpenXava.
     * Pertenece a la frontera de presentacion: el nucleo de negocio nunca depende de este texto.
     *
     * @param cantidadSugerida tamano del lote proyectado evaluado por el motor.
     */
    public String formatearMensaje(int cantidadSugerida) {
        switch (codigoDiagnostico) {
            case STOCK_OK:
                return "PRODUCCION VIABLE: El stock actual de materias primas cubre "
                     + "la demanda proyectada de " + cantidadSugerida + " unidades.";
            case LIMIT_INGREDIENT:
                return "WARNING OPERATIVO: Cuello de botella en '" + ingredienteLimitante
                     + "'. La prediccion original de " + cantidadSugerida
                     + " unidades ha sido truncada. Produccion maxima real permitida: "
                     + cantidadMaximaPosible.intValue() + " unidades.";
            case ERR_STOCK_NULO:
                return "ERROR CRITICO [CB-006]: Stock no registrado (null) para '"
                     + ingredienteLimitante
                     + "'. Produccion detenida. Capacidad maxima: 0 unidades.";
            case ERR_SIN_RECETA:
                return "ERROR CRITICO: No se puede evaluar la viabilidad. "
                     + "El producto no tiene una receta base asignada.";
            default:
                return "Estado del sistema: " + codigoDiagnostico;
        }
    }
}
