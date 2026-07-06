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
        "DatosDePrediccion [ fecha, producto ]; " +
        "CalculoAlgoritmico [ cantidadSugerida ]; " +
        "AjusteManual [ cantidadManual, justificacion ]"
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

    @ReadOnly
    @Column(length = 6)
    @DefaultValueCalculator(
        value = com.tuempresa.Facturas.calculadores.CalculadorPrediccionPMP.class,
        properties = @PropertyValue(name = "productoId", from = "producto.numero")
    )
    int cantidadSugerida;

    @Column(length = 6)
    Integer cantidadManual;

    @TextArea
    @Column(length = 200)
    String justificacion;
}
