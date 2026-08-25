# vet-system — Clínica Veterinaria "Patitas Felices"

Trabajo Práctico de la materia **Microservicios y APIs Escalables** — Universidad de Palermo, 2C 2026.

Sistema de gestión de la clínica veterinaria. Fase 1: monolito MVC con Spring Boot. Fase 2 (más adelante en la cursada): migración a microservicios.

## Integrantes

- Esteban Castañera

## Stack

- Java 21
- Spring Boot 4.1.0 (Spring Web MVC, Spring Data JPA, Lombok, DevTools)
- MySQL 8 (base `vet_system`)
- Maven (wrapper incluido, no hace falta instalarlo)

## Cómo levantar el proyecto

1. Tener MySQL corriendo en `localhost:3306` con usuario `root` / password `root`.
2. Crear la base de datos (solo la primera vez):

   ```sql
   CREATE DATABASE vet_system CHARACTER SET utf8mb4;
   ```

3. Desde la raíz del proyecto:

   ```bash
   # Windows
   mvnw.cmd spring-boot:run
   # Linux / Mac
   ./mvnw spring-boot:run
   ```

4. La API queda en `http://localhost:8080`. Endpoint de prueba: `GET http://localhost:8080/api/duenios`.

Hibernate crea/actualiza las tablas automáticamente (`spring.jpa.hibernate.ddl-auto=update`).

## Estructura (arquitectura MVC + Repository)

```
src/main/java/com/vetSystem/
├── Controller/   → recibe las peticiones HTTP y devuelve JSON
├── Service/      → lógica de negocio y validaciones
├── Repository/   → acceso a datos (Spring Data JPA)
└── Entity/       → entidades JPA (modelo de dominio)
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
