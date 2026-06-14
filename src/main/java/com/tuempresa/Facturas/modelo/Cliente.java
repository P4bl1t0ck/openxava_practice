package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.Required;
import org.openxava.annotations.View;

import javax.persistence.*;

@Entity
@Getter @Setter
@View(name = "Simple",members = "numero, nombre")
public class Cliente {
    /*Modificaciones para crear id consecutivos*/
    /*Prueba*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /*@Column(length = 6)*/ //No me gusta la columnas :(
    int numero;

    @Column(length = 50)
    @Required
    String nombre;

    /*Referencia a Cliente*/
    @Embedded
    Direccion direccion;
}
