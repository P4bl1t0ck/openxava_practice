package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.Required;
import org.openxava.annotations.View;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Entidad maestra de materia prima. Controla las existencias físicas en bodega
 * (costo unitario, stock actual y stock mínimo de reposición).
 *
 * Cumple AGENTS.md: getters/setters generados por Lombok y campos con acceso de paquete.
 */
@Entity
@EntityListeners(IngredienteStockListener.class)
@Getter
@Setter
@View(members = "Datos Principales [ nombre, unidadMedida, esInventariable ]; Control de Inventario [ stockActual, stockMinimo, costoUnitario ]")
public class Ingrediente {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = 36)
    @Hidden
    String id;

    @Column(length = 10, nullable = false, unique = true)
    @Required
    String codigo;

    @Column(length = 50, nullable = false)
    @Required
    String nombre;

    @Column(length = 15, nullable = false)
    @Required
    String unidadMedida; // Base en gramos o unidades

    @Required
    @DecimalMin("0.01") // Caja Negra: el costo unitario no puede ser negativo ni cero
    @Digits(integer = 8, fraction = 4)
    BigDecimal costoUnitario;

    @Required
    @PositiveOrZero // Caja Negra: el stock físico nunca es negativo (CB-006: además obligatorio)
    @Digits(integer = 8, fraction = 4)
    BigDecimal stockActual;

    @PositiveOrZero // Umbral de reposición; base para futuras alertas de stock
    @Digits(integer = 8, fraction = 4)
    BigDecimal stockMinimo;

    boolean esInventariable;
}
