package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
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
    String id; // acceso de paquete (AGENTS.md); antes era 'public'

    //Para que llame de las clases Productos  y ingrediente, los datos
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "descripcion") //<-- asi concide con Producto
    Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "nombre")
    Ingrediente ingrediente;

    @Required
    @DecimalMin("0.0001") // Caja Negra: la dosis de receta debe ser estrictamente positiva
    @Digits(integer = 8, fraction = 4)
    BigDecimal cantidadGramos;

    @ReadOnly
    @Stereotype("MONEY")
    @Depends("cantidadGramos")
    //Edicion de el getter de Costo Item
    public  BigDecimal  getCostoItem(){
        /* Programación defensiva (CB-003): se verifica además que costoUnitario no sea null
         * antes de invocar multiply(), evitando un NullPointerException silencioso cuando el
         * ingrediente existe pero su costo aún no fue cargado en bodega. */
        if (ingrediente == null || cantidadGramos == null || ingrediente.getCostoUnitario() == null){
            return BigDecimal.ZERO;
        }
        return ingrediente.getCostoUnitario().multiply(cantidadGramos);
    }


}
