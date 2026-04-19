package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.htmlunit.corejs.javascript.annotations.JSSetter;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable @Getter @Setter
public class Direccion {
    @Column(length = 30)
    String viaPblca;
    @Column (length = 5)
    int codigoPostal;
    @Column(length = 20)
    String municipio;
    @Column(length = 30)
    String provincia;
}
