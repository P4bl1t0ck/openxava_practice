package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter@Setter
@View(members =
        "Datos de Predicción [ fecha, producto ]; " +
                "Cálculo Algorítmico [ cantidadSugerida PMP ]; " +
                "Ajuste Manual (Override) [ cantidadManual, justificacion ]"
)
public class Prediccion {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = 36)
    @Hidden
    private String id;

    @Required
    @DefaultValueCalculator(org.openxava.calculators.CurrentDateCalculator.class)
    private Date fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "descripcion") // Conectado al Producto del tutorial
    private Producto producto;

    @Required
    @ReadOnly
    @Column(length = 6)
    //Label("Cantidad Sugerida (PMP 21 días)")
    private int cantidadSugerida;

    @Column(length = 6)
    //Label("Cantidad Manual (Override)")
    private Integer cantidadManual; // Permite al administrador ajustar la predicción

    @TextArea
    @Column(length = 200)
    //Label("Justificación del Cambio")
    private String justificacion;
}
