package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class RecetaItem {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = 36)
    @Hidden //No es nesecario que sepa esto el cliente
    public  String id;

    //Para que llame de las clases Productos  y ingrediente, los datos
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "descripcion") //<-- asi concide con Producto
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "nombre")
    private Ingrediente ingrediente;

    @Required
    private BigDecimal cantidadGramos;

    @ReadOnly
    @Stereotype("MONEY")
    @Depends("cantidadGramos")
    //Edicion de el getter de Costo Item
    public  BigDecimal  getCostoItem(){
        if (ingrediente != null && cantidadGramos != null){
            /*Aqui modificamos la funcion getter de CostoItem
            * donde evaluaremos y se calculara el costo unitario
            * por la cantidad de gramos de los ingredientes
            * contal de cualquiera de los dos sea distinto a nulo*/
            return ingrediente.getCostoUnitario().multiply(cantidadGramos);
        }
        return  BigDecimal.ZERO;
    }


}
