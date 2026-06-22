package com.tuempresa.Facturas.modelo;

import com.tuempresa.Facturas.vo.ResultadoSimulacion;
import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.*;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
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
    @Size(min = 3, max = 50) // Caja Negra: bloquea nombres demasiado cortos o con desbordamiento
    String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @DescriptionsList(descriptionProperties = "descripcion")
    Categoria categoria;

    @Money
    @Required
    @DecimalMin("0.01") // Caja Negra: prohibe precios negativos o cero
    @Digits(integer = 8, fraction = 2)
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
    // LÓGICA DE NEGOCIO PURA: EVALUACIÓN DE SUFICIENCIA DE MATERIA PRIMA
    // =========================================================================

    /** Lote de prueba estandarizado sobre el que se proyecta la viabilidad. */
    private static final int CANTIDAD_LOTE = 100;

    /** Escala fija de alta precisión logística exigida por el dominio (gramos). */
    private static final int ESCALA = 4;

    /**
     * Motor de viabilidad de producción (dominio puro, sin dependencias de UI).
     *
     * <p>Recorre la receta del producto y, mediante un barrido de cuello de botella
     * ("bottleneck scan"), determina cuántas unidades del lote proyectado pueden
     * producirse con el stock físico disponible.</p>
     *
     * <p>Programación defensiva:</p>
     * <ul>
     *   <li><b>CB-003</b>: ítems huérfanos (ingrediente o cantidad nulos) se omiten sin lanzar
     *       {@link NullPointerException}.</li>
     *   <li><b>CB-006</b>: si {@code stockActual == null} (inconsistencia de migración de bodega),
     *       la capacidad se fuerza a cero y se aborta de inmediato, evitando el falso positivo
     *       de viabilidad.</li>
     * </ul>
     *
     * @return un {@link ResultadoSimulacion} estructurado e inmutable.
     */
    @Transient
    public ResultadoSimulacion calcularSimulacion() {
        if (this.recetaItems == null || this.recetaItems.isEmpty()) {
            return ResultadoSimulacion.sinReceta();
        }

        BigDecimal lote = new BigDecimal(CANTIDAD_LOTE);
        int cantidadFinal = CANTIDAD_LOTE;
        String ingredienteLimitante = null;

        for (RecetaItem item : this.recetaItems) {
            Ingrediente ingrediente = item.getIngrediente();
            BigDecimal cantidadGramos = item.getCantidadGramos();

            // CB-003: ítem huérfano o sin dosis definida -> se ignora de forma segura
            if (ingrediente == null || cantidadGramos == null) {
                continue;
            }

            // CB-006: stock no registrado -> producción imposible de garantizar
            BigDecimal stockDisponible = ingrediente.getStockActual();
            if (stockDisponible == null) {
                return ResultadoSimulacion.errorStockNulo(ingrediente.getNombre());
            }

            BigDecimal gramosRequeridos = cantidadGramos.multiply(lote).setScale(ESCALA, RoundingMode.DOWN);

            // Camino alterno (desabastecimiento): se calcula la capacidad real del insumo
            if (stockDisponible.compareTo(gramosRequeridos) < 0) {
                int maxProducibleConInsumo = stockDisponible
                        .divide(cantidadGramos, 0, RoundingMode.DOWN)
                        .intValue();

                // Conserva el insumo más restrictivo (cuello de botella)
                if (maxProducibleConInsumo < cantidadFinal) {
                    cantidadFinal = maxProducibleConInsumo;
                    ingredienteLimitante = ingrediente.getNombre();
                }
            }
        }

        if (ingredienteLimitante != null) {
            return ResultadoSimulacion.limitado(new BigDecimal(cantidadFinal), ingredienteLimitante);
        }
        return ResultadoSimulacion.viable(new BigDecimal(cantidadFinal));
    }

    /**
     * Adaptador de presentación para OpenXava: expone el diagnóstico del motor como texto
     * en la pestaña "Algoritmo Caja Blanca". Delega el cálculo en {@link #calcularSimulacion()}
     * y el formateo en el propio Value Object (Clean Architecture).
     */
    @Transient
    @TextArea
    @ReadOnly
    public String getSimulacionViabilidad() {
        return calcularSimulacion().formatearMensaje(CANTIDAD_LOTE);
    }
}