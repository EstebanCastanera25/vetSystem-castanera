package com.vetSystem.Service;

import com.vetSystem.DTO.MascotaDTO;
import com.vetSystem.DTO.PaginaDTO;
import com.vetSystem.Entity.Duenio;
import com.vetSystem.Entity.Mascota;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.MascotaMapper;
import com.vetSystem.Repository.DuenioRepository;
import com.vetSystem.Repository.MascotaRepository;
import com.vetSystem.util.PaginaUtil;
import com.vetSystem.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vetSystem.Exception.CupoExcedidoException;


@Service
@RequiredArgsConstructor
public class MascotaService implements InterfaceService<MascotaDTO> {

    private final MascotaRepository mascotaRepository;
    private final DuenioRepository duenioRepository;
    private final MascotaMapper mascotaMapper;

    private static final int CUPO_MAXIMO_DE_MASCOTAS = 5;

    // @Transactional(readOnly) mantiene la sesión JPA abierta para que el mapper
    // pueda leer mascota.getDuenio() (relación LAZY) al armar duenioId/duenioNombre
    @Override
    @Transactional(readOnly = true)
    public List<MascotaDTO> listarEntidades() {
        List<Mascota> mascotas = mascotaRepository.findAll();
        List<MascotaDTO> resultado = new ArrayList<>();
        for (Mascota mascota : mascotas) {
            resultado.add(mascotaMapper.toDTO(mascota));
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MascotaDTO> buscarPorId(Long id) {
        Optional<Mascota> mascota = mascotaRepository.findById(id);
        if (mascota.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mascotaMapper.toDTO(mascota.get()));
    }

    // Las mascotas de un dueño (endpoint anidado) — valida que el dueño exista
    @Transactional(readOnly = true)
    public List<MascotaDTO> listarPorDuenio(Long duenioId) {
        if (!duenioRepository.existsById(duenioId)) {
            throw new ResourceNotFoundException("Duenio", duenioId);
        }
        List<Mascota> mascotas = mascotaRepository.findByDuenioId(duenioId);
        List<MascotaDTO> resultado = new ArrayList<>();
        for (Mascota mascota : mascotas) {
            resultado.add(mascotaMapper.toDTO(mascota));
        }
        return resultado;
    }

    // Registrar una mascota — el dueño se resuelve desde dto.duenioId y debe existir
    @Override
    @Transactional
    public MascotaDTO registrarEntidad(MascotaDTO dto) {
        normalizar(dto);
        Duenio duenio = duenioRepository.findById(dto.getDuenioId())
                .orElseThrow(() -> new ResourceNotFoundException("Duenio", dto.getDuenioId()));

        // Regla de negocio: el duenio no puede pasar el cupo de mascotas activas.
        long mascotasActivas = mascotaRepository.countByDuenioId(duenio.getId());
        if (mascotasActivas >= CUPO_MAXIMO_DE_MASCOTAS) {
            throw new CupoExcedidoException("El duenio id " + duenio.getId() + " ya tiene "
                    + mascotasActivas + " mascotas activas y el cupo maximo es de "
                    + CUPO_MAXIMO_DE_MASCOTAS);
        }

        Mascota mascota = mascotaMapper.toEntity(dto);  
        mascota.setDuenio(duenio);                      
        return mascotaMapper.toDTO(mascotaRepository.save(mascota));
    }

    // Modificar una mascota (el id viaja dentro del DTO; no se cambia de dueño)
    @Override
    @Transactional
    public MascotaDTO modificarEntidad(MascotaDTO dto) {
        normalizar(dto);
        Mascota mascota = mascotaRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota", dto.getId()));
        mascota.setNombre(dto.getNombre());
        mascota.setEspecie(dto.getEspecie());
        mascota.setRaza(dto.getRaza());
        mascota.setFechaNacimiento(dto.getFechaNacimiento());
        return mascotaMapper.toDTO(mascotaRepository.save(mascota));
    }

    @Override
    public void eliminarEntidad(Long id) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota", id));
        mascotaRepository.delete(mascota);
    }

    // Buscar por nombre (sin distinguir mayúsculas)
    @Override
    @Transactional(readOnly = true)
    public Optional<MascotaDTO> buscarPorString(String nombre) {
        Optional<Mascota> mascota = mascotaRepository.findByNombreIgnoreCase(nombre);
        if (mascota.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mascotaMapper.toDTO(mascota.get()));
    }

    // El orden con el que se listan las mascotas si el cliente no pide ninguno.
    // Termina en el id a propósito: dos mascotas con el mismo nombre tienen que quedar
    // siempre en el mismo orden relativo entre una página y otra.
    private static final Sort ORDEN_POR_DEFECTO =
            Sort.by(Sort.Order.asc("nombre"), Sort.Order.asc("id"));

    // Por qué campos se puede ordenar. La columna duenioNombre de la tabla del frontend
    // corresponde a duenio.nombre en la entidad; esta lista traduce entre ambos nombres.
    // Lo que no esté en esta lista cae al orden por defecto.
    private static final Map<String, String> CAMPOS_ORDENABLES = Map.of(
            "id", "id",
            "nombre", "nombre",
            "especie", "especie",
            "raza", "raza",
            "fechaNacimiento", "fechaNacimiento",
            "duenioNombre", "duenio.nombre");

    // Buscador paginado del frontend: texto parcial contra datos de la mascota y de su dueño.
    // Queda FUERA del contrato InterfaceService (no lleva @Override) porque su semántica
    // es distinta a la de buscarPorString: aquél es match exacto y devuelve 0 ó 1, éste es
    // parcial, ignora mayúsculas y devuelve una página de N. Mismo patrón que listarPorDuenio.
    //
    // La regla del texto vacío vive ACÁ, no en el controller: un solo lugar decide qué
    // significa "sin filtro" y vale igual para el frontend, para Swagger y para Postman.
    // isBlank() (no isEmpty()) para que "   " también cuente como vacío, y trim() para que
    // "  rocky  " no se convierta en un LIKE '%  rocky  %' que no encuentra nada.
    @Transactional(readOnly = true)
    public PaginaDTO<MascotaDTO> buscarPorTexto(String texto, Integer pagina, Integer tamanio,
                                                String orden, String direccion) {
        Sort ordenPedido = PaginaUtil.armarOrden(orden, direccion, CAMPOS_ORDENABLES, ORDEN_POR_DEFECTO);
        Pageable pageable = PaginaUtil.armarPageable(pagina, tamanio, ordenPedido);

        // Se pagina en la BASE (LIMIT/OFFSET), no en memoria: traer todo para después
        // quedarse con diez sería paginar la pantalla, no la consulta.
        Page<Mascota> paginaDeMascotas;
        if (texto == null || texto.isBlank()) {
            paginaDeMascotas = mascotaRepository.findAll(pageable);
        } else {
            paginaDeMascotas = mascotaRepository.buscarPorTexto(texto.trim(), pageable);
        }

        List<MascotaDTO> contenido = new ArrayList<>();
        for (Mascota mascota : paginaDeMascotas.getContent()) {
            contenido.add(mascotaMapper.toDTO(mascota));
        }
        return PaginaUtil.armar(contenido, paginaDeMascotas);
    }

    // Un solo lugar decide como se guarda el texto de esta entidad. Se llama desde el alta
    // y desde la edicion, asi no hay forma de que una de las dos se olvide.
    private void normalizar(MascotaDTO dto) {
        dto.setNombre(TextoUtil.aTitulo(dto.getNombre()));
        dto.setEspecie(TextoUtil.aTitulo(dto.getEspecie()));
        dto.setRaza(TextoUtil.aTitulo(dto.getRaza()));
    }
}
