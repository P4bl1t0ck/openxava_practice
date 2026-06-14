package com.tuempresa.Facturas.modelo;

import com.tuempresa.Facturas.calculadores.CalculadorPrecioPorUnidad;
import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.Depends;
import org.openxava.annotations.PropertyValue;
import org.openxava.annotations.Stereotype;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
public class Detalle {
    int cantidad;
    @ManyToOne(fetch= FetchType.LAZY, optional = true)
    Producto producto;
    @Stereotype("DINERO")
    @Depends("precioPorUnidad, cantidad")
    public BigDecimal getImporte(){
         if (precioPorUnidad == null ) return BigDecimal.ZERO;
         return new BigDecimal(cantidad).multiply(precioPorUnidad);
    }
    @DefaultValueCalculator(value = CalculadorPrecioPorUnidad.class, properties = @PropertyValue(name = "numeroProducto",from = "producto.numero"))
    @Stereotype("DINERO")
    BigDecimal precioPorUnidad;
}
