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
│   └── config/       → SwaggerConfig (metadatos de OpenAPI) y CorsConfig
├── frontend/         → interfaz web (HTML + Bootstrap + JS), NO la sirve Spring
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
| `index.html` | Dueños: listar, crear, editar y eliminar |
| `mascotas.html` | Mascotas: CRUD completo, con el dueño elegido de un combo |
| `veterinarios.html` | Veterinarios: CRUD completo |
| `turnos.html` | Turnos: listar, dar de alta y cambiar el estado (PATCH) |

Para abrirlo hay que **servirlo por HTTP** (no con doble clic). Con la app corriendo en otra terminal:

```bash
# Opción A: Python (viene instalado en la mayoría de las máquinas)
python -m http.server 5500 --directory frontend
# y abrir http://localhost:5500/index.html

# Opción B: extensión "Live Server" de VS Code
# click derecho sobre frontend/index.html → "Open with Live Server"
```

> **Por qué no sirve el doble clic.** Así el navegador abre el archivo con el protocolo `file://` y
> manda el origen `null`, que CORS rechaza. El permiso de
> [`config/CorsConfig.java`](src/main/java/com/vetSystem/config/CorsConfig.java) está dado para
> `http://localhost:5500` y `http://127.0.0.1:5500`, así que hay que usar **ese puerto**. Si el
> servidor arranca en otro (por ejemplo 5501 porque el 5500 estaba ocupado), hay que agregar ese
> origen a `CorsConfig`.

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
| `DuenioServiceTest` | Unitario (Mockito, sin Spring) | Listado, búsqueda, alta con cédula duplicada, baja inexistente |
| `TurnoServiceTest` | Unitario (Mockito, sin Spring) | Alta de turno y la regla de superposición de horarios |
| `DuenioControllerTest` | Web slice (`@WebMvcTest` + MockMvc) | Contrato HTTP: 200 / 201 / 400 / 404 / 409 y los headers de CORS |
| `SwaggerDocsTest` | Contexto (`@SpringBootTest` + MockMvc) | Que `/v3/api-docs` genere la especificación con los `@Tag` y los `@Schema` |
| `VetSystemApplicationTests` | Contexto (`@SpringBootTest` sobre H2) | Que el contexto de Spring levante y el esquema se genere |

