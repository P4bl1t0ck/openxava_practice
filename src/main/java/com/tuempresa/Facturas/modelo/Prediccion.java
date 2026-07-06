package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@View(members =
        "Datos de Prediccion [ fecha, producto ]; " +
        "Calculo Algoritmico [ cantidadSugerida ]; " +
        "Ajuste Manual (Override) [ cantidadManual, justificacion ]"
)
public class Prediccion {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = 36)
    @Hidden
    String id;

    @Required
    @DefaultValueCalculator(org.openxava.calculators.CurrentDateCalculator.class)
    Date fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "descripcion")
    Producto producto;

    @Required
    @ReadOnly
    @Column(length = 6)
    int cantidadSugerida;

    @Column(length = 6)
    Integer cantidadManual;

    @TextArea
    @Column(length = 200)
    String justificacion;
}
