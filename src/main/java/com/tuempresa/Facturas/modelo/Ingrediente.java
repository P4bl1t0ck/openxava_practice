package com.tuempresa.Facturas.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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

    @NotNull
    @Min(0)
    @Digits(integer = 8, fraction = 4)
    BigDecimal stockActual;

    @Min(0)
    @Digits(integer = 8, fraction = 4)
    BigDecimal stockMinimo;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 4)
    BigDecimal costoUnitario;
}