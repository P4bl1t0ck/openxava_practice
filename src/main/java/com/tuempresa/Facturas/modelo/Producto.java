package com.tuempresa.Facturas.modelo;

import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.*;
import javax.persistence.*;
import java.math.*;
import java.util.*;

@Entity
@Getter @Setter
@View(members = 
    "Datos Principales [ numero, descripcion; precio, categoria; autor ]; " +
    "Multimedia y Notas { fotos; observaciones }; " +
    "Componentes OptiPan { recetaItems; costoTotal }; " +
    "Algoritmo Caja Blanca { simulacionViabilidad }" // <-- Pestaña para tu tarea
)
public class Producto {

    @Id
    @Column(length = 6)
    int numero;

    @Column(length = 50)
    @Required
    String descripcion;

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
    public BigDecimal getCostoTotal() {
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

    // =========================================================================
    // ALGORITMO DE LÓGICA DE NEGOCIO: EVALUACIÓN DE SUFICIENCIA DE MATERIA PRIMA
    // =========================================================================
    //String respuesta = "Resultado de Simulación (Lote Proyectado: 100 unidades)";
    @Transient
    @TextArea
    @ReadOnly
    //@Label("Resultado de Simulación -Lote Proyectado: 100 unidades-")
    public String getSimulacionViabilidad() {
        int cantidadSugerida = 100; // Lote de prueba estandarizado
        int cantidadFinal = cantidadSugerida;
        String mensaje = "PRODUCCIÓN VIABLE: El stock actual de materias primas cubre la demanda proyectada.";

        // Cobertura de condiciones: Validación de seguridad interna
        if (this.recetaItems == null || this.recetaItems.isEmpty()) {
            return "ERROR CRÍTICO: No se puede evaluar la viabilidad. El producto no tiene una receta base asignada.";
        }

        // Bucle estructural (Iteración sobre la receta del producto)
        for (RecetaItem item : this.recetaItems) {
            if (item.getIngrediente() != null && item.getCantidadGramos() != null 
                && item.getIngrediente().getStockActual() != null) {
                
                // Calcular gramos necesarios para producir 100 unidades de este pan
                BigDecimal gramosRequeridos = item.getCantidadGramos().multiply(new BigDecimal(cantidadSugerida));
                BigDecimal stockDisponible = item.getIngrediente().getStockActual();

                // Condición de desabastecimiento (Camino alterno de Caja Blanca)
                if (stockDisponible.compareTo(gramosRequeridos) < 0) {
                    // Calcular la capacidad real del ingrediente cuello de botella
                    int maxProducibleConInsumo = stockDisponible.divide(item.getCantidadGramos(), 0, RoundingMode.DOWN).intValue();

                    // Identificar si este insumo es el limitante más crítico
                    if (maxProducibleConInsumo < cantidadFinal) {
                        cantidadFinal = maxProducibleConInsumo;
                        mensaje = "WARNING OPERATIVO: Stock insuficiente de '" + item.getIngrediente().getNombre() + 
                                  "'. La predicción original de " + cantidadSugerida + " unidades ha sido truncada. " +
                                  "Producción máxima real permitida: " + cantidadFinal + " unidades.";
                    }
                }
            }
        }
        return mensaje;
    }
}