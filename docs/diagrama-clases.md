# Diagrama de clases — Dominio de la Clínica Veterinaria

Modelo de dominio del Sprint 1 (4 entidades + enum de estado).

```mermaid
classDiagram
    class Duenio {
        +Long id
        +String nombre
        +String apellido
        +String cedula  «unique»
        +Integer telefono
        +String email
        +List~Mascota~ mascotas
    }

    class Mascota {
        +Long id
        +String nombre
        +String especie
        +String raza
        +Date fecha
        +Duenio duenio
    }

    class Veterinario {
        +Long id
        +String nombre
        +String apellido
        +String matricula  «unique»
        +String especialidad
        +String email
        +List~Turno~ turnos
    }

    class Turno {
        +Long id
        +Date fecha
        +String motivo
        +EstadoTurno estado
        +Mascota mascota
        +Veterinario veterinario
    }

    class EstadoTurno {
        <<enumeration>>
        PENDIENTE
        CONFIRMADO
        ATENDIDO
        CANCELADO
    }

    Duenio "1" --> "N" Mascota : tiene
    Mascota "1" --> "N" Turno : asiste a
    Veterinario "1" --> "N" Turno : atiende
    Turno --> EstadoTurno : estado
```

## Relaciones

| Relación | Tipo | Anotación JPA |
|---|---|---|
| Dueño → Mascotas | 1 a N | `@OneToMany(mappedBy = "duenio")` en `Duenio` |
| Mascota → Dueño | N a 1 | `@ManyToOne` + `@JoinColumn(name = "duenio_id")` en `Mascota` |
| Turno → Mascota | N a 1 | `@ManyToOne` + `@JoinColumn(name = "mascota_id")` en `Turno` |
| Turno → Veterinario | N a 1 | `@ManyToOne` + `@JoinColumn(name = "veterinario_id")` en `Turno` |

La relación Mascota↔Veterinario es indirecta: se da **a través de Turno** (una mascota es atendida por un veterinario en un turno).
