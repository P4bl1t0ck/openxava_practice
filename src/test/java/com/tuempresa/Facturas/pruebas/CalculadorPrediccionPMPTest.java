package com.tuempresa.Facturas.pruebas;

import org.openxava.tests.ModuleTestBase;
import org.junit.Ignore;
import com.tuempresa.Facturas.calculadores.CalculadorPrediccionPMP;
import com.tuempresa.Facturas.modelo.*;
import org.openxava.jpa.XPersistence;
import javax.persistence.EntityManager;
import java.util.Date;

@Ignore("Ignorado para CI/CD sin servidor Tomcat corriendo")
public class CalculadorPrediccionPMPTest extends ModuleTestBase {

    public CalculadorPrediccionPMPTest(String testName) {
        super(testName, "Facturas", "Prediccion");
    }

    public void testPMPConVentasReales() throws Exception {
        login("admin", "admin");
        
        EntityManager em = XPersistence.getManager();
        
        // 1. Preparar un producto de prueba
        Producto p = new Producto();
        p.setNumero(9999);
        p.setDescripcion("Producto PMP Test");
        em.persist(p);
        
        // 2. Insertar 21 ventas reales
        long unDia = 86400000L;
        for(int i = 0; i < 21; i++) {
            CierreDiario c = new CierreDiario();
            c.setFechaCierre(new Date(System.currentTimeMillis() - ((21 - i) * unDia)));
            c.setEstado("Cerrado");
            
            DetalleCierre dc = new DetalleCierre();
            dc.setProducto(p);
            dc.setCantidadVendida(10); // Valor constante para simplificar el test (Promedio debe dar 10)
            dc.setCantidadMerma(0);
            c.getDetalleCierres().add(dc);
            em.persist(c);
        }
        
        em.flush();
        
        // 3. Instanciar y probar el calculador (Caja Blanca)
        CalculadorPrediccionPMP calculador = new CalculadorPrediccionPMP();
        calculador.setProductoId(9999);
        
        int resultado = (Integer) calculador.calculate();
        
        // 4. Verificación
        assertEquals("El PMP con historial constante de 10 ventas diarias debe ser 10", 10, resultado);
    }

    public void testPMPConListaVacia() throws Exception {
        login("admin", "admin");
        
        CalculadorPrediccionPMP calculador = new CalculadorPrediccionPMP();
        calculador.setProductoId(8888); // Producto que no tiene historial en BD
        
        int resultado = (Integer) calculador.calculate();
        
        assertEquals("El PMP debe retornar 0 si no hay historial", 0, resultado);
    }
}
