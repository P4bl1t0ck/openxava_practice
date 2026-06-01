package com.tuempresa.Facturas.modelo;

import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.Required;
import org.openxava.annotations.View;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
/*Nuestra vista personalizada, aprovechamos, para evitar
* el copia y pega de codigo sin necesidad*/
@View(members = "codigo, nombre; unidadMedida, costoUnitario; stockActual, esInventariable")
public class Ingrediente {
    /*Nuestras caracteristicas de nuestros ingredientes*/
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = 36)
    @Hidden
    private String id;

    @Column(length = 10, nullable = false, unique = true)
    @Required
    private String codigo;

    @Column(length = 50, nullable = false)
    @Required
    private String nombre;

    @Column(length = 15, nullable = false)
    @Required
    private String unidadMedida;
    //Clar que nos basaremos en gramos, o unidades

    @Required
    private BigDecimal costoUnitario;
    private BigDecimal stockActual;
    private boolean esInventariable;

    /*Nuestros getter y setters, los generamos usando herramientas
    * de el Intelig Idea, clave aclarar, por lo viejo de algunos
    * videos de openxava, algunas funciones, como refactores, fueron
    * por investigación propia. Just saying~. */

    public boolean isEsInventariable() {return esInventariable;}
    public void setEsInventariable(boolean esInventariable) {this.esInventariable = esInventariable;}
    public BigDecimal getStockActual() {return stockActual;}
    public void setStockActual(BigDecimal stockActual) {this.stockActual = stockActual;}
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public String getCodigo() {return codigo;}
    public void setCodigo(String codigo) {this.codigo = codigo;}
    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public String getUnidadMedida() {return unidadMedida;}
    public void setUnidadMedida(String unidadMedida) {this.unidadMedida = unidadMedida;}
    public BigDecimal getCostoUnitario() {return costoUnitario;}
    public void setCostoUnitario(BigDecimal costoUnitario) {this.costoUnitario = costoUnitario;}
}
