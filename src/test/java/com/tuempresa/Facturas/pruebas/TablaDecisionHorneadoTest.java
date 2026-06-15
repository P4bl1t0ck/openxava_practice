package com.tuempresa.Facturas.pruebas;

import com.tuempresa.Facturas.modelo.Ingrediente;
import com.tuempresa.Facturas.modelo.Producto;
import com.tuempresa.Facturas.modelo.RecetaItem;
import com.tuempresa.Facturas.vo.ResultadoSimulacion;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Técnica de caja negra: TABLA DE DECISIÓN aplicada al motor de viabilidad
 * {@link Producto#calcularSimulacion()}.
 *
 * <p>El resultado depende de la combinación de tres condiciones. La tabla colapsa las
 * 2^3 combinaciones mediante condiciones "no importa" (—):</p>
 *
 * <pre>
 *  Condiciones                     | R1 | R2 | R3 | R4
 *  --------------------------------+----+----+----+----
 *  C1 ¿Producto tiene receta?      |  N |  S |  S |  S
 *  C2 ¿Algún stockActual == null?  |  — |  S |  N |  N
 *  C3 ¿Stock cubre el lote (100)?  |  — |  — |  S |  N
 *  --------------------------------+----+----+----+----
 *  Acciones / Resultado            |    |    |    |
 *  ERR_SIN_RECETA (cap=0)          |  X |    |    |
 *  ERR_STOCK_NULO (cap=0)          |    |  X |    |
 *  STOCK_OK       (cap=100)        |    |    |  X |
 *  LIMIT_INGREDIENT (0&lt;cap&lt;100)    |    |    |    |  X
 * </pre>
 */
public class TablaDecisionHorneadoTest {

    private Ingrediente ingrediente(String nombre, String stockActual) {
        Ingrediente ing = new Ingrediente();
        ing.setNombre(nombre);
        ing.setCostoUnitario(new BigDecimal("0.5000"));
        ing.setStockActual(stockActual == null ? null : new BigDecimal(stockActual));
        return ing;
    }

    private Producto productoCon(Ingrediente ing, String gramos) {
        Producto p = new Producto();
        Collection<RecetaItem> receta = new ArrayList<>();
        if (ing != null) {
            RecetaItem item = new RecetaItem();
            item.setIngrediente(ing);
            item.setCantidadGramos(new BigDecimal(gramos));
            receta.add(item);
        }
        p.setRecetaItems(receta);
        return p;
    }

    /** R1: sin receta -> ERR_SIN_RECETA, no viable, capacidad 0. */
    @Test
    public void reglaR1_sinReceta() {
        Producto p = new Producto();
        p.setRecetaItems(new ArrayList<>());

        ResultadoSimulacion r = p.calcularSimulacion();

        assertEquals(ResultadoSimulacion.ERR_SIN_RECETA, r.getCodigoDiagnostico());
        assertFalse(r.isViable());
        assertEquals(0, r.getCantidadMaximaPosible().intValue());
    }

    /** R2: con receta pero stockActual null -> ERR_STOCK_NULO, no viable, capacidad 0. */
    @Test
    public void reglaR2_stockNull() {
        Producto p = productoCon(ingrediente("Harina", null), "10");

        ResultadoSimulacion r = p.calcularSimulacion();

        assertEquals(ResultadoSimulacion.ERR_STOCK_NULO, r.getCodigoDiagnostico());
        assertFalse(r.isViable());
        assertEquals(0, r.getCantidadMaximaPosible().intValue());
    }

    /** R3: receta con stock que cubre el lote completo -> STOCK_OK, viable, capacidad 100. */
    @Test
    public void reglaR3_stockCubreLote() {
        // 10 g/ud * 100 = 1000 g requeridos; stock 5000 cubre de sobra
        Producto p = productoCon(ingrediente("Harina", "5000"), "10");

        ResultadoSimulacion r = p.calcularSimulacion();

        assertEquals(ResultadoSimulacion.STOCK_OK, r.getCodigoDiagnostico());
        assertTrue(r.isViable());
        assertEquals(100, r.getCantidadMaximaPosible().intValue());
    }

    /** R4: receta con stock insuficiente para el lote -> LIMIT_INGREDIENT, viable, 0<cap<100. */
    @Test
    public void reglaR4_stockNoCubreLote() {
        // 10 g/ud * 100 = 1000 g requeridos; stock 550 => máx 55 uds
        Producto p = productoCon(ingrediente("Harina", "550"), "10");

        ResultadoSimulacion r = p.calcularSimulacion();

        assertEquals(ResultadoSimulacion.LIMIT_INGREDIENT, r.getCodigoDiagnostico());
        assertTrue(r.isViable());
        assertEquals(55, r.getCantidadMaximaPosible().intValue());
        assertTrue(r.getCantidadMaximaPosible().intValue() > 0);
        assertTrue(r.getCantidadMaximaPosible().intValue() < 100);
    }
}
