package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.Hidden;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter @Setter
/*Hacemos lo mismo con la siguientes entidades*/
public class Categoria extends Identificable {

    @Column(length = 50)
    String descripcion;
}