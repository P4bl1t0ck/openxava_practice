# OptiPan - Sistema MES Artesanal (Maven + OpenXava) 🍞

OptiPan es un Sistema de Ejecución de Manufactura (MES) desarrollado con un enfoque estricto en Validación y Verificación (V&V) de Software. Este proyecto implementa pruebas automatizadas, análisis de complejidad ciclomática y reglas de negocio defensivas para la digitalización de procesos en panaderías artesanales.

## 🛠️ Stack Tecnológico
- **Framework de Desarrollo:** OpenXava 7.7 (Arquitectura dirigida por modelos)
- **Lenguaje de Programación:** Java 17
- **Motor de Base de Datos:** MySQL 8
- **Gestor de Dependencias:** Maven
- **Framework de Pruebas:** JUnit 4

---

## 📋 Requisitos Previos
Para levantar este entorno en tu máquina local, asegúrate de contar con el siguiente software instalado y configurado en tus variables de entorno:
1. **Java Development Kit (JDK) 17** o superior.
2. **Apache Maven**.
3. **Servidor MySQL** (puede ser a través de XAMPP, WAMP o instalación nativa).
4. **Git**.

---

## 🚀 Configuración y Despliegue (Primera Ejecución)

Si es la primera vez que vas a correr el proyecto en tu entorno local, es obligatorio configurar la base de datos y descargar las dependencias. Sigue estos pasos:

### 1. Creación de la Base de Datos (MySQL / phpMyAdmin)
El sistema requiere una base de datos vacía para que Hibernate genere las tablas automáticamente.
1. Inicia tu servidor MySQL (ej. enciende el módulo MySQL en XAMPP).
2. Abre tu gestor de base de datos (ej. phpMyAdmin en `http://localhost/phpmyadmin`).
3. Crea una nueva base de datos **exactamente** con el nombre `facturasdb`.
   - *Comando SQL:* `CREATE DATABASE facturasdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`

### 2. Clonar el repositorio
Abre tu terminal y clona el proyecto en tu directorio de preferencia:
```bash
git clone [https://github.com/P4bl1t0ck/openxava_practice.git](https://github.com/P4bl1t0ck/openxava_practice.git)
cd openxava_practice

```

### 3. Descarga de Paquetes y Empaquetado

Maven necesita descargar todas las dependencias (librerías y plugins) la primera vez. Ejecuta el siguiente comando (puede tardar un par de minutos):

```bash
mvn clean install -DskipTests

```

*Asegúrate de que la terminal muestre el mensaje **BUILD SUCCESS**.*

### 4. Levantar el Servidor Web

Inicia el servidor Apache Tomcat embebido con el siguiente comando:

```bash
mvn exec:java

```

El servidor conectará con `facturasdb` y creará la estructura de tablas. Abre tu navegador web y accede a:
👉 **`http://localhost:8080/Facturas`**

---

## 🧪 Pruebas Automatizadas (QA)

Este proyecto incluye una suite de pruebas automatizadas (Caja Blanca y Caja Negra). Para ejecutar la validación de reglas de negocio por consola, utiliza:

```bash
mvn test

```

---

## 📁 Estructura del Proyecto

* `src/main/java/` - Código fuente de la aplicación (Modelos, Calculadores, Acciones).
* `src/test/java/` - Suite de pruebas automatizadas con JUnit.
* `src/main/resources/` - Archivos de configuración XML y reportes JasperReports.

---

## 👥 Equipo de Desarrollo (UDLA)

* **Gabriel Calderón** - Desarrollo de lógica e integración
* **Eduardo Salazar** - Estabilización de Arquitectura, Pruebas y Aseguramiento de Calidad (QA)
* **Emilio Guerrero** - Validaciones de negocio e inmutabilidad de datos
* **Brandon Arellano** - Modelado C4, Base de Datos y Diagramación
* **Pablo Montalvo** - Algoritmia del Motor Predictivo PMP y Complejidad Ciclomática

