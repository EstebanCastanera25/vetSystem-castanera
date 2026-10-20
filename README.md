# Patitas Felices - Microservicios

TP de Microservicios y APIs Escalables - UP 2C 2026 - Esteban Castanera

En la fase 2 pasamos el monolito de la veterinaria (esta en la rama `main`) a microservicios.

Usamos Spring Boot 4.1.0, Spring Cloud 2025.1.3 (Eureka y Config Server), JPA, MySQL y Maven multi modulo con Java 21.

## Modulos

| Modulo | Puerto | Base |
|---|---|---|
| config-server | 8888 | - |
| eureka-server | 8761 | - |
| ms-duenio | 8081 | duenio_db |
| ms-mascota | 8082 | mascota_db |
| ms-veterinario | 8083 | veterinario_db |
| ms-turno | 8084 | turno_db |

Cada microservicio tiene su base de datos propia. Por eso ya no usamos `@ManyToOne` como en el monolito: la mascota guarda el `duenioId` y el turno guarda `mascotaId` y `veterinarioId` como Long. No se puede hacer una foreign key entre bases distintas.

## Arquitectura

Los cuatro microservicios usan arquitectura hexagonal (ports and adapters) y tienen la misma estructura. Por ejemplo ms-duenio:

```
domain/
  model/           Duenio
  port/in/         DuenioService
  port/out/        DuenioRepository
  exception/       DuenioNotFoundException, DuplicatedEmailException
application/       DuenioServiceImpl
adapter/
  in/web/          DuenioController, GlobalExceptionHandler
  in/web/dto/      DuenioRequest, DuenioResponse, DuenioMapper
  out/persistence/ DuenioJpaRepository
```

- domain: la entidad y los puertos (interfaces). Es lo de adentro y no depende de Spring.
- application: el DuenioServiceImpl implementa el puerto de entrada y usa el de salida. Aca esta la validacion de que no se repita el email.
- adapter: lo que conecta con afuera. El controller es el adaptador de entrada y DuenioJpaRepository el de salida (extiende JpaRepository e implementa DuenioRepository).

La regla es que las dependencias van siempre hacia adentro: los adapters conocen al dominio pero el dominio no conoce a los adapters. Por eso DuenioServiceImpl no importa nada de adapter.

### Por que el controller no usa DuenioServiceImpl

El controller recibe la interface DuenioService y no la clase DuenioServiceImpl. La interface es el puerto de entrada, dice que se puede hacer pero no como. Spring le inyecta la implementacion, entonces si mañana cambia la implementacion (o se usa un mock en un test) el controller queda igual.

Tampoco usa la entidad Duenio, trabaja con los DTOs (DuenioRequest y DuenioResponse) y el mapper hace la conversion. Asi la entidad no queda expuesta en la API.

## Config Server y Eureka

El config server usa el perfil native, o sea que los archivos de configuracion estan en `config-server/src/main/resources/config/`:
- `application.yml`: lo que comparten todos (eureka, jpa, actuator)
- `ms-duenio.yml`, `ms-mascota.yml`, etc: la base de datos y el info de cada uno

En el `application.yml` de cada microservicio quedo solo el nombre, el puerto y el `spring.config.import` que apunta al config server.

Con Eureka cada microservicio se registra con su nombre (ms-duenio, ms-mascota...), asi mas adelante se pueden llamar entre ellos por nombre en vez de usar localhost:puerto.

## Como levantarlo

1. Levantar MySQL y crear las bases (las tablas las crea Hibernate):
```sql
CREATE DATABASE duenio_db;
CREATE DATABASE mascota_db;
CREATE DATABASE veterinario_db;
CREATE DATABASE turno_db;
```
2. Compilar desde la raiz: `mvnw clean package`
3. Levantar en este orden:
   - `mvnw spring-boot:run -pl config-server` (primero, porque si no los microservicios arrancan sin su configuracion)
   - `mvnw spring-boot:run -pl eureka-server` (el dashboard esta en http://localhost:8761)
   - despues los microservicios en cualquier orden: `mvnw spring-boot:run -pl ms-duenio`, y lo mismo con ms-mascota, ms-veterinario y ms-turno
4. Swagger: http://localhost:8081/swagger-ui/index.html (con 8082, 8083 y 8084 para los otros)

## Actuator

Los microservicios tienen habilitados health, info, metrics, env, beans y mappings. Por ejemplo:
- http://localhost:8081/actuator/health devuelve `"status":"UP"` con el detalle de la base
- http://localhost:8081/actuator/info devuelve el nombre, la descripcion y la version
- http://localhost:8081/actuator/metrics

## Endpoints

ms-duenio:

| Metodo | Ruta | Respuesta |
|---|---|---|
| GET | /api/duenios | 200 |
| GET | /api/duenios/{id} | 200 o 404 |
| POST | /api/duenios | 201, 400 si faltan datos, 409 si el email ya existe |
| PUT | /api/duenios/{id} | 200, 404 o 409 |
| DELETE | /api/duenios/{id} | 204 o 404 |

ms-mascota:

| Metodo | Ruta | Respuesta |
|---|---|---|
| GET / POST | /api/mascotas | 200 / 201 |
| GET / PUT / DELETE | /api/mascotas/{id} | 200 / 200 / 204, o 404 |
| GET | /api/mascotas/duenio/{duenioId} | 200 |

ms-veterinario:

| Metodo | Ruta | Respuesta |
|---|---|---|
| GET / POST | /api/veterinarios | 200 / 201, 409 si se repite la matricula |
| GET / PUT / DELETE | /api/veterinarios/{id} | 200 / 200 / 204, o 404 |
| GET | /api/veterinarios/especialidad/{especialidad} | 200 |

ms-turno:

| Metodo | Ruta | Respuesta |
|---|---|---|
| GET / POST | /api/turnos | 200 / 201 (el turno se crea PENDIENTE) |
| GET | /api/turnos/{id} | 200 o 404 |
| GET | /api/turnos/veterinario/{veterinarioId} | 200 |
| GET | /api/turnos/mascota/{mascotaId} | 200 |
| PATCH | /api/turnos/{id}/cancelar | 200, el turno queda CANCELADO |
