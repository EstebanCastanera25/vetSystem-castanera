# vet-system — Clínica Veterinaria "Patitas Felices"

Trabajo Práctico de la materia **Microservicios y APIs Escalables** — Universidad de Palermo, 2C 2026.

Sistema de gestión de la clínica veterinaria. 
Fase 1: monolito MVC con Spring Boot. 


## Integrantes

- Esteban Castañera

## Stack

- Java 21
- Spring Boot 4.1.0 (Spring Web MVC, Spring Data JPA, Lombok, DevTools)
- MapStruct 1.5.5 (mapeo Entity ↔ DTO) + Bean Validation
- springdoc-openapi 3.1.1 (Swagger UI / OpenAPI 3)
- MySQL 8 (base `vet_system`) · H2 en memoria solo para los tests
- JUnit 5 + Mockito + MockMvc
- Frontend: HTML + Bootstrap 5.3.3 (CDN) + JavaScript vanilla con Fetch API
- Maven (wrapper incluido, no hace falta instalarlo)

## Cómo levantar el proyecto

1. Tener MySQL corriendo en `localhost:3306` con usuario `root` / password `root`.
2. Crear la base de datos (solo la primera vez):

   ```sql
   CREATE DATABASE vet_system CHARACTER SET utf8mb4;
   ```

3. Desde la raíz del proyecto:

   ```bash
   # Windows (en PowerShell el .\ es obligatorio; en CMD es opcional)
   .\mvnw.cmd spring-boot:run
   # Linux / Mac
   ./mvnw spring-boot:run
   ```

4. La API queda en `http://localhost:8080`. Endpoint de prueba: `GET http://localhost:8080/api/duenios`.

Hibernate crea/actualiza las tablas automáticamente (`spring.jpa.hibernate.ddl-auto=update`).

## Estructura (arquitectura MVC + Repository)

```
vetSystem-castanera/
├── src/main/java/com/vetSystem/
│   ├── Controller/   → recibe las peticiones HTTP y devuelve JSON
│   ├── Service/      → lógica de negocio y validaciones
│   ├── Repository/   → acceso a datos (Spring Data JPA)
│   ├── Entity/       → entidades JPA (modelo de dominio)
│   ├── DTO/          → lo que viaja por la API (la entidad no sale del Service)
│   ├── Mapper/       → conversión Entity ↔ DTO (MapStruct)
│   ├── Exception/    → excepciones propias + GlobalExceptionHandler + ErrorResponse
│   ├── config/       → SwaggerConfig (metadatos de OpenAPI) y CorsConfig
│   └── util/         → TextoUtil (normaliza nombres) y PaginaUtil (paginación y orden)
├── frontend/         → interfaz web (HTML + Bootstrap + JS), NO la sirve Spring
│   ├── app.js        → núcleo compartido por las 4 páginas
│   └── estilos.css   → lo que Bootstrap no cubre
└── docs/             → diagrama de clases y análisis del monolito
```

## Dominio

- `Duenio` (1) ──< (N) `Mascota`
- `Turno` (N) >── (1) `Mascota` y `Turno` (N) >── (1) `Veterinario`
- `EstadoTurno`: PENDIENTE / CONFIRMADO / ATENDIDO / CANCELADO

Diagrama de clases: [docs/diagrama-clases.md](docs/diagrama-clases.md)

## Sprints

| Rama | Contenido |
|---|---|
| `sprint-01` | Setup del proyecto + modelo de dominio (4 entidades JPA) |
| `sprint-02` | Arquitectura MVC + API REST + CRUD de Dueño |
| `sprint-03` | Relaciones JPA + CRUD de Mascota + JSON circular resuelto |
| `sprint-04` | DTOs + MapStruct + CRUD de Veterinario y Turno (con validación de superposición) |
| `sprint-05` | Bean Validation en los DTOs + `GlobalExceptionHandler` + `ErrorResponse` |
| `sprint-06` | Tests automatizados: JUnit 5 + Mockito (Service) y MockMvc (Controller) |
| `sprint-07` | Swagger/OpenAPI 3 + CORS + frontend con Bootstrap 5 y Fetch + análisis del monolito |


## Documentación de la API (Swagger)

Con la aplicación levantada:

| Qué | URL |
|---|---|
| Documentación interactiva (probar los endpoints) | http://localhost:8080/swagger-ui.html |
| Especificación OpenAPI en JSON | http://localhost:8080/v3/api-docs |

La documentación **se genera sola** a partir de las anotaciones del código: `@Tag` agrupa los
endpoints de cada controller, `@Operation` describe qué hace cada uno, `@ApiResponse` documenta los
códigos HTTP que puede devolver y `@Schema` describe cada campo de los DTOs con un ejemplo. El
título y la versión que se ven arriba salen de [`config/SwaggerConfig.java`](src/main/java/com/vetSystem/config/SwaggerConfig.java).

Desde Swagger UI se puede usar el botón **"Try it out"** para ejecutar los endpoints sin Postman.

## Frontend

La interfaz está en [`frontend/`](frontend/), **fuera de `src/`**: son archivos estáticos que Spring
no sirve, y le pegan a la API por HTTP como lo haría cualquier cliente externo.

| Página | Qué permite |
|---|---|
| `index.html` | Dueños: CRUD, buscador y panel con **sus mascotas** |
| `mascotas.html` | Mascotas: CRUD (el dueño se elige de un combo), buscador y panel con **sus turnos** |
| `veterinarios.html` | Veterinarios: CRUD, buscador y panel con **su agenda del día** |
| `turnos.html` | Turnos: alta, cambio de estado (PATCH), filtro por estado y panel con el **historial de la mascota** |

Las cuatro páginas comparten: buscador contra el backend (`?buscar=`), orden por columna haciendo
click en el encabezado, panel lateral al tocar una fila, y los errores de validación marcados
campo por campo.

**Los cuatro listados están paginados.** `GET /api/duenios?pagina=0&tamanio=10&orden=apellido&direccion=asc`
devuelve `{"contenido": [...], "pagina", "tamanio", "totalElementos", "totalPaginas"}`. Es lo que
pide el **RF-02 del SRS** ("`GET /api/duenios` retorna HTTP 200 con lista paginada"), extendido a
las cuatro entidades. Detalles que conviene poder explicar:

- La respuesta es un **DTO propio** (`PaginaDTO`), no el `Page` de Spring Data: el JSON de esta
  API está en castellano (`mensaje`, no `message`) y, por el mismo criterio con que la entidad JPA
  no sale del service, tampoco sale una clase de la librería de persistencia. La alternativa
  idiomática era `spring.data.web.pageable.serialization-mode=via-dto` + `PagedModel`.
- **El orden lo hace la base, no el navegador.** Ordenar en memoria sólo ordenaría la página que
  estás viendo. Además, todos los órdenes terminan en `id`: sin un orden total determinístico, una
  fila puede aparecer en dos páginas o en ninguna.
- Hay una **lista de campos ordenables** por entidad, que además traduce: la tabla de turnos ordena
  por una columna "mascotaNombre" que en la entidad es `mascota.nombre`. Un campo que no esté en la
  lista cae al orden por defecto, en vez de hacer fallar la consulta con un 500 disparable desde la URL.
- Los parámetros fuera de rango (`pagina=-1`, `tamanio=0`, `tamanio=99999`) **se recortan**; sólo
  da 400 lo que no es un número. Misma doctrina que el buscador: un filtro opcional nunca falla.

**Los desplegables de los formularios también se buscan escribiendo.** En "Nuevo turno" primero
se busca al dueño (por cédula, nombre o apellido) y el combo de mascotas se llena con las de
**ese** dueño, pidiendo `GET /api/duenios/{id}/mascotas`; si el texto no coincide con ningún
dueño, se buscan mascotas directamente. Lo mismo para el veterinario del turno y para el dueño
en "Nueva mascota".

### Cómo está organizado

Hasta el Sprint 7 cada HTML tenía su propio `<script>` con el mismo código repetido cuatro veces
(alrededor de 700 de las 1.489 líneas eran copias). Ahora:

| Archivo | Qué tiene |
|---|---|
| `app.js` | El **núcleo compartido**: `pedir()` (el único fetch), avisos, buscador con debounce, orden, panel lateral, el parseo de los errores de validación y el formato de fechas |
| `estilos.css` | Lo poco que Bootstrap no trae: fila clickeable, flecha del orden y los ajustes de celular |
| `duenios.js`, `mascotas.js`, `veterinarios.js`, `turnos.js` | Lo propio de cada página: sus columnas, su formulario y qué muestra su panel |
| Los cuatro `.html` | **Sólo markup**: no tienen ni una línea de JavaScript adentro |

Se cargan como scripts clásicos (`<script src="app.js">`), **no** como módulos ES: un módulo
exige que el servidor mande el MIME correcto para `.js`, que en Windows sale del registro, y si en
la máquina donde se muestra el TP estuviera mal, la página entera no funcionaría.

> Cada página declara su propia `const URL_API`, y `app.js` **no** la declara: los dos son scripts
> clásicos y comparten el ámbito global, así que una `const` repetida sería un `SyntaxError` que
> impide que se ejecute nada.

Para abrirlo hay que **servirlo por HTTP** (no con doble clic). Con la app corriendo en otra terminal:

```bash
# Opción A: Python (viene instalado en la mayoría de las máquinas)
python -m http.server 5500 --directory frontend
# y abrir http://localhost:5500/index.html

# Opción B: extensión "Live Server" de VS Code
# click derecho sobre frontend/index.html → "Open with Live Server"
```



## Tests

```bash
.\mvnw.cmd test                              # todos
.\mvnw.cmd test "-Dtest=DuenioServiceTest"   # una sola clase
.\mvnw.cmd test "-Dsurefire.runOrder=random" # verifica que los tests sean independientes
```

**No hace falta tener MySQL levantado**: los tests corren contra una base H2 en memoria
(`src/test/resources/application.properties`, que reemplaza al de `src/main` en el classpath de test).

| Clase | Tipo | Qué cubre |
|---|---|---|
| `DuenioServiceTest` | Unitario (Mockito, sin Spring) | Listado, búsqueda, alta con cédula duplicada, baja inexistente y el buscador (incluido el texto vacío) |
| `MascotaServiceTest` | Unitario (Mockito, sin Spring) | El buscador de mascotas |
| `VeterinarioServiceTest` | Unitario (Mockito, sin Spring) | El buscador de veterinarios |
| `TurnoServiceTest` | Unitario (Mockito, sin Spring) | Alta de turno, la regla de superposición de horarios y el filtro por estado |
| `DuenioRepositoryTest` | Persistencia (`@DataJpaTest` sobre H2) | Que el `@Query` del buscador funcione de verdad: con Mockito el repositorio es un mock y la consulta **nunca se ejecuta** |
| `MascotaRepositoryTest` | Persistencia (`@DataJpaTest` sobre H2) | Que el `JOIN` encuentre una mascota buscando por el apellido de su dueño |
| `DuenioControllerTest` | Web slice (`@WebMvcTest` + MockMvc) | Contrato HTTP: 200 / 201 / 400 / 404 / 409, el buscador y los headers de CORS |
| `TurnoControllerTest` | Web slice (`@WebMvcTest` + MockMvc) | El filtro `?estado=`, y que un estado inexistente devuelva 400 y no 500 |
| `VeterinarioControllerTest` | Web slice (`@WebMvcTest` + MockMvc) | Que una matrícula en minúsculas se acepte (201) y que otro formato siga dando 400 |
| `TextoUtilTest` | Unitario **sin mocks** | La normalización de nombres: mayúsculas, espacios de más, acentos, null |
| `PaginaUtilTest` | Unitario **sin mocks** | El recorte de `pagina`/`tamanio` y la lista de campos ordenables |
| `SwaggerDocsTest` | Contexto (`@SpringBootTest` + MockMvc) | Que `/v3/api-docs` genere la especificación con los `@Tag` y los `@Schema` |
| `VetSystemApplicationTests` | Contexto (`@SpringBootTest` sobre H2) | Que el contexto de Spring levante y el esquema se genere |

