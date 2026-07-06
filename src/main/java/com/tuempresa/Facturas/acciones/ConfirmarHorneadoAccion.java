package com.tuempresa.Facturas.acciones;

import com.tuempresa.Facturas.modelo.Ingrediente;
import com.tuempresa.Facturas.modelo.HistorialHorneado;
import com.tuempresa.Facturas.modelo.Producto;
import com.tuempresa.Facturas.modelo.RecetaItem;
import com.tuempresa.Facturas.vo.ResultadoSimulacion;
import org.openxava.actions.ViewBaseAction;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Users;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * Acción de OpenXava que cierra el ciclo de vida transaccional de OptiPan:
 * confirma una orden de horneado y descuenta físicamente las materias primas
 * del inventario de forma proporcional a la cantidad aprobada por el motor de viabilidad.
 *
 * <p>El descuento ocurre sobre entidades gestionadas por el {@link EntityManager} de OpenXava;
 * el framework hace commit automático al finalizar el request, dando atomicidad (ACID):
 * si la acción lanza una excepción, no se persiste ningún cambio parcial.</p>
 *
 * <p>Principio OCP: la lógica de viabilidad permanece intacta en {@link Producto#calcularSimulacion()};
 * esta acción de la capa de aplicación solo orquesta la decisión y la mutación.</p>
 */
public class ConfirmarHorneadoAccion extends ViewBaseAction {

    /** Escala fija de alta precisión logística (gramos) exigida por el dominio. */
    private static final int ESCALA = 4;

    @Override
    public void execute() throws Exception {
        Object numeroValor = getView().getValue("numero");
        if (numeroValor == null) {
            addError("optipan.error.producto_no_seleccionado");
            return;
        }

        EntityManager em = XPersistence.getManager();
        Producto producto = em.find(Producto.class, Integer.valueOf(numeroValor.toString()));
        if (producto == null) {
            addError("optipan.error.producto_no_seleccionado");
            return;
        }

        ResultadoSimulacion resultado = producto.calcularSimulacion();

        // Sanitización previa: nunca descontar inventario sobre un diagnóstico inválido
        if (ResultadoSimulacion.ERR_SIN_RECETA.equals(resultado.getCodigoDiagnostico())) {
            addError("optipan.error.sin_receta");
            return;
        }
        if (ResultadoSimulacion.ERR_STOCK_NULO.equals(resultado.getCodigoDiagnostico())) {
            addError("optipan.error.stock_nulo", resultado.getIngredienteLimitante());
            return;
        }

        BigDecimal cantidadAProducir = resultado.getCantidadMaximaPosible();
        if (cantidadAProducir == null || cantidadAProducir.compareTo(BigDecimal.ZERO) <= 0) {
            addError("optipan.error.cero_produccion");
            return;
        }

        // Mutación física del inventario, proporcional a la cantidad realmente producible
        for (RecetaItem item : producto.getRecetaItems()) {
            if (item.getIngrediente() == null || item.getCantidadGramos() == null) {
                continue; // CB-003: ítem huérfano, se omite
            }

            Ingrediente ingrediente = em.find(Ingrediente.class, item.getIngrediente().getId());
            if (ingrediente == null || ingrediente.getStockActual() == null) {
                continue;
            }

            BigDecimal gramosConsumidos = item.getCantidadGramos()
                    .multiply(cantidadAProducir)
                    .setScale(ESCALA, RoundingMode.DOWN);

            BigDecimal nuevoStock = ingrediente.getStockActual()
                    .subtract(gramosConsumidos)
                    .max(BigDecimal.ZERO) // el stock físico nunca queda negativo
                    .setScale(ESCALA, RoundingMode.DOWN);

            ingrediente.setStockActual(nuevoStock);
            // Entidad gestionada: Hibernate incluye el UPDATE en el commit del request
        }

        // Fuerza el disparo de @PostUpdate dentro de este flujo antes de cerrar la acción.
        em.flush();
        registrarHistorialHorneado(em, producto, cantidadAProducir, resultado);

        addMessage("optipan.horneado_confirmado", cantidadAProducir.intValue(), producto.getDescripcion());
    }

    private void registrarHistorialHorneado(EntityManager em,
                                            Producto producto,
                                            BigDecimal cantidadAProducir,
                                            ResultadoSimulacion resultado) {
        HistorialHorneado historial = new HistorialHorneado();
        historial.setFechaHorneado(new Date());
        historial.setUsuario(obtenerUsuarioActual());
        historial.setProducto(producto);
        historial.setCantidadHorneada(cantidadAProducir.intValue());
        historial.setCodigoDiagnostico(resultado.getCodigoDiagnostico());
        em.persist(historial);
    }

    private String obtenerUsuarioActual() {
        String usuario = Users.getCurrent();
        return usuario == null || usuario.trim().isEmpty() ? "sistema" : usuario;
    }
}
