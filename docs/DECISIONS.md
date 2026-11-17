# Registro de decisiones

Este registro conserva el contexto de las decisiones acordadas para TaskFlow
API. La [especificación](../PROJECT_SPEC.md) es la fuente de los requisitos
vigentes; las [issues](https://github.com/gerardoparrajc/taskflow-api/issues)
son la fuente del estado y las dependencias de ejecución. Las decisiones
pendientes no deben interpretarse como aprobadas.

Al cambiar una decisión relevante, añade una entrada con fecha, contexto,
decisión y consecuencias; enlaza la issue o decisión relacionada cuando exista.
Si reemplaza una entrada anterior, indícalo expresamente y actualiza la
especificación y las issues afectadas. No registres credenciales ni datos
sensibles.

## 2026-11-16: objetivo y alcance del MVP

- **Contexto:** el objetivo es aprender desarrollando una API de proyectos y
  tareas para varias personas.
- **Decisión:** ofrecer una API sin interfaz web, con proyectos y tareas
  privados por usuario. El MVP incluye las operaciones de la especificación
  original y añade edición, filtros, ordenación y paginación.
- **Consecuencias:** se necesita autenticación y autorización por propietario;
  la consulta individual de tareas no se incluyó. El detalle del proyecto
  devuelve conteos, no una lista sin paginar.
- **Issues:** [#7](https://github.com/gerardoparrajc/taskflow-api/issues/7),
  [#9](https://github.com/gerardoparrajc/taskflow-api/issues/9),
  [#10](https://github.com/gerardoparrajc/taskflow-api/issues/10),
  [#12](https://github.com/gerardoparrajc/taskflow-api/issues/12).

## 2026-11-16: cuentas y autenticación

- **Contexto:** las cuentas deben proteger los datos individuales y permitir
  aprender autenticación para APIs.
- **Decisión:** registro con correo único y contraseña, verificación previa al
  acceso, reenvío de verificación, recuperación y cambio de contraseña,
  consulta y eliminación de cuenta. Se utiliza JWT con duración predeterminada
  de 15 minutos, configurable, sin token de renovación.
- **Consecuencias:** se vuelve a iniciar sesión al caducar el token; un cambio o
  recuperación de contraseña invalida los JWT anteriores. Los códigos de
  verificación y recuperación son de un solo uso y con caducidad configurable.
  Los plazos concretos y el mecanismo de invalidación siguen pendientes.
- **Issues:** [#4](https://github.com/gerardoparrajc/taskflow-api/issues/4),
  [#5](https://github.com/gerardoparrajc/taskflow-api/issues/5),
  [#6](https://github.com/gerardoparrajc/taskflow-api/issues/6),
  [#8](https://github.com/gerardoparrajc/taskflow-api/issues/8),
  [#11](https://github.com/gerardoparrajc/taskflow-api/issues/11).

## 2026-11-16: ciclo de vida de tareas

- **Contexto:** se quiere poder corregir tareas terminadas y editar tareas que
  hayan vencido.
- **Decisión:** se permiten todas las transiciones salvo `COMPLETED` a `PENDING`
  directamente; para reabrir se puede pasar a `IN_PROGRESS`. Las fechas nuevas
  deben ser hoy o futuras, pero una fecha existente vencida puede conservarse
  al editar otros campos. El borrado de un proyecto elimina sus tareas.
- **Consecuencias:** las validaciones de fecha no deben impedir editar una
  tarea solo porque su fecha existente haya pasado.
- **Issues:** [#9](https://github.com/gerardoparrajc/taskflow-api/issues/9),
  [#10](https://github.com/gerardoparrajc/taskflow-api/issues/10).

## 2026-11-16: persistencia y pruebas

- **Contexto:** el entorno existente incluye PostgreSQL y el proyecto necesita
  cambios de esquema reproducibles y pruebas de persistencia reales.
- **Decisión:** usar PostgreSQL, Liquibase para migraciones y Testcontainers con
  PostgreSQL para pruebas de integración locales y en CI, además de pruebas
  unitarias y de controladores.
- **Consecuencias:** las pruebas de integración necesitan Docker; CI debe fallar
  si falla cualquiera de las pruebas. Liquibase fue elegido frente a Flyway.
- **Issues:** [#1](https://github.com/gerardoparrajc/taskflow-api/issues/1),
  [#3](https://github.com/gerardoparrajc/taskflow-api/issues/3).

## 2026-11-16: correo para desarrollo y demo

- **Contexto:** verificar cuentas y recuperar contraseñas requiere correo. No
  hay dominio propio disponible y no se quiere habilitar facturación.
- **Decisión:** probar Brevo para correo real sin dominio propio y evaluar un
  buzón de pruebas local. Las pruebas automatizadas no dependerán de envíos
  reales.
- **Resultado de la validación (2026-11-17, [#2](https://github.com/gerardoparrajc/taskflow-api/issues/2)):** con una cuenta Free y sin habilitar facturación, se verificó un remitente y la API transaccional respondió `HTTP 201`. El mensaje llegó a la bandeja de entrada de prueba. En el mensaje recibido, Brevo sustituyó el dominio del remitente por `@brevosend.com`. Es un resultado observado con esta cuenta y destinatario; no garantiza la entrega a otros proveedores o destinatarios.
- **Buzón local:** se añadió Mailpit al DevContainer. Se comprobó que un mensaje enviado desde el contenedor a `mailpit:1025` aparece en su interfaz web. Esta comprobación manual no depende de Brevo ni envía el mensaje a internet. La aplicación todavía no está conectada a Mailpit.
- **Consecuencias:** Mailpit queda como buzón local para desarrollo. La integración con la aplicación y la decisión de usar Brevo en la demo siguen pendientes. Las pruebas automatizadas no deben enviar correo real.
- **Límites y restricciones:** la documentación consultada de Brevo indica hasta 300 emails diarios en el plan gratuito; el límite puede cambiar y debe revisarse en la cuenta. Sin dominio autenticado, Brevo puede modificar el remitente visible.
- **Credenciales:** no guardar API keys en el repositorio, issues, capturas ni logs. Introducirlas solo en el entorno local y borrarlas de la sesión después de la prueba.
- **Alternativas consideradas:** emuladores de AWS para pruebas locales, SES
  real y Resend. Resend requiere dominio verificado para enviar a otros
  destinatarios; AWS Free Tier y SES tienen límites y condiciones temporales.
  No se decidió integrar estos servicios.
- **Issue:** [#2](https://github.com/gerardoparrajc/taskflow-api/issues/2).
- **Referencias:** [Brevo: requisitos de remitente](https://help.brevo.com/hc/en-us/articles/14925263522578-Comply-with-Gmail-Yahoo-and-Microsoft-s-requirements-for-email-senders),
  [Resend: dominio de pruebas](https://resend.com/docs/knowledge-base/403-error-resend-dev-domain),
  [Brevo: límites del plan Free](https://help.brevo.com/hc/en-us/articles/208580669-FAQs-What-are-the-limits-of-the-Free-plan),
  [Brevo: remitentes](https://help.brevo.com/hc/en-us/articles/208836149-Create-a-new-sender-From-name-and-From-email),
  [Brevo: envío transaccional por API](https://developers.brevo.com/reference/send-transac-email),
  [Mailpit: instalación con Docker](https://mailpit.axllent.org/docs/install/docker/).

## 2026-11-16: ejecución y alojamiento

- **Contexto:** se busca una demo pública de aprendizaje sin habilitar
  facturación, además de conservar el entorno local.
- **Decisión:** explorar Render para la API y Neon para PostgreSQL como parte del
  MVP, condicionados a comprobar que las cuentas y la configuración permiten
  usarlos sin facturación. Preparar una imagen Docker multi-stage. La publicación
  en GHCR se pospone.
- **Consecuencias:** no se garantiza la disponibilidad de los planes gratuitos;
  pueden suspenderse al alcanzar límites o por inactividad. Si se requiere
  facturación, detenerse y consultar antes de continuar. La automatización del
  despliegue aún no se ha decidido.
- **Alternativas consideradas:** Render PostgreSQL gratuito caduca; Railway
  ofrece crédito gratuito limitado; AWS, Google Cloud y Azure tienen cuotas o
  créditos sujetos a condiciones. Ninguno se seleccionó como plataforma única.
- **Issue:** [#14](https://github.com/gerardoparrajc/taskflow-api/issues/14).
- **Referencias:** [Render Free](https://render.com/docs/free),
  [Neon Pricing](https://neon.com/pricing).

## 2026-11-16: organización del trabajo

- **Contexto:** se necesita un plan abordable por personas y agentes.
- **Decisión:** organizar el trabajo en issues incrementales, con criterios de
  aceptación, pruebas y dependencias; GitHub refleja su estado actual.
- **Consecuencias:** las cuestiones aún abiertas se resuelven antes de
  implementarlas y sus decisiones se incorporan a este registro y a la
  especificación. La documentación no sustituye la comprobación de las issues.
- **Referencia:** [plan de issues](https://github.com/gerardoparrajc/taskflow-api/issues).
