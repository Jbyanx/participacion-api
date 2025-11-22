# 🗳️ Módulo de Participación Ciudadana y Auditoría (Grupo 6)

## 1. 🎯 Visión General del Proyecto

Este servicio es el corazón del sistema democrático de Conecta Ciudad. [cite_start]Nuestra misión principal, definida por el Taller DevOps, es garantizar que el proceso de participación ciudadana sea **justo, transparente y verificable**[cite: 38]. Somos responsables de la gestión completa del ciclo de vida del voto, desde la validación de la ventana de tiempo del proyecto hasta la generación de alertas por manipulación de datos.

Este módulo opera bajo una arquitectura de Microservicios, consumiendo información de Proyectos e Identidad (Grupo 2) a través de **Feign Clients** y validando tokens JWT para la autenticación.

***

## 2. 🛡️ Características Críticas de Integridad y Seguridad

Para cumplir con el requisito de "Auditoría y No Repudio", hemos implementado los siguientes mecanismos:

### A. Non-Repudiation (No Repudio)
Para garantizar la inmutabilidad, cada registro de voto se sella con un hash criptográfico SHA-256. Este hash se calcula usando el ID del ciudadano, el ID del proyecto, la decisión y el timestamp de la transacción.

### B. Detección Activa de Fraude
Hemos desarrollado un Motor de Auditoría que actúa como un "guardián" del sistema.
* **Mecanismo:** El endpoint `/auditorias/verificar-integridad` ejecuta un barrido completo de la tabla `votaciones`.
* **Acción:** Recalcula el hash de cada voto y lo compara con el hash almacenado en la columna `hash_verificacion`.
* **Resultado:** Si hay una discrepancia (indicando manipulación directa en la base de datos), se genera una **Alerta de Fraude** en la tabla `alertas_auditoria`.

### C. Exclusión de Votos Corruptos
Los votos marcados por el motor de auditoría como fraudulentos (`Votacion.fraudulento = true`) son automáticamente **excluidos** de los cálculos de resultados y porcentajes finales (`GET /votaciones/{idProyecto}/resultados`), asegurando la integridad de la estadística pública.

***

## 3. ⚙️ Arquitectura Técnica y DevOps

| Componente | Detalle |
| :--- | :--- |
| **Stack Principal** | Spring Boot 3, Java 21, JPA/Hibernate. |
| **Base de Datos** | PostgreSQL. |
| **Migraciones** | **Liquibase** (Gestión estricta de esquema en todos los ambientes). |
| **Inter-Servicios** | Feign Clients (Consumo seguro de la API del Grupo 2). |
| **Integración Continua** | GitHub Actions (Build, Test y Empaquetado en contenedor). |
| **Despliegue Continuo** | Azure App Service (Despliegue del JAR final). |

***

## 4. 🔒 Contrato de Roles y Seguridad (RBAC)

[cite_start]Hemos implementado seguridad de método (`@PreAuthorize`) basada en los roles provistos por el Grupo 2, adhiriéndonos al principio de **Mínimo Privilegio** y protegiendo la privacidad de los votantes[cite: 42].

| Módulo | Endpoint (Ruta) | Funcionalidad | Roles Requeridos | Notas de Seguridad |
| :--- | :--- | :--- | :--- | :--- |
| **Votaciones** | `POST /votar/{idProyecto}` | Registrar Voto | `hasRole('CIUDADANO')` | Exclusivo para ciudadanos. |
| **Votaciones** | `GET /mis-votos` | Historial de votos | `hasRole('CIUDADANO')` | Un ciudadano solo ve su propia actividad. |
| **Votaciones** | `GET /resultados` | Resultados Agregados | `isAuthenticated()` | Transparencia: Visible para todo usuario logueado (Ciudadano, Curador, Líder, Admin). |
| **Votaciones** | `GET /{votacionId}` | Ver Voto Específico | `hasAnyRole('ADMINISTRADOR', 'CURADOR')` | **PRIVACIDAD:** Revela identidad del votante, restringido a supervisión. |
| **Auditoría** | `POST /verificar-integridad` | Motor Anti-Fraude | `hasAnyRole('ADMINISTRADOR', 'CURADOR')` | Operación de alta sensibilidad. |
| **Auditoría** | `GET /alertas` | Listar Alertas de Fraude | `hasAnyRole('ADMINISTRADOR', 'CURADOR')` | Acceso a reportes de seguridad. |
| **Auditoría** | `GET /votos` | Listar Todos los Votos | `hasAnyRole('ADMINISTRADOR', 'CURADOR')` | **PRIVACIDAD CRÍTICA:** Solo roles de auditoría ven la lista completa con IDs. |

***

## 5. 🏗️ Estructura de Liquibase

Nuestras migraciones garantizan la trazabilidad del esquema. El `changeSet 5` es clave para la funcionalidad anti-fraude:

| ChangeSet ID | Tabla | Descripción |
| :--- | :--- | :--- |
| `1-create-votaciones` | `votaciones` | Creación de tabla y constraint de unicidad por voto. |
| `2-create-auditoria-votos` | `auditoria_votos` | Creación de tabla de historial. |
| `3-create-alertas-auditoria` | `alertas_auditoria` | Creación de tabla base para alertas. |
| `4-add-robustness-columns-alertas` | `alertas_auditoria` | Ajuste final del esquema para campos de auditoría detallados (`accion`, `ip_origen`). |
| `5-add-fraud-flag-votaciones` | `votaciones` | Agrega columna `fraudulento` (BOOLEAN) para **excluir votos corruptos del conteo**. |