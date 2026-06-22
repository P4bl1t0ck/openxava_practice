package com.tuempresa.Facturas.pruebas;

import com.tuempresa.Facturas.modelo.Ingrediente;
import com.tuempresa.Facturas.modelo.Producto;
import com.tuempresa.Facturas.modelo.RecetaItem;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Pruebas de CAJA NEGRA (Partición de Equivalencia + Análisis de Valores Límite) sobre las
 * restricciones Jakarta Bean Validation declaradas en las entidades del catálogo.
 *
 * <p>Se valida propiedad por propiedad con {@link Validator#validateProperty}, aislando cada
 * campo de los demás. El interpolador {@link ParameterMessageInterpolator} evita la dependencia
 * de un proveedor EL en el classpath de test.</p>
 *
 * <p><b>Alcance:</b> {@code @Size}, {@code @DecimalMin}, {@code @Digits}, {@code @PositiveOrZero}.
 * El rechazo de {@code null} corresponde a {@code @Required} (anotación de OpenXava, no Jakarta),
 * que se verifica en la UI y queda fuera de este test unitario por diseño.</p>
 */
public class ValidacionCajaNegraTest {

    private static Validator validator;

    @BeforeClass
    public static void init() {
        validator = Validation.byDefaultProvider().configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();
    }

    // --------------------------- Helpers --------------------------------------

    private void assertValido(Object obj, String prop, Object detalle) {
        assertTrue("Se esperaba VÁLIDO en " + prop + " = " + detalle,
                validator.validateProperty(obj, prop).isEmpty());
    }

    private void assertInvalido(Object obj, String prop, Object detalle) {
        assertFalse("Se esperaba INVÁLIDO en " + prop + " = " + detalle,
                validator.validateProperty(obj, prop).isEmpty());
    }

    private Producto productoCon(String descripcion, String precio) {
        Producto p = new Producto();
        p.setDescripcion(descripcion);
        p.setPrecio(precio == null ? null : new BigDecimal(precio));
        return p;
    }

    private Ingrediente ingredienteCon(String costo, String stockActual, String stockMinimo) {
        Ingrediente ing = new Ingrediente();
        ing.setCostoUnitario(costo == null ? null : new BigDecimal(costo));
        ing.setStockActual(stockActual == null ? null : new BigDecimal(stockActual));
        ing.setStockMinimo(stockMinimo == null ? null : new BigDecimal(stockMinimo));
        return ing;
    }

    // ======================= Producto.descripcion =============================
    // @Size(min=3, max=50): válido [3..50], inválido <3 o >50

    @Test
    public void descripcion_longitud2_invalida() {
        assertInvalido(productoCon("Pa", "1.00"), "descripcion", "len=2");
    }

    @Test
    public void descripcion_longitud3_valida() {
        assertValido(productoCon("Pan", "1.00"), "descripcion", "len=3");
    }

    @Test
    public void descripcion_longitud50_valida() {
        assertValido(productoCon("a".repeat(50), "1.00"), "descripcion", "len=50");
    }

    @Test
    public void descripcion_longitud51_invalida() {
        assertInvalido(productoCon("a".repeat(51), "1.00"), "descripcion", "len=51");
    }

    // ========================= Producto.precio ================================
    // @DecimalMin("0.01") + @Digits(integer=8, fraction=2)

    @Test
    public void precio_cero_invalido() {
        assertInvalido(productoCon("Pan", "0.00"), "precio", "0.00");
    }

    @Test
    public void precio_negativo_invalido() {
        assertInvalido(productoCon("Pan", "-0.01"), "precio", "-0.01");
    }

    @Test
    public void precio_minimo_001_valido() {
        assertValido(productoCon("Pan", "0.01"), "precio", "0.01");
    }

    @Test
    public void precio_maximo8Enteros2Decimales_valido() {
        assertValido(productoCon("Pan", "12345678.99"), "precio", "8 int / 2 dec");
    }

    @Test
    public void precio_nueveEnteros_invalido_porDigits() {
        assertInvalido(productoCon("Pan", "123456789.00"), "precio", "9 enteros");
    }

    @Test
    public void precio_tresDecimales_invalido_porDigits() {
        assertInvalido(productoCon("Pan", "1.999"), "precio", "3 decimales");
    }

    // ====================== Ingrediente.costoUnitario =========================
    // @DecimalMin("0.01") + @Digits(integer=8, fraction=4)

    @Test
    public void costoUnitario_cero_invalido() {
        assertInvalido(ingredienteCon("0.00", "0", "0"), "costoUnitario", "0.00");
    }

    @Test
    public void costoUnitario_minimo_valido() {
        assertValido(ingredienteCon("0.01", "0", "0"), "costoUnitario", "0.01");
    }

    // ======================= Ingrediente.stockActual ==========================
    // @PositiveOrZero + @Digits(integer=8, fraction=4)

    @Test
    public void stockActual_negativo_invalido() {
        assertInvalido(ingredienteCon("1.00", "-0.0001", "0"), "stockActual", "-0.0001");
    }

    @Test
    public void stockActual_cero_valido() {
        assertValido(ingredienteCon("1.00", "0", "0"), "stockActual", "0");
    }

    @Test
    public void stockActual_positivoMinimo_valido() {
        assertValido(ingredienteCon("1.00", "0.0001", "0"), "stockActual", "0.0001");
    }

    // ======================= Ingrediente.stockMinimo ==========================
    // @PositiveOrZero (opcional: null permitido, no lleva @Required)

    @Test
    public void stockMinimo_negativo_invalido() {
        assertInvalido(ingredienteCon("1.00", "0", "-1"), "stockMinimo", "-1");
    }

    @Test
    public void stockMinimo_null_valido_porqueEsOpcional() {
        assertValido(ingredienteCon("1.00", "0", null), "stockMinimo", "null");
    }

    // ======================= RecetaItem.cantidadGramos ========================
    // @DecimalMin("0.0001") + @Digits(integer=8, fraction=4)

    private RecetaItem recetaConGramos(String gramos) {
        RecetaItem item = new RecetaItem();
        item.setCantidadGramos(gramos == null ? null : new BigDecimal(gramos));
        return item;
    }

    @Test
    public void cantidadGramos_cero_invalida() {
        assertInvalido(recetaConGramos("0.0000"), "cantidadGramos", "0");
    }

    @Test
    public void cantidadGramos_minima_valida() {
        assertValido(recetaConGramos("0.0001"), "cantidadGramos", "0.0001");
    }

    @Test
    public void cantidadGramos_negativa_invalida() {
        assertInvalido(recetaConGramos("-1"), "cantidadGramos", "-1");
    }
}
