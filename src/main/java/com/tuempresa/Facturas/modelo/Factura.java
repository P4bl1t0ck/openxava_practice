package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.CollectionView;
import org.openxava.annotations.View;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Collection;

@Entity @Getter @Setter
@View(extendsView = "super.DEFAULT",
        members= "pedidos(pedidos)"
)
@View(name="SinClientesNiPedidos",
 members =
         "anyo, numero, fecha;"
                 +"detalles;"+
"observaciones")
public class Factura extends DocumentoComercial{

    @OneToMany(mappedBy = "factura")
    @CollectionView("SinClientesFactura")
    Collection<Pedido> pedidos;
}
