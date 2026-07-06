package com.tuempresa.Facturas.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@View(members = "DatosPrincipales [ nombre, unidadMedida, esInventariable ]; ControlDeInventario [ stockActual, stockMinimo, costoUnitario ]")
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ReadOnly // MAGIA: Evita el error "Es obligado" y permite el Auto Increment
    Integer id;

    @Column(length = 50)
    @Required
    String nombre;

    @Column(length = 20)
    @Required
    String unidadMedida;

    boolean esInventariable;

    @Required
    BigDecimal stockActual;

    @Required
    BigDecimal stockMinimo;

    @Required
    BigDecimal costoUnitario;
}