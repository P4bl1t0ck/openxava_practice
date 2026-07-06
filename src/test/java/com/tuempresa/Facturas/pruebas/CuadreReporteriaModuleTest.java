package com.tuempresa.Facturas.pruebas;

import com.tuempresa.Facturas.modelo.AlertaStock;
import com.tuempresa.Facturas.modelo.CierreDiario;
import com.tuempresa.Facturas.modelo.CuadreAdministrativo;
import com.tuempresa.Facturas.modelo.DetalleCierre;
import com.tuempresa.Facturas.modelo.HistorialHorneado;
import com.tuempresa.Facturas.modelo.Ingrediente;
import com.tuempresa.Facturas.modelo.Prediccion;
import com.tuempresa.Facturas.modelo.Producto;
import com.tuempresa.Facturas.modelo.RecetaItem;
import org.openxava.jpa.XPersistence;
import org.openxava.tests.ModuleTestBase;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CuadreReporteriaModuleTest extends ModuleTestBase {

    private static final String PRODUCTO_DESCRIPCION = "JUNIT TABLERO EFICIENCIA";
    private static final String INGREDIENTE_NOMBRE = "000 JUNIT INGREDIENTE REPORTE";
    private static final String ALERTA_MENSAJE = "JUNIT ALERTA PARA REPORTE";

    private String cuadreId;
    private Integer productoNumero;

    public CuadreReporteriaModuleTest(String testName) {
        super(testName, "CuadreAdministrativo");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        limpiarDatosPrueba();
        crearDatosPrueba();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            limpiarDatosPrueba();
        }
        finally {
            super.tearDown();
        }
    }

    public void testCuadreAdministrativoCalculaMetricasFinancieras() throws Exception {
        login("admin", "admin");

        setValue("id", cuadreId);
        execute("CRUD.refresh");

        assertValue("producto.numero", String.valueOf(productoNumero));
        assertValue("cantidadSugeridaPmp", "120");
        assertValue("cantidadHorneadaReal", "100");
        assertValue("cantidadVendidaReal", "90");
        assertValue("cantidadMermaReal", "10");
        assertValue("costoHundido", "10.00");
        assertValue("costoOportunidad", "70.00");
    }

    public void testOrdenCompraAutomaticaGeneraPdfDesdeAlertasCriticas() throws Exception {
        login("admin", "admin");

        changeModule("AlertaStock");
        execute("Mode.list");
        assertAction("AlertaStockReporte.generarOrdenCompraAutomatica");

        execute("AlertaStockReporte.generarOrdenCompraAutomatica");

        assertPopupPDFLine(1, "Orden de Compra Automatica");
        assertPopupPDFLine(5, ALERTA_MENSAJE);
    }

    private void crearDatosPrueba() {
        EntityManager manager = XPersistence.getManager();
        Date hoy = new Date();

        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setNombre(INGREDIENTE_NOMBRE);
        ingrediente.setUnidadMedida("gramos");
        ingrediente.setEsInventariable(true);
        ingrediente.setStockActual(new BigDecimal("2.0000"));
        ingrediente.setStockMinimo(new BigDecimal("5.0000"));
        ingrediente.setCostoUnitario(new BigDecimal("0.5000"));
        manager.persist(ingrediente);

        Producto producto = new Producto();
        producto.setDescripcion(PRODUCTO_DESCRIPCION);
        producto.setPrecio(new BigDecimal("3.50"));

        RecetaItem item = new RecetaItem();
        item.setProducto(producto);
        item.setIngrediente(ingrediente);
        item.setCantidadGramos(new BigDecimal("2.0000"));

        producto.setRecetaItems(new ArrayList<>());
        producto.getRecetaItems().add(item);
        manager.persist(producto);

        Prediccion prediccion = new Prediccion();
        prediccion.setFecha(hoy);
        prediccion.setProducto(producto);
        prediccion.setCantidadSugerida(120);
        manager.persist(prediccion);

        HistorialHorneado historial = new HistorialHorneado();
        historial.setFechaHorneado(hoy);
        historial.setUsuario("admin");
        historial.setProducto(producto);
        historial.setCantidadHorneada(100);
        historial.setCodigoDiagnostico("STOCK_OK");
        manager.persist(historial);

        DetalleCierre detalle = new DetalleCierre();
        detalle.setProducto(producto);
        detalle.setCantidadVendida(90);
        detalle.setCantidadMerma(10);

        CierreDiario cierre = new CierreDiario();
        cierre.setFechaCierre(hoy);
        cierre.setEstado("CERRADO");
        cierre.setAuditadoPor("JUNIT");
        cierre.setDetalleCierres(new ArrayList<>());
        cierre.getDetalleCierres().add(detalle);
        manager.persist(cierre);

        AlertaStock alerta = new AlertaStock();
        alerta.setFechaAlerta(hoy);
        alerta.setIngrediente(ingrediente);
        alerta.setNivelAlerta("CRITICO");
        alerta.setStockProyectadoTresDias(new BigDecimal("2.0000"));
        alerta.setMensaje(ALERTA_MENSAJE);
        manager.persist(alerta);

        CuadreAdministrativo cuadre = new CuadreAdministrativo();
        cuadre.setFechaAnalisis(hoy);
        cuadre.setProducto(producto);
        manager.persist(cuadre);

        XPersistence.commit();
        XPersistence.reset();

        cuadreId = cuadre.getId();
        productoNumero = producto.getNumero();
    }

    private void limpiarDatosPrueba() {
        EntityManager manager = XPersistence.getManager();

        eliminarTodos(
            manager.createQuery(
                "select c from CuadreAdministrativo c where c.producto.descripcion = :descripcion",
                CuadreAdministrativo.class
            )
            .setParameter("descripcion", PRODUCTO_DESCRIPCION)
            .getResultList()
        );

        eliminarTodos(
            manager.createQuery(
                "select a from AlertaStock a where a.ingrediente.nombre = :nombre",
                AlertaStock.class
            )
            .setParameter("nombre", INGREDIENTE_NOMBRE)
            .getResultList()
        );

        eliminarTodos(
            manager.createQuery(
                "select h from HistorialHorneado h where h.producto.descripcion = :descripcion",
                HistorialHorneado.class
            )
            .setParameter("descripcion", PRODUCTO_DESCRIPCION)
            .getResultList()
        );

        eliminarTodos(
            manager.createQuery(
                "select p from Prediccion p where p.producto.descripcion = :descripcion",
                Prediccion.class
            )
            .setParameter("descripcion", PRODUCTO_DESCRIPCION)
            .getResultList()
        );

        eliminarTodos(
            manager.createQuery(
                "select c from CierreDiario c join c.detalleCierres d where d.producto.descripcion = :descripcion",
                CierreDiario.class
            )
            .setParameter("descripcion", PRODUCTO_DESCRIPCION)
            .getResultList()
        );

        eliminarTodos(
            manager.createQuery(
                "select p from Producto p where p.descripcion = :descripcion",
                Producto.class
            )
            .setParameter("descripcion", PRODUCTO_DESCRIPCION)
            .getResultList()
        );

        eliminarTodos(
            manager.createQuery(
                "select i from Ingrediente i where i.nombre = :nombre",
                Ingrediente.class
            )
            .setParameter("nombre", INGREDIENTE_NOMBRE)
            .getResultList()
        );

        XPersistence.commit();
        XPersistence.reset();
    }

    private void eliminarTodos(List<?> entidades) {
        EntityManager manager = XPersistence.getManager();
        for (Object entidad : entidades) {
            Object administrada = manager.contains(entidad) ? entidad : manager.merge(entidad);
            manager.remove(administrada);
        }
    }
}
