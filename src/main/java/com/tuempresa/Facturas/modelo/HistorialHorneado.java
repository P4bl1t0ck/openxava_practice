package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter @Setter
@View(members =
    "fechaHorneado, usuario; " +
    "producto, cantidadHorneada; " +
    "codigoDiagnostico"
)
public class HistorialHorneado {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = 36)
    @Hidden
    String id;

    @Required
    @ReadOnly
    Date fechaHorneado;

    @Required
    @Column(length = 40)
    @ReadOnly
    String usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "descripcion")
    @ReadOnly
    Producto producto;

    @Required
    @ReadOnly
    Integer cantidadHorneada;

    @Required
    @Column(length = 30)
    @ReadOnly
    String codigoDiagnostico;
}
