package com.tuempresa.Facturas.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class LineaOrdenCompraData {

    String codigoIngrediente;
    String nombreIngrediente;
    String unidadMedida;
    BigDecimal stockActual;
    BigDecimal stockMinimo;
    BigDecimal cantidadSugeridaCompra;
    String prioridad;
    String mensaje;
    Date fechaAlerta;
}
