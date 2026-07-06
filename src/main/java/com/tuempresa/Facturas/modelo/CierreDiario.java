package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.Required;
import org.openxava.annotations.View;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@Getter
@Setter
@View(members="fechaCierre, estado, auditadoPor; detalleCierres")
public class CierreDiario {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = 36)
    @Hidden
    String id;

    @Required
    Date fechaCierre;

    @Column(length = 20)
    @Required
    String estado;

    @Column(length = 50)
    String auditadoPor;

    // Composicion estricta de los detalles de venta/mermas
    @ElementCollection
    @ListProperties("producto.nombre, cantidadVendida, cantidadMerma")
    Collection<DetalleCierre> detalleCierres = new ArrayList<>();
}
