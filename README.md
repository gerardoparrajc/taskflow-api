# TaskFlow API

TaskFlow API es un proyecto de aprendizaje para desarrollar una API REST de
gestión de proyectos y tareas. El repositorio incluye un entorno de desarrollo
con Java 21, Maven, PostgreSQL, GitHub CLI y GitHub Copilot CLI.

La aplicación Spring Boot está inicializada. GitHub Actions está configurado
para compilar el proyecto y ejecutar sus pruebas en los cambios dirigidos a
`main`. Las funcionalidades de la API se desarrollarán mediante las issues del
proyecto.

## Proyecto y plan

El MVP incluirá cuentas, autenticación, gestión de proyectos y tareas, y una
demo pública condicionada a la viabilidad de los servicios gratuitos.

- [Especificación y decisiones pendientes](PROJECT_SPEC.md).
- [Registro de decisiones](docs/DECISIONS.md).
- [Plan de implementación](docs/PLAN.md).
- [Instrucciones para agentes](AGENTS.md).
- [Issues y estado del trabajo](https://github.com/gerardoparrajc/taskflow-api/issues).

La especificación describe los requisitos acordados; las issues indican el
estado y las dependencias del trabajo.

## Abrir el DevContainer

1. Instala Docker con Docker Compose y la extensión **Dev Containers** de VS Code.
2. Copia [`.env.example`](.env.example) a `.env` en la raíz del repositorio y
   sustituye `POSTGRES_PASSWORD` por una contraseña local. No publiques este
   archivo ni guardes secretos en el repositorio.
3. Abre esta carpeta en VS Code y ejecuta **Dev Containers: Reopen in Container**.
4. En el terminal del contenedor, ejecuta `gh auth login` si necesitas autenticar
   GitHub CLI. Inicia `copilot` y utiliza `/login` si solicita autenticación.
   Inicia sesión también en la extensión de Copilot de VS Code.

Copilot CLI requiere acceso a un plan de Copilot compatible. GitHub CLI y
Copilot son herramientas distintas. No se incorporan tokens a la imagen.

La primera apertura instala las herramientas y espera a que PostgreSQL esté
disponible. La CLI de Copilot se instala para el usuario `vscode`, sin ejecutar
su instalador como `root`. Los fallos de instalación interrumpen la
configuración.

## Ejecutar y validar la aplicación

El proyecto Maven está en `taskflow-api/` y utiliza Java 21 y Spring Boot 4.
Desde la raíz del repositorio, ejecuta:

```bash
cd taskflow-api
./mvnw clean test-compile
./mvnw test
```

`clean test-compile` limpia y compila el código principal y el código de pruebas.
`test` ejecuta la suite y devuelve un error si alguna prueba falla.

Las pruebas de integración requieren acceso a un motor Docker. El DevContainer
utiliza Docker-in-Docker: su motor es independiente del motor del host y requiere
ejecutar el DevContainer con privilegios. Comprueba el acceso con `docker info`.

Testcontainers crea un PostgreSQL temporal para las pruebas; no utiliza la base
de datos persistente de desarrollo. La primera ejecución puede descargar imágenes
Docker. Si Docker no está disponible, la prueba falla en lugar de omitirse.

Para iniciar la aplicación:

```bash
./mvnw spring-boot:run
```

Spring Boot inicia el servidor en el puerto 8080. Detén el proceso con `Ctrl+C`.
La aplicación está en su fase inicial; las rutas y funcionalidades de negocio se
añadirán en las siguientes issues.

GitHub Actions ejecuta la compilación y las pruebas en `push` a `main` y en
pull requests cuyo destino sea `main`. El workflow está en
[`.github/workflows/ci.yml`](.github/workflows/ci.yml).

## PostgreSQL y el entorno de desarrollo

El DevContainer inicia PostgreSQL. Los terminales y procesos iniciados por VS
Code reciben estas variables de entorno:

- `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/taskflow`
- `SPRING_DATASOURCE_USERNAME=taskflow`
- `SPRING_DATASOURCE_PASSWORD`, tomado de `.env`

La aplicación utiliza estas variables para conectar con PostgreSQL. Al arrancar,
Liquibase aplica las migraciones pendientes desde el changelog versionado.
Hibernate valida el esquema frente a las entidades, pero no lo crea ni modifica.

PostgreSQL no publica el puerto 5432 al host. Las variables se configuran
mediante `remoteEnv`, por lo que un proceso iniciado directamente con
`docker exec` no las recibe automáticamente.

PostgreSQL guarda sus datos en un volumen, montado en `/var/lib/postgresql`, la
ruta de volumen de la imagen oficial a partir de PostgreSQL 18. Cambiar la
contraseña en `.env` no cambia la de una base ya inicializada: debes modificarla
en PostgreSQL y recrear el contenedor para actualizar su entorno.

## Pruebas de correo: Mailpit y Brevo

### Mailpit local

Mailpit recibe mensajes de prueba y los muestra en el navegador; no los entrega a destinatarios de internet. Después de abrir el DevContainer, visita [http://localhost:8025](http://localhost:8025).

Desde un terminal dentro del DevContainer, se puede comprobar la recepción SMTP con:

```bash
curl --url smtp://mailpit:1025 \
  --mail-from test@taskflow.invalid \
  --mail-rcpt test@taskflow.invalid \
  --upload-file - <<'EOF'
From: TaskFlow <test@taskflow.invalid>
To: Prueba local <test@taskflow.invalid>
Subject: Prueba de Mailpit

Este mensaje debe aparecer solamente en Mailpit.
EOF
```

El nombre `mailpit` resuelve dentro de la red de Docker Compose. El puerto `1025` no se publica al host; el puerto `8025` se usa para la interfaz web. Esta prueba confirma que Mailpit recibe mensajes, pero no que la aplicación TaskFlow esté integrada con él.

### Prueba manual con Brevo

Brevo se probó con una cuenta Free, sin habilitar facturación. En la prueba realizada, la API respondió `HTTP 201` y el mensaje llegó a la bandeja de entrada. El remitente visible apareció con el dominio `@brevosend.com`. Esto valida esa prueba concreta, no garantiza la entrega a otros destinatarios.

La documentación de Brevo indica un límite de hasta 300 envíos diarios para el plan gratuito; revisa el límite vigente en tu cuenta. Sin un dominio autenticado, Brevo puede sustituir el dominio del remitente.

Para repetir la prueba, usa un remitente validado y un buzón propio que puedas consultar. En un terminal local del DevContainer, introduce los valores cuando se soliciten:

```bash
read -rsp "API key de Brevo: " BREVO_API_KEY
printf '\n'
read -rp "Dirección remitente validada: " BREVO_SENDER_EMAIL
read -rp "Tu buzón de prueba: " BREVO_TEST_RECIPIENT
export BREVO_API_KEY BREVO_SENDER_EMAIL BREVO_TEST_RECIPIENT
```

Envía un único correo:

```bash
node --input-type=module <<'NODE'
for (const name of ["BREVO_API_KEY", "BREVO_SENDER_EMAIL", "BREVO_TEST_RECIPIENT"]) {
  if (!process.env[name]) throw new Error(`Falta la variable ${name}`);
}

const response = await fetch("https://api.brevo.com/v3/smtp/email", {
  method: "POST",
  headers: {
    accept: "application/json",
    "api-key": process.env.BREVO_API_KEY,
    "content-type": "application/json",
  },
  body: JSON.stringify({
    sender: { name: "Prueba TaskFlow", email: process.env.BREVO_SENDER_EMAIL },
    to: [{ email: process.env.BREVO_TEST_RECIPIENT }],
    subject: "Prueba de correo de TaskFlow",
    textContent: "Correo de prueba enviado desde Brevo.",
  }),
});

console.log(`HTTP ${response.status}`);
console.log(await response.text());
if (!response.ok) process.exitCode = 1;
NODE
```

Después de la prueba, elimina las variables de esa sesión:

```bash
unset BREVO_API_KEY BREVO_SENDER_EMAIL BREVO_TEST_RECIPIENT
```

No publiques la API key ni la escribas en archivos versionados, issues, capturas o logs. No la pongas en el `.env` compartido del repositorio: Compose carga ese archivo en varios contenedores. Una respuesta `HTTP 201` confirma que Brevo aceptó la solicitud; comprueba también la bandeja de entrada y la carpeta de spam para validar la entrega observada.

## Conservar y recuperar conversaciones de Copilot

### Persistencia local

La configuración conserva en volúmenes:

- `/home/vscode/.copilot`: sesiones CLI, índice y configuración de Copilot.
- `/home/vscode/.vscode-server`: estado del servidor remoto de VS Code.
- `/home/vscode/.config/gh`: configuración y autenticación de GitHub CLI.
- `/home/vscode/.m2`: caché de Maven.
- Los datos de PostgreSQL, en un volumen de Docker Compose.

Los volúmenes de usuario usan `devcontainerId` para aislar este entorno de otros
DevContainers. Se reutilizan al reconstruir el mismo entorno. Cambiar de ruta,
host Docker o identidad de DevContainer puede generar otros volúmenes.

El estado de Chat que VS Code almacena en el cliente también depende de
conservar el perfil del editor y volver al mismo workspace.

Desde el terminal integrado del contenedor:

```bash
copilot --continue
copilot --resume
copilot --resume ID-DE-SESION
```

Usa `/session` para consultar el identificador y `/rename` para ponerle un
nombre reconocible. En VS Code, busca sesiones anteriores en la lista de
sesiones de Chat; incluye las externas y archivadas si no aparecen. Para
sesiones CLI compatibles, utiliza **Resume in Terminal**.

Ejecuta Copilot CLI dentro del contenedor y desde la carpeta del proyecto para
que comparta el entorno remoto con VS Code. El CLI del host tiene otro
almacenamiento; su directorio personal no se monta automáticamente. No abras la
misma sesión para escritura en dos procesos a la vez.

### Sincronización con GitHub

Según la documentación actual, Copilot CLI sincroniza sesiones con tu cuenta
por defecto, salvo restricciones de configuración o políticas. En VS Code, la
sincronización es experimental y se activa en los **ajustes personales**:

```json
{
  "github.copilot.chat.localIndex.enabled": true,
  "chat.sessionSync.enabled": true
}
```

No se fuerza este ajuste desde el repositorio: cada colaborador debe decidir
si quiere subir su historial. Comprueba el estado de sincronización en Chat.
Las sesiones sincronizadas son privadas por defecto, incluso en un repositorio
público, pero pueden contener código, prompts y resultados de herramientas.
No incluyas secretos ni publiques sesiones sin revisarlas.

Sincronizar el historial permite consultarlo desde otras superficies y equipos;
no equivale a transportar los procesos, archivos y estado de un agente activo.
La capacidad de reanudar depende del tipo de sesión y del entorno disponible.
La sincronización de sesiones tampoco es lo mismo que **Settings Sync**.

En versiones compatibles existe un traspaso experimental de CLI a Chat:
desde Copilot CLI 1.0.107, activa `/settings experimental on` y ejecuta `/code`.
Requiere acceso al harness Copilot de VS Code y compatibilidad con el entorno
remoto. Si no está disponible, utiliza **Resume in Terminal**.

### Respaldo independiente

- CLI: `/share file RUTA` exporta la conversación a Markdown.
- VS Code: **Chat: Export Chat...** exporta la conversación a JSON.

Guarda las exportaciones fuera del repositorio, en almacenamiento privado. Una
exportación es un registro, no necesariamente una sesión reanudable. Para
respaldar el estado local completo, cierra las sesiones antes de copiar los
volúmenes y respalda también el perfil local de VS Code cuando corresponda.
Restaurar archivos de sesión puede requerir reindexar el historial.

**Reconstruir el contenedor conserva los volúmenes; borrarlos pierde sus datos.**
No uses `docker compose down -v` ni limpiezas de volúmenes si quieres conservar
la base de datos. No elimines los volúmenes `taskflow-...` de usuario. Los
volúmenes no se sincronizan entre ordenadores ni sustituyen un respaldo.
Pueden contener credenciales: utiliza un host Docker y respaldos de confianza.

## Actualizaciones y comprobaciones

`"version": "lts"` obtiene la última LTS de Node al construir una imagen nueva;
no actualiza un contenedor existente. Para renovar herramientas, reconstruye
sin caché con **Dev Containers: Rebuild Container Without Cache**.

La CLI de Copilot se reinstala al crear el contenedor. Para actualizarla sin
reconstruir, ejecuta:

```bash
npm install -g --prefix "$HOME/.local" @github/copilot
```

Dentro del contenedor, comprueba:

```bash
java -version
mvn --version
node -p 'process.release.lts'
gh --version
copilot --version
```

Para comprobar la persistencia, crea una sesión CLI, anota su ID, cierra la
sesión, reconstruye el DevContainer y ejecuta `copilot --resume ID-DE-SESION`.
Comprueba por separado que el historial de Chat sigue disponible en VS Code.

## Referencias

- [Datos de sesiones de Copilot](https://docs.github.com/en/copilot/concepts/security-governance-and-network-settings/session-data)
- [Guardar y sincronizar historial en VS Code](https://code.visualstudio.com/docs/agents/run/sessions/session-history)
- [Gestionar sesiones en VS Code](https://code.visualstudio.com/docs/agents/run/sessions/manage-sessions)
- [Recuperar sesiones CLI](https://docs.github.com/en/copilot/how-tos/copilot-cli/use-copilot-cli/chronicle)
- [Requisitos de Spring Boot](https://docs.spring.io/spring-boot/system-requirements.html)