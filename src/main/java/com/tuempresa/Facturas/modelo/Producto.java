package com.tuempresa.Facturas.modelo; // Conserva tu paquete actual

import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Getter @Setter
// Organizamos la vista para que muestre lo de tus tutoriales y agregue una pestaña para la Receta de OptiPan
@View(members =
        "Datos Principales [ numero, descripcion; precio, categoria; autor ]; " +
                "Multimedia y Notas { fotos; observaciones }; " +
                "Componentes OptiPan { recetaItems; costoTotal }"
)
public class Producto {

    @Id
    @Column(length = 6)
    int numero; // Se queda tal cual como en tu tutorial

    @Column(length = 50)
    @Required
    String descripcion; // Se queda como descripcion

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @DescriptionsList(descriptionProperties = "descripcion")
    Categoria categoria;

    @Money
    BigDecimal precio;

    @Files
    @Column(length = 32)
    String fotos;

    @TextArea
    String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @DescriptionsList
    Autor autor;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("ingrediente.nombre, cantidadGramos, costoItem")
    private Collection<RecetaItem> recetaItems = new ArrayList<>();

    @ReadOnly
    @Stereotype("MONEY")
    public BigDecimal getCostoTotal() { // Calcula el costo dinámicamente en base a los ingredientes
        BigDecimal total = BigDecimal.ZERO;
        if (recetaItems != null) {
            for (RecetaItem item : recetaItems) {
                if (item.getCostoItem() != null) {
                    total = total.add(item.getCostoItem());
                }
            }
        }
        return total;
    }
}