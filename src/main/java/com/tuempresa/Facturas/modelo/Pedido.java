package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.View;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity @Getter @Setter
@View(extendsView = "super.DEFAULT",members = "Factura[factura]"
)
@View(name = "SinClientesFactura",
        members =
                "anyo, numero, fecha;"
        +"detalles;"
        +"observaciones")

public class Pedido extends DocumentoComercial {
    @ManyToOne
    @ReferenceView("SinClientesNiPedidos")
    Factura factura ;
}

