package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Required;

import javax.persistence.*;

@Embeddable
@Getter
@Setter
public class DetalleCierre {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "nombre")
    Producto producto;

    @Required
    Integer cantidadVendida;

    @Required
    Integer cantidadMerma;
}
