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
 * Pruebas de CAJA BLANCA del motor de viabilidad ({@link Producto#calcularSimulacion()}),
 * del adaptador de presentación ({@link Producto#getSimulacionViabilidad()}) y del cálculo
 * de costo ({@link RecetaItem#getCostoItem()}).
 *
 * <p>Diseñadas para cobertura de ramas del 100% sobre el grafo de control. Son pruebas de
 * dominio puro (POJO): no levantan OpenXava ni base de datos. Ejecutar desde el IDE
 * (AGENTS.md: no usar mvn para tests).</p>
 *
 * <p>Mapa de cobertura de decisiones de {@code calcularSimulacion()}:</p>
 * <pre>
 *   D1 recetaItems==null .......... testReceta_null_devuelveSinReceta
 *   D2 recetaItems.isEmpty() ...... testSinReceta_devuelveError
 *   D3 ingrediente==null .......... testCB003_ingredienteNull_noLanzaNPE
 *   D4 cantidadGramos==null ....... testCB003_cantidadGramosNull_seOmite
 *   D5 stockActual==null .......... testCB006_stockNull_truncaCeroYNoViable
 *   D6 stock<requerido (T/F) ...... testCuelloBotella / testStockSuficiente
 *   D6 frontera (stock==req) ...... testFrontera_stockExactoEsViable
 *   D7 maxProd<cantidadFinal (T) .. testCuelloBotella
 *   D7 (F, 2º insumo holgado) ..... testDosInsumos_conservaElMasRestrictivo
 *   D8 limitante!=null (T/F) ...... testCuelloBotella / testStockSuficiente
 * </pre>
 */
public class ProductoSimulacionTest {

    // ----------------------------- Helpers -----------------------------------

    private Ingrediente ingrediente(String nombre, String costoUnitario, String stockActual) {
        Ingrediente ing = new Ingrediente();
        ing.setNombre(nombre);
        ing.setCostoUnitario(costoUnitario == null ? null : new BigDecimal(costoUnitario));
        ing.setStockActual(stockActual == null ? null : new BigDecimal(stockActual));
        return ing;
    }

    private RecetaItem recetaItem(Ingrediente ing, String gramos) {
        RecetaItem item = new RecetaItem();
        item.setIngrediente(ing);
        item.setCantidadGramos(gramos == null ? null : new BigDecimal(gramos));
        return item;
    }

    private Producto productoConReceta(RecetaItem... items) {
        Producto p = new Producto();
        Collection<RecetaItem> receta = new ArrayList<>();
        for (RecetaItem item : items) {
            receta.add(item);
        }
        p.setRecetaItems(receta);
        return p;
    }

    // ------------------------- calcularSimulacion -----------------------------

    /** D1: la colección de receta es null (no inicializada) -> ERR_SIN_RECETA sin NPE. */
    @Test
    public void testReceta_null_devuelveSinReceta() {
        Producto p = new Producto();
        p.setRecetaItems(null);

        ResultadoSimulacion r = p.calcularSimulacion();

        assertFalse(r.isViable());
        assertEquals(ResultadoSimulacion.ERR_SIN_RECETA, r.getCodigoDiagnostico());
    }

    /** D2: producto sin receta (colección vacía) -> no es posible evaluar viabilidad. */
    @Test
    public void testSinReceta_devuelveError() {
        Producto p = productoConReceta();

        ResultadoSimulacion r = p.calcularSimulacion();

        assertFalse(r.isViable());
        assertEquals(ResultadoSimulacion.ERR_SIN_RECETA, r.getCodigoDiagnostico());
    }

    /** D3 (CB-003): ítem huérfano con ingrediente null no debe lanzar NullPointerException. */
    @Test
    public void testCB003_ingredienteNull_noLanzaNPE() {
        Producto p = productoConReceta(recetaItem(null, "10"));

        ResultadoSimulacion r = p.calcularSimulacion(); // no debe lanzar excepción

        assertNotNull(r);
        assertTrue(r.isViable());
        assertEquals(ResultadoSimulacion.STOCK_OK, r.getCodigoDiagnostico());
    }

    /** D4: ingrediente presente pero sin dosis (cantidadGramos null) -> se omite el ítem. */
    @Test
    public void testCB003_cantidadGramosNull_seOmite() {
        Ingrediente harina = ingrediente("Harina", "0.5000", "100");
        Producto p = productoConReceta(recetaItem(harina, null));

        ResultadoSimulacion r = p.calcularSimulacion();

        assertNotNull(r);
        assertTrue(r.isViable());
        assertEquals(ResultadoSimulacion.STOCK_OK, r.getCodigoDiagnostico());
    }

    /** D5 (CB-006): stockActual = null trunca la capacidad a 0 y marca NO viable. */
    @Test
    public void testCB006_stockNull_truncaCeroYNoViable() {
        Ingrediente harina = ingrediente("Harina", "0.5000", null);
        Producto p = productoConReceta(recetaItem(harina, "10"));

        ResultadoSimulacion r = p.calcularSimulacion();

        assertFalse("Stock nulo no puede dictaminar viabilidad", r.isViable());
        assertEquals(ResultadoSimulacion.ERR_STOCK_NULO, r.getCodigoDiagnostico());
        assertEquals(0, r.getCantidadMaximaPosible().intValue());
        assertEquals("Harina", r.getIngredienteLimitante());
    }

    /** D6=F: stock holgado para el lote de 100 -> producción viable plena. */
    @Test
    public void testStockSuficiente_viable() {
        Ingrediente harina = ingrediente("Harina", "0.5000", "100000");
        Producto p = productoConReceta(recetaItem(harina, "10")); // requiere 1000 para 100 uds

        ResultadoSimulacion r = p.calcularSimulacion();

        assertTrue(r.isViable());
        assertEquals(ResultadoSimulacion.STOCK_OK, r.getCodigoDiagnostico());
        assertEquals(100, r.getCantidadMaximaPosible().intValue());
    }

    /** D6 frontera (BVA): stock EXACTAMENTE igual al requerido (compareTo==0) -> viable pleno. */
    @Test
    public void testFrontera_stockExactoEsViable() {
        // 10 g/ud * 100 uds = 1000 g requeridos; stock = 1000 g justos
        Ingrediente harina = ingrediente("Harina", "0.5000", "1000");
        Producto p = productoConReceta(recetaItem(harina, "10"));

        ResultadoSimulacion r = p.calcularSimulacion();

        assertTrue("Stock == requerido NO es desabastecimiento", r.isViable());
        assertEquals(ResultadoSimulacion.STOCK_OK, r.getCodigoDiagnostico());
        assertEquals(100, r.getCantidadMaximaPosible().intValue());
    }

    /** D6=T y D7=T: el insumo limitante trunca la demanda proyectada. */
    @Test
    public void testCuelloBotella_truncaDemanda() {
        // Requiere 1000 g para 100 uds, pero solo hay 550 g => máx 55 uds
        Ingrediente harina = ingrediente("Harina", "0.5000", "550");
        Producto p = productoConReceta(recetaItem(harina, "10"));

        ResultadoSimulacion r = p.calcularSimulacion();

        assertTrue(r.isViable());
        assertEquals(ResultadoSimulacion.LIMIT_INGREDIENT, r.getCodigoDiagnostico());
        assertEquals(55, r.getCantidadMaximaPosible().intValue());
        assertEquals("Harina", r.getIngredienteLimitante());
    }

    /** D7=F y bucle con 2 iteraciones: conserva el insumo MÁS restrictivo, ignora el holgado. */
    @Test
    public void testDosInsumos_conservaElMasRestrictivo() {
        // Harina: 20 g/ud => 2000 req, stock 600 => máx 30 uds (más restrictivo)
        Ingrediente harina = ingrediente("Harina", "0.5000", "600");
        // Sal: 10 g/ud => 1000 req, stock 550 => máx 55 uds (menos restrictivo => D7=F)
        Ingrediente sal = ingrediente("Sal", "0.2000", "550");
        Producto p = productoConReceta(recetaItem(harina, "20"), recetaItem(sal, "10"));

        ResultadoSimulacion r = p.calcularSimulacion();

        assertEquals(ResultadoSimulacion.LIMIT_INGREDIENT, r.getCodigoDiagnostico());
        assertEquals(30, r.getCantidadMaximaPosible().intValue());
        assertEquals("Harina", r.getIngredienteLimitante());
    }

    // ------------------- getSimulacionViabilidad (adaptador) ------------------
    // Cubre los 4 casos alcanzables del switch de ResultadoSimulacion.formatearMensaje()

    @Test
    public void testMensaje_stockOk() {
        Producto p = productoConReceta(recetaItem(ingrediente("Harina", "0.5", "100000"), "10"));
        assertTrue(p.getSimulacionViabilidad().contains("PRODUCCION VIABLE"));
    }

    @Test
    public void testMensaje_limitado() {
        Producto p = productoConReceta(recetaItem(ingrediente("Harina", "0.5", "550"), "10"));
        String msg = p.getSimulacionViabilidad();
        assertTrue(msg.contains("WARNING OPERATIVO"));
        assertTrue(msg.contains("55"));
    }

    @Test
    public void testMensaje_stockNulo() {
        Producto p = productoConReceta(recetaItem(ingrediente("Harina", "0.5", null), "10"));
        assertTrue(p.getSimulacionViabilidad().contains("CB-006"));
    }

    @Test
    public void testMensaje_sinReceta() {
        assertTrue(productoConReceta().getSimulacionViabilidad().contains("no tiene una receta"));
    }

    // --------------------------- getCostoItem ---------------------------------

    /** Camino feliz: costo = costoUnitario * cantidadGramos. */
    @Test
    public void testCostoItem_calculoCorrecto() {
        RecetaItem item = recetaItem(ingrediente("Harina", "0.5000", "100"), "10");
        assertEquals(0, item.getCostoItem().compareTo(new BigDecimal("5.0000")));
    }

    /** Fix NPE: costoUnitario nulo -> ZERO. */
    @Test
    public void testCostoItem_costoUnitarioNull_retornaZero() {
        RecetaItem item = recetaItem(ingrediente("Harina", null, "100"), "10");
        assertEquals(0, item.getCostoItem().compareTo(BigDecimal.ZERO));
    }

    /** ingrediente nulo -> ZERO sin NPE. */
    @Test
    public void testCostoItem_ingredienteNull_retornaZero() {
        RecetaItem item = recetaItem(null, "10");
        assertEquals(0, item.getCostoItem().compareTo(BigDecimal.ZERO));
    }

    /** cantidadGramos nula -> ZERO sin NPE. */
    @Test
    public void testCostoItem_cantidadGramosNull_retornaZero() {
        RecetaItem item = recetaItem(ingrediente("Harina", "0.5000", "100"), null);
        assertEquals(0, item.getCostoItem().compareTo(BigDecimal.ZERO));
    }
}
