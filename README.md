# Taskflow API

Entorno de desarrollo con Java 21, Maven 3.9.11, Node.js LTS, PostgreSQL 18,
GitHub CLI y GitHub Copilot CLI.

## Abrir el DevContainer

1. Instala Docker con Docker Compose y la extension **Dev Containers** de VS Code.
2. Copia [`.env.example`](.env.example) a `.env` en la raiz del proyecto y
   sustituye el valor de `POSTGRES_PASSWORD` por una contrasena local.
   No publiques este archivo.
3. Abre esta carpeta en VS Code y ejecuta **Dev Containers: Reopen in Container**.
4. En el terminal del contenedor, ejecuta `gh auth login`. Inicia `copilot` y
   utiliza `/login` si solicita autenticacion. Inicia sesion tambien en la
   extension de Copilot de VS Code.

Copilot CLI requiere acceso a un plan de Copilot compatible; GitHub CLI y
Copilot no son la misma herramienta. No se incorporan tokens a la imagen.

La primera apertura instala las herramientas y espera a que PostgreSQL este
disponible. La CLI de Copilot se instala para el usuario `vscode`, sin ejecutar
su instalador como root. Los fallos de instalacion interrumpen la configuracion.

## Spring Boot 4 y PostgreSQL

Este repositorio contiene el entorno, no una aplicacion Spring Boot generada.
Al crear la aplicacion en [Spring Initializr](https://start.spring.io/), selecciona
Java 21, Maven y una version estable de Spring Boot 4.x. Incluye el controlador
PostgreSQL y las dependencias de acceso a datos que necesites. La version de
Spring Boot se declara en el proyecto, no se instala en el contenedor.

Los terminales y procesos iniciados por VS Code reciben:

- `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/taskflow`
- `SPRING_DATASOURCE_USERNAME=taskflow`
- `SPRING_DATASOURCE_PASSWORD`, tomado de `.env`

Con una aplicacion Maven generada, puedes ejecutar `./mvnw spring-boot:run`.
VS Code redirige el puerto 8080. PostgreSQL no publica el puerto 5432 al host.
Estas variables se configuran con `remoteEnv`: un proceso iniciado directamente
con `docker exec` no las recibe automaticamente.

PostgreSQL guarda sus datos en un volumen, montado en `/var/lib/postgresql`,
la ruta de volumen de la imagen oficial a partir de PostgreSQL 18. Cambiar la
contrasena en `.env` no cambia la de una base ya inicializada: debes modificarla
en PostgreSQL y recrear el contenedor para actualizar su entorno.

## Conservar y recuperar conversaciones de Copilot

### Persistencia local

La configuracion conserva en volumenes:

- `/home/vscode/.copilot`: sesiones CLI, indice y configuracion de Copilot.
- `/home/vscode/.vscode-server`: estado del servidor remoto de VS Code.
- `/home/vscode/.config/gh`: configuracion y autenticacion de GitHub CLI.
- `/home/vscode/.m2`: cache de Maven.
- Los datos de PostgreSQL, en un volumen de Docker Compose.

Los volumenes de usuario usan `devcontainerId` para aislar este entorno de otros
DevContainers. Se reutilizan al reconstruir el mismo entorno. Cambiar de ruta,
host Docker o identidad de DevContainer puede generar otros volumenes.
El estado de chat que VS Code almacene en el cliente tambien depende de
conservar el perfil del editor y volver al mismo workspace.

Desde el terminal integrado del contenedor:

```bash
copilot --continue
copilot --resume
copilot --resume ID-DE-SESION
```

Usa `/session` para consultar el identificador y `/rename` para ponerle un
nombre reconocible. En VS Code, busca sesiones anteriores en la lista de
sesiones de Chat; incluye las externas y archivadas si no aparecen.
Para sesiones CLI compatibles, utiliza **Resume in Terminal**.

Ejecuta Copilot CLI dentro del contenedor y desde la carpeta del proyecto para
que comparta el entorno remoto con VS Code. El CLI del host tiene otro
almacenamiento; no se monta automaticamente su directorio personal.
No abras la misma sesion para escritura en dos procesos a la vez.

### Sincronizacion con GitHub

Segun la documentacion actual, Copilot CLI sincroniza sesiones con tu cuenta
por defecto, salvo restricciones de configuracion o politicas. En VS Code la
sincronizacion es experimental y se activa en los **ajustes personales**:

```json
{
  "github.copilot.chat.localIndex.enabled": true,
  "chat.sessionSync.enabled": true
}
```

No se fuerza este ajuste desde el repositorio: cada colaborador debe decidir
si quiere subir su historial. Comprueba el estado de sincronizacion en Chat.
Las sesiones sincronizadas son privadas por defecto, incluso en un repositorio
publico, pero pueden contener codigo, prompts y resultados de herramientas.
No incluyas secretos ni publiques sesiones sin revisarlas.

Sincronizar el historial permite consultarlo desde otras superficies y equipos;
no equivale a transportar los procesos, archivos y estado de un agente activo.
La capacidad de reanudar depende del tipo de sesion y del entorno disponible.
La sincronizacion de sesiones tampoco es lo mismo que **Settings Sync**.

En versiones compatibles existe un traspaso experimental de CLI a Chat:
desde Copilot CLI 1.0.107, activa `/settings experimental on` y ejecuta `/code`.
Requiere acceso al harness Copilot de VS Code y compatibilidad con el entorno
remoto. Si no esta disponible, utiliza **Resume in Terminal**.

### Respaldo independiente

- CLI: `/share file RUTA` exporta la conversacion a Markdown.
- VS Code: **Chat: Export Chat...** exporta la conversacion a JSON.

Guarda las exportaciones fuera del repositorio, en almacenamiento privado.
Una exportacion es un registro, no necesariamente una sesion reanudable.
Para respaldar el estado local completo, cierra las sesiones antes de copiar
los volumenes y respalda tambien el perfil local de VS Code cuando corresponda.
Restaurar archivos de sesion puede requerir reindexar el historial.

**Reconstruir el contenedor conserva los volumenes; borrarlos pierde sus datos.**
No uses `docker compose down -v` ni limpiezas de volumenes si quieres conservar
la base de datos. No elimines los volumenes `taskflow-...` de usuario.
Los volumenes no se sincronizan entre ordenadores ni sustituyen un respaldo.
Pueden contener credenciales: utiliza un host Docker y respaldos de confianza.

## Actualizaciones y comprobaciones

`"version": "lts"` obtiene la ultima LTS de Node al construir una imagen nueva;
no actualiza un contenedor existente. Para renovar herramientas, reconstruye
sin cache con **Dev Containers: Rebuild Container Without Cache**.
La CLI de Copilot se reinstala al crear el contenedor; para actualizarla sin
reconstruir, ejecuta `npm install -g --prefix "$HOME/.local" @github/copilot`.

Dentro del contenedor, comprueba:

```bash
java -version
mvn --version
node -p 'process.release.lts'
gh --version
copilot --version
```

Para comprobar persistencia, crea una sesion CLI, anota su ID, cierra la sesion,
reconstruye el DevContainer y ejecuta `copilot --resume ID-DE-SESION`.
Comprueba por separado que el historial de Chat sigue disponible en VS Code.

## Referencias

- [Datos de sesiones de Copilot](https://docs.github.com/en/copilot/concepts/security-governance-and-network-settings/session-data)
- [Guardar y sincronizar historial en VS Code](https://code.visualstudio.com/docs/agents/run/sessions/session-history)
- [Gestionar sesiones en VS Code](https://code.visualstudio.com/docs/agents/run/sessions/manage-sessions)
- [Recuperar sesiones CLI](https://docs.github.com/en/copilot/how-tos/copilot-cli/use-copilot-cli/chronicle)
- [Requisitos de Spring Boot](https://docs.spring.io/spring-boot/system-requirements.html)
