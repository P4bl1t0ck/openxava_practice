package com.tuempresa.Facturas.modelo;

import com.tuempresa.Facturas.calculadores.CalculadorSiguienteNumeroParaAnyo;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;
import org.openxava.calculators.CurrentYearCalculator;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Collection;

@Entity @Getter
@Setter
@View(members =
        "anyo, numero, fecha;" +
        "cliente;" +
        "detalles;" +
        "observaciones"
    )
public class Factura {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @Hidden
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(length = 32)
    String oid;

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
    @ListProperties("producto.numero, producto.descripcion,cantidad")
    Collection<Detalle> detalles;

    @Stereotype("MEMO")
    String observaciones;
}
