package com.tuempresa.Facturas.pruebas;

import org.openxava.tests.ModuleTestBase;
import org.junit.Ignore;

@Ignore("Ignorado para CI/CD sin servidor Tomcat corriendo")
public class ValidacionIngredienteTest extends ModuleTestBase {

    public ValidacionIngredienteTest(String testName) {
        super(testName, "Facturas", "Ingrediente");
    }

    public void testGuardarCostoPositivo() throws Exception {
        login("admin", "admin");
        
        // Escenario: Valores límite correctos
        execute("CRUD.new");
        setValue("nombre", "Harina Test QA");
        setValue("unidadMedida", "KG");
        setValue("costoUnitario", "1.50");
        setValue("stockActual", "10");
        setValue("esInventariable", "true");
        execute("CRUD.save");
        
        assertNoErrors();
        assertMessage("Ingrediente creado con éxito"); // Ajustar según los mensajes de la app
    }

    public void testCostoCeroONegativo() throws Exception {
        login("admin", "admin");
        
        // Escenario: Valores límite incorrectos (0 y negativos)
        execute("CRUD.new");
        setValue("nombre", "Sal Test QA");
        setValue("unidadMedida", "KG");
        setValue("costoUnitario", "-5.00"); // Dato inválido
        setValue("stockActual", "5");
        setValue("esInventariable", "true");
        execute("CRUD.save");
        
        // El sistema debe rechazar este valor mediante validaciones (ej: @Min(0))
        assertError("debe ser mayor que o igual a 0");
    }
}
