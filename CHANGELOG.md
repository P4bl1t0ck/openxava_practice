# Changelog

Todos los cambios notables de **OptiPan** se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el proyecto se adhiere a [Versionado Semántico](https://semver.org/lang/es/).

**Autor:** Brandon Arellano

---

## [Sin publicar] — 2026-06-14

Fase 1 (Estabilización Defensiva) y núcleo de la Fase 2 (Cierre Transaccional).

### Añadido
- **`ResultadoSimulacion`** (`vo/`): Value Object inmutable que encapsula el diagnóstico del
  motor de viabilidad mediante *Static Factory Methods* (`viable`, `limitado`, `errorStockNulo`,
  `sinReceta`) y aísla el texto de la UI con `formatearMensaje(int)` (Clean Architecture).
- **`Producto.calcularSimulacion()`**: método de dominio puro que devuelve un `ResultadoSimulacion`,
  separando la lógica de negocio de la presentación (SRP).
- **`ConfirmarHorneadoAccion`** (`acciones/`): acción OpenXava (`ViewBaseAction`) que cierra el ciclo
  transaccional descontando físicamente `Ingrediente.stockActual` de forma proporcional a la cantidad
  aprobada, con `BigDecimal` (escala 4, `RoundingMode.DOWN`) y piso en cero (US-003).
- **Campo `Ingrediente.stockMinimo`**: umbral de reposición, base para futuras alertas de stock.
- **Validaciones Bean Validation (caja negra):**
  - `Producto.precio` → `@Required`, `@DecimalMin("0.01")`, `@Digits(8,2)`
  - `Producto.descripcion` → `@Size(min=3, max=50)`
  - `Ingrediente.costoUnitario` → `@DecimalMin("0.01")`, `@Digits(8,4)`
  - `Ingrediente.stockActual` → `@Required`, `@PositiveOrZero`, `@Digits(8,4)`
  - `Ingrediente.stockMinimo` → `@PositiveOrZero`, `@Digits(8,4)`
  - `RecetaItem.cantidadGramos` → `@DecimalMin("0.0001")`, `@Digits(8,4)`
- **Controlador `Horneado`** en `controladores.xml` y **módulo `Producto`** en `aplicacion.xml`.
- **Mensajes i18n** de éxito y error para la confirmación de horneado.
- **Pruebas de caja blanca** (`ProductoSimulacionTest`): 20 casos con cobertura de ramas del 100%
  sobre `calcularSimulacion()`, `getSimulacionViabilidad()` y `getCostoItem()`.
- **Pruebas de caja negra** (`ValidacionCajaNegraTest`): 22 casos de Partición de Equivalencia y
  Análisis de Valores Límite sobre las restricciones Jakarta Bean Validation.

### Corregido
- **CB-003 (NullPointerException):** `calcularSimulacion()` y `RecetaItem.getCostoItem()` ahora
  aplican programación defensiva ante ingredientes huérfanos o `costoUnitario` nulo.
- **CB-006 (Falso positivo de viabilidad):** si `stockActual == null`, la capacidad de producción
  se fuerza a cero y se emite `ERR_STOCK_NULO`, en lugar de omitir el ítem y dictar "PRODUCCIÓN VIABLE".

### Cambiado
- **`Ingrediente`** refactorizado a Lombok (`@Getter`/`@Setter`) con campos de acceso de paquete,
  eliminando ~15 getters/setters manuales (cumple AGENTS.md).
- **`getSimulacionViabilidad()`** pasa a ser un adaptador delgado de presentación que delega el
  cálculo en `calcularSimulacion()` y el formateo en `ResultadoSimulacion`.
- **`RecetaItem`**: `id` cambiado de `public` a acceso de paquete; campos `producto`, `ingrediente`
  y `cantidadGramos` a acceso de paquete por consistencia.

### Notas
- Las pruebas no se ejecutan con `mvn` (AGENTS.md): el equipo las corre desde el IDE.
- `@Required` (rechazo de `null`) lo valida OpenXava en la UI, no el `Validator` de Jakarta.
