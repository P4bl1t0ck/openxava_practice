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
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = 36)
    @Hidden
    private String id;

    @Required
    @ReadOnly
    private Date fechaAlerta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "nombre") // Conectado a tus ingredientes
    private Ingrediente ingrediente;

    @Required
    @Column(length = 15)
    private String nivelAlerta; // E.g., "CRÍTICO", "MODERADO"

    @Required
    @Stereotype("DINERO") // Para formatear decimales de stock de forma limpia
    //Label("Stock Proyectado (3 días)")
    private BigDecimal stockProyectadoTresDias;

    @Column(length = 150)
    @ReadOnly
    private String mensaje;
}
