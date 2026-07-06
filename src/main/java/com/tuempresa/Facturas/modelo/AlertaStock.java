package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
public class AlertaStock {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(length = 32)
    @Hidden
    String id;

    @ReadOnly
    Date fechaAlerta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "nombre")
    Ingrediente ingrediente;

    @Required
    @Column(length = 15)
    String nivelAlerta;

    @Required
    @Stereotype("DINERO")
    BigDecimal stockProyectadoTresDias;

    @Column(length = 150)
    @ReadOnly
    String mensaje;
}
