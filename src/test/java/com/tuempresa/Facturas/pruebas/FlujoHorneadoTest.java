package com.tuempresa.Facturas.pruebas;

import org.openxava.tests.ModuleTestBase;
import org.junit.Ignore;

@Ignore("Ignorado para CI/CD sin servidor Tomcat corriendo")
public class FlujoHorneadoTest extends ModuleTestBase {

    public FlujoHorneadoTest(String testName) {
        super(testName, "Facturas", "Producto"); // El CU4 de horneado se lanza desde Producto
    }

    public void testHorneadoConStockSuficiente() throws Exception {
        login("admin", "admin");
        
        // Escenario: Hay stock suficiente. Seleccionamos el primer producto en la lista.
        execute("List.viewDetail", "row=0");
        
        // Ejecutamos la acción personalizada de confirmar horneado (US-003)
        execute("Horneado.confirmarHorneado");
        
        // El sistema no debería arrojar errores (asumiendo que tiene stock)
        assertNoErrors();
    }

    public void testHorneadoSinStockLanzaError() throws Exception {
        login("admin", "admin");
        
        // Escenario: Intentamos hornear algo para lo cual no tenemos ingredientes.
        // Asumiendo que el row=1 es un producto del cual sabemos que falta stock (o provocamos una alerta).
        execute("List.viewDetail", "row=1");
        
        execute("Horneado.confirmarHorneado");
        
        // El sistema debe arrojar validaciones o excepciones si no hay stock
        // Dependiendo de cómo se implementó US-003, esto causaría un error en pantalla.
        assertError("No hay stock suficiente");
    }
}
