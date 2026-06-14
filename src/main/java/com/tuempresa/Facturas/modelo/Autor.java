package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ListProperties;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Getter
@Setter
public class Autor extends Identificable{

    @Column(length = 50)
    String nombre;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.REMOVE)
    /*Remueve todo el autor si el padre es eliminado*/
    @ListProperties("numero, descripcion, precio")
    Collection<Producto> productos;
}
