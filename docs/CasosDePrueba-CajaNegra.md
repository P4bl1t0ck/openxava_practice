# Documento de Casos de Prueba — Caja Negra Avanzada

> **Proyecto:** OptiPan (Sistema MES de Optimización de Producción y Gestión de Inventarios)
> **Institución:** Universidad de Las Américas (UDLA) — Validación y Verificación de Software
> **Autor:** Brandon Arellano
> **Fecha:** 2026-06-14
> **Técnicas aplicadas:** Tabla de Decisión · Transición de Estados
> *(Partición de Equivalencia y Análisis de Valores Límite documentados en `ValidacionCajaNegraTest`)*

---

## Función bajo prueba

`Producto.calcularSimulacion()` — motor de viabilidad de producción que, para un lote
estandarizado de **100 unidades**, determina si el stock de materias primas (`Ingrediente`)
cubre la demanda, aplicando truncamiento dinámico ante cuellos de botella. Devuelve un Value
Object `ResultadoSimulacion` con `viable`, `cantidadMaximaPosible` y `codigoDiagnostico`.

El evento **"Confirmar Horneado"** (`ConfirmarHorneadoAccion`) descuenta el stock proporcional
a la capacidad aprobada, lo que hace evolucionar el estado de viabilidad del producto.

---

## Estado de ejecución

| Entorno | Estado |
|---------|--------|
| Diseño de casos | ✅ Completado |
| Implementación ejecutable (JUnit 4) | ✅ `TablaDecisionHorneadoTest`, `TransicionEstadosHorneadoTest` |
| Ejecución | ⏳ **Pendiente** — ejecutar desde IntelliJ IDEA (este entorno no tiene JDK/Maven). AGENTS.md indica que el equipo corre los tests desde el IDE |

> **Cómo ejecutar:** clic derecho sobre cada clase de test → *Run*. La columna
> "Resultado Real" debe completarse con el resultado observado (✅ Pass / ❌ Fail).

---

# TÉCNICA 1 — TABLA DE DECISIÓN

### Reglas de negocio (condiciones → acciones)

| Condiciones | R1 | R2 | R3 | R4 |
|-------------|:--:|:--:|:--:|:--:|
| **C1** ¿El producto tiene receta? | N | S | S | S |
| **C2** ¿Algún ingrediente con `stockActual = null`? | — | S | N | N |
| **C3** ¿El stock cubre el lote completo (100 uds)? | — | — | S | N |
| **Acción / Resultado** | | | | |
| `ERR_SIN_RECETA` (viable=false, cap=0) | ✔ | | | |
| `ERR_STOCK_NULO` (viable=false, cap=0) | | ✔ | | |
| `STOCK_OK` (viable=true, cap=100) | | | ✔ | |
| `LIMIT_INGREDIENT` (viable=true, 0<cap<100) | | | | ✔ |

*(— = condición "no importa"; colapsa las 8 combinaciones a 4 reglas efectivas.)*

### Casos de prueba

| ID | Regla | Descripción | Datos de entrada | Resultado esperado | Resultado real |
|----|:----:|-------------|------------------|--------------------|:--------------:|
| **TD-01** | R1 | Producto sin ítems de receta | `recetaItems = []` | `ERR_SIN_RECETA`, viable=false, cap=0 | ⏳ |
| **TD-02** | R2 | Ingrediente con stock no registrado | Harina, `stockActual=null`, 10 g/ud | `ERR_STOCK_NULO`, viable=false, cap=0 | ⏳ |
| **TD-03** | R3 | Stock holgado para 100 uds | Harina, `stockActual=5000`, 10 g/ud (req. 1000) | `STOCK_OK`, viable=true, cap=100 | ⏳ |
| **TD-04** | R4 | Stock insuficiente (cuello de botella) | Harina, `stockActual=550`, 10 g/ud (req. 1000) | `LIMIT_INGREDIENT`, viable=true, cap=55 | ⏳ |

**Implementación:** `src/test/java/com/tuempresa/Facturas/pruebas/TablaDecisionHorneadoTest.java`
(métodos `reglaR1_sinReceta`, `reglaR2_stockNull`, `reglaR3_stockCubreLote`, `reglaR4_stockNoCubreLote`).

---

# TÉCNICA 2 — TRANSICIÓN DE ESTADOS

### Diagrama de estados

```
                 asignarReceta(stock holgado)
   [SIN_RECETA] ─────────────────────────────► [VIABLE]
                                                   │
                          confirmarHorneado        │  confirmarHorneado
                          (queda > lote)           │  (queda < lote)
                              ┌──────────┐         ▼
                              └────────► [VIABLE]  [LIMITADO]
                                                   │
                                                   │ confirmarHorneado
                                                   │ (consume todo)
                                                   ▼
                                                [AGOTADO] ◄─┐ confirmarHorneado
                                                   │        │ (rechazado)
                                                   └────────┘

   (cualquier estado) ── stockActual = null ──► [BLOQUEADO_NULO] ──confirmarHorneado──► (rechazado)
```

### Definición de estados

| Estado | Significado | `codigoDiagnostico` |
|--------|-------------|---------------------|
| `SIN_RECETA` | Producto sin receta base | `ERR_SIN_RECETA` |
| `VIABLE` | Stock cubre el lote completo (100) | `STOCK_OK` |
| `LIMITADO` | Cuello de botella, 0 < capacidad < 100 | `LIMIT_INGREDIENT` |
| `AGOTADO` | Capacidad = 0 (ni una unidad) | `LIMIT_INGREDIENT` (cap=0) |
| `BLOQUEADO_NULO` | Stock no registrado (null) | `ERR_STOCK_NULO` |

### Tabla de transición

| # | Estado actual | Evento | Estado siguiente |
|---|---------------|--------|------------------|
| T1 | SIN_RECETA | `asignarReceta` (stock holgado) | VIABLE |
| T2 | VIABLE | `confirmarHorneado` (queda > lote) | VIABLE |
| T3 | VIABLE | `confirmarHorneado` (queda < lote) | LIMITADO |
| T4 | LIMITADO | `confirmarHorneado` (consume todo) | AGOTADO |
| T5 | AGOTADO | `confirmarHorneado` | AGOTADO (rechazado) |
| T6 | * (cualquiera) | `stockActual ← null` | BLOQUEADO_NULO |
| T7 | BLOQUEADO_NULO | `confirmarHorneado` | BLOQUEADO_NULO (rechazado) |

### Casos de prueba

Escenario base: **Harina, 10 g/unidad, lote = 100 → 1000 g por horneado pleno.**

| ID | Transición | Precondición / stock | Evento | Resultado esperado (estado · stock) | Resultado real |
|----|:---------:|----------------------|--------|-------------------------------------|:--------------:|
| **TE-01** | T1 | receta vacía → asigna Harina stock=2500 | asignarReceta | VIABLE · 2500 | ⏳ |
| **TE-02** | T2 | VIABLE, stock=2500 | confirmarHorneado | VIABLE · 1500 | ⏳ |
| **TE-03** | T3 | VIABLE, stock=1500 | confirmarHorneado | LIMITADO · 500 | ⏳ |
| **TE-04** | T4 | LIMITADO, stock=500 | confirmarHorneado | AGOTADO · 0 | ⏳ |
| **TE-05** | T5 | AGOTADO, stock=0 | confirmarHorneado | AGOTADO · 0 (sin cambios) | ⏳ |
| **TE-06** | T6 | VIABLE, stock=2500 | `stockActual ← null` | BLOQUEADO_NULO | ⏳ |
| **TE-07** | T7 | BLOQUEADO_NULO | confirmarHorneado | BLOQUEADO_NULO (sin cambios) | ⏳ |

**Implementación:** `src/test/java/com/tuempresa/Facturas/pruebas/TransicionEstadosHorneadoTest.java`
(`recorridoCompletoDeEstados` cubre TE-01…TE-05; `transicionAEstadoBloqueadoPorStockNulo` cubre TE-06…TE-07).

---

## Resumen de la suite de caja negra

| Técnica | Clase de prueba | Nº de casos |
|---------|-----------------|:-----------:|
| Partición de Equivalencia + Valores Límite | `ValidacionCajaNegraTest` | 22 |
| Tabla de Decisión | `TablaDecisionHorneadoTest` | 4 |
| Transición de Estados | `TransicionEstadosHorneadoTest` | 7 (en 2 recorridos) |
| **Total caja negra** | | **33** |

> **Nota de honestidad:** los resultados esperados se derivan del trazado determinista del
> algoritmo (verificado en el análisis de cobertura). Los **resultados reales** deben
> registrarse tras ejecutar las clases JUnit en IntelliJ, dado que este entorno no dispone de
> JDK/Maven para correrlas.
