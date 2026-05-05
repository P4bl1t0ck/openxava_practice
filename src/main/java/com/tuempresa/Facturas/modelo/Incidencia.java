package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.digester.annotations.rules.SetRoot;

@Getter @Setter
public class Incidencia {
    @Getter @Setter
    int cantidad;  //Tiene un campo, por tanto es persistente

    @Getter @Setter/*Tiene un getter y un setter*/
    int precio;

    /*Propiedad persistente*/
    public int getrImporte(){ // No tiene cmapo, ni setter, solo un getter
        return cantidad * precio; // con un calculo
        //Esta es la logica de negocio aplicada dentro
    }
}
