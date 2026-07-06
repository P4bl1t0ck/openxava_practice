package com.tuempresa.Facturas.calculadores;

import org.openxava.calculators.ICalculator;
import org.openxava.jpa.XPersistence;
import javax.persistence.Query;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class CalculadorPrediccionPMP implements ICalculator {

    @Getter @Setter
    private Integer productoId;

    @Override
    public Object calculate() throws Exception {
        if (productoId == null) {
            return 0;
        }

        String jpql = "SELECT d.cantidadVendida FROM CierreDiario c JOIN c.detalleCierres d WHERE d.producto.numero = :prodId ORDER BY c.fechaCierre DESC";
        Query query = XPersistence.getManager().createQuery(jpql);
        query.setParameter("prodId", productoId);
        query.setMaxResults(21);

        List<Integer> resultados = query.getResultList();

        if (resultados == null || resultados.isEmpty()) {
            return 0;
        }

        int N = resultados.size();
        double totalWeighted = 0;
        int sumWeights = 0;

        // La consulta trae los más recientes primero (DESC).
        // Al más reciente (índice 0) le damos el peso mayor (N).
        for (int i = 0; i < N; i++) {
            Integer cantidad = resultados.get(i);
            int weight = N - i;
            totalWeighted += (cantidad != null ? cantidad : 0) * weight;
            sumWeights += weight;
        }

        long prediccion = Math.round(totalWeighted / sumWeights);
        return (int) prediccion;
    }
}
