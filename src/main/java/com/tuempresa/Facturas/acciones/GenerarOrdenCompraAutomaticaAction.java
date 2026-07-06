package com.tuempresa.Facturas.acciones;

import com.tuempresa.Facturas.modelo.AlertaStock;
import com.tuempresa.Facturas.vo.LineaOrdenCompraData;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.openxava.actions.JasperReportBaseAction;
import org.openxava.jpa.XPersistence;

import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerarOrdenCompraAutomaticaAction extends JasperReportBaseAction {

    private static final String NIVEL_CRITICO = "CRITICO";

    List<LineaOrdenCompraData> lineas;

    @Override
    public void execute() throws Exception {
        if (getLineas().isEmpty()) {
            addError("no_hay_alertas_criticas");
            return;
        }

        setFileName("orden-compra-automatica");
        super.execute();
    }

    @Override
    protected JRDataSource getDataSource() {
        return new JRBeanCollectionDataSource(getLineas());
    }

    @Override
    protected String getJRXML() {
        return "reports/orden_compra_automatica.jrxml";
    }

    @Override
    protected Map<String, Object> getParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("titulo", "Orden de Compra Automatica");
        parameters.put("fechaGeneracion", new Date());
        parameters.put("totalAlertas", getLineas().size());
        return parameters;
    }

    List<LineaOrdenCompraData> getLineas() {
        if (lineas == null) {
            lineas = cargarLineas();
        }
        return lineas;
    }

    List<LineaOrdenCompraData> cargarLineas() {
        TypedQuery<AlertaStock> query = XPersistence.getManager().createQuery(
            "select a from AlertaStock a " +
            "join fetch a.ingrediente i " +
            "where a.nivelAlerta = :nivel and " +
            "a.fechaAlerta = (" +
                "select max(a2.fechaAlerta) from AlertaStock a2 where a2.ingrediente = a.ingrediente" +
            ") " +
            "order by i.nombre",
            AlertaStock.class
        );
        query.setParameter("nivel", NIVEL_CRITICO);

        List<LineaOrdenCompraData> resultado = new ArrayList<>();
        for (AlertaStock alerta : query.getResultList()) {
            LineaOrdenCompraData linea = new LineaOrdenCompraData();
            linea.setCodigoIngrediente(String.valueOf(alerta.getIngrediente().getId()));
            linea.setNombreIngrediente(alerta.getIngrediente().getNombre());
            linea.setUnidadMedida(alerta.getIngrediente().getUnidadMedida());
            linea.setStockActual(valorSeguro(alerta.getIngrediente().getStockActual()));
            linea.setStockMinimo(valorSeguro(alerta.getIngrediente().getStockMinimo()));
            linea.setCantidadSugeridaCompra(calcularCantidadSugerida(alerta));
            linea.setPrioridad(alerta.getNivelAlerta());
            linea.setMensaje(alerta.getMensaje());
            linea.setFechaAlerta(alerta.getFechaAlerta());
            resultado.add(linea);
        }
        return resultado;
    }

    BigDecimal calcularCantidadSugerida(AlertaStock alerta) {
        BigDecimal stockMinimo = valorSeguro(alerta.getIngrediente().getStockMinimo());
        BigDecimal stockActual = valorSeguro(alerta.getIngrediente().getStockActual());
        BigDecimal faltante = stockMinimo.subtract(stockActual);
        return faltante.compareTo(BigDecimal.ZERO) > 0 ? faltante : BigDecimal.ZERO;
    }

    BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
