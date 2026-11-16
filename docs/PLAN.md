# Plan de implementación

Este índice resume el plan aprobado. Las issues son la fuente de verdad para
su estado, criterios de aceptación y dependencias; consúltalas antes de
empezar. Los requisitos vigentes y cuestiones abiertas están en la
[especificación](../PROJECT_SPEC.md).

| Issue | Trabajo |
| --- | --- |
| [#1](https://github.com/gerardoparrajc/taskflow-api/issues/1) | Inicializar Spring Boot, Maven y CI. |
| [#2](https://github.com/gerardoparrajc/taskflow-api/issues/2) | Validar Brevo sin dominio propio y el correo de pruebas local. |
| [#3](https://github.com/gerardoparrajc/taskflow-api/issues/3) | Configurar PostgreSQL, Liquibase y Testcontainers. |
| [#4](https://github.com/gerardoparrajc/taskflow-api/issues/4) | Modelar usuarios y establecer la base de seguridad. |
| [#5](https://github.com/gerardoparrajc/taskflow-api/issues/5) | Implementar registro y verificación de correo. |
| [#6](https://github.com/gerardoparrajc/taskflow-api/issues/6) | Implementar inicio de sesión con JWT y aislamiento de usuarios. |
| [#7](https://github.com/gerardoparrajc/taskflow-api/issues/7) | Crear, listar y editar proyectos propios. |
| [#8](https://github.com/gerardoparrajc/taskflow-api/issues/8) | Implementar recuperación y cambio de contraseña. |
| [#9](https://github.com/gerardoparrajc/taskflow-api/issues/9) | Crear, editar, cambiar el estado y eliminar tareas. |
| [#10](https://github.com/gerardoparrajc/taskflow-api/issues/10) | Consultar el detalle de proyectos y eliminarlos en cascada. |
| [#11](https://github.com/gerardoparrajc/taskflow-api/issues/11) | Consultar y eliminar la cuenta propia con sus datos. |
| [#12](https://github.com/gerardoparrajc/taskflow-api/issues/12) | Listar tareas con filtros, ordenación y paginación. |
| [#13](https://github.com/gerardoparrajc/taskflow-api/issues/13) | Documentar el contrato de la API y su ejecución local. |
| [#14](https://github.com/gerardoparrajc/taskflow-api/issues/14) | Preparar una demo pública gratuita en Render y Neon, sujeta a viabilidad. |

Las issues #1 y #2 pueden iniciarse de forma independiente. La integración
del correo depende del resultado de #2; si no es viable sin dominio propio o
facturación, hay que decidir una alternativa antes de continuar. La demo de
#14 también está condicionada a comprobar sus proveedores sin habilitar
facturación. La publicación en GHCR queda para una fase posterior.
