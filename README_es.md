# Base Lirionizer (Base)

- Copyright: 2026 cdsoftware
- Repositorio: git@bitbucket.org:cdsoftware/com.cdsoftware.base.git
- Licencia: GPL 2

## Descripción

Este plugin actúa como el componente base para las personalizaciones de "Lirionizer" en iDempiere. Proporciona herramientas de bajo nivel para la manipulación de archivos del servidor (JSP/HTML), gestión de correos electrónicos y utilidades financieras para pagos no asignados. Es fundamental para aplicar el branding y las configuraciones de sistema específicas de la solución.

## Colaboradores

- info@casadelsoftware.com

## Componentes

- iDempiere Plugin [com.cdsoftware.base](com.cdsoftware.base)
- iDempiere Unit Test Fragment [com.cdsoftware.base.test](com.cdsoftware.base.test)

## Diccionario y Modelos de Datos

### Ventanas y Pestañas
- **Configuración Lirionizer**: Parámetros globales para el comportamiento del plugin base.

### Modelos (Tablas)
- Utiliza principalmente tablas estándar de iDempiere y archivos del sistema de archivos del servidor.

## Prerrequisitos

- Java 11, comandos `java` and `javac`.
- iDempiere 10
- Acceso de escritura al directorio de instalación de iDempiere (para inyección de archivos JSP/HTML).
- Configurar la variable de entorno `IDEMPIERE_REPOSITORY`.

## Características/Documentación

### Paquetes
- `com.cdsoftware.lirionizer.process`: Procesos de actualización de sistema, manejo de correos y finanzas.
- `com.cdsoftware.lirionizer.util`: Utilidades de manejo de archivos ZIP y SQL.
- `com.cdsoftware.lirionizer.base`: Clases base abstractas para el plugin.

### Procesos

| Proceso | Parámetros | Descripción |
|---------|------------|-------------|
| `Lirionizer` | `jettyPath` | Inyecta archivos personalizados (`idempiere.jsp`, `home.properties`) y descomprime plantillas directamente en la estructura de Jetty del servidor. |
| `RequestEMailProcessor` | N/A | Procesa la cola de correos electrónicos entrantes para solicitudes del sistema. |
| `UnallocatedPayments` | N/A | Genera informes o identifica pagos que aún no han sido asignados a facturas. |

### Validaciones y Lógica
- **Inyección de Sistema**: El proceso `Lirionizer` detecta automáticamente la ruta de instalación y la compilación actual para aplicar cambios en la carpeta temporal de Jetty.
- **Gestión de Plantillas**: Permite la actualización dinámica de la interfaz visual mediante archivos ZIP adjuntos al proceso.

## Instrucciones

- Instale el plugin y asegúrese de que el usuario que ejecuta iDempiere tenga permisos suficientes en el disco.
- Adjunte los archivos `idempiere.jsp` o `lirionTemplate.zip` al registro del proceso "Lirionizer" y ejecútelo para aplicar cambios visuales.
- Configure los servicios de correo para habilitar el `RequestEMailProcessor`.

## Enlaces Extra

- [Sitio Web de CDS](https://casadelsoftware.com)

## Comandos

Compilar plugin y ejecutar pruebas:

```bash
./build
```

Usar el parámetro `debug` para el modo de depuración, ejemplo:

```bash
./build debug
```

Para usar `.\build.bat` en Windows.
