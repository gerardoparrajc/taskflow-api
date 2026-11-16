# Instrucciones de Arquitectura y Desarrollo para TaskFlow API

## 1. Pila Tecnológica y Versiones Obligatorias
- **Lenguaje:** Java 21 LTS (Virtual Threads y Pattern Matching para switch en estados de tareas).
- **Framework:** Spring Boot 4.x (sobre Spring Framework 7.x y Jakarta EE 11).
- **Módulo Web:** `spring-boot-starter-webmvc`.
- **Motor JSON:** Jackson 3 (usar exclusivamente importaciones de `tools.jackson.*`).
- **Validación:** `jakarta.validation` (Bean Validation 3.1).
- **Documentación API:** `springdoc-openapi` versión 3.x (`springdoc-openapi-starter-webmvc-ui`).
- **Testing:** JUnit 6, AssertJ y `@MockitoBean` (en sustitución de `@MockBean`).

## 2. Estructura de Proyecto y Empaquetado
- Emplear **Organización por Funcionalidad (Package-by-Feature)**:
  - `com.taskflow.project` (Controlador, Servicio, Entidad `Project`, DTOs, Repository, Mapper).
  - `com.taskflow.task` (Controlador, Servicio, Entidad `Task`, DTOs, Repository, Mapper).
  - `com.taskflow.common` (Manejador global de excepciones RFC 7807 y configuración OpenAPI).

## 3. Patrones de Diseño y Modelado
- **DTOs Inmutables:** Todos los DTOs de entrada y salida DEBEN ser Java `records` inmutables.
- **Inyección de Dependencias:** Usar exclusivamente inyección por constructor (`@RequiredArgsConstructor` o explícito). Nunca `@Autowired` en campos.
- **Tipos del Dominio:**
  - Enumeraciones Java explícitas para estados (`TaskStatus`) y prioridades (`TaskPriority`).
  - Uso estricto de `LocalDateTime` y `LocalDate` para campos temporales.
- **Mapeo entre Capas:** Utilizar **MapStruct** en tiempo de compilación.

## 4. Control de Errores y Excepciones
- Manejo centralizado con `@RestControllerAdvice` devolviendo `ProblemDetail` (RFC 7807).
- Validaciones declarativas en DTOs activadas con `@Valid` en los controladores.
  Aplicar la validación de fecha futura al crear o cambiar `dueDate`, sin
  rechazar la conservación de una fecha existente que haya vencido.

## 5. Reglas de Persistencia
- Spring Data JPA con `JpaRepository`.
- Evitar consultas N+1. Usar `JOIN FETCH`, `@EntityGraph` u otras consultas
  apropiadas para la operación, sin cargar listas completas para obtener conteos.

## 6. Antipatrones Prohibidos
- ❌ No exponer Entidades JPA directamente en los controladores (usar `records`).
- ❌ No usar `@Autowired` sobre campos privados.
- ❌ No importar `com.fasterxml.jackson.*` para clases de Jackson 3.
- ❌ No usar `@MockBean` en las pruebas unitarias o web.
- ❌ No colocar lógica de negocio o bloques `try-catch` dentro de los controladores.

## 7. Reglas para CI/CD y Workflows de GitHub Actions
- Usar la acción oficial `actions/setup-java@v4` configurada con distribución `temurin` y versión `21`.
- Habilitar la caché de dependencias de Maven (`cache: 'maven'`) en GitHub Actions para optimizar los tiempos de ejecución del pipeline.
- Garantizar que los workflows fallen si alguna prueba de JUnit 6 o `MockMvc` no pasa con éxito.
- Para el contenedor Docker, emplear imágenes base oficiales ligeras (como `eclipse-temurin:21-jre-alpine`).

## 8. Contexto y flujo de trabajo para agentes
- Consultar [PROJECT_SPEC.md](PROJECT_SPEC.md) para los requisitos vigentes y
  [docs/DECISIONS.md](docs/DECISIONS.md) para su contexto; el
  [plan](docs/PLAN.md) enlaza las tareas. Distinguir lo acordado de lo pendiente
  y de lo ya implementado.
- Antes de abordar una tarea, revisar su issue y dependencias en
  [GitHub](https://github.com/gerardoparrajc/taskflow-api/issues). No asumir
  que las dependencias están completadas ni que un servicio externo funciona.
- Al acordar o cambiar decisiones relevantes, actualizar el registro, la
  especificación y las issues afectadas; no inventar requisitos para resolver
  cuestiones pendientes. Consultar cuando se necesite una decisión.
- Hacer cambios acotados, preservar modificaciones ajenas y ejecutar las
  pruebas pertinentes. Describir cualquier validación que no pueda ejecutarse.
- No guardar secretos ni datos sensibles; no habilitar facturación ni crear
  recursos de pago. Comprobar la viabilidad de proveedores gratuitos antes de
  utilizarlos.