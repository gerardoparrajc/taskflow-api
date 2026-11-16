# Especificación del proyecto: TaskFlow API

## Estado y fuentes

Este documento describe los requisitos acordados para el MVP, no funcionalidades
ya implementadas. El repositorio dispone de un entorno de desarrollo, pero aún
no contiene la aplicación Spring Boot. La ejecución se planifica mediante
[issues de GitHub](https://github.com/gerardoparrajc/taskflow-api/issues);
consúltalas para conocer su estado y dependencias actuales. El
[plan de implementación](docs/PLAN.md) ofrece un índice de las tareas.

El contexto y las razones de las decisiones se registran en
[docs/DECISIONS.md](docs/DECISIONS.md). Las instrucciones de desarrollo para
agentes están en [AGENTS.md](AGENTS.md).

## Visión y alcance

TaskFlow API es una API REST para aprender a desarrollar un servicio de gestión
de proyectos y tareas. El MVP admite varios usuarios, cada uno con sus propios
proyectos y tareas privados. No incluye una interfaz web: se utilizarán la API,
su documentación OpenAPI y herramientas de prueba.

El MVP debe poder ejecutarse localmente. También se busca una demo pública de
aprendizaje en planes gratuitos, condicionada a comprobar su viabilidad sin
habilitar facturación. No es un despliegue de producción.

## Usuarios y seguridad

- Registro con correo electrónico único y contraseña; el correo debe verificarse
  antes del inicio de sesión. Se puede solicitar un nuevo correo de verificación.
- Inicio de sesión con JWT de acceso, con caducidad configurable de 15 minutos
  por defecto y sin token de renovación. Tras caducar, es necesario volver a
  iniciar sesión.
- Recuperación de contraseña por correo y cambio de contraseña con sesión
  iniciada. Ambos invalidan los JWT emitidos anteriormente.
- Los códigos de verificación y recuperación son de un solo uso y caducan; sus
  plazos son configurables.
- El usuario puede consultar sus datos de cuenta y eliminarla junto con sus
  proyectos, tareas y datos asociados.
- Cada usuario solo puede acceder a sus propios recursos. Las contraseñas se
  almacenan con un algoritmo de hash adecuado; las claves y credenciales se
  configuran fuera del repositorio.
- El correo real se investigará con Brevo, sin dominio propio y sin facturación.
  Su activación y entrega deben comprobarse en [#2](https://github.com/gerardoparrajc/taskflow-api/issues/2);
  no se consideran garantizadas. Se evaluará un buzón de pruebas local y las
  pruebas automatizadas no dependerán de envíos reales.

## Dominio

### Project

| Campo | Requisito |
| --- | --- |
| `id` | `Long`, identificador autogenerado. |
| `name` | Obligatorio, entre 3 y 100 caracteres. |
| `description` | Opcional, máximo 500 caracteres. |
| `createdAt` | `LocalDateTime`, generado al crear el proyecto. |
| Propietario | Usuario al que pertenece el proyecto. |

### Task

| Campo | Requisito |
| --- | --- |
| `id` | `Long`, identificador autogenerado. |
| `title` | Obligatorio, entre 3 y 150 caracteres. |
| `description` | Opcional. |
| `status` | `PENDING`, `IN_PROGRESS` o `COMPLETED`; por defecto `PENDING`. |
| `priority` | `LOW`, `MEDIUM` o `HIGH`; por defecto `MEDIUM`. |
| `dueDate` | `LocalDate` opcional. Al crearla o cambiarla debe ser hoy o una fecha futura. |
| `project` | Proyecto existente al que pertenece obligatoriamente la tarea. |

### Reglas

- Se permiten todas las transiciones de estado excepto `COMPLETED` a `PENDING`
  directamente. Una tarea completada puede reabrirse pasando a `IN_PROGRESS`.
- Una fecha de entrega válida puede quedar en el pasado con el tiempo. Al
  editar otros campos se puede conservar; una fecha nueva debe ser hoy o futura.
- Eliminar un proyecto elimina todas sus tareas. Eliminar una cuenta elimina sus
  proyectos, tareas y datos asociados.
- El detalle de un proyecto incluye el número total de tareas y los conteos por
  estado; no incluye una lista de tareas sin paginar.

## Operaciones de la API

Todas las operaciones de proyectos y tareas requieren autenticación y están
limitadas al propietario. La especificación original define estas rutas:

| Método y ruta | Operación |
| --- | --- |
| `POST /api/v1/projects` | Crear proyecto. |
| `GET /api/v1/projects` | Listar proyectos propios, con paginación. |
| `GET /api/v1/projects/{id}` | Consultar proyecto y conteos de tareas. |
| `DELETE /api/v1/projects/{id}` | Eliminar proyecto y sus tareas. |
| `POST /api/v1/projects/{projectId}/tasks` | Crear tarea en un proyecto propio. |
| `GET /api/v1/projects/{projectId}/tasks` | Listar tareas del proyecto, con filtros, ordenación y paginación. |
| `PATCH /api/v1/tasks/{id}/status` | Cambiar estado. |
| `DELETE /api/v1/tasks/{id}` | Eliminar tarea. |

Además, el MVP debe permitir editar nombre y descripción de un proyecto;
editar título, descripción, prioridad y fecha de una tarea; y realizar las
operaciones de registro, verificación, inicio de sesión, recuperación y cambio
de contraseña, consulta y eliminación de cuenta. Las rutas y contratos
concretos de estas operaciones adicionales están pendientes de definición.
La consulta individual de tareas no se ha incluido en el alcance acordado.

Los filtros de tareas por estado, prioridad y rango de fechas de entrega deben
poder combinarse; los extremos del rango son opcionales. Se podrá ordenar por
fecha de entrega, prioridad, estado, título o identificador. Los listados de
proyectos y tareas serán paginados.

## Arquitectura y calidad

Las reglas técnicas detalladas están en [AGENTS.md](AGENTS.md). En particular:

- Java 21, Maven, Spring Boot 4, Web MVC, Jakarta Validation, Jackson 3,
  springdoc-openapi 3, Spring Data JPA y MapStruct.
- PostgreSQL y migraciones versionadas con Liquibase.
- DTOs de entrada y salida como `records`, inyección por constructor y errores
  centralizados mediante `ProblemDetail`.
- JUnit 6 y AssertJ, pruebas de controladores y pruebas de integración con
  PostgreSQL mediante Testcontainers, tanto en local como en CI.
- Consultas de proyectos y tareas sin problema N+1.

## CI y despliegue

- GitHub Actions debe ejecutar en `push` y `pull_request` sobre `main`:
  configurar Temurin 21, habilitar la caché Maven, ejecutar
  `mvn clean test-compile` y `mvn test`, y fallar si falla la compilación o
  cualquier prueba, incluidas las de integración.
- La demo propuesta utiliza Render para la API y Neon para PostgreSQL, solo
  después de comprobar elegibilidad, límites y ausencia de facturación. Se
  preparará una imagen Docker multi-stage con Java 21.
- La publicación en GHCR está pospuesta; la automatización del despliegue de la
  demo está pendiente de definición. No se presupone que los servicios gratuitos
  sean permanentes o aptos para producción. El desarrollo y las pruebas deben
  seguir funcionando en local.

## Decisiones pendientes

Antes de implementar el área correspondiente, concretar y documentar:

- Resultado de la prueba de Brevo y alternativa si no es viable sin dominio o
  facturación; herramienta concreta para el buzón local.
- Viabilidad de Render y Neon con las cuentas y límites disponibles, y forma de
  desplegar la demo sin habilitar facturación, incluida su automatización.
- Rutas, formatos de petición y respuesta y códigos HTTP de las operaciones
  añadidas; formato y límites de paginación.
- Orden predeterminado de tareas, dirección y tratamiento de fechas ausentes;
  comportamiento y errores ante rangos o parámetros inválidos.
- Plazos predeterminados de los códigos de verificación y recuperación,
  política de contraseñas y mecanismo de invalidación de JWT.

No convertir estas cuestiones en requisitos cerrados sin una decisión explícita.
