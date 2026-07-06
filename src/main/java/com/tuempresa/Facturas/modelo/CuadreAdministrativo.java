package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.Depends;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.Money;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.Required;
import org.openxava.annotations.Stereotype;
import org.openxava.annotations.View;
import org.openxava.calculators.CurrentDateCalculator;
import org.openxava.jpa.XPersistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@View(members =
    "Parametros [ fechaAnalisis, producto ]; " +
    "Operacion [ cantidadSugeridaPmp, cantidadHorneadaReal, cantidadVendidaReal, cantidadMermaReal ]; " +
    "Indicadores [ costoHundido, costoOportunidad ]"
)
public class CuadreAdministrativo {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = 36)
    @Hidden
    String id;

    @Required
    @DefaultValueCalculator(CurrentDateCalculator.class)
    Date fechaAnalisis;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Required
    @DescriptionsList(descriptionProperties = "descripcion")
    Producto producto;

    @Transient
    @ReadOnly
    @Depends("fechaAnalisis, producto")
    public Integer getCantidadSugeridaPmp() {
        if (!tieneContextoCompleto()) return 0;

        List<Number> cantidades = XPersistence.getManager()
            .createQuery(
                "select coalesce(p.cantidadManual, p.cantidadSugerida) " +
                "from Prediccion p " +
                "where p.producto = :producto and p.fecha between :inicio and :fin " +
                "order by p.fecha desc",
                Number.class
            )
            .setParameter("producto", producto)
            .setParameter("inicio", inicioDelDia())
            .setParameter("fin", finDelDia())
            .setMaxResults(1)
            .getResultList();

        return cantidades.isEmpty() ? 0 : cantidades.get(0).intValue();
    }

    @Transient
    @ReadOnly
    @Depends("fechaAnalisis, producto")
    public Integer getCantidadHorneadaReal() {
        if (!tieneContextoCompleto()) return 0;

        Number total = XPersistence.getManager()
            .createQuery(
                "select coalesce(sum(h.cantidadHorneada), 0) " +
                "from HistorialHorneado h " +
                "where h.producto = :producto and h.fechaHorneado between :inicio and :fin",
                Number.class
            )
            .setParameter("producto", producto)
            .setParameter("inicio", inicioDelDia())
            .setParameter("fin", finDelDia())
            .getSingleResult();

        return total.intValue();
    }

    @Transient
    @ReadOnly
    @Depends("fechaAnalisis, producto")
    public Integer getCantidadVendidaReal() {
        if (!tieneContextoCompleto()) return 0;

        Number total = XPersistence.getManager()
            .createQuery(
                "select coalesce(sum(d.cantidadVendida), 0) " +
                "from CierreDiario c join c.detalleCierres d " +
                "where d.producto = :producto and c.fechaCierre between :inicio and :fin",
                Number.class
            )
            .setParameter("producto", producto)
            .setParameter("inicio", inicioDelDia())
            .setParameter("fin", finDelDia())
            .getSingleResult();

        return total.intValue();
    }

    @Transient
    @ReadOnly
    @Depends("fechaAnalisis, producto")
    public Integer getCantidadMermaReal() {
        if (!tieneContextoCompleto()) return 0;

        Number total = XPersistence.getManager()
            .createQuery(
                "select coalesce(sum(d.cantidadMerma), 0) " +
                "from CierreDiario c join c.detalleCierres d " +
                "where d.producto = :producto and c.fechaCierre between :inicio and :fin",
                Number.class
            )
            .setParameter("producto", producto)
            .setParameter("inicio", inicioDelDia())
            .setParameter("fin", finDelDia())
            .getSingleResult();

        return total.intValue();
    }

    @Transient
    @ReadOnly
    @Money
    @Depends("fechaAnalisis, producto")
    public BigDecimal getCostoHundido() {
        if (producto == null || producto.getCostoTotal() == null) return BigDecimal.ZERO;

        return producto.getCostoTotal().multiply(BigDecimal.valueOf(getCantidadMermaReal()));
    }

    @Transient
    @ReadOnly
    @Stereotype("MONEY")
    @Depends("fechaAnalisis, producto")
    public BigDecimal getCostoOportunidad() {
        if (producto == null || producto.getPrecio() == null) return BigDecimal.ZERO;

        int faltante = Math.max(getCantidadSugeridaPmp() - getCantidadHorneadaReal(), 0);
        return producto.getPrecio().multiply(BigDecimal.valueOf(faltante));
    }

    boolean tieneContextoCompleto() {
        return producto != null && fechaAnalisis != null;
    }

    Date inicioDelDia() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaAnalisis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    Date finDelDia() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaAnalisis);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
}
