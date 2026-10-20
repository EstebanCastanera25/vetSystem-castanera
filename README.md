# Patitas Felices - Microservicios

Trabajo practico de Microservicios y APIs Escalables (UP 2C 2026).
En la fase 2 el monolito de la veterinaria (rama `main`) se pasa a microservicios.

Stack: Spring Boot 4.1.0, Spring Data JPA, MySQL, Maven multi-modulo, Java 21.

## Modulos

| Microservicio | Puerto | Base de datos | Estado |
|---|---|---|---|
| ms-duenio | 8081 | duenio_db | Sprint 9 |
| ms-mascota | 8082 | - | Sprint 10 |
| ms-veterinario | 8083 | - | Sprint 10 |
| ms-turno | 8084 | - | Sprint 10 |

## Arquitectura

Cada microservicio usa arquitectura hexagonal (Ports & Adapters). El codigo de ms-duenio esta en `com.patitas.duenio` y se divide en tres capas:

```
domain/
  model/           Duenio (entidad)
  port/in/         DuenioService (lo que se puede hacer con los duenios)
  port/out/        DuenioRepository (lo que el dominio necesita para guardar)
  exception/       DuenioNotFoundException, DuplicatedEmailException
application/       DuenioServiceImpl (casos de uso)
adapter/
  in/web/          DuenioController, GlobalExceptionHandler
  in/web/dto/      DuenioRequest, DuenioResponse, DuenioMapper
  out/persistence/ DuenioJpaRepository
```

- **Domain**: es la parte de adentro. Tiene la entidad y los puertos, que son interfaces. No depende de Spring.
- **Application**: `DuenioServiceImpl` implementa el puerto de entrada y usa el puerto de salida. Aca va la regla de negocio: no puede haber dos duenios con el mismo email.
- **Adapters**: conectan el dominio con el exterior. El controller REST es el de entrada; `DuenioJpaRepository` es el de salida, extiende `JpaRepository` e implementa `DuenioRepository`.

**Regla de dependencias:** las dependencias apuntan siempre hacia adentro. Los adaptadores conocen al dominio, pero el dominio no conoce a los adaptadores. `DuenioServiceImpl` no importa nada de `adapter`.

### Por que el controller no usa DuenioServiceImpl

El controller recibe la interface `DuenioService` y no la clase `DuenioServiceImpl`. La interface es el puerto de entrada: dice que se puede hacer y no como se hace. El controller depende de ese contrato, y Spring le inyecta la implementacion que encuentre. Si mañana cambia la implementacion (otra logica, un mock para tests), el controller no se toca.

Por la misma razon, el controller tampoco trabaja con la entidad `Duenio`. Recibe y devuelve DTOs (`DuenioRequest` / `DuenioResponse`), y el mapper hace la conversion. Asi la entidad de la base no queda expuesta en la API.

## Como levantarlo

1. Arrancar MySQL y crear la base: `CREATE DATABASE duenio_db;` (la tabla `duenios` la crea Hibernate)
2. Compilar desde la raiz: `mvnw clean package -pl ms-duenio`
3. Levantar: `mvnw spring-boot:run -pl ms-duenio`
4. Swagger: http://localhost:8081/swagger-ui/index.html

## Endpoints de ms-duenio

| Metodo | Ruta | Respuesta |
|---|---|---|
| GET | /api/duenios | 200 lista de duenios |
| GET | /api/duenios/{id} | 200, o 404 si no existe |
| POST | /api/duenios | 201, 400 si faltan datos, 409 si el email ya existe |
| PUT | /api/duenios/{id} | 200, 404 o 409 |
| DELETE | /api/duenios/{id} | 204, o 404 si no existe |
