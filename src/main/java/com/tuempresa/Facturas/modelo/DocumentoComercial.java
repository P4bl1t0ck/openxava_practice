package com.tuempresa.Facturas.modelo;

import com.tuempresa.Facturas.calculadores.CalculadorSiguienteNumeroParaAnyo;
import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.*;
import org.openxava.calculators.CurrentYearCalculator;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

@Entity @Getter
@Setter
@View(members =
        "anyo, numero, fecha;" +
                "datos{"+
                "cliente;" +
                "detalles;" +
                "observaciones"+
                "}"
    )
abstract public class DocumentoComercial extends Identificable{
    @DefaultValueCalculator(CurrentYearCalculator.class)
    @Column(length = 4)
    int anyo;
    @Column(length = 6)
    @DefaultValueCalculator(value = CalculadorSiguienteNumeroParaAnyo.class,
    properties = @PropertyValue(name = "anyo"))
    int numero;

    @Required
    @DefaultValueCalculator(CurrentYearCalculator.class)
    LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @ReferenceView("Simple")
    Cliente cliente;

    @ElementCollection
    @ListProperties("producto.numero, producto.descripcion, cantidad, precioPorUnidad," +
            "importe+["
                +"documentoComercial.porcentajeIVA"
                + "documentoComercial.iva"
                + "documentoComercial.importeTotal"
            + "]"
    )
    Collection<Detalle> detalles;

    @Digits(integer=2, fraction=0)
    BigDecimal porcentajeIVA;

    @ReadOnly
    @Stereotype("DINERO")
    @Calculation("sum(detalles.importe)*porcentajeIVA/100")
    BigDecimal iva;

    @ReadOnly
    @Stereotype("DINERO")
    @Calculation("sum(detalles.importe)+iva")
    BigDecimal importeTotal;


    @Stereotype("MEMO")
    String observaciones;
}
