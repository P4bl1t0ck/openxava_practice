package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Required;

import javax.persistence.*;

import org.openxava.annotations.NoFrame;
import org.openxava.annotations.View;

@Embeddable
@Getter
@Setter
@View(members = "producto, cantidadVendida, cantidadMerma")
public class DetalleCierre {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "descripcion")
    @NoFrame
    Producto producto;

    @Required
    Integer cantidadVendida;

    @Required
    Integer cantidadMerma;
}
