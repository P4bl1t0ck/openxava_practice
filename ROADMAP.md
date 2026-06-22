# 🗺️ ROADMAP — OptiPan (Sistema MES de Optimización de Producción y Gestión de Inventarios)

> **Proyecto:** OptiPan · **Institución:** Universidad de Las Américas (UDLA)
> **Materia:** Validación y Verificación de Software
> **Autor:** Brandon Arellano
> **Stack:** Java 17 · OpenXava 7.7 · Maven · JPA/Hibernate · MySQL 8 · JUnit 4
> **Avance global estimado:** ~70%

---

## Principios de ejecución (dependencia piramidal)

```
1. DOMINIO CORE      → blindaje defensivo + Value Objects
2. VALIDACIÓN UI     → Bean Validation (caja negra)
3. APLICACIÓN        → acciones transaccionales (ACID)
4. INTEGRACIÓN       → reportería (JasperReports) y alertas avanzadas
```

---

## ✅ Fase 1 — Estabilización Defensiva y Blindaje de Datos · **COMPLETADA**

Eliminación de la deuda técnica detectada por caja blanca y caja negra.

- [x] **US-001** Blindaje del motor de viabilidad (`Producto.calcularSimulacion()`)
  - [x] CB-003: guards de null contra `NullPointerException` en ítems huérfanos
  - [x] CB-006: `stockActual == null` → capacidad 0 + diagnóstico `ERR_STOCK_NULO`
  - [x] Refactor a Value Object inmutable `ResultadoSimulacion` (Clean Architecture)
- [x] **US-002** Sanitización de entrada en UI (Bean Validation)
  - [x] `precio` → `@DecimalMin("0.01")`, `@Digits(8,2)`
  - [x] `descripcion` → `@Size(min=3, max=50)`
  - [x] `stockActual`/`stockMinimo` → `@PositiveOrZero`, `@Digits(8,4)`
  - [x] `costoUnitario`/`cantidadGramos` → `@DecimalMin`, `@Digits`
- [x] Refactor de `Ingrediente` a Lombok + acceso de paquete (cumple AGENTS.md)
- [x] Suite de pruebas: 20 casos de caja blanca + 22 de caja negra (cobertura de ramas 100%)

---

## ✅ Fase 2 — Persistencia Transaccional y Cierre de Ciclo de Vida · **COMPLETADA (núcleo)**

Conexión de la UI con mutaciones reales bajo aislamiento transaccional.

- [x] **US-003** `ConfirmarHorneadoAccion` (`extends ViewBaseAction`)
  - [x] Descuento físico de `Ingrediente.stockActual` proporcional a la cantidad aprobada
  - [x] `BigDecimal` con escala 4 y `RoundingMode.DOWN`; piso en cero
  - [x] Registro en `controladores.xml` (controlador `Horneado`) y `aplicacion.xml` (módulo `Producto`)
  - [x] Mensajes i18n de éxito/error
- [ ] **Pendiente:** prueba funcional end-to-end en la app levantada (verificar UPDATE en BD)
- [ ] **Pendiente:** registro de un histórico de horneados (auditoría de consumos)

---

## 🔜 Fase 3 — Inteligencia Logística y Reportería Oficial · **PENDIENTE**

- [ ] **US-004** Motor de predicción PMP (Promedio Móvil Ponderado 21 días)
  - [ ] `CalculadorPrediccionPMP` que alimente `Prediccion.cantidadSugerida` (hoy siempre 0)
  - [ ] Fuente de datos: historial de `CierreDiario` / `DetalleCierre`
- [ ] **US-005** Generación automática de `AlertaStock`
  - [ ] Listener JPA `@PostUpdate` en `Ingrediente` que compare `stockActual` vs `stockMinimo`
  - [ ] Creación automática de alerta cuando el stock cae bajo el umbral
- [ ] **US-006** Reportería con JasperReports
  - [ ] Plantilla `.jrxml` de orden de compra de insumos faltantes
  - [ ] Acción de exportación a PDF
  - [ ] Añadir dependencia `jasperreports` explícita en `pom.xml`

---

## 🧹 Fase 4 — Calidad, Seguridad y Despliegue · **PENDIENTE**

- [ ] Externalizar credenciales de BD (`root/1234`) a variables de entorno
- [ ] Cambiar contraseña por defecto `admin/admin` (`naviox-users.properties`)
- [ ] Declarar las 17 entidades en `persistence.xml` (hoy solo 2)
- [ ] Resolver conflicto de datasource HSQLDB (`context.xml`) vs MySQL (`persistence.xml`)
- [ ] Activar tests en `pom.xml` (`skipTests=false`) para CI
- [ ] Limpiar código muerto: `Incidencia` (typo `getrImporte`), `Propiedad`, `Prueba` (sin `@Entity`)

---

## Backlog priorizado (esfuerzo relativo)

| Prioridad | Ítem | Esfuerzo |
|:---------:|------|:--------:|
| 🔴 Alta | US-003 prueba funcional E2E | Bajo |
| 🔴 Alta | Declarar entidades en `persistence.xml` | Bajo |
| 🟠 Media | US-004 Calculador PMP | Alto |
| 🟠 Media | US-005 Alertas automáticas | Alto |
| 🟡 Baja | US-006 JasperReports | Medio |
| 🟡 Baja | Externalizar credenciales | Medio |

---

## Fuera de alcance (SRS)

Pasarelas de pago externas, POS de ventas completo, APIs REST públicas. OptiPan se acota al
**backend de gestión de planta y logística interna**.
