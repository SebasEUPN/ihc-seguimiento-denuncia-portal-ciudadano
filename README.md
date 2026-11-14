# Portal Ciudadano — Consulta y seguimiento de denuncias (Prototipo 2)
> **Fines didácticos.** Prototipo académico del curso de Interacción Humano-Computador (IHC).
> Los datos son ficticios.

## Versión en la nube (Render + Aiven)

Esta versión está preparada para publicarse en **Render** con una base **PostgreSQL** gratuita de **Aiven**
(la misma base la comparten los dos prototipos).

Cambios respecto a la versión local con SQL Server:

- Driver `postgresql` en lugar de `mssql-jdbc` (el plan gratuito de Aiven ya no ofrece MySQL ni SQL Server).
- La estructura está en `src/main/resources/db/schema.sql` y se crea sola al arrancar
  (reúne los scripts 01, 02 y 04, y la ampliación de columnas del 03). Los scripts de `database/`
  se conservan como referencia de la versión SQL Server.
- Los archivos (adjuntos del chat, fotos y documentos) se guardan en la tabla `archivo_almacenado`,
  porque el disco de Render es temporal y cada prototipo corre en un servidor distinto.
- La conexión se configura con variables de entorno:

| Variable | Ejemplo |
|---|---|
| `DB_URL` | `jdbc:postgresql://HOST:PUERTO/defaultdb?sslmode=require` |
| `DB_USER` | `avnadmin` |
| `DB_PASSWORD` | contraseña del servicio en Aiven |

Render construye la imagen con el `Dockerfile` incluido.

Proyecto académico IHC. Backend en **Spring Boot (Java)** que usa la **misma base de datos
SQL Server** (`BD_Denuncias`) del primer prototipo (`sistema-denuncias`).

## 1. Puesta en marcha

Requisito: el primer prototipo ya debe haberse ejecutado al menos una vez (él crea las
tablas y carga los usuarios, ciudadanos y denuncias).

1. **Ampliar la base de datos.** En SSMS abre `database/02_portal_ciudadano.sql` y ejecútalo (F5).
   Solo agrega 4 columnas y 2 índices; no borra nada y puede ejecutarse varias veces.
2. **Configurar la conexión.** En `src/main/resources/application.properties` copia la misma
   URL, usuario y contraseña que usas en `sistema-denuncias`.
3. **Copiar el escudo.** Pon tu archivo `logo-pnp.webp` en `src/main/resources/static/assets/`.
4. **Ejecutar** `PortalCiudadanoApplication` (o `mvn spring-boot:run`).
5. Abre **http://localhost:8081**

Los dos prototipos pueden ejecutarse al mismo tiempo: el sistema de gestión en el puerto 8080
y el portal en el 8081.

## 2. Datos para probar

| Dato | Valor |
|---|---|
| Código de denuncia | `D-000584` (o cualquier otro de la tabla `denuncia`: `D-000585`, `D-000586`...) |
| PIN | el de la propiedad `app.demo.pin` de `application.properties` |

Al arrancar, la clase `DatosPortal` completa lo que falte: asigna el PIN de demostración a los
ciudadanos sin PIN, asigna un funcionario responsable a cada denuncia y (opcional,
`app.demo.denuncias-extra`) agrega las denuncias `D-000604` y `D-000605` al ciudadano de `D-000584`.

Tras 5 PIN incorrectos seguidos la consulta se bloquea 15 minutos. Para desbloquear a mano:

```sql
UPDATE ciudadano SET pin_intentos = 0, pin_bloqueado_hasta = NULL;
```

## 3. Cómo se conecta con el primer prototipo

| En el portal | Tabla | Quién la escribe |
|---|---|---|
| Estado, descripción, carpeta fiscal | `denuncia` | Policía y fiscal (prototipo 1) |
| Historial y notificaciones | `seguimiento_denuncia`, `actuacion` | Prototipo 1 |
| Evidencias | `documento` | Prototipo 1 |
| Chat con el responsable | `conversacion`, `mensaje` | Ambos |

Prueba recomendada: abre el portal con `D-000584` y, en otro navegador, el sistema de gestión
con el funcionario `PNP-2045`. Si el policía cambia el estado o responde en el "Chat
institucional", el ciudadano lo ve en el portal (el chat se actualiza cada 10 segundos).

## 4. Estructura

```
portal-ciudadano/
├── pom.xml
├── database/02_portal_ciudadano.sql     Columnas nuevas (PIN y funcionario responsable)
└── src/main/
    ├── java/com/proyecto/portal/
    │   ├── PortalCiudadanoApplication.java
    │   ├── config/        Configuración web y datos de demostración
    │   ├── security/      Sesión del ciudadano e interceptor que protege /api/portal/**
    │   ├── model/         Entidades JPA (las mismas tablas del prototipo 1)
    │   ├── repository/    Acceso a datos
    │   ├── dto/           Objetos que viajan entre el frontend y el backend
    │   ├── service/       Acceso con PIN, seguimiento, chat y archivos
    │   ├── controller/    Controladores REST
    │   └── exception/     Errores en formato JSON
    └── resources/
        ├── application.properties
        └── static/        index.html · css/styles.css · js/api.js · js/app.js · assets/
```

## 5. Rutas de la API

| Método y ruta | Descripción |
|---|---|
| `POST /api/portal/acceso/consulta` | Paso 1: valida que el código exista |
| `POST /api/portal/acceso/verificar` | Paso 2: verifica el PIN y abre la sesión |
| `GET /api/portal/acceso/sesion` | Devuelve el ciudadano si la consulta sigue abierta |
| `POST /api/portal/acceso/salir` | Cierra la consulta |
| `GET /api/portal/denuncias` | Mis denuncias registradas |
| `GET /api/portal/denuncias/{codigo}` | Detalle, documentos, historial y avance fiscal |
| `GET, POST /api/portal/denuncias/{codigo}/chat` | Leer / enviar mensajes al responsable |
| `GET /api/portal/notificaciones` | Avisos de todas las denuncias del ciudadano |
| `GET /api/portal/adjuntos/{idMensaje}` | Descargar un archivo enviado por el funcionario |

Todas, salvo las de `/acceso/`, exigen haber verificado el PIN, y solo devuelven denuncias
del ciudadano de la sesión.

## 6. Notas

- Si vuelves a ejecutar el script `01_crear_base_datos.sql` (borra todo), ejecuta después el `02`.
- Para descargar archivos del chat, la propiedad `app.archivos.directorio` debe apuntar a la
  carpeta `uploads` del primer prototipo (por defecto asume que ambos proyectos están juntos).
- `app.portal.mostrar-observaciones=false` oculta al ciudadano las observaciones que escriben
  los funcionarios; solo vería los cambios de estado.
