# Base Lirionizer (Base)

- Copyright: 2026 cdsoftware
- Repository: git@bitbucket.org:cdsoftware/com.cdsoftware.base.git
- License: GPL 2

## Description

This plugin acts as the base component for "Lirionizer" customizations in iDempiere. It provides low-level tools for server file manipulation (JSP/HTML), email management, and financial utilities for unallocated payments. It is essential for applying branding and system-specific configurations for the solution.

## Contributors

- info@casadelsoftware.com

## Components

- iDempiere Plugin [com.cdsoftware.base](com.cdsoftware.base)
- iDempiere Unit Test Fragment [com.cdsoftware.base.test](com.cdsoftware.base.test)

## Dictionary & Data Models

### Windows & Tabs
- **Lirionizer Configuration**: Global parameters for the base plugin's behavior.

### Models (Tables)
- Mainly utilizes standard iDempiere tables and server-side filesystem files.

## Prerequisites

- Java 11, commands `java` and `javac`.
- iDempiere 10
- Write access to the iDempiere installation directory (for JSP/HTML file injection).
- Set `IDEMPIERE_REPOSITORY` env variable.

## Features/Documentation

### Packages
- `com.cdsoftware.lirionizer.process`: System update processes, email handling, and finance utilities.
- `com.cdsoftware.lirionizer.util`: ZIP file handling and SQL utilities.
- `com.cdsoftware.lirionizer.base`: Abstract base classes for the plugin.

### Processes

| Process | Parameters | Description |
|---------|------------|-------------|
| `Lirionizer` | `jettyPath` | Injects custom files (`idempiere.jsp`, `home.properties`) and decompresses templates directly into the server's Jetty structure. |
| `RequestEMailProcessor` | N/A | Processes the incoming email queue for system requests. |
| `UnallocatedPayments` | N/A | Generates reports or identifies payments that have not yet been allocated to invoices. |

### Validations & Logic
- **System Injection**: The `Lirionizer` process automatically detects the installation path and current build to apply changes to the Jetty temporary folder.
- **Template Management**: Allows dynamic visual interface updates via ZIP files attached to the process.

## Instructions

- Install the plugin and ensure the user running iDempiere has sufficient disk permissions.
- Attach `idempiere.jsp` or `lirionTemplate.zip` files to the "Lirionizer" process record and run it to apply visual changes.
- Configure email services to enable the `RequestEMailProcessor`.

## Extra Links

- [CDS Website](https://casadelsoftware.com)

## Commands

Compile plugin and run tests:

```bash
./build
```

Use the parameter `debug` for debug mode example:

```bash
./build debug
```

To use `.\build.bat` for windows.
