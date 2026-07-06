package com.tuempresa.Facturas.modelo;

import org.openxava.jpa.XPersistence;

import javax.persistence.PostUpdate;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Detecta ingredientes que quedan bajo el stock mínimo tras una mutación persistida
 * y genera automáticamente una alerta crítica.
 */
public class IngredienteStockListener {

    private static final String NIVEL_CRITICO = "CRITICO";

    @PostUpdate
    public void postUpdate(Ingrediente ingrediente) {
        BigDecimal stockActual = ingrediente.getStockActual();
        BigDecimal stockMinimo = ingrediente.getStockMinimo();

        if (stockActual == null || stockMinimo == null) {
            return;
        }
        if (stockActual.compareTo(stockMinimo) >= 0) {
            return;
        }

        AlertaStock alerta = new AlertaStock();
        alerta.setFechaAlerta(new Date());
        alerta.setIngrediente(ingrediente);
        alerta.setNivelAlerta(NIVEL_CRITICO);
        alerta.setStockProyectadoTresDias(stockActual);
        alerta.setMensaje(
            "ALERTA CRITICA: El ingrediente '" + ingrediente.getNombre()
                + "' quedo por debajo del stock minimo. Stock actual: " + stockActual
                + ", stock minimo: " + stockMinimo + "."
        );

        XPersistence.getManager().persist(alerta);
    }
}
