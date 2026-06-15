package com.tuempresa.Facturas.pruebas;

import com.tuempresa.Facturas.modelo.Ingrediente;
import com.tuempresa.Facturas.modelo.Producto;
import com.tuempresa.Facturas.modelo.RecetaItem;
import com.tuempresa.Facturas.vo.ResultadoSimulacion;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Técnica de caja negra: TRANSICIÓN DE ESTADOS aplicada al ciclo de vida de la viabilidad
 * de producción de un {@link Producto} frente al evento "confirmar horneado".
 *
 * <p>Estados derivados de {@link Producto#calcularSimulacion()}:</p>
 * <pre>
 *   SIN_RECETA      : producto sin ítems de receta            (ERR_SIN_RECETA)
 *   VIABLE          : el stock cubre el lote completo (100)    (STOCK_OK)
 *   LIMITADO        : cuello de botella, 0 &lt; capacidad &lt; 100   (LIMIT_INGREDIENT)
 *   AGOTADO         : capacidad = 0 (no produce ni 1 unidad)   (LIMIT_INGREDIENT, cap=0)
 *   BLOQUEADO_NULO  : stockActual = null                       (ERR_STOCK_NULO)
 * </pre>
 *
 * <p>Tabla de transición verificada:</p>
 * <pre>
 *   Estado actual    | Evento                         | Estado siguiente
 *   -----------------+--------------------------------+----------------
 *   SIN_RECETA       | asignarReceta(stock holgado)   | VIABLE
 *   VIABLE           | confirmarHorneado (queda &gt;lote)| VIABLE
 *   VIABLE           | confirmarHorneado (queda &lt;lote)| LIMITADO
 *   LIMITADO         | confirmarHorneado (consume todo)| AGOTADO
 *   AGOTADO          | confirmarHorneado              | AGOTADO (rechazado)
 *   *                | corromperStock (null)          | BLOQUEADO_NULO
 *   BLOQUEADO_NULO   | confirmarHorneado              | BLOQUEADO_NULO (rechazado)
 * </pre>
 *
 * <p>{@code confirmarHorneado()} replica fielmente la lógica de descuento de
 * {@code ConfirmarHorneadoAccion}: solo descuenta si la capacidad es &gt; 0, restando
 * {@code gramos * capacidad} con escala 4 y {@link RoundingMode#DOWN}, con piso en cero.</p>
 */
public class TransicionEstadosHorneadoTest {

    private static final int ESCALA = 4;

    // ----------------------- Modelo de estados --------------------------------

    private String estadoDe(Producto p) {
        ResultadoSimulacion r = p.calcularSimulacion();
        switch (r.getCodigoDiagnostico()) {
            case ResultadoSimulacion.ERR_SIN_RECETA:
                return "SIN_RECETA";
            case ResultadoSimulacion.ERR_STOCK_NULO:
                return "BLOQUEADO_NULO";
            case ResultadoSimulacion.STOCK_OK:
                return "VIABLE";
            case ResultadoSimulacion.LIMIT_INGREDIENT:
                return r.getCantidadMaximaPosible().intValue() > 0 ? "LIMITADO" : "AGOTADO";
            default:
                return "DESCONOCIDO";
        }
    }

    /** Evento confirmarHorneado: replica el descuento transaccional de ConfirmarHorneadoAccion. */
    private void confirmarHorneado(Producto p) {
        ResultadoSimulacion r = p.calcularSimulacion();
        BigDecimal capacidad = r.getCantidadMaximaPosible();
        if (capacidad == null || capacidad.compareTo(BigDecimal.ZERO) <= 0) {
            return; // rechazado: nada que descontar
        }
        for (RecetaItem item : p.getRecetaItems()) {
            if (item.getIngrediente() == null || item.getCantidadGramos() == null) {
                continue;
            }
            BigDecimal stock = item.getIngrediente().getStockActual();
            if (stock == null) {
                continue;
            }
            BigDecimal consumido = item.getCantidadGramos().multiply(capacidad).setScale(ESCALA, RoundingMode.DOWN);
            BigDecimal nuevo = stock.subtract(consumido).max(BigDecimal.ZERO).setScale(ESCALA, RoundingMode.DOWN);
            item.getIngrediente().setStockActual(nuevo);
        }
    }

    // ----------------------------- Helpers ------------------------------------

    private Producto productoVacio() {
        Producto p = new Producto();
        p.setRecetaItems(new ArrayList<>());
        return p;
    }

    private void asignarReceta(Producto p, Ingrediente ing, String gramos) {
        Collection<RecetaItem> receta = new ArrayList<>();
        RecetaItem item = new RecetaItem();
        item.setIngrediente(ing);
        item.setCantidadGramos(new BigDecimal(gramos));
        receta.add(item);
        p.setRecetaItems(receta);
    }

    private Ingrediente ingrediente(String nombre, String stock) {
        Ingrediente ing = new Ingrediente();
        ing.setNombre(nombre);
        ing.setCostoUnitario(new BigDecimal("0.5000"));
        ing.setStockActual(stock == null ? null : new BigDecimal(stock));
        return ing;
    }

    // ------------------------- Recorrido de estados ---------------------------

    /**
     * Recorre la secuencia completa de transiciones del ciclo de vida.
     * Harina = 10 g/ud, lote = 100 => 1000 g por horneado pleno.
     */
    @Test
    public void recorridoCompletoDeEstados() {
        Producto p = productoVacio();

        // Estado inicial
        assertEquals("SIN_RECETA", estadoDe(p));

        // SIN_RECETA --asignarReceta(stock 2500, holgado)--> VIABLE
        Ingrediente harina = ingrediente("Harina", "2500");
        asignarReceta(p, harina, "10");
        assertEquals("VIABLE", estadoDe(p));

        // VIABLE --confirmarHorneado (consume 1000, quedan 1500 > 1000)--> VIABLE
        confirmarHorneado(p);
        assertEquals("VIABLE", estadoDe(p));
        assertEquals(0, harina.getStockActual().compareTo(new BigDecimal("1500")));

        // VIABLE --confirmarHorneado (consume 1000, quedan 500 < 1000)--> LIMITADO
        confirmarHorneado(p);
        assertEquals("LIMITADO", estadoDe(p));
        assertEquals(0, harina.getStockActual().compareTo(new BigDecimal("500")));

        // LIMITADO --confirmarHorneado (cap=50, consume 500, queda 0)--> AGOTADO
        confirmarHorneado(p);
        assertEquals("AGOTADO", estadoDe(p));
        assertEquals(0, harina.getStockActual().compareTo(BigDecimal.ZERO));

        // AGOTADO --confirmarHorneado--> AGOTADO (rechazado, sin cambios)
        confirmarHorneado(p);
        assertEquals("AGOTADO", estadoDe(p));
        assertEquals(0, harina.getStockActual().compareTo(BigDecimal.ZERO));
    }

    /** Transición de corrupción de datos: cualquier estado --stock=null--> BLOQUEADO_NULO. */
    @Test
    public void transicionAEstadoBloqueadoPorStockNulo() {
        Producto p = productoVacio();
        Ingrediente harina = ingrediente("Harina", "2500");
        asignarReceta(p, harina, "10");
        assertEquals("VIABLE", estadoDe(p));

        harina.setStockActual(null); // corrupción / migración fallida
        assertEquals("BLOQUEADO_NULO", estadoDe(p));

        // BLOQUEADO_NULO --confirmarHorneado--> BLOQUEADO_NULO (rechazado, no muta)
        confirmarHorneado(p);
        assertEquals("BLOQUEADO_NULO", estadoDe(p));
    }
}
