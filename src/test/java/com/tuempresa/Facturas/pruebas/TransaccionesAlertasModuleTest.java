package com.tuempresa.Facturas.pruebas;

import com.tuempresa.Facturas.modelo.AlertaStock;
import com.tuempresa.Facturas.modelo.HistorialHorneado;
import com.tuempresa.Facturas.modelo.Ingrediente;
import com.tuempresa.Facturas.modelo.Producto;
import com.tuempresa.Facturas.modelo.RecetaItem;
import org.openxava.jpa.XPersistence;
import org.openxava.tests.ModuleTestBase;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransaccionesAlertasModuleTest extends ModuleTestBase {

    private static final String PRODUCTO_DESCRIPCION = "JUNIT PRODUCTO HORNEADO ALERTA";
    private static final String INGREDIENTE_NOMBRE = "JUNIT INGREDIENTE ALERTA";

    private Integer productoNumero;
    private Integer ingredienteId;

    public TransaccionesAlertasModuleTest(String testName) {
        super(testName, "Producto");
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

    public void testConfirmarHorneadoDescuentaInventarioYRegistraAuditoriaYAlerta() throws Exception {
        login("admin", "admin");

        setValue("numero", String.valueOf(productoNumero));
        execute("CRUD.refresh");
        assertValue("descripcion", PRODUCTO_DESCRIPCION);

        execute("Horneado.confirmarHorneado");
        assertNoErrors();

        XPersistence.reset();

        Ingrediente ingrediente = XPersistence.getManager().find(Ingrediente.class, ingredienteId);
        assertEquals(0, ingrediente.getStockActual().compareTo(new BigDecimal("200.0000")));

        Long historiales = XPersistence.getManager()
            .createQuery(
                "select count(h) from HistorialHorneado h " +
                "where h.producto.numero = :productoNumero and h.cantidadHorneada = :cantidad and h.codigoDiagnostico = :codigo",
                Long.class
            )
            .setParameter("productoNumero", productoNumero)
            .setParameter("cantidad", 100)
            .setParameter("codigo", "STOCK_OK")
            .getSingleResult();

        Long alertas = XPersistence.getManager()
            .createQuery(
                "select count(a) from AlertaStock a " +
                "where a.ingrediente.id = :ingredienteId and a.nivelAlerta = :nivel",
                Long.class
            )
            .setParameter("ingredienteId", ingredienteId)
            .setParameter("nivel", "CRITICO")
            .getSingleResult();

        assertEquals(Long.valueOf(1), historiales);
        assertEquals(Long.valueOf(1), alertas);
    }

    private void crearDatosPrueba() {
        EntityManager manager = XPersistence.getManager();

        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setNombre(INGREDIENTE_NOMBRE);
        ingrediente.setUnidadMedida("gramos");
        ingrediente.setEsInventariable(true);
        ingrediente.setStockActual(new BigDecimal("1200.0000"));
        ingrediente.setStockMinimo(new BigDecimal("500.0000"));
        ingrediente.setCostoUnitario(new BigDecimal("0.5000"));
        manager.persist(ingrediente);

        Producto producto = new Producto();
        producto.setDescripcion(PRODUCTO_DESCRIPCION);
        producto.setPrecio(new BigDecimal("3.50"));

        RecetaItem item = new RecetaItem();
        item.setProducto(producto);
        item.setIngrediente(ingrediente);
        item.setCantidadGramos(new BigDecimal("10.0000"));

        producto.setRecetaItems(new ArrayList<>());
        producto.getRecetaItems().add(item);

        manager.persist(producto);
        XPersistence.commit();
        XPersistence.reset();

        productoNumero = producto.getNumero();
        ingredienteId = ingrediente.getId();
    }

    private void limpiarDatosPrueba() {
        EntityManager manager = XPersistence.getManager();

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
